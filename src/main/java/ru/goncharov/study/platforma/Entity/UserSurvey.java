package ru.goncharov.study.platforma.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class UserSurvey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatId;

    private String name;
    private String phone;
    private String questionAbout;

    @Enumerated(EnumType.STRING)
    private SurveyState state;
}