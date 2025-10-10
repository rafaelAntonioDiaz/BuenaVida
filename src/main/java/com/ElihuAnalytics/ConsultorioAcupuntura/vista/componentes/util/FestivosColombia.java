package com.ElihuAnalytics.ConsultorioAcupuntura.vista.componentes.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class FestivosColombia {

    private FestivosColombia() {}

    public static boolean esFestivo(LocalDate fecha) {
        return nombreFestivo(fecha).isPresent();
    }

    public static Optional<String> nombreFestivo(LocalDate fecha) {
        Map<LocalDate, String> festivos = festivosPorAnio(fecha.getYear());
        return Optional.ofNullable(festivos.get(fecha));
    }

    public static Map<LocalDate, String> festivosPorAnio(int year) {
        Map<LocalDate, String> res = new LinkedHashMap<>();

        // Fijos (no se mueven)
        put(res, LocalDate.of(year, Month.JANUARY, 1), "Año Nuevo");
        put(res, LocalDate.of(year, Month.MAY, 1), "Día del Trabajo");
        put(res, LocalDate.of(year, Month.JULY, 20), "Independencia de Colombia");
        put(res, LocalDate.of(year, Month.AUGUST, 7), "Batalla de Boyacá");
        put(res, LocalDate.of(year, Month.DECEMBER, 8), "Inmaculada Concepción");
        put(res, LocalDate.of(year, Month.DECEMBER, 25), "Navidad");

        // Pascua y festivos relacionados
        LocalDate pascua = pascuaOccidental(year);
        put(res, pascua.minusDays(3), "Jueves Santo");
        put(res, pascua.minusDays(2), "Viernes Santo");

        // Ley Emiliani (se traslada al lunes siguiente si no cae en lunes)
        trasladarALunes(res, LocalDate.of(year, Month.JANUARY, 6), "Epifanía del Señor");
        trasladarALunes(res, LocalDate.of(year, Month.MARCH, 19), "San José");
        trasladarALunes(res, LocalDate.of(year, Month.JUNE, 29), "San Pedro y San Pablo");
        trasladarALunes(res, LocalDate.of(year, Month.AUGUST, 15), "Asunción de la Virgen");
        trasladarALunes(res, LocalDate.of(year, Month.OCTOBER, 12), "Día de la Raza");
        trasladarALunes(res, LocalDate.of(year, Month.NOVEMBER, 1), "Todos los Santos");
        trasladarALunes(res, LocalDate.of(year, Month.NOVEMBER, 11), "Independencia de Cartagena");

        // Festivos móviles basados en Pascua (con traslado a lunes)
        trasladarALunes(res, pascua.plusDays(39), "Ascensión del Señor");    // Jueves -> lunes siguiente
        trasladarALunes(res, pascua.plusDays(60), "Corpus Christi");         // Jueves -> lunes siguiente
        trasladarALunes(res, pascua.plusDays(68), "Sagrado Corazón de Jesús"); // Domingo -> lunes siguiente

        return res;
    }

    private static void trasladarALunes(Map<LocalDate, String> mapa, LocalDate fecha, String nombre) {
        LocalDate observancia = (fecha.getDayOfWeek() == DayOfWeek.MONDAY)
                ? fecha
                : fecha.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        put(mapa, observancia, nombre);
    }

    private static void put(Map<LocalDate, String> mapa, LocalDate fecha, String nombre) {
        mapa.put(fecha, nombre);
    }

    // Algoritmo de Pascua occidental (Meeus/Jones/Butcher)
    private static LocalDate pascuaOccidental(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;      // 3=Marzo, 4=Abril
        int day = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(year, month, day);
    }
}