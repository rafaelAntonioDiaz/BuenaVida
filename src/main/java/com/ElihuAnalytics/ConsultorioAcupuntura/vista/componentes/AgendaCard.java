package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes;

import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Paciente;
import com.ElihuAnalytics.ConsultorioAcupuntura.modelo.Sesion;
import com.ElihuAnalytics.ConsultorioAcupuntura.servicio.SesionService;
import com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.util.FestivosColombia;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AgendaCard extends Div {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FORMATO_MES = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es"));
    private static final Duration DURACION_SESION = Duration.ofHours(1);
    private final Paciente paciente;
    private final SesionService sesionService;
    private YearMonth mesMostrado = YearMonth.now();
    private final Map<LocalDate, Integer> sesionesPorDia = new HashMap<>();
    private Div calendario;
    private Div calendarioContainer;
    private Div listaContainer;
    private LocalDate diaSeleccionado;
    private Div celdaSeleccionada;
    private HorizontalLayout navMes;
    private Span etiquetaMes;

    public AgendaCard(Paciente paciente, SesionService sesionService) {
        this.paciente = paciente;
        this.sesionService = sesionService;
        setWidthFull();
        getStyle()
                .set("padding", "var(--lumo-space-l)")
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("box-sizing", "border-box")
                .set("min-width", "0");
        add(new H3("Programación de Sesiones"));
        navMes = construirNavMes();
        add(navMes);
        // Contenedores para mantener el orden visual estable
        listaContainer = new Div();
        listaContainer.setWidthFull();
        listaContainer.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "8px");
        add(listaContainer);

        calendarioContainer = new Div();
        calendarioContainer.setWidthFull();
        add(calendarioContainer);

        // Cargar datos iniciales y construir secciones
        recargarSesionesMes();
        refrescarListaSesiones();

        calendario = construirCalendarioMes();
        calendarioContainer.removeAll();
        calendarioContainer.add(calendario);

        // Controles de agendamiento (el botón Agendar va AL FINAL)
        TimePicker horaPicker = new TimePicker();
        horaPicker.setLabel("Hora");
        horaPicker.setStep(Duration.ofMinutes(30));
        horaPicker.setValue(LocalTime.of(9, 0));
        horaPicker.setWidthFull();

        TextField motivo = new TextField();
        motivo.setLabel("Motivo");
        motivo.setPlaceholder("Ej.: Control, dolor lumbar...");
        motivo.setClearButtonVisible(true);
        motivo.setMaxLength(120);
        motivo.setWidthFull();

        TextField direccion = new TextField();
        direccion.setLabel("Dirección");
        direccion.setPlaceholder("Ej.: Calle 12 #34-56, Apto 301");
        direccion.setClearButtonVisible(true);
        direccion.setMaxLength(200);
        direccion.setWidthFull();

        Button agendar = new Button("Agendar");
        agendar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        agendar.setWidthFull();

        agendar.addClickListener(e -> {
            if (diaSeleccionado == null) {
                Notification.show("Selecciona un día en el calendario.");
                return;
            }
            if (horaPicker.getValue() == null) {
                Notification.show("Selecciona una hora.");
                return;
            }
            String txtMotivo = Optional.ofNullable(motivo.getValue()).orElse("").trim();
            if (txtMotivo.isBlank()) {
                Notification.show("Escribe el motivo.");
                return;
            }
            String txtDireccion = Optional.ofNullable(direccion.getValue()).orElse("").trim();
            if (txtDireccion.isBlank()) {
                Notification.show("La dirección es obligatoria.");
                return;
            }
            LocalDateTime inicio = diaSeleccionado.atTime(horaPicker.getValue());
            if (inicio.isBefore(LocalDateTime.now())) {
                Notification.show("No puedes agendar en el pasado.");
                return;
            }
            if (!sesionService.estaDisponible(inicio, DURACION_SESION)) {
                Notification.show("No disponible: existe otra cita en la hora previa o en la hora posterior de desplazamiento.");
                return;
            }

            Sesion sesion = new Sesion();
            sesion.setFecha(inicio);
            sesion.setMotivo(txtMotivo);
            sesion.setEstado(Sesion.EstadoSesion.PROGRAMADA);
            sesion.setPaciente(paciente);
            sesion.setLugar(txtDireccion);
            sesionService.guardarSesion(sesion);

            Notification.show("Sesión programada: " + inicio.toLocalDate().format(FORMATO_FECHA) + " " + horaPicker.getValue());
            recargarSesionesMes();
            repintarCalendario();
            refrescarListaSesiones();
            motivo.clear();
            direccion.clear();
        });

        // Diseño responsive del formulario
        FormLayout form = new FormLayout();
        form.setWidthFull();
        // Orden: primero campos, AL FINAL el botón Agendar
        form.add(horaPicker, motivo, direccion, agendar);

        // En móvil: 1 columna. En ≥600px: 2 columnas; Motivo, Dirección y Agendar ocupan el ancho completo.
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );
        form.setColspan(motivo, 2);
        form.setColspan(direccion, 2);
        form.setColspan(agendar, 2);

        add(form);

        add(new Paragraph("Tip: primero gestiona tus citas desde la lista. Luego selecciona un día en el calendario, llena los campos y por último pulsa Agendar."));
    }

    private HorizontalLayout construirNavMes() {
        Button prev = new Button("‹");
        prev.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        prev.addClickListener(e -> actualizarMesMostrado(mesMostrado.minusMonths(1)));

        etiquetaMes = new Span(capitalizarInicialMes(FORMATO_MES.format(mesMostrado)));
        etiquetaMes.getStyle().set("font-weight", "600");

        Button next = new Button("›");
        next.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        next.addClickListener(e -> actualizarMesMostrado(mesMostrado.plusMonths(1)));

        HorizontalLayout nav = new HorizontalLayout(prev, etiquetaMes, next);
        nav.setWidthFull();
        nav.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        nav.setSpacing(true);
        return nav;
    }

    private void actualizarMesMostrado(YearMonth nuevoMes) {
        mesMostrado = nuevoMes;
        etiquetaMes.setText(capitalizarInicialMes(FORMATO_MES.format(mesMostrado)));
        recargarSesionesMes();
        repintarCalendario();
        refrescarListaSesiones();
        if (diaSeleccionado == null || !YearMonth.from(diaSeleccionado).equals(mesMostrado)) {
            diaSeleccionado = null;
            if (celdaSeleccionada != null) {
                celdaSeleccionada.getStyle().remove("box-shadow");
                celdaSeleccionada = null;
            }
        }
    }

    private void recargarSesionesMes() {
        sesionesPorDia.clear();
        sesionService.obtenerSesionesPorPacienteYMes(paciente.getId(), mesMostrado).stream()
                .filter(s -> s.getEstado() == Sesion.EstadoSesion.PROGRAMADA)
                .collect(Collectors.groupingBy(s -> s.getFecha().toLocalDate(), Collectors.counting()))
                .forEach((k, v) -> sesionesPorDia.put(k, v.intValue()));
    }

    private Div construirCalendarioMes() {
        Div cont = new Div();
        cont.setWidthFull();
        cont.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(7, minmax(0, 1fr))")
                .set("gap", "6px")
                .set("box-sizing", "border-box")
                .set("max-width", "100%")
                .set("min-width", "0");

        String[] dias = {"L", "M", "X", "J", "V", "S", "D"};
        for (String d : dias) {
            Div hd = new Div();
            hd.setText(d);
            hd.getStyle()
                    .set("text-align", "center")
                    .set("font-weight", "600")
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("min-width", "0");
            if ("D".equals(d)) {
                hd.getStyle().set("color", "var(--lumo-error-text-color)");
            }
            cont.add(hd);
        }

        LocalDate primero = mesMostrado.atDay(1);
        int offset = primero.getDayOfWeek().getValue() - 1; // lunes=1
        int diasMes = mesMostrado.lengthOfMonth();

        for (int i = 0; i < offset; i++) cont.add(crearCeldaVacia());

        for (int dia = 1; dia <= diasMes; dia++) {
            LocalDate fecha = mesMostrado.atDay(dia);
            int count = sesionesPorDia.getOrDefault(fecha, 0);
            Div celda = crearCeldaDia(fecha, count);
            cont.add(celda);
        }
        return cont;
    }

    private Div crearCeldaVacia() {
        Div d = new Div();
        d.getStyle()
                .set("min-height", "42px")
                .set("border-radius", "8px")
                .set("background", "transparent")
                .set("min-width", "0");
        return d;
    }

    private Div crearCeldaDia(LocalDate fecha, int count) {
        boolean esDomingo = fecha.getDayOfWeek() == DayOfWeek.SUNDAY;
        boolean esFestivo = FestivosColombia.esFestivo(fecha);

        Div cell = new Div();
        cell.getStyle()
                .set("min-height", "42px")
                .set("padding", "6px")
                .set("border-radius", "8px")
                .set("cursor", "pointer")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "space-between")
                .set("box-sizing", "border-box")
                .set("min-width", "0")
                .set("overflow", "hidden");

        Div num = new Div();
        num.setText(String.valueOf(fecha.getDayOfMonth()));
        num.getStyle().set("font-weight", "600");

        if (esDomingo) {
            num.getStyle().set("color", "var(--lumo-error-text-color)");
            cell.getElement().setProperty("aria-label", "Domingo");
        }
        if (esFestivo) {
            cell.getStyle().set("background", "var(--lumo-error-color-10pct)");
            FestivosColombia.nombreFestivo(fecha)
                    .ifPresent(nombre -> cell.getElement().setProperty("title", "Festivo: " + nombre));
            if (!esDomingo) {
                num.getStyle().set("color", "var(--lumo-error-text-color)");
            }
        } else if (count > 0) {
            cell.getStyle().set("background", "var(--lumo-primary-color-10pct)");
        }

        if (fecha.equals(LocalDate.now())) {
            cell.getStyle().set("border", "2px solid var(--lumo-primary-color-50pct)");
        } else {
            cell.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
        }

        com.vaadin.flow.component.button.Button badgeBtn = null;
        if (count > 0) {
            badgeBtn = new com.vaadin.flow.component.button.Button(String.valueOf(count));
            badgeBtn.addThemeVariants(
                    com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY_INLINE,
                    com.vaadin.flow.component.button.ButtonVariant.LUMO_SMALL
            );
            badgeBtn.getStyle()
                    .set("min-width", "18px")
                    .set("height", "18px")
                    .set("line-height", "18px")
                    .set("text-align", "center")
                    .set("font-size", "11px")
                    .set("color", "white")
                    .set("background", "var(--lumo-primary-color)")
                    .set("border-radius", "10px")
                    .set("padding", "0 4px");
            LocalDate fechaDetalle = fecha;
            badgeBtn.addClickListener(e -> abrirDetalleDia(fechaDetalle));
        }

        if (badgeBtn != null) {
            cell.add(num, badgeBtn);
        } else {
            cell.add(num);
        }

        cell.addClickListener(e -> {
            if (celdaSeleccionada != null) {
                celdaSeleccionada.getStyle().remove("box-shadow");
            }
            celdaSeleccionada = cell;
            diaSeleccionado = fecha;
            cell.getStyle().set("box-shadow", "0 0 0 2px var(--lumo-primary-color)");
        });

        return cell;
    }

    private void repintarCalendario() {
        calendarioContainer.removeAll();
        calendario = construirCalendarioMes();
        calendarioContainer.add(calendario);
    }

    private void abrirDetalleDia(LocalDate fecha) {
        Dialog dlg = new Dialog();
        dlg.setHeaderTitle("Sesiones para " + fecha.format(FORMATO_FECHA));

        Div cont = new Div();
        cont.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "8px");

        final Runnable[] refrescarLista = new Runnable[1];
        refrescarLista[0] = () -> {
            cont.removeAll();
            var sesiones = sesionService.obtenerSesionesPorPacienteYDia(paciente.getId(), fecha);
            if (sesiones.isEmpty()) {
                cont.add(new Paragraph("No hay sesiones en este día."));
            } else {
                sesiones.forEach(s -> cont.add(crearItemSesion(dlg, s, fecha, refrescarLista[0])));
            }
        };
        refrescarLista[0].run();

        Button cerrar = new Button("Cerrar", e -> dlg.close());
        dlg.add(cont);
        dlg.getFooter().add(cerrar);
        dlg.open();
    }

    // Item de detalle del día (responsive)
    private Div crearItemSesion(Dialog parent, Sesion s, LocalDate fechaDia, Runnable refrescarLista) {
        Div item = new Div();
        item.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("align-items", "center")
                .set("gap", "8px")
                .set("padding", "6px 0");

        String lugar = Optional.ofNullable(s.getLugar()).filter(v -> !v.isBlank()).orElse("Sin dirección");
        String info = FORMATO_HORA.format(s.getFecha()) + " · " + s.getMotivo() + " · " + s.getEstado() + " · " + lugar;
        Span texto = new Span(info);
        texto.getStyle()
                .set("flex", "1 1 280px")
                .set("min-width", "0")
                .set("white-space", "normal")
                .set("overflow-wrap", "anywhere");

        Button editarDireccion = new Button("Editar dirección");
        editarDireccion.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        editarDireccion.addClickListener(e -> abrirEditarDireccionDialog(s, refrescarLista));

        Button reprogramar = new Button("Reprogramar");
        reprogramar.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        reprogramar.addClickListener(e -> abrirReprogramarDialog(parent, s, fechaDia));

        Button cancelar = new Button("Cancelar");
        cancelar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        cancelar.addClickListener(e -> {
            ConfirmDialog cd = new ConfirmDialog();
            cd.setHeader("Cancelar sesión");
            cd.setText("¿Confirmas cancelar esta sesión?");
            cd.setConfirmText("Sí, cancelar");
            cd.setCancelText("No");
            cd.addConfirmListener(ev -> {
                sesionService.cancelarSesion(s.getId());
                Notification.show("Sesión cancelada.");
                recargarSesionesMes();
                repintarCalendario();
                parent.close();
                abrirDetalleDia(fechaDia);
            });
            cd.open();
        });

        HorizontalLayout acciones = new HorizontalLayout(editarDireccion, reprogramar, cancelar);
        acciones.setSpacing(true);
        acciones.setWrap(true);
        acciones.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        acciones.getStyle()
                .set("flex", "0 1 320px")
                .set("min-width", "0");

        item.add(texto, acciones);
        return item;
    }

    // Lista de citas (sección superior)
    private void refrescarListaSesiones() {
        listaContainer.removeAll();

        Span titulo = new Span("Citas programadas del mes");
        titulo.getStyle().set("font-weight", "600");
        listaContainer.add(titulo);

        var sesiones = sesionService.obtenerSesionesPorPacienteYMes(paciente.getId(), mesMostrado).stream()
                .filter(s -> s.getEstado() == Sesion.EstadoSesion.PROGRAMADA)
                .sorted(Comparator.comparing(Sesion::getFecha))
                .collect(Collectors.toList());

        if (sesiones.isEmpty()) {
            Paragraph vacio = new Paragraph("No tienes citas programadas en este mes.");
            listaContainer.add(vacio);
            return;
        }

        Div cont = new Div();
        cont.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "8px");
        sesiones.forEach(s -> cont.add(crearItemSesionInline(s)));
        listaContainer.add(cont);
    }

    // Item de la lista superior (responsive)
    private Div crearItemSesionInline(Sesion s) {
        Div item = new Div();
        item.getStyle()
                .set("display", "flex")
                .set("flex-wrap", "wrap")
                .set("align-items", "center")
                .set("gap", "8px")
                .set("padding", "6px 0");

        String lugar = Optional.ofNullable(s.getLugar()).filter(v -> !v.isBlank()).orElse("Sin dirección");
        String info = s.getFecha().format(FORMATO_FECHA) + " " + FORMATO_HORA.format(s.getFecha()) +
                " · " + s.getMotivo() + " · " + s.getEstado() + " · " + lugar;
        Span texto = new Span(info);
        texto.getStyle()
                .set("flex", "1 1 280px")
                .set("min-width", "0")
                .set("white-space", "normal")
                .set("overflow-wrap", "anywhere");

        Button editarDireccion = new Button("Editar dirección");
        editarDireccion.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        editarDireccion.addClickListener(e ->
                abrirEditarDireccionDialog(s, () -> {
                    refrescarListaSesiones();
                    recargarSesionesMes();
                    repintarCalendario();
                })
        );

        Button reprogramar = new Button("Reprogramar");
        reprogramar.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        reprogramar.addClickListener(e -> abrirReprogramarDialogInline(s));

        Button cancelar = new Button("Cancelar");
        cancelar.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        cancelar.addClickListener(e -> {
            ConfirmDialog cd = new ConfirmDialog();
            cd.setHeader("Cancelar sesión");
            cd.setText("¿Confirmas cancelar esta sesión?");
            cd.setConfirmText("Sí, cancelar");
            cd.setCancelText("No");
            cd.addConfirmListener(ev -> {
                sesionService.cancelarSesion(s.getId());
                Notification.show("Sesión cancelada.");
                recargarSesionesMes();
                repintarCalendario();
                refrescarListaSesiones();
            });
            cd.open();
        });

        HorizontalLayout acciones = new HorizontalLayout(editarDireccion, reprogramar, cancelar);
        acciones.setSpacing(true);
        acciones.setWrap(true);
        acciones.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        acciones.getStyle()
                .set("flex", "0 1 320px")
                .set("min-width", "0");

        item.add(texto, acciones);
        return item;
    }

    private void abrirEditarDireccionDialog(Sesion s, Runnable onSaved) {
        Dialog dlg = new Dialog();
        dlg.setHeaderTitle("Editar dirección");

        TextField tf = new TextField("Dirección");
        tf.setWidthFull();
        tf.setMaxLength(200);
        tf.setClearButtonVisible(true);
        tf.setPlaceholder("Dirección donde deseas ser atendido");
        tf.setValue(Optional.ofNullable(s.getLugar()).orElse(""));

        Button guardar = new Button("Guardar", e -> {
            String val = Optional.ofNullable(tf.getValue()).orElse("").trim();
            if (val.isBlank()) {
                Notification.show("La dirección no puede estar vacía.");
                return;
            }
            s.setLugar(val);
            sesionService.guardarSesion(s);
            Notification.show("Dirección actualizada.");
            dlg.close();
            if (onSaved != null) onSaved.run();
        });
        guardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cerrar = new Button("Cerrar", e -> dlg.close());

        dlg.add(tf);
        dlg.getFooter().add(new HorizontalLayout(guardar, cerrar));
        dlg.open();
    }

    private void abrirReprogramarDialog(Dialog parent, Sesion s, LocalDate fechaDia) {
        Dialog dlg = new Dialog();
        dlg.setHeaderTitle("Reprogramar sesión");

        DatePicker dp = new DatePicker("Fecha");
        dp.setValue(s.getFecha().toLocalDate());

        TimePicker tp = new TimePicker("Hora");
        tp.setStep(Duration.ofMinutes(30));
        tp.setValue(s.getFecha().toLocalTime());

        Button guardar = new Button("Guardar", e -> {
            if (dp.getValue() == null || tp.getValue() == null) {
                Notification.show("Selecciona fecha y hora.");
                return;
            }
            LocalDateTime nuevoInicio = dp.getValue().atTime(tp.getValue());
            if (nuevoInicio.isBefore(LocalDateTime.now())) {
                Notification.show("No puedes reprogramar al pasado.");
                return;
            }
            var ok = sesionService.reprogramarSesion(s.getId(), nuevoInicio, DURACION_SESION);
            if (ok.isEmpty()) {
                Notification.show("No disponible por conflicto con otra cita (ventana de desplazamiento).");
                return;
            }
            Notification.show("Sesión reprogramada.");
            recargarSesionesMes();
            repintarCalendario();
            dlg.close();
            parent.close();
            abrirDetalleDia(nuevoInicio.toLocalDate());
        });

        Button cancelar = new Button("Cerrar", e -> dlg.close());

        HorizontalLayout acciones = new HorizontalLayout(guardar, cancelar);
        acciones.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        dlg.add(new HorizontalLayout(dp, tp));
        dlg.getFooter().add(acciones);
        dlg.open();
    }

    // Versión inline para la lista superior (sin parent dialog)
    private void abrirReprogramarDialogInline(Sesion s) {
        Dialog dlg = new Dialog();
        dlg.setHeaderTitle("Reprogramar sesión");

        DatePicker dp = new DatePicker("Fecha");
        dp.setValue(s.getFecha().toLocalDate());

        TimePicker tp = new TimePicker("Hora");
        tp.setStep(Duration.ofMinutes(30));
        tp.setValue(s.getFecha().toLocalTime());

        Button guardar = new Button("Guardar", e -> {
            if (dp.getValue() == null || tp.getValue() == null) {
                Notification.show("Selecciona fecha y hora.");
                return;
            }
            LocalDateTime nuevoInicio = dp.getValue().atTime(tp.getValue());
            if (nuevoInicio.isBefore(LocalDateTime.now())) {
                Notification.show("No puedes reprogramar al pasado.");
                return;
            }
            var ok = sesionService.reprogramarSesion(s.getId(), nuevoInicio, DURACION_SESION);
            if (ok.isEmpty()) {
                Notification.show("No disponible por conflicto con otra cita (ventana de desplazamiento).");
                return;
            }
            Notification.show("Sesión reprogramada.");
            recargarSesionesMes();
            repintarCalendario();
            refrescarListaSesiones();
            dlg.close();
        });

        Button cancelar = new Button("Cerrar", e -> dlg.close());

        HorizontalLayout acciones = new HorizontalLayout(guardar, cancelar);
        acciones.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        dlg.add(new HorizontalLayout(dp, tp));
        dlg.getFooter().add(acciones);
        dlg.open();
    }

    private String capitalizarInicialMes(String texto) {
        if (texto == null || texto.isBlank()) return texto;
        return texto.substring(0, 1).toUpperCase(new Locale("es")) + texto.substring(1);
    }
}