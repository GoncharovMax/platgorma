package ru.goncharov.study.platforma.mapper;

import ru.goncharov.study.platforma.Entity.AppointmentEntity;
import ru.goncharov.study.platforma.dto.AppointmentDto;

public class AppointmentMapper {

    public static AppointmentDto toDto(AppointmentEntity e) {
        return new AppointmentDto(
                e.getId(),
                e.getChatId(),
                e.getDate(),
                e.getTime(),
                e.getFullName(),
                e.getIcsUid(),
                e.isNotified()
        );
    }

    public static AppointmentEntity toEntity(AppointmentDto d) {
        AppointmentEntity e = new AppointmentEntity();
        e.setChatId(d.chatId());
        e.setDate(d.date());
        e.setTime(d.time());
        e.setFullName(d.fullName());
        e.setIcsUid(d.icsUid());
        e.setNotified(d.notified());
        return e;
    }
}