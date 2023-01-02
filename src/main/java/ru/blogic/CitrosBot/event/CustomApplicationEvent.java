package ru.blogic.CitrosBot.event;

import org.springframework.context.ApplicationEvent;
import ru.blogic.CitrosBot.entity.UserEntity;
import ru.blogic.CitrosBot.enums.ModuleEnum;

/**
 * Ивент, отвечающий за отправку пользователю информации о боте, а также о доступных модулях
 *
 * @author eyakimov
 */
public class CustomApplicationEvent extends ApplicationEvent {

    private ModuleEnum moduleEnum;

    private UserEntity userEntity;

    public CustomApplicationEvent(Object source, ModuleEnum moduleEnum, UserEntity userEntity) {
        super(source);
        this.moduleEnum = moduleEnum;
        this.userEntity = userEntity;
    }

    public UserEntity getUser() {
        return userEntity;
    }

    public void setUser(UserEntity userEntity) {
        this.userEntity = userEntity;
    }

    public ModuleEnum getModuleEnum() {
        return moduleEnum;
    }

    public void setModuleEnum(ModuleEnum moduleEnum) {
        this.moduleEnum = moduleEnum;
    }
}
