package ru.goncharov.study.platforma.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.goncharov.study.platforma.CallbackData;
import ru.goncharov.study.platforma.util.CalendarUtils;

import java.time.YearMonth;


/**
 * Сервис для перелистывания месяцев в inline-календаре.
 * Получает callbackData вида PREV_YYYY-MM или NEXT_YYYY-MM
 * и отправляет обновлённый календарь.
 */
@Service
@RequiredArgsConstructor
public class CalendarService {

    private final TelegramClient telegramClient;

    /**
     * Обрабатывает смену месяца по кнопкам "<<"/">>".
     */


    @SneakyThrows
    public void changeMonth(Long chatId, String data) {
        String ymStr = data.substring(CallbackData.PREV_PREFIX.length());

        YearMonth ym = YearMonth.parse(ymStr);

        // Отправляем пользователю календарь на новый месяц
        telegramClient.execute(
                SendMessage.builder()
                        .chatId(chatId)
                        .text("Выберите дату:")
                        .replyMarkup(CalendarUtils.buildMonthCalendar(ym))
                        .build()
        );
    }
}