package com.ElihuAnalytics.ConsultorioAcupuntura.config;

/*Redirecciona a los que lleguen al acupunturabuenavida.qzz.io
al acupunturarafaeldiaz.pro*/

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import java.io.IOException;

/**
 * Redirige todo el tráfico de dominios secundarios al dominio principal:
 * https://acupunturarafaeldiaz.pro
 */
@Component
public class DomainRedirectFilter implements Filter {

    // Dominio principal (el que quieres que Google y tus usuarios vean)
    private static final String NEW_DOMAIN = "acupunturarafaeldiaz.pro";

    // Lista de dominios antiguos o secundarios que deben redirigirse
    private static final String[] OLD_DOMAINS = {
            "acupunturabucaramanga.info",
            "medicinaancestralbucaramanga.info"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String host = req.getServerName();

        // Si el dominio de entrada coincide con alguno de los antiguos...
        for (String oldDomain : OLD_DOMAINS) {
            if (host.equalsIgnoreCase(oldDomain)) {

                // Mantiene ruta y query string
                String redirectUrl = "https://" + NEW_DOMAIN + req.getRequestURI();
                if (req.getQueryString() != null) {
                    redirectUrl += "?" + req.getQueryString();
                }

                // 301 = Redirección permanente
                res.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                res.setHeader("Location", redirectUrl);
                return;
            }
        }

        // Si no coincide, sigue el flujo normal
        chain.doFilter(request, response);
    }
}
