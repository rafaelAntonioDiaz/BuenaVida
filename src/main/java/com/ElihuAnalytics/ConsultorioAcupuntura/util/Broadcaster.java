package com.ElihuAnalytics.ConsultorioAcupuntura.util;

import com.vaadin.flow.shared.Registration;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Broadcaster simple para enviar mensajes (notificaciones) en tiempo real
 * a todas las vistas que estén suscritas.
 */
public class Broadcaster {

    // Lista de listeners (cada paciente conectado escucha aquí)
    private static final CopyOnWriteArrayList<Consumer<String>> listeners = new CopyOnWriteArrayList<>();

    /**
     * Registra un nuevo listener (ej: un paciente en su vista).
     * Retorna un Registration para que luego se pueda desuscribir.
     */
    public static Registration register(Consumer<String> listener) {
        listeners.add(listener);

        // Devuelve un "desuscriptor" que elimina al listener cuando ya no se necesite
        return () -> listeners.remove(listener);
    }

    /**
     * Envía un mensaje a todos los listeners registrados.
     */
    public static void broadcast(String message) {
        for (Consumer<String> listener : listeners) {
            listener.accept(message);
        }
    }
}
