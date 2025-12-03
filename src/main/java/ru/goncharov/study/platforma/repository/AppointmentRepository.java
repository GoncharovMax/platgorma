package ru.goncharov.study.platforma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.goncharov.study.platforma.Entity.AppointmentEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {

    List<AppointmentEntity> findByDate(LocalDate date);

    boolean existsByDateAndTime(LocalDate date, LocalTime time);
}