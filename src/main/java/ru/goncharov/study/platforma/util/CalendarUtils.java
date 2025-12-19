package ru.goncharov.study.platforma.util;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.goncharov.study.platforma.CallbackData;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Утилита для построения inline-календаря на месяц.
 * Возвращает InlineKeyboardMarkup, где:
 * - прошлые даты заблокированы,
 * - дни кликабельны callback'ом вида DAY_YYYY-MM-DD,
 * - снизу навигация PREV_YYYY-MM / NEXT_YYYY-MM.
 */
public class CalendarUtils {

    public static InlineKeyboardMarkup buildMonthCalendar(YearMonth ym) {
        List<InlineKeyboardRow> rows = new ArrayList<>();

        // Заголовок месяца (на русском)
        String monthRus = ym.getMonth().getDisplayName(java.time.format.TextStyle.FULL, new Locale("ru"));
        monthRus = monthRus.substring(0, 1).toUpperCase() + monthRus.substring(1);

        // 1) Заголовок
        rows.add(new InlineKeyboardRow(
                btn(monthRus + " " + ym.getYear(), CallbackData.IGNORE)
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

        LocalDate today = LocalDate.now();
        LocalDate firstDay = ym.atDay(1);

        // Сдвиг под первый день месяца (Пн=1 ... Вс=7)
        int shift = firstDay.getDayOfWeek().getValue();
        if (shift == 7) shift = 0; // воскресенье делаем 0

        InlineKeyboardRow row = new InlineKeyboardRow();

        // Пустые ячейки до первого дня месяца
        for (int i = 0; i < shift; i++) {
            row.add(btn(" ", CallbackData.IGNORE));
        }

        // Заполняем дни месяца
        for (int d = 1; d <= ym.lengthOfMonth(); d++) {

            // Если строка заполнена — добавляем и начинаем новую
            if (row.size() == 7) {
                rows.add(row);
                row = new InlineKeyboardRow();
            }

            LocalDate date = ym.atDay(d);

            String text = String.valueOf(d);
            String callback = CallbackData.DAY_PREFIX + date; // DAY_YYYY-MM-DD

            // Прошлые даты блокируем
            if (date.isBefore(today)) {
                text = "✖️" + d;
                callback = CallbackData.IGNORE;
            }

            row.add(btn(text, callback));
        }

        // Добавляем последнюю строку, если не пустая
        if (!row.isEmpty()) {
            rows.add(row);
        }

        // 3) Навигация по месяцам
        rows.add(new InlineKeyboardRow(
                btn("<<", CallbackData.PREV_PREFIX + ym.minusMonths(1)), // PREV_YYYY-MM
                btn(">>", CallbackData.NEXT_PREFIX + ym.plusMonths(1))  // NEXT_YYYY-MM
        ));

        return new InlineKeyboardMarkup(rows);
    }

    // Удобный конструктор кнопки
    private static InlineKeyboardButton btn(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }
}