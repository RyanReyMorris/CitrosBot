package ru.blogic.CitrosBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.blogic.CitrosBot.entity.Anecdote;

import java.util.List;

public interface AnecdoteRepository extends JpaRepository<Anecdote, Long> {
    /**
     * Поиск всех анекдотов для конкретного пользователя
     */
    List<Anecdote> findAllByAuthor_Id(Long authorId);
}
