package ru.goncharov.study.platforma.mapper;

import ru.goncharov.study.platforma.Entity.UserSurvey;
import ru.goncharov.study.platforma.dto.UserSurveyDto;

public class UserSurveyMapper {

    public static UserSurveyDto toDto(UserSurvey e) {
        return new UserSurveyDto(
                e.getId(),
                e.getChatId(),
                e.getName(),
                e.getPhone(),
                e.getQuestionAbout(),
                e.getState()
        );
    }

    public static void updateEntity(UserSurvey e, UserSurveyDto d) {
        e.setName(d.name());
        e.setPhone(d.phone());
        e.setQuestionAbout(d.questionAbout());
        e.setState(d.state());
    }
}