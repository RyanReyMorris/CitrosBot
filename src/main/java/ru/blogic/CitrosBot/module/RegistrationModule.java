package ru.blogic.CitrosBot.module;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import ru.blogic.CitrosBot.enums.ModuleEnum;

import java.util.Map;

/**
 * Модуль чат-бота, отвечающий за регистрацию пользователя.
 * Пользователь указывает свои личные данные, по которым к нему будет обращаться бот, а также другие коллеги.
 *
 * @author eyakimov
 */
@Service
public class RegistrationModule implements Module {



    @Override
    public ModuleEnum getModuleType() {
        return ModuleEnum.REGISTRATION_MODULE;
    }
}
