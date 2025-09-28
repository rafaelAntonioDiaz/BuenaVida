self.addEventListener('notificationclick', (event) => {
    const notification = event.notification;
    const action = event.action;

    console.log('Notificación clickeada:', action || 'default');

    notification.close();

    // Manejar acciones específicas
    const handleAction = async () => {
        const data = notification.data || {};
        const sesionId = data.sesionId;

        switch (action) {
            case 'confirmar':
                await fetch('/api/citas/confirmar/' + sesionId, { method: 'POST' });
                break;
            case 'reagendar':
                // Abrir la app en la página de reagendamiento
                clients.openWindow('/reagendar/' + sesionId);
                return;
            case 'cancelar':
                await fetch('/api/citas/cancelar/' + sesionId, { method: 'POST' });
                break;
            default:
                // Click en la notificación sin botón específico
                break;
        }

        // Abrir o enfocar la aplicación
        const windowClients = await clients.matchAll({ type: 'window' });
        if (windowClients.length > 0) {
            windowClients[0].focus();
        } else {
            clients.openWindow('/');
        }
    };

    event.waitUntil(handleAction());
});

self.addEventListener('notificationclose', (event) => {
    console.log('Notificación cerrada:', event.notification.tag);
});

// Manejar notificaciones programadas
self.addEventListener('message', (event) => {
    if (event.data.type === 'SCHEDULE_NOTIFICATION') {
        const { title, options, delay } = event.data;

        setTimeout(() => {
            self.registration.showNotification(title, options);
        }, delay);
    }
});