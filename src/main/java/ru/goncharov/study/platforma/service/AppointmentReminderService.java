package ru.goncharov.study.platforma.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.goncharov.study.platforma.Entity.AppointmentEntity;
import ru.goncharov.study.platforma.repository.AppointmentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentReminderService {

    private final AppointmentRepository appointmentRepo;
    private final TelegramClient telegramClient;

    @Scheduled(fixedDelayString = "${reminder.check-ms:60000}")
    @Transactional
    public void sendReminders() {
        LocalDateTime now = LocalDateTime.now();

        var list = appointmentRepo.findByNotifiedFalseAndRemindAtLessThanEqual(now);

        for (var ap : list) {
            try {
                telegramClient.execute(
                        SendMessage.builder()
                                .chatId(ap.getChatId())
                                .text("⏰ Напоминание!\n\n" +
                                        "До приёма осталось 24 часа:\n" +
                                        "📅 " + ap.getDate() + "\n" +
                                        "🕒 " + ap.getTime())
                                .build()
                );
                ap.setNotified(true);
                appointmentRepo.save(ap);
            } catch (Exception e) {
                log.error("Reminder send failed, id={}", ap.getId(), e);
            }
        }
    }
}