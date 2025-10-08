package com.ElihuAnalytics.ConsultorioAcupuntura.vista;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.HistoriaClinica;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Paciente;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.FileStorageService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.HistoriaClinicaService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.PacienteService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.SesionService;
import com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.tratamiento.*;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.NotaPrivadaService;
import com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.tratamiento.DoctorNotasPrivadasCard;

/**
 * Vista orquestadora del tratamiento para el médico:
 * Orden de cards:
 *  1) Paciente (foto y datos)
 *  2) Estados de salud (más recientes primero)
 *  3) Antecedentes relevantes (más reciente)
 *  4) Diagnósticos (historial; más reciente editable)
 *  5) Recomendaciones (más reciente editable)
 *  6) Prescripciones (más reciente editable)
 *  7) Adjuntos (catálogo)
 */
@Route(value = "medico/tratamiento", layout = LayoutPrincipal.class)
@PageTitle("Determinación del tratamiento")
@RolesAllowed({"ADMINISTRADOR", "MEDICO"})
@CssImport("./styles/medico-tratamiento.css")
public class DeterminacionTratamientoView extends VerticalLayout {

    private final PacienteService pacienteService;
    private final SesionService sesionService;
    private final HistoriaClinicaService historiaClinicaService;
    private final FileStorageService fileStorageService;
    private final NotaPrivadaService notaPrivadaService;

    private ComboBox<Paciente> cbPaciente;
    private FormLayout grid;

    public DeterminacionTratamientoView(PacienteService pacienteService,
                                        SesionService sesionService,
                                        HistoriaClinicaService historiaClinicaService,
                                        FileStorageService fileStorageService,
                                        NotaPrivadaService notaPrivadaService) {
        this.pacienteService = pacienteService;
        this.sesionService = sesionService;
        this.historiaClinicaService = historiaClinicaService;
        this.fileStorageService = fileStorageService;
        this.notaPrivadaService = notaPrivadaService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(new H2("Determinación del tratamiento"));

        // Selector de paciente
        cbPaciente = new ComboBox<>("Paciente");
        cbPaciente.setWidthFull();
        cbPaciente.setClearButtonVisible(true);
        cbPaciente.setItemLabelGenerator(p -> p.getNombres() + " " + p.getApellidos());
        cbPaciente.setItems(pacienteService.listarTodos());
        cbPaciente.addValueChangeListener(e -> recargar());
        add(cbPaciente);

        // Malla responsive
        grid = new FormLayout();
        grid.setWidthFull();
        grid.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("900px", 2),
                new FormLayout.ResponsiveStep("1400px", 3));
        add(grid);
    }

    private void recargar() {
        grid.removeAll();
        Paciente p = cbPaciente.getValue();
        if (p == null) {
            grid.add(new Paragraph("Selecciona un paciente para ver su información clínica."));
            return;
        }

            try {
                // Cargar/crear Historia Clínica
                HistoriaClinica hc = historiaClinicaService.obtenerOCrearPorPacienteId(p.getId());


                // Ensamblar cards en el orden solicitado
                Component cPaciente = new PacienteHeaderCard(p);
                Component cEstados = new EstadosSaludResumenCard(hc);
                Component cNotasPriv = new DoctorNotasPrivadasCard(hc, notaPrivadaService);
                Component cAntecedentes = new AntecedentesRelevantesCard(hc.getId(), historiaClinicaService);
                Component cDiagnosticos = new DiagnosticosCard(hc.getId(), historiaClinicaService);
                Component cRecs = new RecomendacionesCardEditor(hc.getId(), historiaClinicaService);
                Component cRx   = new PrescripcionesCardEditor(hc.getId(), historiaClinicaService);
                Component cAdj = new AdjuntosCatalogoCard(hc, historiaClinicaService, fileStorageService);

                grid.add(cPaciente, cEstados, cAntecedentes, cNotasPriv,  cDiagnosticos, cRecs, cRx, cAdj);
            } catch (Exception ex) {
                grid.add(new Paragraph("No fue posible cargar la historia clínica del paciente seleccionado."));
            }
    }

}