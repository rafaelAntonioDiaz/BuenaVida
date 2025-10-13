package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Paciente;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;

/**
 * Card de bienvenida para el paciente con diseño destacado.
 * Usa un fondo con gradiente verde y texto blanco para llamar la atención.
 */
public class BienvenidaCard extends Div {

    public BienvenidaCard(Paciente paciente) {
        // Aplicar estilos base de card
        addClassName("card");

        // El CSS ya maneja el fondo verde y el color blanco
        // mediante .paciente-grid .card:nth-child(1)

        // Título: ¡Qué gusto, <Nombre>!
        H3 titulo = new H3("¡Qué gusto, " + paciente.getNombres() + "!");
        titulo.getStyle()
                .set("margin", "0")
                .set("margin-bottom", "var(--lumo-space-s)")
                .set("color", "white"); // Asegurar que sea blanco

        // Texto descriptivo
        Paragraph texto = new Paragraph(
                "Aquí puedes programar sesiones, reportar tu estado de salud y gestionar tus antecedentes relevantes."
        );
        texto.getStyle()
                .set("margin", "0")
                .set("color", "rgba(255, 255, 255, 0.95)") // Blanco semi-transparente
                .set("line-height", "1.6");

        // Ensamblar
        add(titulo, texto);
    }
}