package bot;

import commands.AppBotCommand;
import commands.BotCommonCommands;
import functions.FilterOperation;
import functions.ImageOperation;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import utils.ImageUtils;
import utils.PhotoMessageUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Bot extends TelegramLongPollingBot {

    HashMap<String, Message> messages = new HashMap<>();
    List<String> users = new ArrayList<>();

    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        saveNewUser(update);
        try {
            SendMessage responseTextMessage = runCommonCommand(message);
            if (responseTextMessage != null) {
                execute(responseTextMessage);
                return;
            }

            responseTextMessage = runPhotoMessage(message);
            if (responseTextMessage != null) {
                execute(responseTextMessage);
                return;
            }

            Object sendFilteredPhoto = runPhotoFilter(message);
            if (sendFilteredPhoto != null) {
                if (sendFilteredPhoto instanceof SendPhoto) {
                    execute((SendPhoto) sendFilteredPhoto);
                } else {
                    execute((SendMessage) sendFilteredPhoto);
                }

            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private SendMessage runCommonCommand(Message message) throws InvocationTargetException, IllegalAccessException {
        String text = message.getText();
        BotCommonCommands commands = new BotCommonCommands();
        Method[] methods = commands.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(AppBotCommand.class)) {
                AppBotCommand command = method.getAnnotation(AppBotCommand.class);
                if (command.name().equals(text)) {
                    method.setAccessible(true);
                    String responseText = (String) method.invoke(commands);
                    if (responseText != null) {
                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(message.getChatId().toString());
                        sendMessage.setText(responseText);
                        return sendMessage;
                    }
                }
            }
        }
        return null;
    }

    private SendMessage runPhotoMessage(Message message) {
        if (!message.hasPhoto()) {
            return null;
        }
        String chatId = message.getChatId().toString();
        messages.put(message.getChatId().toString(), message);
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        ArrayList<KeyboardRow> allKeyboardRows = new ArrayList<>(getKeyboardsRows(FilterOperation.class));
        keyboard.setKeyboard(allKeyboardRows);
        keyboard.setOneTimeKeyboard(true);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyMarkup(keyboard);
        sendMessage.setChatId(chatId);
        sendMessage.setText("Выберете фильтр");
        return sendMessage;
    }

    private Object runPhotoFilter(Message message) throws Exception {
        ImageOperation operation = ImageUtils.getOperation(message.getText());
        if (operation == null) {
            return null;
        }
        String chatId = message.getChatId().toString();
        message = messages.get(chatId);
        if (messages != null) {
            String photoPath = getAndSavePhoto(message);
            return preparePhoto(message, photoPath, operation);
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText("Отправьте фото, чтобы воспользоваться фильтром");
            return sendMessage;
        }
    }

    private String getAndSavePhoto(Message message) throws TelegramApiException {
        PhotoSize photo = message.getPhoto().get(2);
        GetFile getFile = new GetFile(photo.getFileId());
        File file = execute(getFile);
        String path = photo.getFileId() + ".jpeg";
        downloadFile(file, new java.io.File(path));
        return path;
    }

    private SendPhoto preparePhoto(Message message, String path, ImageOperation operation) throws Exception {
        PhotoMessageUtils.processingImage(path, operation);
        InputFile photo = new InputFile(new java.io.File(path));
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(message.getChatId().toString());
        sendPhoto.setPhoto(photo);
        sendPhoto.setCaption("Edited image");
        return sendPhoto;
    }

    private ArrayList<KeyboardRow> getKeyboardsRows(Class someClass) {
        Method[] methods = someClass.getMethods();
        ArrayList<AppBotCommand> commands = new ArrayList<>();
        for (Method method : methods) {
            if (method.isAnnotationPresent(AppBotCommand.class)) {
                commands.add(method.getAnnotation(AppBotCommand.class));
            }
        }
        ArrayList<KeyboardRow> keyboardRows = new ArrayList<>();
        int buttonsCount = 0;
        for (int i = 0; i < 3; i++) {
            KeyboardRow row = new KeyboardRow();
            for (int j = 0; j < 3; j++) {
                if (buttonsCount < commands.size()) {
                    KeyboardButton button = new KeyboardButton(commands.get(buttonsCount).name());
                    row.add(button);
                    buttonsCount++;
                }
            }
            keyboardRows.add(row);
        }
        return keyboardRows;
    }

    public void saveNewUser(Update update) {
        User user = update.getMessage().getFrom();
        String userName = user.getUserName();
        if (!users.contains(userName)) {
            users.add(userName);
        }
    }

    public List<String> getUsers() {
        return users;
    }

    public String getBotUsername() {
        return "stringerDD_bot";
    }

    @Override
    public String getBotToken() {
        return "6015669949:AAFwT95h2JkKYdw8aiont-GEmrikz6M3mas";
    }
}
