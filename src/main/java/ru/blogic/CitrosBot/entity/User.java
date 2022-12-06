package ru.blogic.CitrosBot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.sql.Date;
import java.util.Objects;

@Entity
@Table(name = "users")
@Getter
@ToString
@RequiredArgsConstructor
public class User {
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "status")
    //sets the broadcast time of events for your time zone
    private String status;

    @Column(name = "is_registered")
    //sets the broadcast time of events for your time zone
    private boolean isRegistered;

    @Column(name = "birthday")
    private Date birthday;

    @Column(name = "full_name")
    // on/off send event
    private String fullName;

    public void changeUserStatus(String status) {
        this.status = status;
    }

    public void changeFullName(String fullName) {
        this.fullName = fullName;
    }

    public void changeRegistrationStatus(boolean isRegistered) {
        this.isRegistered = isRegistered;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && Objects.equals(chatId, user.chatId) && Objects.equals(status, user.status) && Objects.equals(birthday, user.birthday) && Objects.equals(fullName, user.fullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, chatId, status, birthday, fullName);
    }

    public static UserBuilder newBuilder() {
        return new User().new UserBuilder();
    }

    public class UserBuilder {
        private UserBuilder() {
        }

        public UserBuilder setId(long id) {
            User.this.id = id;
            return this;
        }

        public UserBuilder setChatId(Long chatId) {
            User.this.chatId = chatId;
            return this;
        }

        public UserBuilder setStatus(String status) {
            User.this.status = status;
            return this;
        }

        public UserBuilder setRegistered(boolean isRegistered) {
            User.this.isRegistered = isRegistered;
            return this;
        }

        public UserBuilder setBirthday(Date birthday) {
            User.this.birthday = birthday;
            return this;
        }

        public UserBuilder setFullName(String fullName) {
            User.this.fullName = fullName;
            return this;
        }

        public User build() {
            return User.this;
        }
    }
}
