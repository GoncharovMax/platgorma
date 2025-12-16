package ru.goncharov.study.platforma.Entity;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentDto(
        Long id,
        Long chatId,
        LocalDate date,
        LocalTime time,
        String fullName,
        String icsUid,
        boolean notified
) {}