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
import ru.goncharov.study.platforma.Entity.SurveyState;
import ru.goncharov.study.platforma.Entity.UserSurvey;
import ru.goncharov.study.platforma.repository.UserSurveyRepository;

import java.util.List;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final UserSurveyRepository surveyRepository;

    public UpdateConsumer(BotProperties botProperties, UserSurveyRepository surveyRepository) {
        this.surveyRepository = surveyRepository;
        this.telegramClient = new OkHttpTelegramClient(botProperties.getToken());

    }
    @SneakyThrows
    @Override
    public void consume(Update update) {
        //    System.out.printf("Пришло сообщение %s от %s%n ",
        //    update.getMessage().getText(),
        //    update.getMessage().getChatId());
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();

            if (messageText.equals("/start")) {
                sendMainMenu(chatId);
            } else {
                sendMessage(chatId, "Для появления меню введите /start");
            }
        } else if (update.hasCallbackQuery()) {
            handleCallBackQuery(update.getCallbackQuery());

        }
    }

    private void handleCallBackQuery(CallbackQuery callbackQuery) {
        var data = callbackQuery.getData();
        var chatId = callbackQuery.getFrom().getId();
        switch (data){
            case "menu" -> sendMainMenu(chatId);
            case "test" -> startSurvey(chatId);
            case "record" -> sendRecord(chatId);
            case "catalog" -> sendCatalog(chatId);

            default -> sendMessage(chatId, "Неизвестная команда");
        }

    }

    @SneakyThrows
    private void sendMessage(Long chatId, String messageText) {
        SendMessage message = SendMessage.builder()
                .text(messageText)
                .chatId(chatId)
                .build();

        telegramClient.execute(message);
    }

    private void sendCatalog(Long chatId) {
        sendMessage(chatId,"Скоро всё будет работать");
    }

    private void sendRecord(Long chatId) {
        sendMessage(chatId,"Скоро всё будет работать");
    }

    private void startSurvey(Long chatId) {
        UserSurvey survey = surveyRepository.findByChatId(chatId)
                .orElseGet(UserSurvey::new);

        survey.setChatId(chatId);
        survey.setState(SurveyState.ASK_NAME);
        surveyRepository.save(survey);

        sendMessage(chatId, "Давайте познакомимся!\n\nКак вас зовут?");
    }


    @SneakyThrows
            private void sendMainMenu(Long chatId) {
                SendMessage message = SendMessage.builder()
                        .text("Здравствуйте! Вас приветствует Платформа Комфорта — эксперт в области дизайн-проектов, ремонта и комплексного оснащения жилья.\n" +
                                "\n" +
                                "Мы создаём продуманные интерьерные решения, выполняем ремонт любой сложности и предоставляем широкий ассортимент товаров для отделки и благоустройства.\n" +
                                "\n" +
                                "Рады помочь вам воплотить вашу идею в реальность!")
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
}
