package ru.goncharov.study.platforma.dto;

import ru.goncharov.study.platforma.Entity.SurveyState;

public record UserSurveyDto(
        Long id,
        Long chatId,
        String name,
        String phone,
        String questionAbout,
        SurveyState state
) {}