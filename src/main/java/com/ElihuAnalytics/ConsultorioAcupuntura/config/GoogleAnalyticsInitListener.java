package com.ElihuAnalytics.ConsultorioAcupuntura.config;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import elemental.json.Json;
import elemental.json.JsonObject;

public class GoogleAnalyticsInitListener implements VaadinServiceInitListener {

    private static final String GA_ID = "G-D20SMLWRYG";

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            UI ui = uiEvent.getUI();
            Page page = ui.getPage();

            // Carga gtag.js de forma segura (método oficial en Vaadin 24.8)
            page.addJavaScript("https://www.googletagmanager.com/gtag/js?id=" + GA_ID);

            // Inicializa gtag (se ejecuta una vez al cargar la página)
            page.executeJs("""
                if (!window.gtag) {
                    window.dataLayer = window.dataLayer || [];
                    function gtag(){dataLayer.push(arguments);}
                    gtag('js', new Date());
                    gtag('config', $0, { 
                        send_page_view: false,
                        transport_type: 'beacon'
                    });
                }
                """, GA_ID);

            // Tracking de rutas: envía page_view en cada navegación
            ui.addBeforeEnterListener(this::sendPageView);
        });
    }

    private void sendPageView(BeforeEnterEvent event) {
        UI ui = UI.getCurrent();
        if (ui == null) return;

        Page page = ui.getPage();
        String path = ui.getInternals()
                .getActiveViewLocation()
                .getPathWithQueryParameters();

        if (path == null || path.isEmpty()) {
            path = "/";
        }

        // Envía el page_view actualizado
        page.executeJs(
                "if (window.gtag) { window.gtag('config', $0, {page_path: $1}); }",
                GA_ID, path
        );
    }
}