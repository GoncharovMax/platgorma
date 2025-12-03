package ru.goncharov.study.platforma;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.goncharov.study.platforma.Config.BotProperties;
import ru.goncharov.study.platforma.Entity.*;
import ru.goncharov.study.platforma.repository.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final UserSurveyRepository surveyRepository;
    private final AppointmentRepository appointmentRepository;

    public UpdateConsumer(BotProperties botProperties,
                          UserSurveyRepository surveyRepository,
                          AppointmentRepository appointmentRepository) {

        this.surveyRepository = surveyRepository;
        this.appointmentRepository = appointmentRepository;
        this.telegramClient = new OkHttpTelegramClient(botProperties.getToken());
    }

    @Override
    @SneakyThrows
    public void consume(Update update) {

        Long chatId = null;

        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();

            // Проверяем, не идёт ли сейчас анкета
            if (processSurvey(chatId, messageText)) {
                return;
            }

            if (messageText.equals("/start")) {
                sendMainMenu(chatId);
            } else {
                sendMessage(chatId, "Для появления меню введите /start");
            }
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getFrom().getId();
            handleCallBackQuery(update.getCallbackQuery());
        }
    }

    // ============================================
    //            АНКЕТА "Я ПЕРВЫЙ РАЗ"
    // ============================================

    private boolean processSurvey(Long chatId, String text) {

        var surveyOpt = surveyRepository.findByChatId(chatId);

        if (surveyOpt.isEmpty()) return false;

        var survey = surveyOpt.get();

        switch (survey.getState()) {

            case ASK_NAME -> {
                survey.setName(text);
                survey.setState(SurveyState.ASK_PHONE);
                surveyRepository.save(survey);

                sendMessage(chatId, "Отлично! Теперь укажите ваш номер телефона:");
                return true;
            }

            case ASK_PHONE -> {
                survey.setPhone(text);
                survey.setState(SurveyState.ASK_QUESTION);
                surveyRepository.save(survey);

                sendMessage(chatId, "Последний вопрос — что вы хотите уточнить или какой проект планируете?");
                return true;
            }

            case ASK_QUESTION -> {
                survey.setQuestionAbout(text);
                survey.setState(SurveyState.FINISHED);
                surveyRepository.save(survey);

                sendFinishMenu(chatId);
                return true;
            }

            default -> {
                return false;
            }
        }
    }

    @SneakyThrows
    private void sendFinishMenu(Long chatId) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Спасибо! Мы сохранили ваши ответы.\nЧто вы хотите сделать дальше?")
                .build();

        var btn1 = InlineKeyboardButton.builder()
                .text("Главная")
                .callbackData("menu")
                .build();

        var btn2 = InlineKeyboardButton.builder()
                .text("Запись на приём")
                .callbackData("record")
                .build();

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(
                List.of(
                        new InlineKeyboardRow(btn1),
                        new InlineKeyboardRow(btn2)
                )
        );

        message.setReplyMarkup(markup);
        telegramClient.execute(message);
    }

    private void startSurvey(Long chatId) {
        UserSurvey survey = surveyRepository.findByChatId(chatId)
                .orElseGet(UserSurvey::new);

        survey.setChatId(chatId);
        survey.setState(SurveyState.ASK_NAME);
        surveyRepository.save(survey);

        sendMessage(chatId, "Давайте познакомимся!\n\nКак вас зовут?");
    }

    // ============================================
    //            CALL BACK HANDLER
    // ============================================

    private void handleCallBackQuery(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getFrom().getId();

        try {
            switch (data) {

                case "menu" -> sendMainMenu(chatId);
                case "test" -> startSurvey(chatId);
                case "record" -> sendRecord(chatId);
                case "catalog" -> sendCatalog(chatId);

                default -> {
                    // Month calendar controls
                    if (data.startsWith("PREV_")) {
                        YearMonth ym = YearMonth.parse(data.substring(5));
                        sendMonthCalendar(chatId, ym);
                    } else if (data.startsWith("NEXT_")) {
                        YearMonth ym = YearMonth.parse(data.substring(5));
                        sendMonthCalendar(chatId, ym);
                    }
                    // Day selected from month calendar
                    else if (data.startsWith("DAY_")) {
                        LocalDate date = LocalDate.parse(data.substring(4));
                        handleDateSelect(chatId, date);
                    }
                    // Old 7-day DATE_ prefix (backward compatibility)
                    else if (data.startsWith("DATE_")) {
                        LocalDate date = LocalDate.parse(data.substring(5));
                        handleDateSelect(chatId, date);
                    }
                    // Time selection
                    else if (data.startsWith("TIME_")) {
                        handleTimeSelect(chatId, data);
                    } else {
                        sendMessage(chatId, "Неизвестная команда");
                    }
                }
            }
        } catch (Exception ex) {
            // Без падения приложения — покажем ошибку пользователю
            sendMessage(chatId, "Произошла ошибка: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ============================================
    //               ЗАПИСЬ НА ПРИЁМ
    // ============================================

    @SneakyThrows
    private void sendRecord(Long chatId) {
        // Показываем календарь на текущий месяц
        sendMonthCalendar(chatId, YearMonth.now());
    }

    private void sendMonthCalendar(Long chatId, YearMonth ym) throws Exception {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Выберите удобную дату:")
                .replyMarkup(CalendarUtils.buildMonthCalendar(ym))
                .build();

        telegramClient.execute(message);
    }

    // Для обратной совместимости: если придёт DATE_ (7 дней)
    @SneakyThrows
    private void handleDateSelect(Long chatId, LocalDate date) {
        // Показать доступные времена для выбранной даты
        SendMessage msg = SendMessage.builder()
                .chatId(chatId)
                .text("Вы выбрали дату: " + date + "\nТеперь выберите время:")
                .replyMarkup(buildTimeKeyboard(date))
                .build();

        telegramClient.execute(msg);
    }

    private InlineKeyboardMarkup buildTimeKeyboard(LocalDate date) {
        List<LocalTime> free = getAvailableTimes(date);

        List<InlineKeyboardRow> rows = new ArrayList<>();

        if (free.isEmpty()) {
            // Если свободных нет — показать кнопку назад и сообщение
            InlineKeyboardButton noSlots = InlineKeyboardButton.builder()
                    .text("Свободного времени нет, выберите другую дату")
                    .callbackData("record")
                    .build();
            rows.add(new InlineKeyboardRow(noSlots));
            return new InlineKeyboardMarkup(rows);
        }

        for (LocalTime t : free) {

            InlineKeyboardButton btn = InlineKeyboardButton.builder()
                    .text(t.toString())
                    .callbackData("TIME_" + date + "_" + t)
                    .build();

            rows.add(new InlineKeyboardRow(btn));
        }

        return new InlineKeyboardMarkup(rows);
    }

    @SneakyThrows
    private void handleTimeSelect(Long chatId, String data) {

        // data expected "TIME_YYYY-MM-DD_HH:MM"
        String[] parts = data.split("_", 3); // ["TIME", "YYYY-MM-DD", "HH:MM"]
        if (parts.length < 3) {
            sendMessage(chatId, "Неправильные данные времени.");
            return;
        }

        LocalDate date = LocalDate.parse(parts[1]);
        LocalTime time = LocalTime.parse(parts[2]);

        // Проверка занятости
        boolean busy = appointmentRepository.findAll().stream()
                .anyMatch(a -> date.equals(a.getDate()) && time.equals(a.getTime()));

        if (busy) {
            sendMessage(chatId, "Извините, выбранное время уже занято. Пожалуйста, выберите другое время.");
            // Показать оставшиеся свободные слоты
            SendMessage msg = SendMessage.builder()
                    .chatId(chatId)
                    .text("Свободные слоты на " + date + ":")
                    .replyMarkup(buildTimeKeyboard(date))
                    .build();
            telegramClient.execute(msg);
            return;
        }

        AppointmentEntity ap = new AppointmentEntity();
        ap.setChatId(chatId);
        ap.setDate(date);
        ap.setTime(time);

        appointmentRepository.save(ap);

        SendMessage msg = SendMessage.builder()
                .chatId(chatId)
                .text("Вы успешно записаны на:\n📅 " + date + "\n⏰ " + time)
                .replyMarkup(buildFinishRecordMenu())
                .build();

        telegramClient.execute(msg);
    }

    private InlineKeyboardMarkup buildFinishRecordMenu() {

        var main = InlineKeyboardButton.builder()
                .text("Главная")
                .callbackData("menu")
                .build();

        var again = InlineKeyboardButton.builder()
                .text("Записаться ещё")
                .callbackData("record")
                .build();

        return new InlineKeyboardMarkup(
                List.of(
                        new InlineKeyboardRow(main),
                        new InlineKeyboardRow(again)
                )
        );
    }

    // ============================================
    //               ПРОЧЕЕ МЕНЮ
    // ============================================

    private void sendCatalog(Long chatId) {
        sendMessage(chatId, "Скоро всё будет работать");
    }

    private void sendRecordConfirmation(Long chatId) {
        sendMessage(chatId, "Запись подтверждена!");
    }

    @SneakyThrows
    private void sendMessage(Long chatId, String messageText) {
        SendMessage message = SendMessage.builder()
                .text(messageText)
                .chatId(chatId)
                .build();

        telegramClient.execute(message);
    }

    @SneakyThrows
    private void sendMainMenu(Long chatId) {

        SendMessage message = SendMessage.builder()
                .text("Здравствуйте! Вас приветствует Платформа Комфорта — эксперт в области дизайн-проектов, ремонта и комплексного оснащения жилья.\n\nРады помочь вам воплотить вашу идею в реальность!")
                .chatId(chatId)
                .build();

        var button1 = InlineKeyboardButton.builder()
                .text("Главная")
                .callbackData("menu")
                .build();

        var button2 = InlineKeyboardButton.builder()
                .text("Я первый раз")
                .callbackData("test")
                .build();

        var button3 = InlineKeyboardButton.builder()
                .text("Запись на приём")
                .callbackData("record")
                .build();

        var button4 = InlineKeyboardButton.builder()
                .text("Каталог")
                .callbackData("catalog")
                .build();

        List<InlineKeyboardRow> keyboardRows = List.of(
                new InlineKeyboardRow(button1),
                new InlineKeyboardRow(button2),
                new InlineKeyboardRow(button3),
                new InlineKeyboardRow(button4)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);

        message.setReplyMarkup(markup);

        telegramClient.execute(message);
    }

    // ==========================================
    //             КАЛЕНДАРЬ
    // ==========================================

    private List<LocalTime> getAvailableTimes(LocalDate date) {
        // Шаблон временных слотов (можешь изменить)
        List<LocalTime> all = List.of(
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                LocalTime.of(11, 0),
                LocalTime.of(12, 0),
                LocalTime.of(13, 0),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0),
                LocalTime.of(16, 0),
                LocalTime.of(17, 0)
        );

        // Берём все записи на дату и исключаем занятые времена
        List<LocalTime> booked = appointmentRepository.findAll().stream()
                .filter(a -> date.equals(a.getDate()))
                .map(AppointmentEntity::getTime)
                .collect(Collectors.toList());

        return all.stream()
                .filter(t -> !booked.contains(t))
                .collect(Collectors.toList());
    }

    private static class CalendarUtils {

        public static InlineKeyboardMarkup buildMonthCalendar(YearMonth ym) {
            List<InlineKeyboardRow> rows = new ArrayList<>();

            // Заголовок месяца
// Заголовок месяца (русские названия)
            String monthRus = ym.getMonth()
                    .getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("ru"));

// Первая буква — заглавная
            monthRus = monthRus.substring(0, 1).toUpperCase() + monthRus.substring(1);

            InlineKeyboardRow header = new InlineKeyboardRow();
            header.add(InlineKeyboardButton.builder()
                    .text(monthRus + " " + ym.getYear())
                    .callbackData("IGNORE")
                    .build());
            rows.add(header);;

            // Дни недели
            InlineKeyboardRow weekDays = new InlineKeyboardRow();
            weekDays.add(btn("Пн", "IGNORE"));
            weekDays.add(btn("Вт", "IGNORE"));
            weekDays.add(btn("Ср", "IGNORE"));
            weekDays.add(btn("Чт", "IGNORE"));
            weekDays.add(btn("Пт", "IGNORE"));
            weekDays.add(btn("Сб", "IGNORE"));
            weekDays.add(btn("Вс", "IGNORE"));
            rows.add(weekDays);

            LocalDate first = ym.atDay(1);
            int shift = first.getDayOfWeek().getValue();
            if (shift == 7) shift = 0;

            InlineKeyboardRow weekRow = new InlineKeyboardRow();

            // пустые ячейки
            for (int i = 0; i < shift; i++) {
                weekRow.add(btn(" ", "IGNORE"));
            }
            LocalDate today = LocalDate.now();

// дни месяца
            for (int day = 1; day <= ym.lengthOfMonth(); day++) {

                if (weekRow.size() == 7) {
                    rows.add(weekRow);
                    weekRow = new InlineKeyboardRow();
                }

                LocalDate d = ym.atDay(day);

                String text = String.valueOf(day);
                String callback = "DAY_" + d;

                if (d.isBefore(today)) {
                    text = "✖\uFE0F" + day; //стиль
                    callback = "IGNORE";   // нельзя нажать
                }

                weekRow.add(btn(text, callback));
            }


            if (!weekRow.isEmpty()) rows.add(weekRow);

            // стрелки
            InlineKeyboardRow arrows = new InlineKeyboardRow();
            arrows.add(btn("<<", "PREV_" + ym.minusMonths(1)));
            arrows.add(btn(">>", "NEXT_" + ym.plusMonths(1)));
            rows.add(arrows);

            return new InlineKeyboardMarkup(rows);
        }

        private static InlineKeyboardButton btn(String text, String data) {
            return InlineKeyboardButton.builder()
                    .text(text)
                    .callbackData(data)
                    .build();
        }
    }
}