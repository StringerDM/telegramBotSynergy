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

public class Bot extends TelegramLongPollingBot {

    Class[] commandClasses = new Class[]{BotCommonCommands.class};

    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();

        try {
            SendMessage responseTextMessage = runCommonCommand(message);
            if (responseTextMessage != null) {
                execute(responseTextMessage);
                return;
            }

            SendPhoto sendFilteredPhoto = runPhotoFilter(message);
            if (sendFilteredPhoto != null) {
                execute(sendFilteredPhoto);
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

    private SendPhoto runPhotoFilter(Message message) throws Exception {
        ImageOperation operation = ImageUtils.getOperation(message.getCaption());
        if (operation == null) {
            return null;
        }
        String photoPath = getAndSavePhoto(message);
        return preparePhoto(message, photoPath, operation);
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

//        sendPhoto.setReplyMarkup(getKeyboard());

        sendPhoto.setChatId(message.getChatId().toString());
        sendPhoto.setPhoto(photo);
        sendPhoto.setCaption("Edited image");
        return sendPhoto;
    }

    private ReplyKeyboardMarkup getKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        ArrayList<KeyboardRow> allKeyboardRows = new ArrayList<>();
        allKeyboardRows.addAll(getKeyboardsRows(BotCommonCommands.class));
        allKeyboardRows.addAll(getKeyboardsRows(FilterOperation.class));
        keyboard.setKeyboard(allKeyboardRows);
        keyboard.setOneTimeKeyboard(true);
        return keyboard;
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

    public String getBotUsername() {
        return "stringerDD_bot";
    }

    @Override
    public String getBotToken() {
        return "6015669949:AAFwT95h2JkKYdw8aiont-GEmrikz6M3mas";
    }
}
