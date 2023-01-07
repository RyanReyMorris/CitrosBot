package ru.blogic.CitrosBot.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.blogic.CitrosBot.entity.UserEntity;
import ru.blogic.CitrosBot.service.MessageService;
import ru.blogic.CitrosBot.service.UserService;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.*;

/**
 * Сервис по оповещению пользователей о дне рождения их коллеги
 *
 * @author eyakimov
 */
@Slf4j
@Transactional
@EnableScheduling
@Service
public class BirthdayService {
    /**
     * Час отправки сообщения
     */
    @Value("${bot.birthday.hour}")
    private Integer sendingHour;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    /**
     * Метод по созданию событий отправки пользователям уведомления о дне рождения их коллеги
     * Имениннику отправляется поздравление, а всем его коллегам - уведомление о дне рождения именинника
     * Метод запускается каждый день в 00:00.
     */
    @Scheduled(cron = "${bot.birthday.cron}")
    public void eventService() {
        log.info("Ежедневная проверка именинников");
        LocalDate birthdayDate = LocalDate.now();
        List<UserEntity> birthdayPersons = userService.findAllBirthdayPersons(birthdayDate);
        String logText = birthdayPersons.isEmpty() ? "Именинников сегодня нет" : "Сегодня есть именинники";
        log.info(logText);
        List<UserEntity> notBirthdayPersons = userService.findAllNonBirthdayPersons(birthdayDate);
        for (UserEntity user : birthdayPersons) {
            Calendar calendarUserTime = getCalendarForUser(user.getTimeZone());
            SendEvent sendEvent = new SendEvent();
            String text = MessageFormat.format(":birthday: С днем рождения вас, {0}! Желаю всего самого наилучшего :balloon:", user.getFullName());
            SendMessage sendMessage = messageService.getMessage(text, user.getChatId());
            sendEvent.setSendMessage(sendMessage);
            new Timer().schedule(new SimpleTask(sendEvent), calendarUserTime.getTime());
        }
        for (UserEntity user : notBirthdayPersons) {
            for (UserEntity birthdayUser : birthdayPersons) {
                Calendar calendarUserTime = getCalendarForUser(user.getTimeZone());
                SendEvent sendEvent = new SendEvent();
                String text = MessageFormat.format(":birthday: У {0} из {1} сегодня день рождения! Не забудьте поздравить", birthdayUser.getFullName(), birthdayUser.getDepartment().getNameOfDepartment());
                SendMessage sendMessage = messageService.getMessage(text, user.getChatId());
                sendEvent.setSendMessage(sendMessage);
                new Timer().schedule(new SimpleTask(sendEvent), calendarUserTime.getTime());
            }
        }
    }

    /**
     * Метод получения точной даты отправки события для пользователя в зависимости от его часового пояса
     *
     * @param timeZone - часовой пояс пользователя
     * @return Calendar - дата отправки события
     */
    private Calendar getCalendarForUser(String timeZone) {
        TimeZone userTimeZone = TimeZone.getTimeZone(timeZone);
        Calendar userCalendar = Calendar.getInstance(userTimeZone);
        int timeZoneHour = userTimeZone.getOffset(userCalendar.getTimeInMillis()) / 1000 / 3600;
        TimeZone appTimeZone = TimeZone.getDefault();
        Calendar appCalendar = Calendar.getInstance(appTimeZone);
        int appZoneHour = appTimeZone.getOffset(appCalendar.getTimeInMillis()) / 1000 / 3600;
        int hour = appZoneHour > timeZoneHour ? sendingHour + (appZoneHour - timeZoneHour) : sendingHour - (timeZoneHour - appZoneHour);
        Calendar calendarForUser = Calendar.getInstance();
        calendarForUser.setTime(new Date());
        calendarForUser.add(Calendar.HOUR_OF_DAY, hour);
        calendarForUser.set(Calendar.MINUTE, 0);
        calendarForUser.set(Calendar.SECOND, 0);
        calendarForUser.set(Calendar.MILLISECOND, 0);
        return calendarForUser;
    }
}
