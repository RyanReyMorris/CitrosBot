package ru.blogic.CitrosBot.event;

import org.springframework.context.ApplicationEvent;
import ru.blogic.CitrosBot.entity.User;
import ru.blogic.CitrosBot.enums.ModuleEnum;

/**
 * Ивент, отвечающий за отправку пользователю информации о боте, а также о доступных модулях
 *
 * @author eyakimov
 */
public class CustomApplicationEvent extends ApplicationEvent {

    private ModuleEnum moduleEnum;

    private User user;

    public CustomApplicationEvent(Object source, ModuleEnum moduleEnum, User user) {
        super(source);
        this.moduleEnum = moduleEnum;
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ModuleEnum getModuleEnum() {
        return moduleEnum;
    }

    public void setModuleEnum(ModuleEnum moduleEnum) {
        this.moduleEnum = moduleEnum;
    }
}
