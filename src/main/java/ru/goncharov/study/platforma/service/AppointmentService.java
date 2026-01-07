package ru.goncharov.study.platforma.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.goncharov.study.platforma.Entity.AppointmentEntity;
import ru.goncharov.study.platforma.Entity.SurveyState;
import ru.goncharov.study.platforma.Entity.UserSurvey;
import ru.goncharov.study.platforma.dto.AppointmentDto;
import ru.goncharov.study.platforma.mapper.AppointmentMapper;
import ru.goncharov.study.platforma.repository.AppointmentRepository;
import ru.goncharov.study.platforma.repository.UserSurveyRepository;
import ru.goncharov.study.platforma.util.CalendarUtils;
import ru.goncharov.study.platforma.dto.UserSurveyDto;
import ru.goncharov.study.platforma.mapper.UserSurveyMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepo;
    private final UserSurveyRepository surveyRepo;
    private final TelegramClient telegramClient;
    private final YandexCalendarService calendarService;
    private final SurveyService surveyService;
    private final TimeSlotService timeSlotService;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");


    // Начало записи на приём
    public void start(Long chatId) throws Exception {
        var surveyOpt = surveyRepo.findByChatId(chatId);
        if (surveyOpt.isEmpty()) {
            surveyService.start(chatId);
            return;
        }
        UserSurvey survey = surveyOpt.get();
        UserSurveyDto surveyDto = UserSurveyMapper.toDto(survey);

        if (surveyDto.state() != SurveyState.FINISHED) {
            surveyService.start(chatId);
            return;
        }

        sendMonth(chatId, YearMonth.now());
    }


    @SneakyThrows
    public void sendMonth(Long chatId, YearMonth ym) {
        telegramClient.execute(
                SendMessage.builder()
                        .chatId(chatId)
                        .text("Выберите дату:")
                        .replyMarkup(CalendarUtils.buildMonthCalendar(ym))
                        .build()
        );
    }

    // Обработка выбора дня
    @SneakyThrows
    public void handleDay(Long chatId, String data) {
        LocalDate date = LocalDate.parse(data.substring(4));

        telegramClient.execute(
                SendMessage.builder()
                        .chatId(chatId)
                        .text("Вы выбрали " + date.format(DATE_FORMAT)
                                + ". Теперь выберите время:")
                        .replyMarkup(buildTimeKeyboard(date))
                        .build()
        );
    }


    // Обработка выбора времени
    @Transactional
    @SneakyThrows
    public void handleTime(Long chatId, String data) {

        var parts = data.split("_", 3);
        LocalDate date = LocalDate.parse(parts[1]);
        LocalTime time = LocalTime.parse(parts[2]);

        LocalDateTime appointmentAt = LocalDateTime.of(date, time);
        LocalDateTime remindAt = appointmentAt.minusHours(24);

        if (appointmentRepo.existsByDateAndTime(date, time)) {
            sendBusy(chatId, date);
            return;
        }

        UserSurvey survey = surveyRepo.findByChatId(chatId).orElseThrow();
        UserSurveyDto surveyDto = UserSurveyMapper.toDto(survey);

        AppointmentDto dto = new AppointmentDto(
                null,
                chatId,
                date,
                time,
                surveyDto.name(),
                null,
                false
        );

        String icsUid = calendarService.createEvent(
                surveyDto.name(),
                buildDescription(survey, chatId, date, time),
                chatId,
                date,
                time
        );

        dto = new AppointmentDto(
                dto.id(),
                dto.chatId(),
                dto.date(),
                dto.time(),
                dto.fullName(),
                icsUid,
                false
        );

        AppointmentEntity ap = AppointmentMapper.toEntity(dto);
        ap.setRemindAt(remindAt);
        ap.setNotified(false);

        ap = appointmentRepo.save(ap);

        sendConfirmation(chatId, survey, date, time, ap.getId());
    }

    private void sendBusy(Long chatId, LocalDate date) throws Exception {
        telegramClient.execute(
                SendMessage.builder()
                        .chatId(chatId)
                        .text("Время занято, выберите другое")
                        .replyMarkup(buildTimeKeyboard(date))
                        .build()
        );
    }

    private String buildDescription(
            UserSurvey survey,
            Long chatId,
            LocalDate date,
            LocalTime time
    ) {
        return """
            Имя: %s
            Телефон: %s
            Проект: %s
            Дата: %s
            Время: %s
            ChatID: %s
            """.formatted(
                survey.getName(),
                survey.getPhone(),
                survey.getQuestionAbout(),
                date.format(DATE_FORMAT),
                time,
                chatId
        );
    }

    private InlineKeyboardMarkup buildTimeKeyboard(LocalDate date) {

        List<LocalTime> booked = appointmentRepo.findByDate(date)
                .stream()
                .map(AppointmentEntity::getTime)
                .toList();

        var free = timeSlotService.availableSlots(date, booked);

        if (free.isEmpty()) {
            return new InlineKeyboardMarkup(
                    List.of(new InlineKeyboardRow(
                            InlineKeyboardButton.builder()
                                    .text("Нет свободного времени")
                                    .callbackData("record")
                                    .build()
                    ))
            );
        }

        List<InlineKeyboardRow> rows = free.stream()
                .map(t -> new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(t.toString())
                                .callbackData("TIME_" + date + "_" + t)
                                .build()
                ))
                .toList();

        return new InlineKeyboardMarkup(rows);
    }

    @SneakyThrows
    private void sendConfirmation(
            Long chatId,
            UserSurvey survey,
            LocalDate date,
            LocalTime time,
            Long appointmentId
    ) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(
                List.of(
                        new InlineKeyboardRow(
                                InlineKeyboardButton.builder()
                                        .text("Главная")
                                        .callbackData("menu")
                                        .build()
                        ),
                        new InlineKeyboardRow(
                                InlineKeyboardButton.builder()
                                        .text("Запись на приём")
                                        .callbackData("record")
                                        .build()
                        ),
                        new InlineKeyboardRow(
                                InlineKeyboardButton.builder()
                                        .text("❌ Отменить запись")
                                        .callbackData("cancel_" + appointmentId)
                                        .build()
                        )
                )
        );

        telegramClient.execute(
                SendMessage.builder()
                        .chatId(chatId)
                        .text(
                                "Запись создана!\n\n" +
                                        "📅 " + date.format(DATE_FORMAT) + "\n" +
                                        "⏰ " + time + "\n\n" +
                                        "Имя: " + survey.getName() + "\n" +
                                        "Телефон: " + survey.getPhone() + "\n" +
                                        "Проект: " + survey.getQuestionAbout()
                        )
                        .replyMarkup(markup)
                        .build()
        );
    }

    @Transactional
    @SneakyThrows
    public void cancel(Long chatId, Long appointmentId) {

        var apOpt = appointmentRepo.findById(appointmentId);
        if (apOpt.isEmpty()) {
            telegramClient.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text("Запись не найдена 🤷‍♂️")
                    .build());
            return;
        }

        AppointmentEntity ap = apOpt.get();

        // защита: отменять может только владелец
        if (!ap.getChatId().equals(chatId)) {
            telegramClient.execute(SendMessage.builder()
                    .chatId(chatId)
                    .text("Нельзя отменить чужую запись.")
                    .build());
            return;
        }

        // удалить событие в Яндекс календаре (нужно добавить метод deleteEvent(uid))
        if (ap.getIcsUid() != null && !ap.getIcsUid().isBlank()) {
           calendarService.deleteEvent(ap.getIcsUid());
        }

        appointmentRepo.delete(ap);

        telegramClient.execute(
                SendMessage.builder()
                        .chatId(chatId)
                        .text("✅ Запись отменена.")
                        .replyMarkup(new InlineKeyboardMarkup(
                                List.of(new InlineKeyboardRow(
                                        InlineKeyboardButton.builder()
                                                .text("Записаться заново")
                                                .callbackData("record")
                                                .build()
                                ))
                        ))
                        .build()
        );
    }

}