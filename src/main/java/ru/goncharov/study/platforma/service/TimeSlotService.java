package ru.goncharov.study.platforma.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class TimeSlotService {

    private static final List<LocalTime> ALL_SLOTS = List.of(
            LocalTime.of(9, 0),
            LocalTime.of(10, 0),
            LocalTime.of(11, 0),
            LocalTime.of(12, 0),
            LocalTime.of(13, 0),
            LocalTime.of(14, 0),
            LocalTime.of(15, 0),
            LocalTime.of(16, 0),
            LocalTime.of(17, 0)
    );

    public List<LocalTime> availableSlots(
            LocalDate date,
            List<LocalTime> booked
    ) {
        return ALL_SLOTS.stream()
                .filter(t -> !booked.contains(t))
                .toList();
    }
}