package ru.goncharov.study.platforma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.goncharov.study.platforma.Config.BotProperties;

@EnableConfigurationProperties(BotProperties.class)
@SpringBootApplication
public class PlatformaApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformaApplication.class, args);
    }

}
