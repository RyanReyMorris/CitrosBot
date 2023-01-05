package ru.blogic.CitrosBot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.blogic.CitrosBot.entity.Anecdote;
import ru.blogic.CitrosBot.repository.AnecdoteRepository;

import java.text.MessageFormat;
import java.util.List;
import java.util.Random;

/**
 * Сервис для работы с анекдотами
 *
 * @author eyakimov
 */
@Service
public class AnecdoteServiceImpl implements AnecdoteService {

    @Autowired
    private AnecdoteRepository anecdoteRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAnecdote(Long anecdoteId) {
        anecdoteRepository.deleteById(anecdoteId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Anecdote> getAllAnecdotes() {
        return anecdoteRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveAnecdote(Anecdote anecdote) {
        anecdoteRepository.save(anecdote);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Anecdote getRandomAnecdote() {
        List<Anecdote> anecdotes = anecdoteRepository.findAll();
        if (anecdotes.isEmpty()) {
            return null;
        }
        return anecdotes.get(getRandomNumberInRange(0, anecdotes.size()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Anecdote> getAnecdotesByUser(Long authorId) {
        return anecdoteRepository.findAllByAuthor_Id(authorId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isExistingAnecdote(Long id) {
        return anecdoteRepository.findById(id).isPresent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Anecdote getAnecdoteById(Long id) {
        return anecdoteRepository.findById(id).
                orElseThrow(() -> new RuntimeException(MessageFormat.format("Анекдота с id = {0} не было найдено", id)));
    }

    /**
     * Метод получения рандомного числа в диапазоне
     *
     * @param min - минимальное число
     * @param max - максимальное число
     * @return int - числовое значение
     */
    public int getRandomNumberInRange(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }
}
