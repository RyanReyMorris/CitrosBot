package ru.blogic.CitrosBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.blogic.CitrosBot.entity.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    User findByChatId (Long chatId);
}