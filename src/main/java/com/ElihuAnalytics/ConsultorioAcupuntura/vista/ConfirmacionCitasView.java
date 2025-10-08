package com.ElihuAnalytics.ConsultorioAcupuntura.vista;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Paciente;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.NotificacionService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.PacienteService;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.SesionService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Route(value = "medico/citas", layout = LayoutPrincipal.class)
@PageTitle("Confirmación de citas")
@RolesAllowed({"ADMIN", "MEDICO"})
@CssImport("./styles/medico-citas.css") // Corregido: añadido prefijo ./
public class ConfirmacionCitasView extends VerticalLayout {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final PacienteService pacienteService;
    private final SesionService sesionService;
    private final NotificacionService notificacionService;

    private ComboBox<Paciente> cbPaciente;
    private DatePicker dpFecha;
    private Div lista;

    public ConfirmacionCitasView(PacienteService pacienteService,
                                 SesionService sesionService,
                                 NotificacionService notificacionService) {
        this.pacienteService = pacienteService;
        this.sesionService = sesionService;
        this.notificacionService = notificacionService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        addClassName("medico-citas-view");

        H2 titulo = new H2("Gestión de citas");
        add(titulo);

        cbPaciente = new ComboBox<>("Paciente (opcional)");
        cbPaciente.setItemLabelGenerator(p -> p.getNombres() + " " + p.getApellidos());
        cbPaciente.setClearButtonVisible(true);
        cbPaciente.setWidthFull();
        cbPaciente.setItems(pacienteService.listarTodos());
        cbPaciente.addValueChangeListener(e -> recargar());

        dpFecha = new DatePicker("Fecha");
        dpFecha.setWidth("16rem");
        dpFecha.setValue(LocalDate.now());
        dpFecha.addValueChangeListener(e -> recargar());

        Button btnHoy = new Button("Hoy", e -> {
            dpFecha.setValue(LocalDate.now());
            recargar();
        });
        Button btnSemana = new Button("Semana", e -> abrirRango(LocalDate.now(), LocalDate.now().plusDays(6)));
        Button btnMes = new Button("Mes", e -> {
            YearMonth ym = YearMonth.now();
            abrirRango(ym.atDay(1), ym.atEndOfMonth());
        });
        Button btnAnio = new Button("Año", e -> {
            LocalDate ini = LocalDate.now().withDayOfYear(1);
            LocalDate fin = LocalDate.now().withMonth(12).withDayOfMonth(31);
            abrirRango(ini, fin);
        });

        HorizontalLayout filtros = new HorizontalLayout(cbPaciente, dpFecha, btnHoy, btnSemana, btnMes, btnAnio);
        filtros.setWidthFull();
        filtros.setAlignItems(FlexComponent.Alignment.END);
        filtros.getStyle().set("flex-wrap", "wrap");
        filtros.addClassName("filters");

        lista = new Div();
        lista.addClassName("lista-sesiones");
        lista.setWidthFull();

        add(filtros, lista);
        recargar();
    }

    private void abrirRango(LocalDate ini, LocalDate fin) {
        Dialog dlg = new Dialog();
        dlg.setHeaderTitle("Citas no realizadas · " + FECHA.format(ini) + " - " + FECHA.format(fin));

        Div cont = new Div();
        cont.addClassName("lista-sesiones");

        List<Sesion> sesiones = buscarPendientesEntre(ini, fin);
        if (sesiones.isEmpty()) {
            cont.add(new Paragraph("No hay citas no realizadas en el rango."));
        } else {
            sesiones.forEach(s -> cont.add(itemSesion(s)));
        }
        Button cerrar = new Button("Cerrar", e -> dlg.close());
        dlg.add(cont);
        dlg.getFooter().add(cerrar);
        dlg.open();
    }

    private void recargar() {
        lista.removeAll();
        LocalDate fecha = Optional.ofNullable(dpFecha.getValue()).orElse(LocalDate.now());
        List<Sesion> deHoy = buscarPendientesEntre(fecha, fecha);
        if (deHoy.isEmpty()) {
            lista.add(new Paragraph("No hay citas no realizadas para hoy" +
                    (cbPaciente.getValue() != null ? " de este paciente." : ".")));
            return;
        }
        deHoy.forEach(s -> lista.add(itemSesion(s)));
    }

