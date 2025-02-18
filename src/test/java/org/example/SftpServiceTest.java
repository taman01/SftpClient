package org.example;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import org.example.sftpclient.model.SftpService;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class SftpServiceTest {

    private SftpService service;
    private FakeChannelSftp fakeSftp;

    @BeforeMethod
    public void setup() {
        service = new SftpService();
        fakeSftp = new FakeChannelSftp();
    }

    // Тесты для проверки корректности IP-адреса (Model)
    @Test
    public void testValidateIpValid() {
        Assert.assertTrue(service.validateIp("192.168.0.1"), "Корректный IP должен проходить валидацию");
        Assert.assertTrue(service.validateIp("127.0.0.1"), "Корректный IP должен проходить валидацию");
    }

    @Test
    public void testValidateIpInvalid() {
        Assert.assertFalse(service.validateIp("999.999.999.999"), "Некорректный IP должен не проходить валидацию");
        Assert.assertFalse(service.validateIp("abc.def.ghi.jkl"), "Некорректный IP должен не проходить валидацию");
    }

    // Тест загрузки данных, когда файл пустой
    @Test
    public void testLoadDataEmpty() throws JSchException {
        fakeSftp.setInputStream(new ByteArrayInputStream("".getBytes()));
        Properties properties = service.loadData(fakeSftp);
        Assert.assertTrue(properties.isEmpty(), "Properties должны быть пустыми при пустом входном потоке");
    }

    // Тест загрузки данных с корректным содержимым
    @Test
    public void testLoadDataWithContent() throws JSchException {
        // Формат properties: key=value\n
        String content = "example.com=192.168.0.1\n";
        fakeSftp.setInputStream(new ByteArrayInputStream(content.getBytes()));
        Properties properties = service.loadData(fakeSftp);
        Assert.assertEquals(properties.getProperty("example.com"), "192.168.0.1", "Данные не загружены корректно");
    }

    // Тест сохранения данных
    @Test
    public void testSaveData() throws JSchException {
        Properties properties = new Properties();
        properties.setProperty("test.com", "192.168.0.2");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        fakeSftp.setOutputStream(baos);
        service.saveData(fakeSftp, properties);

        String writtenData = baos.toString();
        Assert.assertTrue(writtenData.contains("test.com=192.168.0.2"), "Сохранённые данные не содержат ожидаемую запись");
    }

    // Fake-реализация ChannelSftp для тестирования
    private static class FakeChannelSftp extends ChannelSftp {
        private ByteArrayInputStream inputStream;
        private ByteArrayOutputStream outputStream;

        public void setInputStream(ByteArrayInputStream inputStream) {
            this.inputStream = inputStream;
        }

        public void setOutputStream(ByteArrayOutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        public InputStream get(String src) {
            return inputStream;
        }

        @Override
        public OutputStream put(String dst) {

            if (outputStream == null) {
                outputStream = new ByteArrayOutputStream();
            }
            return outputStream;
        }

        @Override
        public void disconnect() {
        }
    }
}
