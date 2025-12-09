package ru.goncharov.study.platforma.util;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class CalendarUtils {

    public static InlineKeyboardMarkup buildMonthCalendar(YearMonth ym) {

        List<InlineKeyboardRow> rows = new ArrayList<>();

        // Заголовок
        String monthRus = ym.getMonth().getDisplayName(
                java.time.format.TextStyle.FULL, new java.util.Locale("ru")
        );
        monthRus = monthRus.substring(0, 1).toUpperCase() + monthRus.substring(1);

        rows.add(new InlineKeyboardRow(
                btn(monthRus + " " + ym.getYear(), "IGNORE")
        ));

        rows.add(new InlineKeyboardRow(
                btn("Пн","IGNORE"), btn("Вт","IGNORE"), btn("Ср","IGNORE"),
                btn("Чт","IGNORE"), btn("Пт","IGNORE"), btn("Сб","IGNORE"), btn("Вс","IGNORE")
        ));

        LocalDate today = LocalDate.now();
        LocalDate first = ym.atDay(1);
        int shift = first.getDayOfWeek().getValue();
        if (shift == 7) shift = 0;

        InlineKeyboardRow row = new InlineKeyboardRow();

        for (int i = 0; i < shift; i++) row.add(btn(" ", "IGNORE"));

        for (int d = 1; d <= ym.lengthOfMonth(); d++) {

            if (row.size() == 7) {
                rows.add(row);
                row = new InlineKeyboardRow();
            }

            LocalDate date = ym.atDay(d);
            String text = String.valueOf(d);
            String callback = "DAY_" + date;

            if (date.isBefore(today)) {
                text = "✖️" + d;
                callback = "IGNORE";
            }

            row.add(btn(text, callback));
        }

        if (!row.isEmpty()) rows.add(row);

        rows.add(new InlineKeyboardRow(
                btn("<<", "PREV_" + ym.minusMonths(1)),
                btn(">>", "NEXT_" + ym.plusMonths(1))
        ));

        return new InlineKeyboardMarkup(rows);
    }

    private static InlineKeyboardButton btn(String t, String d) {
        return InlineKeyboardButton.builder().text(t).callbackData(d).build();
    }
}