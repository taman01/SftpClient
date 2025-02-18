package org.example.sftpclient.controller;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import org.example.sftpclient.model.SftpService;
import org.example.sftpclient.view.ConsoleView;

import java.util.Properties;
import java.util.Map;
import java.util.TreeMap;

public class SftpController {
    private final ConsoleView view;
    private final SftpService service;

    public SftpController(ConsoleView view, SftpService service) {
        this.view = view;
        this.service = service;
    }

    public void start() throws JSchException {
        // Получение параметров подключения
        String host = view.getInput("Введите адрес SFTP-сервера: ");
        int port = Integer.parseInt(view.getInput("Введите порт: "));
        String username = view.getInput("Введите логин: ");
        String password = view.getInput("Введите пароль: ");


        // Подключение к серверу
        ChannelSftp channel = service.connect(host, port, username, password);
        if (channel == null) {
            view.showMessage("Ошибка подключения. Завершение работы.");
            return;
        }

        // Загрузка данных из файла
        Properties properties = service.loadData(channel);
        boolean running = true;
        while (running) {


            view.showMessage("\nМеню:\n"
                    + "1. Получить список доменов и IP\n"
                    + "2. Найти IP по домену\n"
                    + "3. Найти домен по IP\n"
                    + "4. Добавить новую запись\n"
                    + "5. Удалить запись\n"
                    + "6. Выход");
            String choice = view.getInput("Выберите действие: ");

            switch (choice) {
                case "1":
                    // Сортировка доменов по алфавиту с использованием TreeMap
                    Map<String, String> sorted = new TreeMap<>();
                    for (String key : properties.stringPropertyNames()) {
                        sorted.put(key, properties.getProperty(key));
                    }
                    StringBuilder sb = new StringBuilder();
                    for (Map.Entry<String, String> entry : sorted.entrySet()) {
                        sb.append(entry.getKey())
                                .append(" -> ")
                                .append(entry.getValue())
                                .append("\n");
                    }
                    view.showMessage(sb.toString());
                    break;
                case "2":
                    String domain = view.getInput("Введите домен: ");
                    String ip = properties.getProperty(domain);
                    if (ip == null) {
                        view.showMessage("Домен не найден");
                    } else {
                        view.showMessage(ip);
                    }
                    break;
                case "3":
                    String searchIp = view.getInput("Введите IP-адрес: ");
                    String foundDomain = null;
                    for (String key : properties.stringPropertyNames()) {
                        if (properties.getProperty(key).equals(searchIp)) {
                            foundDomain = key;
                            break;
                        }
                    }
                    if (foundDomain == null) {
                        view.showMessage("IP не найден");
                    } else {
                        view.showMessage(foundDomain);
                    }
                    break;
                case "4":
                    String newDomain = view.getInput("Введите новый домен: ");
                    String newIp = view.getInput("Введите IP-адрес: ");
                    if (properties.containsKey(newDomain) || properties.containsValue(newIp)) {
                        view.showMessage("Ошибка: Домен или IP уже существуют.");
                    } else if (!service.validateIp(newIp)) {
                        view.showMessage("Ошибка: Некорректный IP-адрес.");
                    } else {
                        properties.setProperty(newDomain, newIp);
                        service.saveData(channel, properties);
                        view.showMessage("Данные сохранены.");
                    }
                    break;
                case "5":
                    String key = view.getInput("Введите домен или IP для удаления: ");
                    if (properties.containsKey(key)) {
                        properties.remove(key);
                    } else {
                        String domainToRemove = null;
                        for (String k : properties.stringPropertyNames()) {
                            if (properties.getProperty(k).equals(key)) {
                                domainToRemove = k;
                                break;
                            }
                        }
                        if (domainToRemove != null) {
                            properties.remove(domainToRemove);
                        } else {
                            view.showMessage("Запись не найдена.");
                            break;
                        }
                    }
                    service.saveData(channel, properties);
                    view.showMessage("Запись удалена.");
                    break;
                case "6":
                    view.showMessage("Завершение работы.");
                    running = false;
                    break;
                default:
                    view.showMessage("Некорректный ввод. Попробуйте снова.");
            }
        }
        channel.disconnect();
        view.close();
    }
}


