package ru.goncharov.study.platforma.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table (name = "appointments")
@Data
@Setter
@Getter
public class AppointmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatId;

    private LocalDate date;

    private LocalTime time;

    private String fullName;

    private String icsUid;


}
