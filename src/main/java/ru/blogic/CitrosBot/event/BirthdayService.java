package ru.blogic.CitrosBot.event;

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
import java.text.ParseException;
import java.util.*;

/**
 * Сервис по оповещению пользователей о дне рождения их коллеги
 *
 * @author eyakimov
 */
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

    @Scheduled(cron = "${bot.birthday.cron}")
    public void eventService() throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date birthdayDate = calendar.getTime();
        List<UserEntity> birthdayPersons = userService.findAllBirthdayPersons(birthdayDate);
        List<UserEntity> notBirthdayPersons = userService.findAllNonBirthdayPersons(birthdayDate);
        for (UserEntity user : birthdayPersons) {
            Calendar calendarUserTime = getCalendarForUser(user, birthdayDate);
            SendEvent sendEvent = new SendEvent();
            String text = MessageFormat.format(":birthday: С днем рождения вас, {0}! Желаю всего самого наилучшего :balloon:", user.getFullName());
            SendMessage sendMessage = messageService.getMessage(text, user.getChatId());
            sendEvent.setSendMessage(sendMessage);
            new Timer().schedule(new SimpleTask(sendEvent), calendarUserTime.getTime());
        }
        for (UserEntity user : notBirthdayPersons) {
            for (UserEntity birthdayUser : birthdayPersons) {
                Calendar calendarUserTime = getCalendarForUser(user, birthdayDate);
                SendEvent sendEvent = new SendEvent();
                String text = MessageFormat.format(":birthday: У {0} из {1} сегодня день рождения! Не забудьте поздравить", birthdayUser.getFullName(), birthdayUser.getDepartment().getNameOfDepartment());
                SendMessage sendMessage = messageService.getMessage(text, user.getChatId());
                sendEvent.setSendMessage(sendMessage);
                new Timer().schedule(new SimpleTask(sendEvent), calendarUserTime.getTime());
            }
        }
    }

    private Calendar getCalendarForUser(UserEntity user, Date date) {
        TimeZone userTimeZone = TimeZone.getTimeZone(user.getTimeZone());
        Calendar userCalendar = Calendar.getInstance(userTimeZone);
        int timeZoneHour = userTimeZone.getOffset(userCalendar.getTimeInMillis()) / 1000 / 3600;
        TimeZone appTimeZone = TimeZone.getDefault();
        Calendar appCalendar = Calendar.getInstance(appTimeZone);
        int appZoneHour = appTimeZone.getOffset(appCalendar.getTimeInMillis()) / 1000 / 3600;
        int hour = appZoneHour > timeZoneHour ? sendingHour + (appZoneHour - timeZoneHour) : sendingHour - (timeZoneHour - appZoneHour);
        Calendar calendarForUser = Calendar.getInstance();
        calendarForUser.setTime(date);
        calendarForUser.add(Calendar.HOUR_OF_DAY, hour);
        calendarForUser.add(Calendar.MINUTE, 6);
        return calendarForUser;
    }
}
