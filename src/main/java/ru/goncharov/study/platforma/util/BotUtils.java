package ru.goncharov.study.platforma.util;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;

/**
 * Вспомогательные методы для кнопок и сообщений
 */
public class BotUtils {

    public static InlineKeyboardButton button(String text, String callback) {
        return InlineKeyboardButton
                .builder()
                .text(text)
                .callbackData(callback)
                .build();
    }

    public static InlineKeyboardMarkup markupRows(List<InlineKeyboardRow> rows) {
        return new InlineKeyboardMarkup(rows);
    }

    public static SendMessage sendMessage(Long chatId, String text, InlineKeyboardMarkup markup) {
        SendMessage msg = SendMessage
                .builder()
                .chatId(chatId)
                .text(text)
                .build();
        msg.setReplyMarkup(markup);
        return msg;
    }
}