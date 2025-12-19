package ru.goncharov.study.platforma;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.goncharov.study.platforma.Entity.SurveyState;
import ru.goncharov.study.platforma.Entity.UserSurvey;
import ru.goncharov.study.platforma.repository.UserSurveyRepository;
import ru.goncharov.study.platforma.service.SurveyService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SurveyServiceTest {

    @Mock
    private UserSurveyRepository repo;

    @Mock
    private TelegramClient telegramClient;

    @InjectMocks
    private SurveyService surveyService;

    @Test
    void process_whenSurveyNotFound_returnsFalse() throws Exception {
        Long chatId = 123L;

        when(repo.findByChatId(chatId)).thenReturn(Optional.empty());

        boolean result = surveyService.process(chatId, "Текст");

        assertFalse(result);
        verify(repo, never()).save(any());
        verifyNoInteractions(telegramClient);
    }

    @Test
    void process_ASK_NAME_setsNameAndAsksPhone() throws Exception {
        Long chatId = 123L;

        // исходное состояние анкеты
        UserSurvey survey = new UserSurvey();
        survey.setId(1L);
        survey.setChatId(chatId);
        survey.setState(SurveyState.ASK_NAME);

        when(repo.findByChatId(chatId)).thenReturn(Optional.of(survey));
        when(repo.save(any(UserSurvey.class))).thenAnswer(inv -> inv.getArgument(0));
        when(telegramClient.execute(any(SendMessage.class))).thenReturn(null);

        // действие
        boolean result = surveyService.process(chatId, "Максим");

        // метод вернул true
        assertTrue(result);

        // имя записано, состояние перешло в ASK_PHONE
        assertEquals("Максим", survey.getName());
        assertEquals(SurveyState.ASK_PHONE, survey.getState());

        // анкета сохранена
        verify(repo).save(survey);

        // отправлено сообщение с просьбой указать телефон
        ArgumentCaptor<SendMessage> msgCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClient).execute(msgCaptor.capture());

        SendMessage msg = msgCaptor.getValue();

        assertEquals(String.valueOf(chatId), msg.getChatId());
        assertTrue(msg.getText().contains("номер телефона"));
    }

    @Test
    void process_ASK_QUESTION_finishesSurveyAndSendsEndMessage() throws Exception {
        Long chatId = 123L;

        UserSurvey survey = new UserSurvey();
        survey.setId(1L);
        survey.setChatId(chatId);
        survey.setName("Максим");
        survey.setPhone("+79991234567");
        survey.setState(SurveyState.ASK_QUESTION);

        when(repo.findByChatId(chatId)).thenReturn(Optional.of(survey));
        when(repo.save(any(UserSurvey.class))).thenAnswer(inv -> inv.getArgument(0));
        when(telegramClient.execute(any(SendMessage.class))).thenReturn(null);

        boolean result = surveyService.process(chatId, "дизайн квартиры 60 м²");

        assertTrue(result);
        assertEquals("дизайн квартиры 60 м²", survey.getQuestionAbout());
        assertEquals(SurveyState.FINISHED, survey.getState());

        verify(repo).save(survey);
        verify(telegramClient, atLeastOnce()).execute(any(SendMessage.class));
    }
}