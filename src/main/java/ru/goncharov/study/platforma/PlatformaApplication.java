package ru.goncharov.study.platforma;

import io.github.cdimascio.dotenv.Dotenv;
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
        Dotenv dotenvLocal = Dotenv.configure()
                .filename(".env.local")
                .ignoreIfMissing()
                .load();

        Dotenv dotenv = Dotenv.configure()
                .filename(".env")
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
        dotenvLocal.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));

        SpringApplication.run(PlatformaApplication.class, args);
    }
}