package ru.goncharov.study.platforma.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final TelegramClient telegramClient;

    @SneakyThrows
    public void sendMainMenu(Long chatId) {
        SendMessage msg = SendMessage.builder()
                .chatId(chatId)
                .text("Здравствуйте! Вас приветствует Платформа Комфорта — эксперт в области дизайн-проектов, ремонта и оснащения жилья.")
                .build();

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(
                List.of(
                        new InlineKeyboardRow(InlineKeyboardButton.builder().text("Главная").callbackData("menu").build()),
                        new InlineKeyboardRow(InlineKeyboardButton.builder().text("Я первый раз").callbackData("test").build()),
                        new InlineKeyboardRow(InlineKeyboardButton.builder().text("Запись на приём").callbackData("record").build()),
                        new InlineKeyboardRow(InlineKeyboardButton.builder().text("Каталог").callbackData("catalog").build())
                )
        );

        msg.setReplyMarkup(markup);
        telegramClient.execute(msg);
    }

    @SneakyThrows
    public void sendUnknown(Long chatId) {
        telegramClient.execute(
                SendMessage.builder()
                        .chatId(chatId)
                        .text("Я вас не понимаю. Введите /start.")
                        .build()
        );
    }

    @SneakyThrows
    public void sendCatalog(Long chatId) {
        telegramClient.execute(
                SendMessage.builder()
                        .chatId(chatId)
                        .text("Каталог скоро будет готов!")
                        .build()
        );
    }
}