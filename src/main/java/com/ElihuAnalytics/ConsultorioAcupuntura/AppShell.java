package com.ElihuAnalytics.ConsultorioAcupuntura;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Meta;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.PageTitle; // <-- Importación Requerida
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.theme.Theme;

/**
 * Clase principal de configuración de la Shell de Vaadin.
 * Aquí se definen el tema, el PWA, los estilos y los metadatos globales para SEO.
 */
@PWA(
        name = "Buena Vida Acupuntura",
        shortName = "Buena Vida",
        description = "Consultorio en el ciberespacio",
        backgroundColor = "#ffffff",
        themeColor = "#4CAF50",
        iconPath = "icons/icon.png",
        manifestPath = "manifest.webmanifest",
        offlinePath = "offline.html",
        offlineResources = { "images/offline.png" }
)
@Theme("consultorio")
@Push(PushMode.AUTOMATIC)

// --- INICIO DE CONFIGURACIÓN SEO GLOBAL ---
// Establece el TÍTULO global por defecto
@PageTitle("Acupuntura a Domicilio en Bucaramanga | Rafael Díaz Sarmiento")
// Establece la DESCRIPCIÓN global por defecto
@Meta(name = "description", content = "Servicio profesional de acupuntura y medicina ancestral a domicilio en Bucaramanga, Floridablanca y Girón. Tratamientos efectivos para diversas dolencias.")
@Meta(name = "keywords", content = "acupuntura, medicina ancestral, Rafael Antonio Díaz Sarmiento, Bucaramanga, Floridablanca, Girón, Parkinson, asma, gota, esclerosis múltiple")
@Meta(name = "author", content = "Rafael Antonio Díaz Sarmiento")
// --- FIN DE CONFIGURACIÓN SEO GLOBAL ---

// Importar estilos globales y específicos de vistas
@CssImport("./styles/global-theme.css")
@CssImport("./styles/app-layout.css")
@CssImport("./styles/home-view.css")
@CssImport("./styles/paciente-view.css")
@CssImport("./styles/medico-admin.css")
@CssImport("./styles/medico-citas.css")
@CssImport("./styles/medico-tratamiento.css")
public class AppShell implements AppShellConfigurator {
    // No se necesita nada más aquí.
}