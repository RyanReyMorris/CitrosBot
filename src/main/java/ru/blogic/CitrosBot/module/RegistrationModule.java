package ru.blogic.CitrosBot.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.blogic.CitrosBot.entity.User;
import ru.blogic.CitrosBot.enums.ModuleEnum;
import ru.blogic.CitrosBot.repository.UserRepository;

/**
 * Модуль чат-бота, отвечающий за регистрацию пользователя.
 * Пользователь указывает свои личные данные, по которым к нему будет обращаться бот, а также другие коллеги.
 *
 * @author eyakimov
 */
@Service
public class RegistrationModule implements Module {

    @Autowired
    private UserRepository userRepository;

    @Override
    public BotApiMethod<?> execute(Update update) {
        User user = new User();
        Chat chat = update.getMessage().getChat();
        user.setChatId(update.getMessage().getChatId());
        user.setStatus("reg");
        user.setFullName(chat.getFirstName()+" "+chat.getLastName());
        userRepository.save(user);
        SendMessage  sendMessage = new SendMessage();
        User user2 = userRepository.findByChatId(update.getMessage().getChatId());
        sendMessage.setText("Привет "+ user2.getFullName());
        sendMessage.setChatId(user.getChatId());
        return sendMessage;
    }

    @Override
    public ModuleEnum getModuleType() {
        return ModuleEnum.REGISTRATION_MODULE;
    }
}
