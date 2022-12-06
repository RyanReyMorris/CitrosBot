package ru.blogic.CitrosBot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.blogic.CitrosBot.entity.User;
import ru.blogic.CitrosBot.enums.ModuleEnum;
import ru.blogic.CitrosBot.repository.UserRepository;

import java.text.MessageFormat;
import java.util.Optional;

/**
 * Сервис для работы с пользователем
 *
 * @author eyakimov
 */
@Service
public class UserServiceIml implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public ModuleEnum getModuleByUserState(Message message) {
        Optional<User> user = userRepository.findById(message.getChat().getId());
        user = user.isPresent() ? user : Optional.of(createNewUser(message));
        return ModuleEnum.valueOf(user.get().getStatus());
    }

    private User createNewUser(Message message) {
        String firstName = message.getChat().getFirstName() != null ? message.getChat().getFirstName() : "";
        String lastName = message.getChat().getLastName() != null ? message.getChat().getLastName() : "";
        String fullName = MessageFormat.format(
                "{0} {1}", firstName, lastName);
        User user = User.newBuilder()
                .setId(message.getFrom().getId())
                .setChatId(message.getChatId())
                .setFullName(fullName)
                .setStatus(ModuleEnum.REGISTRATION_MODULE.name())
                .setRegistered(false)
                .build();
        userRepository.save(user);
        return user;
    }
}
