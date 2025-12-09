package ru.goncharov.study.platforma.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class YandexCalendarService {

    private final OkHttpClient client = new OkHttpClient();

    @Value("${yandex.caldav.username}")
    private String username;

    @Value("${yandex.caldav.app-password}")
    private String appPassword;

    @Value("${yandex.caldav.calendar-url}")
    private String calendarUrl;

    private static final MediaType MEDIA_TEXT = MediaType.get("text/calendar; charset=utf-8");

    /**
     * Создаёт одночасовое событие в календаре Yandex CalDAV.
     */
    public String createEvent(String title, String description, Long chatId, LocalDate date, LocalTime time) {
        try {
            String uid = UUID.randomUUID().toString();

            ZoneOffset offset = ZoneOffset.systemDefault().getRules().getOffset(Instant.now());
            LocalDateTime localStart = date.atTime(time);
            Instant startInstant = localStart.toInstant(offset);
            Instant endInstant = localStart.plusHours(1).toInstant(offset);

            String dtStart = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                    .withZone(ZoneOffset.UTC)
                    .format(startInstant);

            String dtEnd = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                    .withZone(ZoneOffset.UTC)
                    .format(endInstant);

            String ics = buildIcs(uid, title, description, chatId, dtStart, dtEnd);

            String putUrl = calendarUrl;
            if (!putUrl.endsWith("/")) putUrl += "/";
            putUrl = putUrl + uid + ".ics";

            Request request = new Request.Builder()
                    .url(putUrl)
                    .put(RequestBody.create(ics, MEDIA_TEXT))
                    .header("Authorization", Credentials.basic(username, appPassword))
                    .header("Content-Type", "text/calendar; charset=utf-8")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    log.info("Событие создано в календаре Yandex. UID={}", uid);
                    return uid;
                } else {
                    log.error("Yandex CalDAV ошибка: {} - {}",
                            response.code(),
                            response.body() != null ? response.body().string() : "<empty>");
                }
            }

        } catch (Exception e) {
            log.error("Ошибка createEvent()", e);
        }

        return null;
    }

    private String buildIcs(String uid, String title, String description, Long chatId, String dtStartUtc, String dtEndUtc) {
        return "BEGIN:VCALENDAR\r\n" +
                "VERSION:2.0\r\n" +
                "PRODID:-//PlatformaComfort//TelegramBot//RU\r\n" +
                "BEGIN:VEVENT\r\n" +
                "UID:" + uid + "\r\n" +
                "SUMMARY:" + escapeICalText(title) + "\r\n" +
                "DESCRIPTION:" + escapeICalText(description) + "\r\n" +
                "DTSTAMP:" + nowUtcStamp() + "\r\n" +
                "DTSTART:" + dtStartUtc + "\r\n" +
                "DTEND:" + dtEndUtc + "\r\n" +
                "END:VEVENT\r\n" +
                "END:VCALENDAR\r\n";
    }

    private String nowUtcStamp() {
        return DateTimeFormatter.ofPattern("ddMMyyyy'T'HHmmss'Z'")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now());
    }

    private String escapeICalText(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace(",", "\\,")
                .replace(";", "\\;");
    }
}