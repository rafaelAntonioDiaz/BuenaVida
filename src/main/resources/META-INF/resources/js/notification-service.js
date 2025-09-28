class NotificationService {
    constructor() {
        this.permission = 'default';
        this.initializeNotifications();
    }

    async initializeNotifications() {
        // Verificar soporte del navegador
        if (!('Notification' in window)) {
            console.warn('Este navegador no soporta notificaciones de escritorio');
            return;
        }

        // Solicitar permisos si es necesario
        if (Notification.permission === 'default') {
            this.permission = await Notification.requestPermission();
        } else {
            this.permission = Notification.permission;
        }

        console.log('Notificaciones inicializadas. Permiso:', this.permission);
    }

    // Mostrar notificación nativa del sistema
    showNativeNotification(title, options = {}) {
        if (this.permission !== 'granted') {
            console.warn('Permisos de notificación no otorgados');
            return null;
        }

        const defaultOptions = {
            icon: '/icons/icon-192x192.png',
            badge: '/icons/badge-72x72.png',
            vibrate: [200, 100, 200],
            requireInteraction: false, // Cambiar a false para pruebas
            data: {
                timestamp: Date.now(),
                sesionId: options.sesionId || null
            }
        };

        const finalOptions = { ...defaultOptions, ...options };

        console.log('Creando notificación:', title, finalOptions);
        const notification = new Notification(title, finalOptions);

        // Configurar eventos
        notification.onclick = (event) => {
            event.preventDefault();
            window.focus();

            console.log('Notificación clickeada');

            // Llamar callback si existe
            if (window.notificationShown) {
                window.notificationShown(
                    finalOptions.data.sesionId || 'unknown',
                    title,
                    finalOptions.body || 'Sin mensaje'
                );
            }

            notification.close();
        };

        notification.onshow = () => {
            console.log('Notificación mostrada');

            // Llamar callback cuando se muestre la notificación
            if (window.notificationShown) {
                window.notificationShown(
                    finalOptions.data.sesionId || 'unknown',
                    title,
                    finalOptions.body || 'Sin mensaje'
                );
            }
        };

        notification.onclose = () => {
            console.log('Notificación cerrada');
        };

        notification.onerror = (error) => {
            console.error('Error en notificación:', error);
        };

        return notification;
    }

    // Notificación específica para citas
    showAppointmentConfirmation(sesionId, fecha, paciente, motivo) {
        const title = '🏥 Cita de Acupuntura';
        const body = `${paciente} - ${fecha}\n${motivo}`;

        console.log('Mostrando notificación de cita:', {sesionId, fecha, paciente, motivo});

        return this.showNativeNotification(title, {
            body: body,
            tag: `cita-${sesionId}`, // Evita notificaciones duplicadas
            sesionId: sesionId,
            data: {
                sesionId: sesionId,
                tipo: 'confirmacion_cita',
                paciente: paciente,
                fecha: fecha
            }
        });
    }
}

// Instancia global
window.notificationService = new NotificationService();

// Función principal expuesta para llamar desde Java/Vaadin
window.showAppointmentNotification = (sesionId, fecha, paciente, motivo) => {
    console.log('showAppointmentNotification llamada con:', {sesionId, fecha, paciente, motivo});
    return window.notificationService.showAppointmentConfirmation(sesionId, fecha, paciente, motivo);
};

// Función para solicitar permisos
window.requestNotificationPermission = async () => {
    console.log('Solicitando permisos de notificación...');
    return await window.notificationService.initializeNotifications();
};

// Debug: Exponer función de prueba simple
window.testNotification = () => {
    console.log('Test de notificación simple');
    if (window.notificationService.permission === 'granted') {
        const notification = new Notification('Prueba', {
            body: 'Esta es una notificación de prueba',
            icon: '/icons/icon-192x192.png'
        });

        notification.onshow = () => {
            console.log('Notificación de prueba mostrada');
        };

        return notification;
    } else {
        console.warn('Sin permisos para notificaciones');
    }
};

console.log('notification-service.js cargado correctamente');
