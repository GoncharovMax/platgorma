package ru.goncharov.study.platforma.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.goncharov.study.platforma.util.CalendarUtils;

import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final TelegramClient telegramClient;

    @SneakyThrows
    public void changeMonth(Long chatId, String data) {
        YearMonth ym = YearMonth.parse(data.substring(5));

        telegramClient.execute(
                SendMessage.builder()
                        .chatId(chatId)
                        .text("Выберите дату:")
                        .replyMarkup(CalendarUtils.buildMonthCalendar(ym))
                        .build()
        );
    }
}