package ru.blogic.CitrosBot.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.blogic.CitrosBot.TelegramBot;
import ru.blogic.CitrosBot.entity.UserEntity;
import ru.blogic.CitrosBot.enums.ButtonEnum;
import ru.blogic.CitrosBot.enums.ModuleEnum;
import ru.blogic.CitrosBot.service.ButtonKeyboard;
import ru.blogic.CitrosBot.service.MessageService;
import ru.blogic.CitrosBot.service.UserService;

import java.text.MessageFormat;

/**
 * Модуль "Дни рождения" предназначен для оповещения пользователей о дне рождения их коллеги.
 * Пользователь указывает, хочет ли он подключить данную функцию.
 * В любой момент пользователь может отменить подписку на оповещение.
 *
 * @author eyakimov
 */
@Service
public class BirthdayModule implements Module {

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Lazy
    @Autowired
    private TelegramBot telegramBot;

    /**
     * {@inheritDoc}
     */
    @Override
    public BotApiMethod<?> executeMessage(Update update) {
        Long chatId = update.getMessage().getChat().getId();
        if (update.getMessage().hasEntities()) {
            return infoMessage(chatId);
        }
        return messageService.getErrorMessage(chatId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BotApiMethod<?> executeCallbackQuery(Update update) {
        Message message = update.getCallbackQuery().getMessage();
        Long chatId = message.getChat().getId();
        UserEntity userEntity = userService.findUserById(chatId);
        String button = update.getCallbackQuery().getData();
        switch (ButtonEnum.valueOf(button)) {
            case ENABLE_BIRTHDAY_MODULE:
                telegramBot.deleteMessage(message);
                userEntity.changeBirthdayModuleOn(true);
                userEntity.changeActiveModule(ModuleEnum.MAIN_MENU_MODULE.name());
                userService.saveUser(userEntity);
                return successfulEnable(chatId);
            case DISABLE_BIRTHDAY_MODULE:
                telegramBot.deleteMessage(message);
                userEntity.changeBirthdayModuleOn(false);
                userEntity.changeActiveModule(ModuleEnum.MAIN_MENU_MODULE.name());
                userService.saveUser(userEntity);
                return successfulDisable(chatId);
            case EXIT_MODULE:
                telegramBot.deleteMessage(message);
                userEntity.changeActiveModule(ModuleEnum.MAIN_MENU_MODULE.name());
                userService.saveUser(userEntity);
                userService.saveUser(userEntity);
                return null;
        }
        return messageService.getErrorMessage(chatId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModuleEnum getModuleType() {
        return ModuleEnum.BIRTHDAY_MODULE;
    }

    /**
     * Сообщение об успешном отключении модуля
     *
     * @param chatId - id чата
     * @return SendMessage
     */
    private SendMessage successfulDisable(Long chatId) {
        String text = "Модуль успешно отключен!";
        return messageService.getMessage(text, chatId);
    }

    /**
     * Сообщение об успешном подключении модуля
     *
     * @param chatId - id чата
     * @return SendMessage
     */
    private SendMessage successfulEnable(Long chatId) {
        String text = "Модуль успешно подключен!";
        return messageService.getMessage(text, chatId);
    }

    /**
     * Информационное сообщение при входе в модуль
     *
     * @param chatId - id чата
     * @return SendMessage
     */
    private SendMessage infoMessage(Long chatId) {
        String text = MessageFormat.format(
                ":birthday: Вы находитесь в модуле BIRTHDAY! {0}" +
                        ":balloon: Модуль предназначен для оповещения вас о днях рождения ваших коллег, у которых также подключен данный модуль.{1}" +
                        ":balloon: Вы можете подключить данный модуль нажав кнопку Подключить. {2}" +
                        ":balloon: В любой момент вы можете отменить подписку на модуль, нажав кнопку Отключить. {3}" +
                        ":balloon: Каждый день я буду проверять наличие именинников. Если они есть, я обязательно оповещу вас об этом! {4}" +
                        ":balloon: Подключайтесь и будьте в курсе всех дней рождения в вашем офисе (и не только в вашем) :innocent: {5}",
                "\n", "\n", "\n", "\n", "\n", "\n");
        ButtonKeyboard buttonKeyboard = new ButtonKeyboard();
        buttonKeyboard.addMessageButton(0, ButtonEnum.ENABLE_BIRTHDAY_MODULE.name(), ButtonEnum.ENABLE_BIRTHDAY_MODULE.getButtonName());
        buttonKeyboard.addMessageButton(1, ButtonEnum.DISABLE_BIRTHDAY_MODULE.name(), ButtonEnum.DISABLE_BIRTHDAY_MODULE.getButtonName());
        buttonKeyboard.addMessageButton(2, ButtonEnum.EXIT_MODULE.name(), ButtonEnum.EXIT_MODULE.getButtonName());
        return messageService.getMessageWithButtons(text, chatId, buttonKeyboard.getMessageButtons());
    }
}
