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
import ru.goncharov.study.platforma.Entity.CatalogItem;
import ru.goncharov.study.platforma.repository.CatalogItemRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CatalogService {

    private final CatalogItemRepository repository;
    private final TelegramClient telegramClient;

    @SneakyThrows
    public void showCategories(Long chatId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(
                List.of(
                        row("🪵 Кварцевый ламинат", "CAT_QUARTZ"),
                        row("🧱 Плитка", "CAT_TILE"),
                        row("🖼 Обои", "CAT_WALLPAPER"),
                        row("⬅️ В меню", "menu")
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

    @SneakyThrows
    public void showCategory(Long chatId, String category) {
        List<CatalogItem> items = repository.findByCategory(category);

        if (items.isEmpty()) {
            sendText(chatId, "В этой категории пока нет товаров");
            return;
        }

        for (CatalogItem item : items) {
            InlineKeyboardMarkup markup = new InlineKeyboardMarkup(
                    List.of(
                            row("🖼 Посмотреть фото", item.getPhotoUrl()),
                            row("⬅️ Назад", "catalog")
                    )
            );

            telegramClient.execute(
                    SendMessage.builder()
                            .chatId(chatId)
                            .text(
                                    "📦 <b>" + item.getName() + "</b>\n\n" +
                                            item.getDescription()
                            )
                            .parseMode("HTML")
                            .replyMarkup(markup)
                            .build()
            );
        }
    }

    private InlineKeyboardRow row(String text, String data) {

        InlineKeyboardButton btn = InlineKeyboardButton.builder()
                .text(text)
                .build();

        if (data == null || data.isBlank()) {
            btn.setCallbackData("noop");
        } else if (data.startsWith("http")) {
            btn.setUrl(data);
        } else {
            btn.setCallbackData(data);
        }

        return new InlineKeyboardRow(btn);
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