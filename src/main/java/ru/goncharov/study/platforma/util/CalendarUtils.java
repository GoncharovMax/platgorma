package ru.goncharov.study.platforma.util;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.goncharov.study.platforma.CallbackData;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Утилита для построения inline-календаря на месяц.
 * Возвращает InlineKeyboardMarkup, где:
 * - прошлые даты заблокированы,
 * - дни кликабельны callback'ом вида DAY_YYYY-MM-DD,
 * - снизу навигация PREV_YYYY-MM / NEXT_YYYY-MM.
 */
public class CalendarUtils {

    public static InlineKeyboardMarkup buildMonthCalendar(YearMonth ym) {
        List<InlineKeyboardRow> rows = new java.util.ArrayList<>();

        // 1) Заголовок месяца
        rows.add(new InlineKeyboardRow(
                btn(ym.getMonth().getDisplayName(java.time.format.TextStyle.FULL_STANDALONE, java.util.Locale.forLanguageTag("ru"))
                        + " " + ym.getYear(), CallbackData.IGNORE)
        ));

        // 2) Дни недели
        rows.add(new InlineKeyboardRow(
                btn("Пн", CallbackData.IGNORE),
                btn("Вт", CallbackData.IGNORE),
                btn("Ср", CallbackData.IGNORE),
                btn("Чт", CallbackData.IGNORE),
                btn("Пт", CallbackData.IGNORE),
                btn("Сб", CallbackData.IGNORE),
                btn("Вс", CallbackData.IGNORE)
        ));

        LocalDate firstDay = ym.atDay(1);
        int daysInMonth = ym.lengthOfMonth();

        // Сдвиг (Пн=0 ... Вс=6)
        int shift = firstDay.getDayOfWeek().getValue() - 1;

        InlineKeyboardRow week = new InlineKeyboardRow();

        // 3) Пустые клетки до 1 числа
        for (int i = 0; i < shift; i++) {
            week.add(btn(" ", CallbackData.IGNORE));
        }

        // 4) Числа месяца
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = ym.atDay(day);

            week.add(btn(String.valueOf(day), "DAY_" + date)); // или твой формат CallbackData

            // Закрываем неделю ровно на 7 кнопках
            if (week.size() == 7) {
                rows.add(week);
                week = new InlineKeyboardRow();
            }
        }

        // 5) Добиваем последнюю неделю пустыми до 7
        if (!week.isEmpty()) {
            while (week.size() < 7) {
                week.add(btn(" ", CallbackData.IGNORE));
            }
            rows.add(week);
        }

        // 6) Навигация
        rows.add(new InlineKeyboardRow(
                btn("<<", "PREV_" + ym.minusMonths(1)),
                btn(">>", "NEXT_" + ym.plusMonths(1))
        ));

        return new InlineKeyboardMarkup(rows);
    }

    private static InlineKeyboardButton btn(String text, String data) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(data)
                .build();
    }
}