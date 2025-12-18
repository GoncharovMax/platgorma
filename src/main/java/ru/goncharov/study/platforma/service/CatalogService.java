package ru.goncharov.study.platforma.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.goncharov.study.platforma.Entity.CatalogItem;
import ru.goncharov.study.platforma.repository.CatalogItemRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogService {

    private final CatalogItemRepository repository;
    private final TelegramClient telegramClient;

    // ===== 1. Показ категорий =====
    @SneakyThrows
    public void showCategories(Long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(
                List.of(
                        new InlineKeyboardRow(
                                InlineKeyboardButton.builder()
                                        .text("🪵 Кварцевый ламинат")
                                        .callbackData("CAT_QUARTZ")
                                        .build()
                        ),
                        new InlineKeyboardRow(
                                InlineKeyboardButton.builder()
                                        .text("🧱 Плитка")
                                        .callbackData("CAT_TILE")
                                        .build()
                        ),
                        new InlineKeyboardRow(
                                InlineKeyboardButton.builder()
                                        .text("🖼 Обои")
                                        .callbackData("CAT_WALLPAPER")
                                        .build()
                        ),
                        new InlineKeyboardRow(
                                InlineKeyboardButton.builder()
                                        .text("⬅️ В меню")
                                        .callbackData("menu")
                                        .build()
                        )
                )
        );

        telegramClient.execute(
                SendMessage.builder()
                        .chatId(chatId)
                        .text("Выберите категорию:")
                        .replyMarkup(markup)
                        .build()
        );
    }

    // ===== 2. Показ товаров категории =====
    public void showCategory(Long chatId, String category) {
        List<CatalogItem> items = repository.findByCategory(category);

        if (items.isEmpty()) {
            sendText(chatId, "В этой категории пока нет товаров");
            return;
        }

        for (CatalogItem item : items) {
            SendPhoto sendPhoto = SendPhoto.builder()
                    .chatId(chatId.toString())
                    .photo(new InputFile(item.getPhotoId()))
                    .caption(
                            "<b>" + item.getName() + "</b>\n\n" +
                                    item.getDescription()
                    )
                    .parseMode("HTML")
                    .build();

            try {
                telegramClient.execute(sendPhoto);
            } catch (TelegramApiException e) {
                log.error("Ошибка отправки фото itemId={}", item.getId(), e);
            }
        }

        sendBack(chatId);
    }

    // ===== 3. Кнопка назад =====
    @SneakyThrows
    private void sendBack(Long chatId) {
        telegramClient.execute(
                SendMessage.builder()
                        .chatId(chatId)
                        .text("Вернуться:")
                        .replyMarkup(
                                new InlineKeyboardMarkup(
                                        List.of(
                                                new InlineKeyboardRow(
                                                        InlineKeyboardButton.builder()
                                                                .text("⬅️ Назад к категориям")
                                                                .callbackData("catalog")
                                                                .build()
                                                )
                                        )
                                )
                        )
                        .build()
        );
    }

    @SneakyThrows
    private void sendText(Long chatId, String text) {
        telegramClient.execute(
                SendMessage.builder()
                        .chatId(chatId)
                        .text(text)
                        .build()
        );
    }
}