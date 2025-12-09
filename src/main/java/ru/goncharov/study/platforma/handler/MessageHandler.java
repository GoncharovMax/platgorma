package ru.goncharov.study.platforma.handler;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import ru.goncharov.study.platforma.service.MenuService;
import ru.goncharov.study.platforma.service.SurveyService;



@Component
@RequiredArgsConstructor
public class MessageHandler {

    private final SurveyService surveyService;
    private final MenuService menuService;

    public void handle(Message msg) throws Exception {
        Long chatId = msg.getChatId();
        String text = msg.getText();

        if (surveyService.process(chatId, text)) return;

        if ("/start".equals(text)) menuService.sendMainMenu(chatId);
        else menuService.sendUnknown(chatId);
    }
}