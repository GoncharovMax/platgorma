package ru.goncharov.study.platforma;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.goncharov.study.platforma.handler.CallbackHandler;
import ru.goncharov.study.platforma.handler.MessageHandler;


@Component
@RequiredArgsConstructor
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final MessageHandler messageHandler;
    private final CallbackHandler callbackHandler;

    @Override
    @SneakyThrows
    public void consume(Update update) {
        if (update.hasMessage()) {
            messageHandler.handle(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            callbackHandler.handle(update.getCallbackQuery());
        }
    }
}