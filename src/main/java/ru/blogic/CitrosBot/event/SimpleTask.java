package ru.blogic.CitrosBot.event;

import java.util.TimerTask;

/**
 * Класс-обертка для создаваемой задачи
 *
 * @author eyakimov
 */
public class SimpleTask extends TimerTask {
    /**
     * Отправляемое событие
     */
    private final SendEvent sendEvent;

    public SimpleTask(SendEvent sendEvent) {
        this.sendEvent = sendEvent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        sendEvent.start();
    }
}
