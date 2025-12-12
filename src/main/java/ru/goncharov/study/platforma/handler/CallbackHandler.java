package ru.goncharov.study.platforma.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.goncharov.study.platforma.service.AppointmentService;
import ru.goncharov.study.platforma.service.CalendarService;
import ru.goncharov.study.platforma.service.MenuService;
import ru.goncharov.study.platforma.service.SurveyService;

@Component
@RequiredArgsConstructor
public class CallbackHandler {

    private final MenuService menuService;
    private final SurveyService surveyService;
    private final AppointmentService appointmentService;
    private final CalendarService calendarService;

    public void handle(CallbackQuery cb) throws Exception {
        Long chatId = cb.getFrom().getId();
        String data = cb.getData();

        switch (data) {
            case "menu" -> menuService.sendMainMenu(chatId);
            case "test" -> surveyService.start(chatId);
            case "record" -> appointmentService.start(chatId);
            case "catalog" -> menuService.sendCatalog(chatId);
            default -> {
                if (data.startsWith("DAY_")) appointmentService.handleDay(chatId, data);
                else if (data.startsWith("TIME_")) appointmentService.handleTime(chatId, data);
                else if (data.startsWith("PREV_") || data.startsWith("NEXT_"))
                    calendarService.changeMonth(chatId, data);
                if (data.startsWith("CAT_")) { // Категория
                    menuService.sendCatalogSubCategory(chatId, data.substring(4));
                } else if (data.startsWith("ITEM_")) { // Товар
                    String[] parts = data.substring(5).split("_", 2);
                    String category = parts[0];
                    String item = parts[1];
                    menuService.sendCatalogSubItems(chatId, category, item);
                } else if (data.startsWith("SUB_")) { // Подкатегория
                    String[] parts = data.substring(4).split("_", 3);
                    String category = parts[0];
                    String item = parts[1];
                    String subItem = parts[2];
                    menuService.sendCatalogFinal(chatId, category, item, subItem);
                } else if (data.startsWith("PREV_") || data.startsWith("NEXT_")) {
                    calendarService.changeMonth(chatId, data);
                } else if (data.startsWith("DAY_")) {
                    appointmentService.handleDay(chatId, data);
                } else if (data.startsWith("TIME_")) {
                    appointmentService.handleTime(chatId, data);
                }
            }
        }
    }
}