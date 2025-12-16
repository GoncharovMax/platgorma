package ru.goncharov.study.platforma.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.goncharov.study.platforma.Entity.SurveyState;
import ru.goncharov.study.platforma.Entity.UserSurvey;
import ru.goncharov.study.platforma.repository.UserSurveyRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SurveyService {

    private final UserSurveyRepository repo;
    private final TelegramClient telegramClient;

    @SneakyThrows
    public boolean process(Long chatId, String text) {
        var surveyOpt = repo.findByChatId(chatId);
        if (surveyOpt.isEmpty()) return false;

        var survey = surveyOpt.get();

        switch (survey.getState()) {
            case ASK_NAME -> {
                survey.setName(text);
                survey.setState(SurveyState.ASK_PHONE);
                repo.save(survey);
                send(chatId, "Укажите ваш номер телефона:");
                return true;
            }
            case ASK_PHONE -> {
                survey.setPhone(text);
                survey.setState(SurveyState.ASK_QUESTION);
                repo.save(survey);
                send(chatId, "С каким вопросом вы обращаетесь?\n" +
                        "Напишите коротко — мы подготовимся к встрече.\n" +
                        "(ввод текста) Например: «дизайн квартиры 60 м²», «подбор ламината и плитки», «ремонт под ключ»");
                return true;
            }
            case ASK_QUESTION -> {
                survey.setQuestionAbout(text);
                survey.setState(SurveyState.FINISHED);
                repo.save(survey);
                sendEndSurvey(chatId);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    public void start(Long chatId) {
        UserSurvey survey = repo.findByChatId(chatId).orElseGet(UserSurvey::new);
        survey.setChatId(chatId);
        survey.setState(SurveyState.ASK_NAME);
        repo.save(survey);

        send(chatId, "Давайте познакомимся! Как вас зовут?");
    }

    @SneakyThrows
    private void send(Long chatId, String text) {
        telegramClient.execute(
                SendMessage.builder().chatId(chatId).text(text).build()
        );
    }

    @SneakyThrows
    private void sendEndSurvey(Long chatId) {
        SendMessage msg = SendMessage.builder()
                .chatId(chatId)
                .text("Спасибо \uD83E\uDD0D Мы получим вашу заявку после записи на приём.")
                .replyMarkup(new InlineKeyboardMarkup(
                        List.of(
                                new InlineKeyboardRow(
                                        InlineKeyboardButton.builder().text("Главная").callbackData("menu").build()
                                ),
                                new InlineKeyboardRow(
                                        InlineKeyboardButton.builder().text("Запись на приём").callbackData("record").build()
                                )
                        )
                ))
                .build();

        telegramClient.execute(msg);
    }
}