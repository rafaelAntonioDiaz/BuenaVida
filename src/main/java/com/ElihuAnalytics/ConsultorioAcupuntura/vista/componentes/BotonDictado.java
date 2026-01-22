package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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

        // 2. JavaScript Nativo Ultra-Compatible (iOS/Android/PC)
        String jsLogic = """
            const btn = this;
            const txt = $0;
            
            const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;

            if (!SpeechRecognition) {
                alert("Tu navegador no soporta dictado.");
                return;
            }

            if (!btn._recognition) {
                btn._recognition = new SpeechRecognition();
                // CRÍTICO PARA IPHONE COLOMBIA: Definir la región exacta
                btn._recognition.lang = 'es-CO'; 
                
                // CRÍTICO PARA IPHONE: iOS necesita ver los resultados 'en vivo' o se congela
                btn._recognition.interimResults = true; 
                btn._recognition.continuous = false; // iOS prefiere false, se apaga solo al hacer silencio

                // --- EVENTOS ---
                btn._recognition.onstart = function() {
                    btn.style.backgroundColor = "var(--lumo-error-color)";
                    btn.style.color = "white";
                    
                    // Feedback visual directo sin ir al servidor
                    txt._placeholderOriginal = txt.placeholder;
                    txt.placeholder = "🔴 Escuchando... habla ahora";
                };

                btn._recognition.onend = function() {
                    btn.style.backgroundColor = ""; 
                    btn.style.color = "";
                    txt.placeholder = txt._placeholderOriginal || "";
                    btn._isRecording = false;
                };

                btn._recognition.onresult = function(event) {
                    let transcript = "";
                    
                    // iOS a veces manda los resultados mezclados, forzamos la lectura total
                    for (let i = event.resultIndex; i < event.results.length; ++i) {
                        transcript += event.results[i][0].transcript;
                    }

                    if (transcript.length > 0) {
                        // Concatenar con lo que ya estaba escrito
                        let textoPrevio = btn._textoPrevio || txt.value || "";
                        if (textoPrevio.length > 0 && !textoPrevio.endsWith(" ")) {
                            textoPrevio += " ";
                        }
                        txt.value = textoPrevio + transcript;
                        
                        // Si es el resultado final, guardamos el texto base para la próxima frase
                        if (event.results[event.results.length - 1].isFinal) {
                            btn._textoPrevio = txt.value;
                            // ¡Avisar a Vaadin que el texto cambió de verdad!
                            txt.dispatchEvent(new Event('input', { bubbles: true }));
                            txt.dispatchEvent(new Event('change', { bubbles: true }));
                        }
                    }
                };

                btn._recognition.onerror = function(event) {
                    btn.style.backgroundColor = "";
                    btn._isRecording = false;
                    txt.placeholder = txt._placeholderOriginal || "";
                };
            }

            // --- LÓGICA DE CLICK ---
            if (btn._isRecording) {
                btn._recognition.stop();
            } else {
                try {
                    // Guardar el estado actual del texto antes de empezar a grabar
                    btn._textoPrevio = txt.value; 
                    btn._recognition.start();
                    btn._isRecording = true;
                } catch (e) {
                    btn._recognition.abort();
                    btn._isRecording = false;
                }
            }
        """;

        // IMPORTANTE: NO usamos addClickListener de Java para evitar el lag de red.
        // Lo inyectamos directo al navegador.
        this.getElement().executeJs("this.addEventListener('click', function() { " + jsLogic + " })", campoDestino.getElement());
    }
}