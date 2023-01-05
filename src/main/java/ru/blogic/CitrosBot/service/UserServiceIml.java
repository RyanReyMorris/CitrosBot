package ru.blogic.CitrosBot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.blogic.CitrosBot.entity.UserEntity;
import ru.blogic.CitrosBot.enums.ModuleEnum;
import ru.blogic.CitrosBot.repository.UserRepository;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

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
    public List<UserEntity> findUsersWithAnecdotes() {
        return userRepository.findUsersWithAnecdotes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserEntity> findAllNonBirthdayPersons(Date birthday) {
        return userRepository.findByBirthdayIsNotAndIsBirthdayModuleOnIsTrue(birthday);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserEntity> findAllBirthdayPersons(Date birthday) {
        return userRepository.findByBirthdayAndIsBirthdayModuleOnIsTrue(birthday);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserEntity> findAdmins() {
        return userRepository.findAllByIsAdminIsTrue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveUser(UserEntity userEntity) {
        userRepository.save(userEntity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserEntity findUserById(Long id) {
        return userRepository.findById(id).
                orElseThrow(() -> new RuntimeException(MessageFormat.format("Пользователя с id = {0} не было найдено", id)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModuleEnum getModuleByUserState(Long id) {
        UserEntity userEntity = userRepository.findById(id).
                orElseThrow(() -> new RuntimeException(MessageFormat.format("Пользователя с id = {0} не было найдено", id)));
        return ModuleEnum.valueOf(userEntity.getActiveModule());
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
    public UserEntity createNewUser(Message message) {
        UserEntity userEntity = UserEntity.newBuilder()
                .setId(message.getFrom().getId())
                .setChatId(message.getChatId())
                .setFullName(message.getFrom().getFirstName())
                .setActiveModule(ModuleEnum.REGISTRATION_MODULE.name())
                .setRegistered(false)
                .build();
        userRepository.save(userEntity);
        return userEntity;
    }
}
