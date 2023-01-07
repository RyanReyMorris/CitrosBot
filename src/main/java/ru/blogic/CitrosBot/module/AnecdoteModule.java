package ru.blogic.CitrosBot.module;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import ru.blogic.CitrosBot.TelegramBot;
import ru.blogic.CitrosBot.entity.Anecdote;
import ru.blogic.CitrosBot.entity.UserEntity;
import ru.blogic.CitrosBot.enums.AnecdoteTypeEnum;
import ru.blogic.CitrosBot.enums.ButtonEnum;
import ru.blogic.CitrosBot.enums.ModuleEnum;
import ru.blogic.CitrosBot.service.AnecdoteService;
import ru.blogic.CitrosBot.service.ButtonKeyboard;
import ru.blogic.CitrosBot.service.MessageService;
import ru.blogic.CitrosBot.service.UserService;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Модуль "Анекдоты" предназначен для записи пользователями анекдотов, которые в последующем можно прослушать.
 * Пользователь может записать видео/аудио/фото анекдот, получить рандомный анекдот, выбрать анекдоты от конкретного пользователя.
 *
 * @author eyakimov
 */
@Service
public class AnecdoteModule implements Module {

    @Autowired
    private AnecdoteService anecdoteService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Lazy
    @Autowired
    private TelegramBot telegramBot;

    /**
     * Мапа предназначенная для хранения анекдота перед его сохранением в БД. Ключ - id пользователя, значение - анекдот.
     * Сохранение анекдота производится только после согласия пользователя.
     */
    private final Map<Long, Anecdote> preSaveAnecdote = new HashMap<>();

    /**
     * Мапа предназначенная для хранения данных о количестве выводимых данных при выводе списка пользователей или анекдотов
     * Ключ - id пользователя, значение - количество выводимой информации.
     */
    private final Map<Long, Integer> rangeOfInfo = new HashMap<>();

