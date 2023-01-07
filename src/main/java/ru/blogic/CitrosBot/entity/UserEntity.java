package ru.blogic.CitrosBot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

/**
 * Сущность пользователя
 *
 * @author eyakimov
 */
@Entity(name = "UserEntity")
@Table(name = "user_entity")
@Getter
@ToString
@RequiredArgsConstructor
public class UserEntity {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "time_zone")
    private String timeZone;

    @Column(name = "active_module")
    private String activeModule;

    /**
     * Флаг изменения той или иной информации о пользователе.
     * Необходим для обработки входящих сообщений и нажатий кнопок в модуле ChangeInfoModule
     */
    @Column(name = "user_info_status")
    private String userInfoStatus;

    /**
     * Флаг создания анекдота.
     * Необходим для обработки входящих сообщений и нажатий кнопок в модуле AnecdoteModule
     */
    @Column(name = "user_anecdote_status")
    private String userAnecdoteStatus;

    @Column(name = "is_registered")
    private boolean isRegistered;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "is_admin")
    private boolean isAdmin;

    @Column(name = "is_blocked")
    private boolean isBlocked;

    @Column(name = "is_birthday_module_on")
    private boolean isBirthdayModuleOn;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "id_department", referencedColumnName = "id")
    private Department department;

    public void changeAdminStatus(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public void changeBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public void changeTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public void changeActiveModule(String activeModule) {
        this.activeModule = activeModule;
    }

    public void changeFullName(String fullName) {
        this.fullName = fullName;
    }

    public void changeRegistrationStatus(boolean isRegistered) {
        this.isRegistered = isRegistered;
    }

    public void changeDepartment(Department department) {
        this.department = department;
    }

    public void changeUserInfoStatus(String userInfoStatus) {
        this.userInfoStatus = userInfoStatus;
    }

    public void changeUserAnecdoteStatus(String userAnecdoteStatus) {
        this.userAnecdoteStatus = userAnecdoteStatus;
    }

    public void changeUserBlockStatus(boolean isBlocked) {
        this.isBlocked = isBlocked;
    }

    public void changeBirthdayModuleOn(boolean isBirthdayModuleOn) {
        this.isBirthdayModuleOn = isBirthdayModuleOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        return isRegistered == that.isRegistered && isAdmin == that.isAdmin && isBlocked == that.isBlocked
                && isBirthdayModuleOn == that.isBirthdayModuleOn && Objects.equals(id, that.id)
                && Objects.equals(chatId, that.chatId) && Objects.equals(timeZone, that.timeZone)
                && Objects.equals(activeModule, that.activeModule) && Objects.equals(userInfoStatus, that.userInfoStatus)
                && Objects.equals(userAnecdoteStatus, that.userAnecdoteStatus) && Objects.equals(birthday, that.birthday)
                && Objects.equals(fullName, that.fullName) && Objects.equals(department, that.department);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, chatId, timeZone, activeModule, userInfoStatus, userAnecdoteStatus, isRegistered,
                birthday, fullName, isAdmin, isBlocked, isBirthdayModuleOn, department);
    }

    public static UserBuilder newBuilder() {
        return new UserEntity().new UserBuilder();
    }

    public class UserBuilder {
        private UserBuilder() {
        }

        public UserBuilder setId(Long id) {
            UserEntity.this.id = id;
            return this;
        }

        public UserBuilder setChatId(Long chatId) {
            UserEntity.this.chatId = chatId;
            return this;
        }

        public UserBuilder setTimeZone(String timeZone) {
            UserEntity.this.timeZone = timeZone;
            return this;
        }

        public UserBuilder setActiveModule(String activeModule) {
            UserEntity.this.activeModule = activeModule;
            return this;
        }

        public UserBuilder setUserInfoStatus(String userInfoStatus) {
            UserEntity.this.userInfoStatus = userInfoStatus;
            return this;
        }

        public UserBuilder setUserAnecdoteStatus(String userAnecdoteStatus) {
            UserEntity.this.userAnecdoteStatus = userAnecdoteStatus;
            return this;
        }

        public UserBuilder setRegistered(boolean isRegistered) {
            UserEntity.this.isRegistered = isRegistered;
            return this;
        }

        public UserBuilder setAdmin(boolean isAdmin) {
            UserEntity.this.isAdmin = isAdmin;
            return this;
        }

        public UserBuilder setBirthdayModuleOn(boolean isBirthdayModuleOn) {
            UserEntity.this.isBirthdayModuleOn = isBirthdayModuleOn;
            return this;
        }

        public UserBuilder setBlocked(boolean isBlocked) {
            UserEntity.this.isBlocked = isBlocked;
            return this;
        }

        public UserBuilder setBirthday(LocalDate birthday) {
            UserEntity.this.birthday = birthday;
            return this;
        }

        public UserBuilder setFullName(String fullName) {
            UserEntity.this.fullName = fullName;
            return this;
        }

        public UserBuilder setDepartment(Department department) {
            UserEntity.this.department = department;
            return this;
        }

        public UserEntity build() {
            return UserEntity.this;
        }
    }
}
