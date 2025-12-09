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
import ru.goncharov.study.platforma.Entity.UserSurvey;
import ru.goncharov.study.platforma.repository.AppointmentRepository;
import ru.goncharov.study.platforma.repository.UserSurveyRepository;
import ru.goncharov.study.platforma.util.CalendarUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository repo;
    private final TelegramClient telegramClient;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // Начало записи на приём
    public void start(Long chatId) throws Exception {
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
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                LocalTime.of(12, 0),
                LocalTime.of(13, 0),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                LocalTime.of(16, 0),
                LocalTime.of(17, 0)
        );

        List<LocalTime> booked = repo.findAll().stream()
                .filter(a -> a.getDate().equals(date))
                .map(AppointmentEntity::getTime)
                .toList();

        List<InlineKeyboardRow> rows = allSlots.stream()
                .filter(t -> !booked.contains(t))
                .map(t -> new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text(t.toString())
                                .callbackData("TIME_" + date + "_" + t)
                                .build()
                ))
                .toList();

        // Если свободных слотов нет
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

        // Создаем запись через сеттеры
        AppointmentEntity ap = new AppointmentEntity();
        ap.setChatId(chatId);
        ap.setDate(date);
        ap.setTime(time);
        repo.save(ap);

        // Создаём сообщение о записи с кнопками меню
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(
                List.of(
                        new InlineKeyboardRow(
                                InlineKeyboardButton.builder().text("Главная").callbackData("menu").build()
                        ),
                        new InlineKeyboardRow(
                                InlineKeyboardButton.builder().text("Запись на приём").callbackData("record").build()
                        )
                )
        );

        SendMessage msg = SendMessage.builder()
                .chatId(chatId)
                .text("Запись создана!\n📅 " + date.format(DATE_FORMAT) + "\n⏰ " + time)
                .replyMarkup(markup)
                .build();

        telegramClient.execute(msg);
    }

}