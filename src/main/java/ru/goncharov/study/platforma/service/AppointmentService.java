package ru.goncharov.study.platforma.service;

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
import ru.goncharov.study.platforma.repository.AppointmentRepository;
import ru.goncharov.study.platforma.repository.UserSurveyRepository;
import ru.goncharov.study.platforma.util.CalendarUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository repo;
    private final UserSurveyRepository surveyRepo;
    private final TelegramClient telegramClient;
    private final YandexCalendarService yandexCalendarService;
    private final SurveyService surveyService; // внедряем SurveyService

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // Начало записи на приём
    public void start(Long chatId) throws Exception {
        Optional<UserSurvey> surveyOpt = surveyRepo.findByChatId(chatId);
        if (surveyOpt.isEmpty() || surveyOpt.get().getState() != SurveyState.FINISHED) {
            surveyService.start(chatId); // запускаем опрос
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
                        .text("Вы выбрали " + date.format(DATE_FORMAT) + ". Теперь выберите время:")
                        .replyMarkup(buildTimeKeyboard(date))
                        .build()
        );
    }

    // Формирование клавиатуры со свободными временем
    private InlineKeyboardMarkup buildTimeKeyboard(LocalDate date) {
        List<LocalTime> allSlots = List.of(
                LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0),
                LocalTime.of(12, 0), LocalTime.of(13, 0), LocalTime.of(14, 0),
                LocalTime.of(15, 0), LocalTime.of(16, 0), LocalTime.of(17, 0)
        );

        List<LocalTime> booked = repo.findAll().stream()
                .filter(a -> a.getDate().equals(date))
                .map(AppointmentEntity::getTime)
                .toList();

        List<InlineKeyboardRow> rows = new java.util.ArrayList<>(
                allSlots.stream()
                        .filter(t -> !booked.contains(t))
                        .map(t -> new InlineKeyboardRow(
                                InlineKeyboardButton.builder()
                                        .text(t.toString())
                                        .callbackData("TIME_" + date + "_" + t)
                                        .build()
                        ))
                        .toList()
        );

        if (rows.isEmpty()) {
            InlineKeyboardButton btn = InlineKeyboardButton.builder()
                    .text("Свободного времени нет, выберите другую дату")
                    .callbackData("record")
                    .build();
            rows.add(new InlineKeyboardRow(btn));
        }

        return new InlineKeyboardMarkup(rows);
    }

    // Обработка выбора времени
    @SneakyThrows
    public void handleTime(Long chatId, String data) {
        String[] parts = data.split("_", 3);
        if (parts.length < 3) return;

        LocalDate date = LocalDate.parse(parts[1]);
        LocalTime time = LocalTime.parse(parts[2]);

        // Проверка занятости
        boolean busy = repo.findAll().stream()
                .anyMatch(a -> a.getDate().equals(date) && a.getTime().equals(time));

        if (busy) {
            telegramClient.execute(
                    SendMessage.builder()
                            .chatId(chatId)
                            .text("Извините, выбранное время занято. Пожалуйста, выберите другое время.")
                            .replyMarkup(buildTimeKeyboard(date))
                            .build()
            );
            return;
        }

        // Проверка опроса
        Optional<UserSurvey> surveyOpt = surveyRepo.findByChatId(chatId);
        if (surveyOpt.isEmpty() || surveyOpt.get().getState() != SurveyState.FINISHED) {
            surveyService.start(chatId);
            return;
        }

        // Создание записи
        UserSurvey survey = surveyOpt.get();
        AppointmentEntity ap = new AppointmentEntity();
        ap.setChatId(chatId);
        ap.setDate(date);
        ap.setTime(time);
        ap.setFullName(survey.getName() != null ? survey.getName() : "Не указано");
        repo.save(ap);

        // Создание события в Яндекс.Календаре
        String title = "Запись: " + survey.getName();
        String description = "Имя: " + survey.getName() + "\n" +
                "Телефон: " + survey.getPhone() + "\n" +
                "Вопрос/проект: " + survey.getQuestionAbout() + "\n" +
                "Дата: " + date.format(DATE_FORMAT) + "\n" +
                "Время: " + time + "\n" +
                "ChatID: " + chatId;

        String uid = yandexCalendarService.createEvent(title, description, chatId, date, time);
        if (uid != null) {
            ap.setIcsUid(uid);
            repo.save(ap);
        }

        // Подтверждение пользователю
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(
                List.of(
                        new InlineKeyboardRow(InlineKeyboardButton.builder().text("Главная").callbackData("menu").build()),
                        new InlineKeyboardRow(InlineKeyboardButton.builder().text("Запись на приём").callbackData("record").build())
                )
        );

        telegramClient.execute(
                SendMessage.builder()
                        .chatId(chatId)
                        .text("Запись создана!\n📅 " + date.format(DATE_FORMAT) + "\n⏰ " + time +
                                "\n\nИмя: " + survey.getName() +
                                "\nТелефон: " + survey.getPhone() +
                                "\nПроект: " + survey.getQuestionAbout())
                        .replyMarkup(markup)
                        .build()
        );
    }
}