package ru.blogic.CitrosBot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.blogic.CitrosBot.entity.User;
import ru.blogic.CitrosBot.enums.ModuleEnum;
import ru.blogic.CitrosBot.repository.UserRepository;

import java.text.MessageFormat;

/**
 * Сервис для работы с пользователем
 *
 * @author eyakimov
 */
@Service
public class UserServiceIml implements UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User findUserById(Long id) {
        return userRepository.findById(id).
                orElseThrow(() -> new RuntimeException(MessageFormat.format("Пользователя с id = {0} не было найдено", id)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModuleEnum getModuleByUserState(Long id) {
        User user = userRepository.findById(id).
                orElseThrow(() -> new RuntimeException(MessageFormat.format("Пользователя с id = {0} не было найдено", id)));
        return ModuleEnum.valueOf(user.getStatus());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkIfUserIsNew(Long id) {
        return userRepository.findById(id).isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User createNewUser(Message message) {
        User user = User.newBuilder()
                .setId(message.getFrom().getId())
                .setChatId(message.getChatId())
                .setStatus(ModuleEnum.REGISTRATION_MODULE.name())
                .setRegistered(false)
                .build();
        userRepository.save(user);
        return user;
    }
}
