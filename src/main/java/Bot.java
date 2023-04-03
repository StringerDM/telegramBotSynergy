import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import utils.PhotoMessageUtils;

import java.util.ArrayList;

public class Bot extends TelegramLongPollingBot {

    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        System.out.println(message.getText());
        try {
            if (message.hasText()) {
                sendAnswer(update, "Your Message: " + message.getText());
                if (message.getText().contains("/set_filter")) {
                    PhotoMessageUtils.setOperation(message.getText());
                }
            }
            if (message.hasPhoto()) {
                String path = getAndSavePhoto(update);
                PhotoMessageUtils.processingImage(path);
                sendBackPhoto(update, path);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendAnswer(Update update, String message) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId().toString());
        sendMessage.setText(message);
        execute(sendMessage);
    }

    private String getAndSavePhoto(Update update) throws TelegramApiException {
        PhotoSize photo = update.getMessage().getPhoto().get(2);
        GetFile getFile = new GetFile(photo.getFileId());
        File file = execute(getFile);
        String path = photo.getFileId() + ".jpeg";
        downloadFile(file, new java.io.File(path));
        return path;
    }

    private void sendBackPhoto(Update update, String path) throws TelegramApiException {
        InputFile photo = new InputFile(new java.io.File(path));
        SendPhoto sendPhoto = new SendPhoto();

        sendPhoto.setReplyMarkup(getKeyboard("greyScale", "onlyRed", "onlyGreen", "onlyBlue", "sepia"));

        sendPhoto.setChatId(update.getMessage().getChatId().toString());
        sendPhoto.setPhoto(photo);
        sendPhoto.setCaption("Edited image");
        execute(sendPhoto);
    }

    private ReplyKeyboardMarkup getKeyboard(String... buttonNames) {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        ArrayList<KeyboardRow> keyboardRows = new ArrayList<>();
        int buttonsCount = 0;
        for (int i = 0; i < 3; i++) {
            KeyboardRow row = new KeyboardRow();
            for (int j = 0; j < 3; j++) {
                if (buttonsCount > buttonNames.length) {
                    break;
                }
                KeyboardButton button = new KeyboardButton("/set_filter " + buttonNames[i]);
                row.add(button);
                buttonsCount++;
            }
            keyboardRows.add(row);
        }
        keyboard.setKeyboard(keyboardRows);
        keyboard.setOneTimeKeyboard(true);
        return keyboard;
    }

    public String getBotUsername() {
        return "stringerDD_bot";
    }

    @Override
    public String getBotToken() {
        return "6015669949:AAFwT95h2JkKYdw8aiont-GEmrikz6M3mas";
    }
}
