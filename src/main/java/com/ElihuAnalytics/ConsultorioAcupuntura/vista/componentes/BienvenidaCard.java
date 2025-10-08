package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Paciente;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;

/**
 * Card de bienvenida para el paciente.
 * Se genera a partir de la sección existente en PacienteView (crearSeccionBienvenida),
 * replicando el estilo de "card" y el contenido.
 */
public class BienvenidaCard extends Div {

    public BienvenidaCard(Paciente paciente) {
        // Estilo de "card" (equivalente a crearCard())
        getStyle()
                .set("padding", "var(--lumo-space-l)")
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        // Título: ¡Bienvenido, <Nombre>!
        H3 titulo = new H3("¡Que gusto, " + paciente.getNombres() + "!");
        // Texto descriptivo: igual al de la vista original
        Paragraph texto = new Paragraph(
                "Aquí puedes programar sesiones, reportar tu estado de salud y gestionar tus antecedentes relevantes."
        );

        // Ensamblar
        add(titulo, texto);
    }
}