    /**
     * Мапа предназначенная для хранения данных об авторе просматриваемых анекдотов. Применяется при просмотре анекдотов
     * от конкретного автора, а именно при перелистывании страниц кнопками вперед и назад
     */
    private final Map<Long, Long> checkedAuthor = new HashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public BotApiMethod<?> executeMessage(Update update) {
        Long chatId = update.getMessage().getChat().getId();
        UserEntity userEntity = userService.findUserById(chatId);
        Message message = update.getMessage();
        if (message.hasEntities()
                && message.getEntities().get(0).getText().equals("/anecdote")
                && userEntity.getUserAnecdoteStatus() == null) {
            return infoMessage(chatId);
        }
        if (message.hasEntities() && userEntity.getUserAnecdoteStatus().equals(ButtonEnum.CREATE_ANECDOTE.name())) {
            return goingToQuit(chatId);
        }
        if (userEntity.getUserAnecdoteStatus() == null) {
            return messageService.getErrorMessage(chatId);
        }
        switch (ButtonEnum.valueOf(userEntity.getUserAnecdoteStatus())) {
            case CREATE_ANECDOTE:
                if (message.hasVideo()) {
                    String fileId = message.getVideo().getFileId();
                    Anecdote anecdote = preSaveAnecdote.get(chatId);
                    anecdote.changeFileId(fileId);
                    anecdote.changeFileType(AnecdoteTypeEnum.VIDEO.name());
                    preSaveAnecdote.put(chatId, anecdote);
                    return preSaveAnecdote(chatId);
                }
                if (message.hasVoice()) {
                    String fileId = message.getVoice().getFileId();
                    Anecdote anecdote = preSaveAnecdote.get(chatId);
                    anecdote.changeFileId(fileId);
                    anecdote.changeFileType(AnecdoteTypeEnum.VOICE.name());
                    preSaveAnecdote.put(chatId, anecdote);
                    return preSaveAnecdote(chatId);
                }
                if (message.hasVideoNote()) {
                    String fileId = message.getVideoNote().getFileId();
                    Anecdote anecdote = preSaveAnecdote.get(chatId);
                    anecdote.changeFileId(fileId);
                    anecdote.changeFileType(AnecdoteTypeEnum.VIDEO_NOTE.name());
                    preSaveAnecdote.put(chatId, anecdote);
                    return preSaveAnecdote(chatId);
                }
                if (message.hasPhoto()) {
                    String fileId = message.getPhoto().get(0).getFileId();
                    Anecdote anecdote = preSaveAnecdote.get(chatId);
                    anecdote.changeFileId(fileId);
                    anecdote.changeFileType(AnecdoteTypeEnum.PHOTO.name());
                    preSaveAnecdote.put(chatId, anecdote);
                    return preSaveAnecdote(chatId);
                }
                if (preSaveAnecdote.get(chatId) != null) {
                    return messageService.getErrorMessage(chatId);
                }
                Anecdote anecdote = Anecdote.newBuilder()
                        .setAuthor(userEntity)
                        .setName(message.getText())
                        .build();
                preSaveAnecdote.put(chatId, anecdote);
                return anecdoteNameSaved(chatId);
            case CHANGE_INFO_NAME:
        }
        return messageService.getErrorMessage(chatId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BotApiMethod<?> executeCallbackQuery(Update update) {
        Message message = update.getCallbackQuery().getMessage();
        Long chatId = message.getChat().getId();
        UserEntity userEntity = userService.findUserById(chatId);
        String button = update.getCallbackQuery().getData();
        Anecdote anecdote;
        int max;
        ButtonEnum buttonEnum = ButtonEnum.EXIT_MODULE;
        telegramBot.deleteMessage(message);
        String anecdoteId = StringUtils.substringAfter(button, ":");
        if (!anecdoteId.isEmpty()) {
            anecdoteService.deleteAnecdote(Long.valueOf(anecdoteId));
            return deleteAnecdote(chatId);
        }
        try {
            buttonEnum = ButtonEnum.valueOf(button);
        } catch (Exception exception) {
            Long id = Long.valueOf(button);
            if (anecdoteService.isExistingAnecdote(id)) {
                rangeOfInfo.put(chatId, 5);
                checkedAuthor.remove(chatId);
                anecdote = anecdoteService.getAnecdoteById(id);
                sendAnecdoteToUser(anecdote, chatId);
                return null;
            }
            if (!userService.checkIfUserIsNew(id)) {
                checkedAuthor.put(chatId, id);
                rangeOfInfo.put(chatId, 5);
                return getListOfAnecdotesByAuthor(chatId);
            }
        }
        switch (buttonEnum) {
            case MY_ANECDOTES:
                checkedAuthor.put(chatId, chatId);
                rangeOfInfo.put(chatId, 5);
                return getListOfAnecdotesByAuthor(chatId);
            case UNDO_ANECDOTES:
                max = rangeOfInfo.get(chatId) - 5;
                rangeOfInfo.put(chatId, max);
                return getListOfAnecdotesByAuthor(chatId);
            case REDO_ANECDOTES:
                max = rangeOfInfo.get(chatId) + 5;
                rangeOfInfo.put(chatId, max);
                return getListOfAnecdotesByAuthor(chatId);
            case UNDO_USERS:
                max = rangeOfInfo.get(chatId) - 5;
                rangeOfInfo.put(chatId, max);
                return getUserList(chatId);
            case REDO_USERS:
                max = rangeOfInfo.get(chatId) + 5;
                rangeOfInfo.put(chatId, max);
                return getUserList(chatId);
            case GET_AUTHOR_ANECDOTES:
                rangeOfInfo.put(chatId, 5);
                return getUserList(chatId);
            case CREATE_ANECDOTE:
                userEntity.changeUserAnecdoteStatus(ButtonEnum.CREATE_ANECDOTE.name());
                userService.saveUser(userEntity);
                return createAnecdoteName(chatId);
            case GET_RANDOM_ANECDOTE:
                anecdote = anecdoteService.getRandomAnecdote();
                if (anecdote == null) {
                    return noAnecdotes(chatId);
                }
                sendAnecdoteToUser(anecdote, chatId);
                return null;
            case YES:
                anecdote = preSaveAnecdote.get(chatId);
                anecdoteService.saveAnecdote(anecdote);
                preSaveAnecdote.remove(chatId);
                userEntity.changeUserAnecdoteStatus(null);
                userService.saveUser(userEntity);
                return successSaved(chatId);
            case NO:
                preSaveAnecdote.remove(chatId);
                userEntity.changeUserAnecdoteStatus(null);
                userService.saveUser(userEntity);
                return doNotSaveAnecdote(chatId);
            case EXIT_MODULE:
                rangeOfInfo.remove(chatId);
                preSaveAnecdote.remove(chatId);
                checkedAuthor.remove(chatId);
                userEntity.changeUserAnecdoteStatus(null);
                userEntity.changeActiveModule(ModuleEnum.MAIN_MENU_MODULE.name());
                userService.saveUser(userEntity);
                return null;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModuleEnum getModuleType() {
        return ModuleEnum.ANECDOTE_MODULE;
    }

    /**
     * Сообщение при удалении анекдота
     *
     * @param chatId - id чата
     * @return SendMessage
     */
    private SendMessage deleteAnecdote(Long chatId) {
        String text = "Анекдот был успешно удален";
        return messageService.getMessage(text, chatId);
    }

    /**
     * Получение табулированного списка анекдотов для конкретного автора
     *
     * @param chatId - id чата
     * @return SendMessage
     */
    private SendMessage getListOfAnecdotesByAuthor(Long chatId) {
        Long authorId = checkedAuthor.get(chatId);
        String text = "Выберите анекдот:";
        ButtonKeyboard buttonKeyboard = new ButtonKeyboard();
        List<Anecdote> anecdotes = anecdoteService.getAnecdotesByUser(authorId);
        if (anecdotes.isEmpty()) {
            return noAnecdotes(chatId);
        }
        if (anecdotes.size() < 6) {
            for (int i = 0; i < anecdotes.size(); i++) {
                buttonKeyboard.addMessageButton(i, anecdotes.get(i).getId().toString(), anecdotes.get(i).getName());
            }
            buttonKeyboard.addMessageButton(anecdotes.size(), ButtonEnum.EXIT_MODULE.name(), ButtonEnum.EXIT_MODULE.getButtonName());
            return messageService.getMessageWithButtons(text, chatId, buttonKeyboard.getMessageButtons());
        }
        int max_range = rangeOfInfo.get(chatId);
        int max = Math.min(max_range, anecdotes.size());
        int min = max_range - 5;
        for (int i = min; i < max; i++) {
            buttonKeyboard.addMessageButton(i, anecdotes.get(i).getId().toString(), anecdotes.get(i).getName());
        }
        if (max_range >= anecdotes.size()) {
            buttonKeyboard.addMessageButton(max, ButtonEnum.UNDO_ANECDOTES.name(), ButtonEnum.UNDO_ANECDOTES.getButtonName());
            buttonKeyboard.addMessageButton(max + 1, ButtonEnum.EXIT_MODULE.name(), ButtonEnum.EXIT_MODULE.getButtonName());
            return messageService.getMessageWithButtons(text, chatId, buttonKeyboard.getMessageButtons());
        }
        if (min == 0) {
            buttonKeyboard.addMessageButton(max, ButtonEnum.REDO_ANECDOTES.name(), ButtonEnum.REDO_ANECDOTES.getButtonName());
            buttonKeyboard.addMessageButton(max + 1, ButtonEnum.EXIT_MODULE.name(), ButtonEnum.EXIT_MODULE.getButtonName());
            return messageService.getMessageWithButtons(text, chatId, buttonKeyboard.getMessageButtons());
        }
        buttonKeyboard.addMessageButton(max, ButtonEnum.UNDO_ANECDOTES.name(), ButtonEnum.UNDO_ANECDOTES.getButtonName());
        buttonKeyboard.addMessageButton(max, ButtonEnum.REDO_ANECDOTES.name(), ButtonEnum.REDO_ANECDOTES.getButtonName());
        buttonKeyboard.addMessageButton(max + 1, ButtonEnum.EXIT_MODULE.name(), ButtonEnum.EXIT_MODULE.getButtonName());
        return messageService.getMessageWithButtons(text, chatId, buttonKeyboard.getMessageButtons());
    }

    /**
     * Получение табулированного списка пользователей, у которых есть хотя бы один анекдот
     *
     * @param chatId - id чата
     * @return SendMessage
     */
    private SendMessage getUserList(Long chatId) {
        String text = "Выберите автора:";
        ButtonKeyboard buttonKeyboard = new ButtonKeyboard();
        List<UserEntity> usersWithAnecdotes = userService.findUsersWithAnecdotes();
        if (usersWithAnecdotes.isEmpty()) {
            return noAnecdotes(chatId);
        }
        if (usersWithAnecdotes.size() < 6) {
            for (int i = 0; i < usersWithAnecdotes.size(); i++) {
                buttonKeyboard.addMessageButton(i, usersWithAnecdotes.get(i).getId().toString(), usersWithAnecdotes.get(i).getFullName());
            }
            buttonKeyboard.addMessageButton(usersWithAnecdotes.size(), ButtonEnum.EXIT_MODULE.name(), ButtonEnum.EXIT_MODULE.getButtonName());
            return messageService.getMessageWithButtons(text, chatId, buttonKeyboard.getMessageButtons());
        }
        int max_range = rangeOfInfo.get(chatId);
        int max = Math.min(max_range, usersWithAnecdotes.size());
        int min = max_range - 5;
        for (int i = min; i < max; i++) {
            buttonKeyboard.addMessageButton(i, usersWithAnecdotes.get(i).getId().toString(), usersWithAnecdotes.get(i).getFullName());
        }
        if (max_range >= usersWithAnecdotes.size()) {
            buttonKeyboard.addMessageButton(max, ButtonEnum.UNDO_USERS.name(), ButtonEnum.UNDO_USERS.getButtonName());
            buttonKeyboard.addMessageButton(max + 1, ButtonEnum.EXIT_MODULE.name(), ButtonEnum.EXIT_MODULE.getButtonName());
            return messageService.getMessageWithButtons(text, chatId, buttonKeyboard.getMessageButtons());
        }
        if (min == 0) {
            buttonKeyboard.addMessageButton(max, ButtonEnum.REDO_USERS.name(), ButtonEnum.REDO_USERS.getButtonName());
            buttonKeyboard.addMessageButton(max + 1, ButtonEnum.EXIT_MODULE.name(), ButtonEnum.EXIT_MODULE.getButtonName());
            return messageService.getMessageWithButtons(text, chatId, buttonKeyboard.getMessageButtons());
        }
        buttonKeyboard.addMessageButton(max, ButtonEnum.UNDO_USERS.name(), ButtonEnum.UNDO_USERS.getButtonName());
        buttonKeyboard.addMessageButton(max, ButtonEnum.REDO_USERS.name(), ButtonEnum.REDO_USERS.getButtonName());
        buttonKeyboard.addMessageButton(max + 1, ButtonEnum.EXIT_MODULE.name(), ButtonEnum.EXIT_MODULE.getButtonName());
        return messageService.getMessageWithButtons(text, chatId, buttonKeyboard.getMessageButtons());
    }

    /**
     * Метод отправки анекдота пользователю.
     *
     * @param anecdote - объект анекдота
     * @param chatId   - id чата
     */
    private void sendAnecdoteToUser(Anecdote anecdote, Long chatId) {
        PartialBotApiMethod<Message> fileToSend = getFileToSendByAnecdote(anecdote, chatId);
        telegramBot.sendMessageToUser(fileToSend);
    }

    /**
     * Метод получения объекта PartialBotApiMethod<Message> при отправке анекдота пользователю.
     * В зависимости от того, какой тип файла пользователь отправил при создании анекдота, требуется использовать
     * различные объекты отправки сообщения, классы которых являются наследниками телеграмовского PartialBotApiMethod
     *
     * @param anecdote - объект анекдота
     * @param chatId   - id чата
     * @return SendMessage
     */
    private PartialBotApiMethod<Message> getFileToSendByAnecdote(Anecdote anecdote, Long chatId) {
        String fileId = anecdote.getFileId();
        ButtonKeyboard buttonKeyboard = new ButtonKeyboard();
        String caption = MessageFormat.format("Автор:{0}{1}Название:{2}", anecdote.getAuthor().getFullName(), "\n", anecdote.getName());
        switch (AnecdoteTypeEnum.valueOf(anecdote.getFileType())) {
            case VIDEO:
                SendVideo sendVideo = new SendVideo();
                sendVideo.setChatId(chatId);
                sendVideo.setVideo(new InputFile(fileId));
                sendVideo.setCaption(caption);
                if (anecdote.getAuthor().getId().equals(chatId)) {
                    String callBackData = MessageFormat.format("DELETE_ANECDOTE:{0}", anecdote.getId());
                    buttonKeyboard.addMessageButton(0, callBackData, ButtonEnum.DELETE_ANECDOTE.getButtonName());
                    sendVideo.setReplyMarkup(buttonKeyboard.getMessageButtons());
                }
                return sendVideo;
            case VOICE:
                SendVoice sendVoice = new SendVoice();
                sendVoice.setChatId(chatId);
                sendVoice.setVoice(new InputFile(fileId));
                sendVoice.setCaption(caption);
                if (anecdote.getAuthor().getId().equals(chatId)) {
                    String callBackData = MessageFormat.format("DELETE_ANECDOTE:{0}", anecdote.getId());
                    buttonKeyboard.addMessageButton(0, callBackData, ButtonEnum.DELETE_ANECDOTE.getButtonName());
                    sendVoice.setReplyMarkup(buttonKeyboard.getMessageButtons());
                }
                return sendVoice;
            case VIDEO_NOTE:
                SendVideoNote sendVideoNote = new SendVideoNote();
                sendVideoNote.setChatId(chatId);
                sendVideoNote.setVideoNote(new InputFile(fileId));
                if (anecdote.getAuthor().getId().equals(chatId)) {
                    String callBackData = MessageFormat.format("DELETE_ANECDOTE:{0}", anecdote.getId());
                    buttonKeyboard.addMessageButton(0, callBackData, ButtonEnum.DELETE_ANECDOTE.getButtonName());
                    sendVideoNote.setReplyMarkup(buttonKeyboard.getMessageButtons());
                }
                return sendVideoNote;
            case PHOTO:
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setChatId(chatId);
                sendPhoto.setPhoto(new InputFile(fileId));
                sendPhoto.setCaption(caption);
                if (anecdote.getAuthor().getId().equals(chatId)) {
                    String callBackData = MessageFormat.format("DELETE_ANECDOTE:{0}", anecdote.getId());
                    buttonKeyboard.addMessageButton(0, callBackData, ButtonEnum.DELETE_ANECDOTE.getButtonName());
                    sendPhoto.setReplyMarkup(buttonKeyboard.getMessageButtons());
                }
                return sendPhoto;
            default:
                return new SendMessage();
        }
    }

    /**
     * Сообщение при отсутствии анекдотов в системе
     *
     * @param chatId - id чата
     * @return SendMessage
     */
    private SendMessage noAnecdotes(Long chatId) {
        String text = MessageFormat.format("К сожалению в системе пока что нет ни одного анекдота. Станьте первым!{0}Желаете посмотреть еще что-нибудь?", "\n");
        return messageService.getMessageWithButtons(text, chatId, getModuleMainKeyboard());
    }

    /**
     * Сообщение при успешном создании анекдота
     *
     * @param chatId - id чата
     * @return SendMessage
     */
    private SendMessage successSaved(Long chatId) {
        String text = MessageFormat.format("Отлично, анекдот сохранен!{0}Желаете посмотреть еще что-нибудь?", "\n");
        return messageService.getMessageWithButtons(text, chatId, getModuleMainKeyboard());
    }

    /**
     * Сообщение для отмены создания анекдота
     *
     * @param chatId - id чата
     * @return SendMessage
     */
    private SendMessage doNotSaveAnecdote(Long chatId) {
        String text = MessageFormat.format("Хорошо, попробуем в следующий раз.{0}Желаете посмотреть еще что-нибудь?", "\n");
        return messageService.getMessageWithButtons(text, chatId, getModuleMainKeyboard());
    }

    /**
     * Сообщение для подтверждения создания анекдота
     *
     * @param chatId - id чата
     * @return SendMessage
     */
    private SendMessage preSaveAnecdote(Long chatId) {
        String text = MessageFormat.format("Забавная шутка, я бы тоже посмеялся, да конструкция не позволяет.{0}Могу я сохранить ваш анекдот?", "\n");
        ButtonKeyboard buttonKeyboard = new ButtonKeyboard();
        buttonKeyboard.addMessageButton(0, ButtonEnum.YES.name(), ButtonEnum.YES.getButtonName());
        buttonKeyboard.addMessageButton(0, ButtonEnum.NO.name(), ButtonEnum.NO.getButtonName());
        return messageService.getMessageWithButtons(text, chatId, buttonKeyboard.getMessageButtons());
    }

    /**
     * Сообщение перехода в другой модуль во время создания анекдота. Прерывание создания
     *
     * @param chatId - id чата
     * @return SendMessage
     */
    private SendMessage goingToQuit(Long chatId) {
        String text = "Вы хотите прервать создание анекдота? Если да, то нажмите кнопку выйти, если же нет, то продолжим.";
        ButtonKeyboard buttonKeyboard = new ButtonKeyboard();
        buttonKeyboard.addMessageButton(0, ButtonEnum.EXIT_MODULE.name(), ButtonEnum.EXIT_MODULE.getButtonName());
        return messageService.getMessageWithButtons(text, chatId, buttonKeyboard.getMessageButtons());
    }

    /**
     * Сообщение создания анекдота: отправка файла
     *
     * @param chatId - id чата
     * @return SendMessage
     */
    private SendMessage anecdoteNameSaved(Long chatId) {
        String text = "Отличное название анекдота! Теперь запишите видео-кружок или аудио, или же отправьте мне фото/видео.";
        return messageService.getMessage(text, chatId);
    }

    /**
     * Сообщение создания анекдота: введение названия
     *
     * @param chatId - id чата
     * @return SendMessage
     */
    private SendMessage createAnecdoteName(Long chatId) {
        String text = "Введите название анекдота.";
        return messageService.getMessage(text, chatId);
    }

    /**
     * Информационное сообщение при входе в модуль
     *
     * @param chatId - id чата
     * @return SendMessage
     */
    private SendMessage infoMessage(Long chatId) {
        String text = MessageFormat.format(
                ":clown_face: Вы находитесь в модуле Анекдотов!{0}" +
                        ":anger: Модуль предназначен для просмотра, а также записи анекдотов.{1}" +
                        ":anger: Вы можете записать свой анекдот в качестве фото-мема, видео, голосового сообщения или же в качестве видео-кружка, а я его запомню.{2}" +
                        ":anger: Ваши коллеги смогут насладиться вашими анекдотами в трудный час релиза.{3}" +
                        ":anger: Также и вы можете послушать анекдот вашего коллеги или же получить рандомную шутку.{4}" +
                        ":anger: Все необходимые кнопки представлены ниже. Наслаждайтесь! {5}",
                "\n", "\n", "\n", "\n", "\n", "\n");
        return messageService.getMessageWithButtons(text, chatId, getModuleMainKeyboard());
    }

    /**
     * Метод получения основных кнопок модуля Анекдотов
     *
     * @return InlineKeyboardMarkup - кнопки
     */
    private InlineKeyboardMarkup getModuleMainKeyboard() {
        ButtonKeyboard buttonKeyboard = new ButtonKeyboard();
        buttonKeyboard.addMessageButton(0, ButtonEnum.CREATE_ANECDOTE.name(), ButtonEnum.CREATE_ANECDOTE.getButtonName());
        buttonKeyboard.addMessageButton(1, ButtonEnum.MY_ANECDOTES.name(), ButtonEnum.MY_ANECDOTES.getButtonName());
        buttonKeyboard.addMessageButton(2, ButtonEnum.GET_RANDOM_ANECDOTE.name(), ButtonEnum.GET_RANDOM_ANECDOTE.getButtonName());
        buttonKeyboard.addMessageButton(3, ButtonEnum.GET_AUTHOR_ANECDOTES.name(), ButtonEnum.GET_AUTHOR_ANECDOTES.getButtonName());
        buttonKeyboard.addMessageButton(4, ButtonEnum.EXIT_MODULE.name(), ButtonEnum.EXIT_MODULE.getButtonName());
        return buttonKeyboard.getMessageButtons();
    }

}