    private List<Sesion> buscarPendientesEntre(LocalDate ini, LocalDate fin) {
        List<Paciente> pacientes = cbPaciente.getValue() != null
                ? List.of(cbPaciente.getValue())
                : pacienteService.listarTodos();

        Stream<YearMonth> meses = Stream.iterate(YearMonth.from(ini),
                ym -> !ym.isAfter(YearMonth.from(fin)),
                ym -> ym.plusMonths(1));

        return meses.flatMap(ym -> pacientes.stream()
                        .flatMap(p -> sesionService.obtenerSesionesPorPacienteYMes(p.getId(), ym).stream()))
                .filter(s -> {
                    LocalDate d = s.getFecha().toLocalDate();
                    return !d.isBefore(ini) && !d.isAfter(fin);
                })
                .filter(s -> s.getEstado() == Sesion.EstadoSesion.PROGRAMADA || s.getEstado() == Sesion.EstadoSesion.CONFIRMADA)
                .sorted(Comparator.comparing(Sesion::getFecha))
                .collect(Collectors.toList());
    }

    private Component itemSesion(Sesion s) {
        Div row = new Div();
        row.addClassName("item-sesion");

        String estado = s.getEstado() == Sesion.EstadoSesion.PROGRAMADA ? " (Por confirmar)" : " (Confirmada)";
        String cab = FECHA.format(s.getFecha()) + " " + HORA.format(s.getFecha());
        long mins = Math.max(0, Duration.between(LocalDateTime.now(), s.getFecha()).toMinutes());
        String lugar = Optional.ofNullable(s.getLugar()).filter(v -> !v.isBlank()).map(v -> " · Lugar: " + v).orElse("");
        Paragraph info = new Paragraph(cab + " · " + s.getMotivo() + lugar + " · faltan " + mins + " min" + estado);
        info.addClassName("item-sesion__info");
        if (s.getEstado() == Sesion.EstadoSesion.CONFIRMADA) {
            info.addClassName("item-sesion__confirmada");
        }

        Button confirmar = new Button("Confirmar", e -> {
            if (!puedeConfirmarseHoyConAnticipo(s)) {
                Notification.show("La confirmación debe realizarse el mismo día y al menos 2 horas antes de la sesión.");
                return;
            }
            try {
                sesionService.confirmarSesion(s.getId()).orElseThrow(() -> new RuntimeException("No se pudo confirmar la sesión"));
                String mensaje = "Tu cita ha sido confirmada para el " + FECHA.format(s.getFecha()) + " a las " + HORA.format(s.getFecha());
                notificacionService.enviarConfirmacionPaciente(s, mensaje);
                Notification.show("Sesión confirmada y notificaciones enviadas.");
                UI.getCurrent().getPage().reload();
            } catch (Exception ex) {
                Notification.show("No se pudo confirmar la sesión: " + ex.getMessage());
            }
        });
        confirmar.addThemeVariants(ButtonVariant.LUMO_PRIMARY); // Corregido: usar addThemeVariants
        confirmar.setEnabled(s.getEstado() == Sesion.EstadoSesion.PROGRAMADA && puedeConfirmarseHoyConAnticipo(s));

        Button recordatorio = new Button("Recordatorio", e -> {
            try {
                notificacionService.enviarRecordatorioPaciente(s);
                notificacionService.enviarRecordatorioMedico(s);
                Notification.show("Recordatorio enviado.");
            } catch (Exception ex) {
                Notification.show("No se pudo enviar el recordatorio: " + ex.getMessage());
            }
        });
        recordatorio.addThemeVariants(ButtonVariant.LUMO_PRIMARY); // Corregido: usar addThemeVariants
        recordatorio.setEnabled(s.getEstado() == Sesion.EstadoSesion.CONFIRMADA);

        Button cancelar = new Button("Cancelar", e -> {
            Dialog cd = new Dialog();
            cd.setHeaderTitle("Cancelar sesión");
            cd.add(new Paragraph("¿Confirmas cancelar esta sesión?"));
            Button ok = new Button("Sí, cancelar", ev -> {
                sesionService.cancelarSesion(s.getId());
                cd.close();
                Notification.show("Sesión cancelada.");
                UI.getCurrent().getPage().reload();
            });
            ok.addThemeVariants(ButtonVariant.LUMO_PRIMARY); // Corregido: usar addThemeVariants
            Button no = new Button("No", ev -> cd.close());
            no.addThemeVariants(ButtonVariant.LUMO_TERTIARY); // Corregido: usar addThemeVariants
            HorizontalLayout acciones = new HorizontalLayout(ok, no);
            acciones.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
            cd.getFooter().add(acciones);
            cd.open();
        });
        cancelar.addThemeVariants(ButtonVariant.LUMO_ERROR); // Corregido: usar addThemeVariants

        HorizontalLayout acciones = new HorizontalLayout(confirmar, recordatorio, cancelar);
        acciones.getStyle().set("flex-wrap", "wrap");
        acciones.setWidthFull();

        row.add(info, acciones);
        return row;
    }

    private boolean puedeConfirmarseHoyConAnticipo(Sesion s) {
        if (s == null || s.getFecha() == null) return false;
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limite = s.getFecha().minusHours(2);
        return s.getFecha().toLocalDate().isEqual(LocalDate.now()) && ahora.isBefore(limite);
    }
}