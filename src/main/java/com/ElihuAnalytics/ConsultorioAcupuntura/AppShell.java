package com.ElihuAnalytics.ConsultorioAcupuntura;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.theme.Theme;

/**
 * Clase principal de configuración de la Shell de Vaadin.
 * Aquí se definen el tema, el PWA y configuraciones relacionadas a la UI.
 */
@PWA(
        name = "Consultorio tradicional",
        shortName = "ConsultorioApp",
        description = "Sistema de gestión de consultas",
        backgroundColor = "#ffffff",
        themeColor = "#4CAF50",
        iconPath = "icons/icon.png",
        manifestPath = "manifest.webmanifest",
        offlinePath = "offline.html",
        offlineResources = { "images/offline.png" }
)
@Theme("consultorio")
@Push(PushMode.AUTOMATIC)
public class AppShell implements AppShellConfigurator {
    // Aquí puedes agregar más configuraciones del shell si lo necesitas en el futuro
}
