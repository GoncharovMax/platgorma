package ru.goncharov.study.platforma.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.goncharov.study.platforma.Entity.AppointmentEntity;
import ru.goncharov.study.platforma.repository.AppointmentRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository repository;

    public boolean isTimeAvailable(LocalDate date, LocalTime time) {
        return !repository.existsByDateAndTime(date, time);
    }

    public List<LocalTime> getAvailableTimes(LocalDate date) {
        List<LocalTime> all = List.of(
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                LocalTime.of(12, 0),
                LocalTime.of(13, 0),
                LocalTime.of(14, 0)
        );

        List<AppointmentEntity> booked = repository.findByDate(date);
        List<LocalTime> bookedTimes = booked.stream()
                .map(AppointmentEntity::getTime)
                .toList();

        return all.stream()
                .filter(t -> !bookedTimes.contains(t))
                .toList();
    }
}