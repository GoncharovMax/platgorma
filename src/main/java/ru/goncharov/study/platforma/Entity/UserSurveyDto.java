package ru.goncharov.study.platforma.Entity;

public record UserSurveyDto(
        Long id,
        Long chatId,
        String name,
        String phone,
        String questionAbout,
        SurveyState state
) {}