package ru.goncharov.study.platforma.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "appointments",
        uniqueConstraints = @UniqueConstraint(name = "uk_appointments_date_time", columnNames = {"date", "time"}))
@Data
public class AppointmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatId;

    private LocalDate date;

    private LocalTime time;

    private String fullName;

    private String icsUid;

    private LocalDateTime remindAt;

    private boolean notified = false;


}
