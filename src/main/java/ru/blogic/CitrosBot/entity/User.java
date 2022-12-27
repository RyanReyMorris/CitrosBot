package ru.blogic.CitrosBot.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.sql.Date;
import java.util.Objects;

@Entity(name = "User")
@Table(name = "user")
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
    private String status;

    @Column(name = "is_registered")
    private boolean isRegistered;

    @Column(name = "birthday")
    private Date birthday;

    @Column(name = "full_name")
    private String fullName;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "id", unique = true)
    private Department department;

    public void changeUserStatus(String status) {
        this.status = status;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return isRegistered == user.isRegistered && Objects.equals(id, user.id) && Objects.equals(chatId, user.chatId) && Objects.equals(status, user.status) && Objects.equals(birthday, user.birthday) && Objects.equals(fullName, user.fullName) && Objects.equals(department, user.department);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, chatId, status, isRegistered, birthday, fullName, department);
    }

    public static UserBuilder newBuilder() {
        return new User().new UserBuilder();
    }

    public class UserBuilder {
        private UserBuilder() {
        }

        public UserBuilder setId(Long id) {
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

        public UserBuilder setDepartment(Department department) {
            User.this.department = department;
            return this;
        }

        public User build() {
            return User.this;
        }
    }
}
