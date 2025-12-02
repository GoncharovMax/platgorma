package ru.goncharov.study.platforma;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import ru.goncharov.study.platforma.Config.BotProperties;

@Component
public class MyBot implements SpringLongPollingBot {

    private final UpdateConsumer updateConsumer;
    private final BotProperties botProperties;

    public MyBot(UpdateConsumer updateConsumer, BotProperties botProperties) {
        this.updateConsumer = updateConsumer;
        this.botProperties = botProperties;
    }


    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return updateConsumer;
    }
}
