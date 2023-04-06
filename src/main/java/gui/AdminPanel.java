package gui;

import bot.Bot;
import org.telegram.telegrambots.meta.generics.BotSession;

import javax.swing.*;
import java.util.List;

public class AdminPanel extends JFrame {
    private JPanel mainPanel;
    private JButton startButton, stopButton, updateButton;
    private JScrollPane scrollPanel;
    private JList<String> list1;
    private DefaultListModel<String> userListModel;
    private BotSession botSession;
    private Bot bot;

    public AdminPanel(BotSession botSession, Bot bot) {
        super("Admin Panel");

        this.botSession = botSession;
        this.bot = bot;

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        startButton = new JButton("Start");
        stopButton = new JButton("Stop");
        updateButton = new JButton("Update users");

        createUIComponents();

        startButton.setBounds(10, 10, 200, 25);
        stopButton.setBounds(10, 10, 200, 25);
        updateButton.setBounds(10, 10, 200, 25);
        scrollPanel.setBounds(10, 40, 380, 210);

        mainPanel.add(startButton);
        mainPanel.add(stopButton);
        mainPanel.add(updateButton);

        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(scrollPanel);

        setContentPane(mainPanel);

        startButton.addActionListener(e -> start(botSession));
        stopButton.addActionListener(e -> stop(botSession));
        updateButton.addActionListener(e -> updateUsers());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setVisible(true);

        updateUsers();
    }

    private void updateUsers() {
        List<String> users = bot.getUsers();
//        if (users.isEmpty()) {
//            users.add("blank");
//        }
        userListModel.clear();
        for (String user : users) {
            userListModel.addElement(user);
        }
    }

    public static void start(BotSession botSession) {
        if (!botSession.isRunning()) {
            botSession.start();
            System.out.println("Бот запущен");
        } else {
            System.out.println("Бот уже запущен");
        }
    }

    public static void stop(BotSession botSession) {
        if (botSession.isRunning()) {
            botSession.stop();
            System.out.println("Бот остановлен");
        }
    }

    private void createUIComponents() {
        userListModel = new DefaultListModel<>();
        list1 = new JList<>(userListModel);
        scrollPanel = new JScrollPane(list1);
    }
}
