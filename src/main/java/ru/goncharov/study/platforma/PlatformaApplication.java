package ru.goncharov.study.platforma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.goncharov.study.platforma.Config.BotProperties;

@EnableConfigurationProperties(BotProperties.class)
@SpringBootApplication
@EnableScheduling
public class PlatformaApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformaApplication.class, args);
    }

}
