package ru.katacan.registrationoffice.util;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Утилита для генерации временных слотов записи к врачу.
 * Слоты генерируются на основе рабочего времени врача, перерывов и длительности слота.
 */
public class SlotGenerator {

    /**
     * Генерирует список доступных временных слотов для врача на день.
     *
     * По бизнес-правилу слот для записи всегда равен 60 минутам.
     * Если в данных сохранено другое значение, оно приводится к 60.
     *
     * @param startTime     время начала работы (например, 09:00)
     * @param endTime       время окончания работы (например, 17:00)
     * @param slotMinutes   длительность одного слота в минутах (например, 30)
     * @param breakStart    время начала перерыва (может быть null)
     * @param breakEnd      время окончания перерыва (может быть null)
     * @return список времен начала каждого слота
     */
    public static List<LocalTime> generateSlots(
            LocalTime startTime,
            LocalTime endTime,
            int slotMinutes,
            LocalTime breakStart,
            LocalTime breakEnd) {

        List<LocalTime> slots = new ArrayList<>();

        int effectiveSlotMinutes = normalizeSlotMinutes(slotMinutes);

        if (startTime == null || endTime == null || effectiveSlotMinutes <= 0) {
            return slots;
        }

        LocalTime current = startTime;

        while (true) {
            LocalTime slotEnd = current.plusMinutes(effectiveSlotMinutes);

            // Проверяем, выходит ли слот за рамки рабочего времени
            if (slotEnd.isAfter(endTime)) {
                break;
            }

            // Проверяем, не пересекается ли слот с перерывом
            if (!isSlotDuringBreak(current, slotEnd, breakStart, breakEnd)) {
                slots.add(current);
            }

            current = slotEnd;
        }

        return slots;
    }

    /**
     * Проверяет, пересекается ли временной слот с перерывом врача.
     *
     * @param slotStart  время начала слота
     * @param slotEnd    время окончания слота
     * @param breakStart время начала перерыва
     * @param breakEnd   время окончания перерыва
     * @return true, если слот пересекается с перерывом, иначе false
     */
    private static boolean isSlotDuringBreak(
            LocalTime slotStart,
            LocalTime slotEnd,
            LocalTime breakStart,
            LocalTime breakEnd) {

        // Если перерыва нет, то слот не может быть во время перерыва
        if (breakStart == null || breakEnd == null) {
            return false;
        }

        // Слот пересекается с перерывом, если:
        // - начало слота раньше конца перерыва И
        // - конец слота позже начала перерыва
        return slotStart.isBefore(breakEnd) && slotEnd.isAfter(breakStart);
    }

    /**
     * Рассчитывает общее количество слотов в рабочем дне врача.
     *
     * @param startTime   время начала работы
     * @param endTime     время окончания работы
     * @param slotMinutes длительность одного слота в минутах
     * @param breakStart  время начала перерыва (может быть null)
     * @param breakEnd    время окончания перерыва (может быть null)
     * @return количество слотов
     */
    public static int calculateTotalSlots(
            LocalTime startTime,
            LocalTime endTime,
            int slotMinutes,
            LocalTime breakStart,
            LocalTime breakEnd) {

        int effectiveSlotMinutes = normalizeSlotMinutes(slotMinutes);

        if (startTime == null || endTime == null || effectiveSlotMinutes <= 0) {
            return 0;
        }

        long totalMinutes = java.time.Duration.between(startTime, endTime).toMinutes();

        // Вычитаем время перерыва
        if (breakStart != null && breakEnd != null) {
            long breakMinutes = java.time.Duration.between(breakStart, breakEnd).toMinutes();
            totalMinutes -= breakMinutes;
        }

        return (int) (totalMinutes / effectiveSlotMinutes);
    }

    private static int normalizeSlotMinutes(int slotMinutes) {
        if (slotMinutes <= 0) {
            return 0;
        }

        return 60;
    }
}

