package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextArea;

public class BotonDictado extends Button {

    public BotonDictado(TextArea campoDestino) {
        // 1. Configuración Visual
        setIcon(new Icon(VaadinIcon.MICROPHONE));
        addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        setTooltipText("Presiona para dictar");

        getStyle().set("border-radius", "50%");
        getStyle().set("width", "var(--lumo-size-m)");
        getStyle().set("height", "var(--lumo-size-m)");
        getStyle().set("padding", "0");

        // 2. JavaScript Robusto (Iterativo y con Logs Visibles)
        String jsScript = """
            const textArea = $0;
            const boton = $1;
            
            if (!('webkitSpeechRecognition' in window)) {
                alert("Navegador no soportado. Usa Chrome.");
                return;
            }

            if (!window.recognition) {
                window.recognition = new webkitSpeechRecognition();
                window.recognition.continuous = false; // Detenerse al dejar de hablar
                window.recognition.interimResults = false; // Solo resultados finales
                window.recognition.lang = 'es-ES';

                window.recognition.onstart = function() {
                    boton.style.backgroundColor = "var(--lumo-error-color)";
                    boton.style.color = "white";
                };

                window.recognition.onend = function() {
                    boton.style.backgroundColor = ""; 
                    boton.style.color = "";
                };

                window.recognition.onresult = function(event) {
                    let transcriptFinal = "";
                    
                    // Bucle robusto para capturar todo lo que se dijo
                    for (let i = event.resultIndex; i < event.results.length; ++i) {
                        if (event.results[i].isFinal) {
                            transcriptFinal += event.results[i][0].transcript;
                        }
                    }

                    if (transcriptFinal.length > 0) {
                        let textAnterior = textArea.value;
                        // Añadir espacio si ya había texto
                        textArea.value = textAnterior + (textAnterior ? " " : "") + transcriptFinal;
                        
                        // Forzar a Vaadin a reconocer el cambio
                        textArea.dispatchEvent(new Event('input', { bubbles: true }));
                        textArea.dispatchEvent(new Event('change', { bubbles: true }));
                    } else {
                        console.warn("Se recibió evento pero sin texto final.");
                    }
                };
                
                window.recognition.onnomatch = function(event) {
                    alert("No se reconoció ninguna palabra.");
                };

                window.recognition.onerror = function(event) {
                    boton.style.backgroundColor = "";
                    // Si es 'no-speech', es que el micrófono no capta nada
                    if (event.error === 'no-speech') {
                         alert("No se detecta sonido. Revisa tu micrófono en Ubuntu.");
                    } else {
                         console.error("Error dictado:", event.error);
                    }
                };
            }

            try {
                window.recognition.start();
            } catch(e) {
                window.recognition.stop();
            }
        """;

        this.addClickListener(e -> {
            campoDestino.getElement().executeJs(jsScript, campoDestino.getElement(), this.getElement());
            Notification.show("Escuchando... habla ahora", 2000, Notification.Position.MIDDLE);
        });
    }
}