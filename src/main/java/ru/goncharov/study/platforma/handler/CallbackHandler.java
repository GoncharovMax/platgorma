package ru.goncharov.study.platforma.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
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
        Long chatId = cb.getFrom().getId();
        String data = cb.getData();

        switch (data) {
            case "menu" -> menuService.sendMainMenu(chatId);
            case "test" -> surveyService.start(chatId);
            case "record" -> appointmentService.start(chatId);
            case "catalog" -> catalogService.showCategories(chatId);

            case "CAT_QUARTZ" -> catalogService.showCategory(chatId, "QUARTZ_LAMINATE");
            case "CAT_TILE" -> catalogService.showCategory(chatId, "TILE");
            case "CAT_WALLPAPER" -> catalogService.showCategory(chatId, "WALLPAPER");

            default -> {
                if (data.startsWith("DAY_")) {
                    appointmentService.handleDay(chatId, data);
                } else if (data.startsWith("TIME_")) {
                    appointmentService.handleTime(chatId, data);
                } else if (data.startsWith("PREV_") || data.startsWith("NEXT_")) {
                    calendarService.changeMonth(chatId, data);
                }
            }
        }
    }
}