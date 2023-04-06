import bot.Bot;
import gui.AdminPanel;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        BotSession botSession;
        Bot bot;
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            bot = new Bot();
            botSession = api.registerBot(bot);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

        SwingUtilities.invokeLater(() -> {
            AdminPanel adminPanel = new AdminPanel(botSession, bot);
        });
    }
}
