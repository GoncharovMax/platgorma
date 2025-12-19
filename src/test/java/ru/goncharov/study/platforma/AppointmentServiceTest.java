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
import ru.goncharov.study.platforma.Entity.AppointmentEntity;
import ru.goncharov.study.platforma.Entity.SurveyState;
import ru.goncharov.study.platforma.Entity.UserSurvey;
import ru.goncharov.study.platforma.repository.AppointmentRepository;
import ru.goncharov.study.platforma.repository.UserSurveyRepository;
import ru.goncharov.study.platforma.service.AppointmentService;
import ru.goncharov.study.platforma.service.SurveyService;
import ru.goncharov.study.platforma.service.TimeSlotService;
import ru.goncharov.study.platforma.service.YandexCalendarService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    private ArgumentCaptor<AppointmentEntity> appointmentEntityCaptor;

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

        // Можно замокать UID, чтобы убедиться, что он тоже попал в сущность
        when(calendarService.createEvent(anyString(), anyString(), eq(survey.getChatId()), eq(date), eq(time)))
                .thenReturn("UID-123");

        // Выполнение метода
        appointmentService.handleTime(survey.getChatId(), "TIME_" + date + "_" + time);

        // Проверка сохранения в репозитории и захватываем аргумент
        verify(appointmentRepo, times(1)).save(appointmentEntityCaptor.capture());
        AppointmentEntity saved = appointmentEntityCaptor.getValue();

        assertEquals(survey.getChatId(), saved.getChatId());
        assertEquals(date, saved.getDate());
        assertEquals(time, saved.getTime());
        assertEquals("Максим", saved.getFullName());
        assertEquals("UID-123", saved.getIcsUid());
        assertFalse(saved.isNotified());

        // Проверка вызова календаря
        verify(calendarService, times(1))
                .createEvent(anyString(), anyString(), eq(survey.getChatId()), eq(date), eq(time));
    }

    @Test
    void testHandleTime_whenTimeBusy_doesNotCreateAppointmentOrCalendarEvent() throws Exception {
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime time = LocalTime.of(10, 0);

        // слот занят
        when(appointmentRepo.existsByDateAndTime(date, time)).thenReturn(true);

        appointmentService.handleTime(survey.getChatId(), "TIME_" + date + "_" + time);

        // Ничего не сохраняем
        verify(appointmentRepo, never()).save(any());

        // Календарь не вызываем
        verify(calendarService, never()).createEvent(any(), any(), anyLong(), any(), any());
    }
}