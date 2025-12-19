package ru.goncharov.study.platforma;

import org.junit.jupiter.api.Test;
import ru.goncharov.study.platforma.service.TimeSlotService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TimeSlotServiceTest {

    private final TimeSlotService service = new TimeSlotService();

    @Test
    void availableSlots_whenSomeBooked_returnsOnlyFree() {
        LocalDate date = LocalDate.of(2025, 12, 19);

        List<LocalTime> booked = List.of(
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                LocalTime.of(15, 0)
        );

        List<LocalTime> free = service.availableSlots(date, booked);

        // не содержит занятые
        assertFalse(free.contains(LocalTime.of(9, 0)));
        assertFalse(free.contains(LocalTime.of(11, 0)));
        assertFalse(free.contains(LocalTime.of(15, 0)));

        // но содержит другие
        assertTrue(free.contains(LocalTime.of(10, 0)));
        assertTrue(free.contains(LocalTime.of(14, 0)));
        assertTrue(free.contains(LocalTime.of(17, 0)));
    }

    @Test
    void availableSlots_whenNoneBooked_returnsAll() {
        LocalDate date = LocalDate.of(2025, 12, 19);
        List<LocalTime> free = service.availableSlots(date, List.of());

        // всего слотов 9 (9:00–17:00)
        assertEquals(9, free.size());
        assertTrue(free.contains(LocalTime.of(9, 0)));
        assertTrue(free.contains(LocalTime.of(17, 0)));
    }

    @Test
    void availableSlots_whenAllBooked_returnsEmpty() {
        LocalDate date = LocalDate.of(2025, 12, 19);

        List<LocalTime> allBooked = List.of(
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

        List<LocalTime> free = service.availableSlots(date, allBooked);

        assertTrue(free.isEmpty());
    }
}