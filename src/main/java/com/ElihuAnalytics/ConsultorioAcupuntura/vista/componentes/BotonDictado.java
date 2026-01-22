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

        // Estilo redondo
        getStyle().set("border-radius", "50%");
        getStyle().set("width", "var(--lumo-size-m)");
        getStyle().set("height", "var(--lumo-size-m)");
        getStyle().set("padding", "0");

        // 2. LÓGICA JAVASCRIPT CORREGIDA (Sin duplicación de texto)
        String jsLogic = """
            const btn = this;
            const txt = $0;
            const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;

            if (!SpeechRecognition) {
                alert("Tu navegador no soporta dictado.");
                return;
            }

            // Inicializar una sola vez
            if (!btn._recognition) {
                btn._recognition = new SpeechRecognition();
                btn._recognition.lang = 'es-CO'; 
                btn._recognition.interimResults = true; // Necesario para que iPhone no se cuelgue
                btn._recognition.continuous = false;    // Se detiene al hacer silencio (comportamiento walkie-talkie)

                // --- AL INICIAR: TOMAMOS LA "FOTO" DEL TEXTO ACTUAL ---
                btn._recognition.onstart = function() {
                    btn.style.backgroundColor = "var(--lumo-error-color)";
                    btn.style.color = "white";
                    
                    // Guardamos lo que había escrito ANTES de empezar a hablar esta frase
                    btn._textoBase = txt.value || "";
                    
                    // Si ya había texto, aseguramos un espacio al final para no pegar las palabras
                    if (btn._textoBase.length > 0 && !btn._textoBase.endsWith(" ")) {
                        btn._textoBase += " ";
                    }
                    
                    txt.placeholder = "Escuchando...";
                };

                btn._recognition.onend = function() {
                    btn.style.backgroundColor = ""; 
                    btn.style.color = "";
                    txt.placeholder = "";
                    btn._isRecording = false;
                    
                    // Disparar evento final para guardar en Java
                    txt.dispatchEvent(new Event('change', { bubbles: true }));
                };

                // --- AL RECIBIR PALABRAS: REEMPLAZAMOS, NO SUMAMOS ---
                btn._recognition.onresult = function(event) {
                    let transcriptActual = "";
                    
                    // Juntamos todo lo que el iPhone ha entendido en ESTA frase
                    for (let i = event.resultIndex; i < event.results.length; ++i) {
                        transcriptActual += event.results[i][0].transcript;
                    }

                    // LA CURA A LA LOCURA:
                    // El valor final es: La FOTO guardada al principio + Lo que estás diciendo ahora
                    // Ya no sumamos sobre lo que acabamos de escribir.
                    txt.value = btn._textoBase + transcriptActual;
                    
                    // Avisamos a Vaadin para que no pierda el hilo
                    txt.dispatchEvent(new Event('input', { bubbles: true }));
                };

                btn._recognition.onerror = function(event) {
                    btn.style.backgroundColor = "";
                    btn._isRecording = false;
                    console.error("Error Speech:", event.error);
                };
            }

            // --- BOTÓN ON/OFF (Walkie-Talkie) ---
            if (btn._isRecording) {
                btn._recognition.stop();
                btn._isRecording = false;
            } else {
                try {
                    btn._recognition.start();
                    btn._isRecording = true;
                } catch (e) {
                    // Si el usuario da clic muy rápido, a veces falla, reseteamos
                    btn._recognition.abort();
                    btn._isRecording = false;
                }
            }
        """;

        this.getElement().executeJs("this.addEventListener('click', function() { " + jsLogic + " })", campoDestino.getElement());
    }
}