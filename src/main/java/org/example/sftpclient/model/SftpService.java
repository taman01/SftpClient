package org.example.sftpclient.model;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;


import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Properties;

public class SftpService {

    // Используем файл формата properties для хранения данных
    private static final String REMOTE_FILE = "domains.properties";

    //Подключение к SFTP - серверу
    public ChannelSftp connect(String host, int port, String username, String password) throws JSchException {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            return channel;
        } catch (JSchException e) {
            System.out.println("Ошибка подключения" + e.getMessage());
            return null;
        }
    }

    // Загрузка данных из файла domains.properties
    public Properties loadData(ChannelSftp channel) throws JSchException {
        Properties properties = new Properties();
        try (InputStream stream = channel.get(REMOTE_FILE)) {
            properties.load(stream);
        } catch (Exception e) {
            System.out.println("Ошибка загрузки данных" + e.getMessage());
        }
        return properties;
    }

    //Сохранение данных в файл domains.properties
    public void saveData(ChannelSftp channel, Properties properties) throws JSchException {
        try (OutputStream stream = channel.put(REMOTE_FILE)) {
            properties.store(stream, "Domain-IP pairs");
        } catch (Exception e) {
            System.out.println("Ошибка сохранения данных" + e.getMessage());
        }
    }

    //Проверка корректности IP-адреса
    public boolean validateIp(String ip) {
        try {
            InetAddress.getByName(ip);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
