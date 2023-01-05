package ru.blogic.CitrosBot.service;

import ru.blogic.CitrosBot.entity.Anecdote;

import java.util.List;

/**
 * Интерфейс сервиса для работы с анекдотами
 *
 * @author eyakimov
 */
public interface AnecdoteService {
    /**
     * Метод удаления анекдота по его id
     *
     * @param anecdoteId - id анекдота
     */
    void deleteAnecdote(Long anecdoteId);

    /**
     * Метод получения анекдота по id
     *
     * @param id анекдота
     * @return - объект анекдота
     */
    Anecdote getAnecdoteById(Long id);

    /**
     * Имеется ли анекдот с таким id в базе данных
     *
     * @param id анекдота
     * @return true, если есть, иначе - false
     */
    boolean isExistingAnecdote(Long id);

    /**
     * Метод получения всех анекдотов
     *
     * @return - список анекдотов
     */
    List<Anecdote> getAllAnecdotes();

    /**
     * Метод получения рандомного анекдота
     *
     * @return - объект анекдота
     */
    Anecdote getRandomAnecdote();

    /**
     * Метод получения всех анекдотов для конкретного пользователя
     *
     * @return - список анекдотов
     */
    List<Anecdote> getAnecdotesByUser(Long userId);

    /**
     * Метод сохранения анекдота в базу данных
     */
    void saveAnecdote(Anecdote anecdote);

}
