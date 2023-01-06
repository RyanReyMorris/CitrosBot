package ru.blogic.CitrosBot.event;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Класс, предназначенный для пинга CitrosBot'a в случае, если он неактивен в течение 30 минут
 * PingTask необходим, поскольку RailWay (аналог Heroku) отключает веб-приложение на бесплатном режиме, в случае,
 * если оно неактивно в течении определенного времени.
 * Удалить класс после перевода бота на любой другой удаленный сервер.
 *
 * @author eyakimov
 */
@Service
@EnableScheduling
@Slf4j
@Getter
@Setter
public class PingTask {
    /**
     * url для пинга
     */
    @Value("${ping.url}")
    private String url;

    /**
     * Метод, предназначенный для пинга заданного url каждые 30 минут
     */
    @Scheduled(fixedRateString = "${ping.period}")
    public void pingMe() {
        try {
            URL url = new URL(getUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            log.info("Пинг сайта: {}, OK: код ответа {}", url.getHost(), connection.getResponseCode());
            connection.disconnect();
        } catch (IOException e) {
            log.error("ОШИБКА, пинг не удался");
            e.printStackTrace();
        }

    }
}
