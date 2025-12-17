package ru.goncharov.study.platforma;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.goncharov.study.platforma.Entity.SurveyState;
import ru.goncharov.study.platforma.Entity.UserSurvey;
import ru.goncharov.study.platforma.dto.AppointmentDto;
import ru.goncharov.study.platforma.repository.AppointmentRepository;
import ru.goncharov.study.platforma.repository.UserSurveyRepository;
import ru.goncharov.study.platforma.service.AppointmentService;
import ru.goncharov.study.platforma.service.SurveyService;
import ru.goncharov.study.platforma.service.TimeSlotService;
import ru.goncharov.study.platforma.service.YandexCalendarService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepo;

    @Mock
    private UserSurveyRepository surveyRepo;

    @Mock
    private TelegramClient telegramClient;

    @Mock
    private YandexCalendarService calendarService;

    @Mock
    private SurveyService surveyService;

    @Mock
    private TimeSlotService timeSlotService;

    @InjectMocks
    private AppointmentService appointmentService;

    @Captor
    private ArgumentCaptor<AppointmentDto> appointmentCaptor;

    private UserSurvey survey;

    @BeforeEach
    void setUp() {
        survey = new UserSurvey();
        survey.setId(1L);
        survey.setChatId(12345L);
        survey.setName("Максим");
        survey.setPhone("+1234567890");
        survey.setQuestionAbout("тест проекта");
        survey.setState(SurveyState.FINISHED);
    }

    @Test
    void testHandleTime_createsAppointmentAndCallsCalendar() throws Exception {
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime time = LocalTime.of(10, 0);

        // Моки
        when(surveyRepo.findByChatId(survey.getChatId())).thenReturn(Optional.of(survey));
        when(appointmentRepo.existsByDateAndTime(date, time)).thenReturn(false);

        // Выполнение метода
        appointmentService.handleTime(survey.getChatId(), "TIME_" + date + "_" + time);

        // Проверка сохранения в репозитории
        verify(appointmentRepo, times(1)).save(any());

        // Проверка вызова календаря
        verify(calendarService, times(1))
                .createEvent(anyString(), anyString(), eq(survey.getChatId()), eq(date), eq(time));
    }

    @Test
    void testHandleTime_whenTimeBusy_sendsBusyMessage() throws Exception {
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime time = LocalTime.of(10, 0);

        when(appointmentRepo.existsByDateAndTime(date, time)).thenReturn(true);

        appointmentService.handleTime(survey.getChatId(), "TIME_" + date + "_" + time);

        // Проверка, что календарь не вызван
        verify(calendarService, never()).createEvent(any(), any(), anyLong(), any(), any());
    }
}