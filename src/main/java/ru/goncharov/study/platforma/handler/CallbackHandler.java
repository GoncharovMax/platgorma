package ru.goncharov.study.platforma.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.goncharov.study.platforma.CallbackData;
import ru.goncharov.study.platforma.Entity.CatalogCategory;
import ru.goncharov.study.platforma.service.*;

@Component
@RequiredArgsConstructor
public class CallbackHandler {

    private final MenuService menuService;
    private final SurveyService surveyService;
    private final AppointmentService appointmentService;
    private final CalendarService calendarService;
    private final CatalogService catalogService;

    public void handle(CallbackQuery cb) throws Exception {

        Long chatId = cb.getMessage().getChatId();
        String data = cb.getData();

        if (data == null || data.isBlank()) {
            return;
        }

        switch (data) {
            case CallbackData.MENU   -> menuService.sendMainMenu(chatId);
            case CallbackData.TEST   -> surveyService.start(chatId);
            case CallbackData.RECORD -> appointmentService.start(chatId);
            case CallbackData.CATALOG-> catalogService.showCategories(chatId);

            case CallbackData.CAT_QUARTZ    -> catalogService.showCategory(chatId, CatalogCategory.QUARTZ_LAMINATE);
            case CallbackData.CAT_TILE      -> catalogService.showCategory(chatId, CatalogCategory.TILE);
            case CallbackData.CAT_WALLPAPER -> catalogService.showCategory(chatId, CatalogCategory.WALLPAPER);

            default -> {
                if (CallbackData.isDay(data)) {
                    appointmentService.handleDay(chatId, data);
                } else if (CallbackData.isTime(data)) {
                    appointmentService.handleTime(chatId, data);
                } else if (CallbackData.isPrevOrNext(data)) {
                    calendarService.changeMonth(chatId, data);
                }
            }
        }
    }
}