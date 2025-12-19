package ru.goncharov.study.platforma.Config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
// Связываем поля класса со свойствами вида bot.token, bot.username
@ConfigurationProperties(prefix = "bot")
public class BotProperties {
    private String token;
    private String username;
}
