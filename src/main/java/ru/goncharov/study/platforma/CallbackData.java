package ru.goncharov.study.platforma;

/**
 * Все callbackData, которые отправляет бот.
 * Удобно, чтобы не держать строки по коду.
 */

public final class CallbackData {
    private CallbackData() {}

    // Служебное (кнопки, которые ничего не делают)
    public static final String IGNORE = "IGNORE";

    // Простые действия
    public static final String MENU = "menu";
    public static final String TEST = "test";
    public static final String RECORD = "record";
    public static final String CATALOG = "catalog";

    // Категории каталога
    public static final String CAT_QUARTZ = "CAT_QUARTZ";
    public static final String CAT_TILE = "CAT_TILE";
    public static final String CAT_WALLPAPER = "CAT_WALLPAPER";

    // Префиксы “динамических” действий
    public static final String DAY_PREFIX  = "DAY_";
    public static final String TIME_PREFIX = "TIME_";
    public static final String PREV_PREFIX = "PREV_";
    public static final String NEXT_PREFIX = "NEXT_";

    public static boolean isDay(String data)  { return data != null && data.startsWith(DAY_PREFIX); }
    public static boolean isTime(String data) { return data != null && data.startsWith(TIME_PREFIX); }
    public static boolean isPrevOrNext(String data) {
        return data != null && (data.startsWith(PREV_PREFIX) || data.startsWith(NEXT_PREFIX));
    }
}