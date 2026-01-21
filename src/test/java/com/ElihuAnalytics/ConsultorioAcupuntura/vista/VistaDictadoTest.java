package com.ElihuAnalytics.ConsultorioAcupuntura.vista;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.HistoriaClinica;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.HistoriaClinicaService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.NotaPrivadaService;
import com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.BotonDictado;
import com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.tratamiento.DiagnosticosCard;
import com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.tratamiento.DoctorNotasPrivadasCard;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VistaDictadoTest {

    @Mock
    private HistoriaClinicaService historiaClinicaService;
    @Mock
    private NotaPrivadaService notaPrivadaService;

    private HistoriaClinica historiaMock;

    @BeforeEach
    void setUp() {
        historiaMock = new HistoriaClinica();
        // Inyectamos ID falso para que JPA no se queje
        ReflectionTestUtils.setField(historiaMock, "id", 1L);
        historiaMock.setDiagnosticoTradicional("Diagnóstico de prueba");
    }

    @Test
    void diagnosticosCard_debeTenerBotonDictado() {
        // 1. Simular respuesta
        when(historiaClinicaService.obtenerPorPacienteIdDeHistoria(anyLong()))
                .thenReturn(Optional.of(historiaMock));

        // 2. Instanciar
        DiagnosticosCard card = new DiagnosticosCard(1L, historiaClinicaService);

        // 3. CORRECCIÓN: Buscamos el botón "Editar" dinámicamente en lugar de por índice fijo
        Button botonEditar = card.getChildren()
                .filter(c -> c instanceof Button)
                .map(c -> (Button) c)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se encontró el botón 'Editar' en la tarjeta"));

        // 4. Clic para abrir el editor
        botonEditar.click();

        // 5. Verificar que aparece el micrófono
        boolean tieneMicrofono = buscarComponentePorClase(card, BotonDictado.class);
        assertTrue(tieneMicrofono, "La DiagnosticosCard debería mostrar un BotonDictado al editar.");
    }

    @Test
    void doctorNotasPrivadasCard_debeTenerBotonDictadoAlCrear() {
        // 1. Simular sin notas
        when(notaPrivadaService.listarDesc(anyLong())).thenReturn(Collections.emptyList());

        // 2. Instanciar
        DoctorNotasPrivadasCard card = new DoctorNotasPrivadasCard(historiaMock, notaPrivadaService);

        // 3. Buscar el botón "Nueva nota" (está dentro de un HorizontalLayout)
        // Buscamos primero el layout de cabecera
        HorizontalLayout header = card.getChildren()
                .filter(c -> c instanceof HorizontalLayout)
                .map(c -> (HorizontalLayout) c)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se encontró el header de acciones"));

        // Dentro del header, buscamos el botón "Nueva nota"
        Button btnNueva = header.getChildren()
                .filter(c -> c instanceof Button && "Nueva nota".equals(((Button)c).getText()))
                .map(c -> (Button) c)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se encontró el botón 'Nueva nota'"));

        btnNueva.click();

        // 4. Verificar micrófono
        boolean tieneMicrofono = buscarComponentePorClase(card, BotonDictado.class);
        assertTrue(tieneMicrofono, "El editor de 'Nueva Nota' debe tener micrófono.");
    }

    // --- Utilidad corregida (sin cast a HasComponents) ---
    private boolean buscarComponentePorClase(Component root, Class<?> targetClass) {
        if (targetClass.isInstance(root)) {
            return true;
        }
        // Usamos getChildren() nativo de Component
        return root.getChildren()
                .anyMatch(child -> buscarComponentePorClase(child, targetClass));
    }
}