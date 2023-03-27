import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Bot extends TelegramLongPollingBot {

    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        System.out.println(message.getText());

        try {
            //1. Продолжая работу над ботом из предыдущих занятий, добавьте боту возможность отправлять сообщения
            sendAnswer(update, "Your Message: " + message.getText());
            //2. Реализуйте возможность получения изображения от пользователя, а также его отправку пользователю обратно
            String path = getAndSavePhoto(update);
            sendBackPhoto(update, path);
        } catch (TelegramApiException e) {
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
        PhotoSize photo = update.getMessage().getPhoto().get(0);
        GetFile getFile = new GetFile(photo.getFileId());
        File file = execute(getFile);
        String path = photo.getFileId() + ".jpeg";
        downloadFile(file, new java.io.File(path));
        return path;
    }

    private void sendBackPhoto(Update update, String path) throws TelegramApiException {
        InputFile photo = new InputFile(new java.io.File(path));
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(update.getMessage().getChatId().toString());
        sendPhoto.setPhoto(photo);
        execute(sendPhoto);
    }

    public String getBotUsername() {
        return "stringerDD_bot";
    }

    @Override
    public String getBotToken() {
        return "6015669949:AAFwT95h2JkKYdw8aiont-GEmrikz6M3mas";
    }
}
