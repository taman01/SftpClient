package org.example;


import com.jcraft.jsch.JSchException;
import org.example.sftpclient.controller.SftpController;
import org.example.sftpclient.model.SftpService;
import org.example.sftpclient.view.ConsoleView;

public class Main
{
    public static void main( String[] args ) throws JSchException {
        ConsoleView view = new ConsoleView();
        SftpService service = new SftpService();
        SftpController controller = new SftpController(view, service);
        controller.start();
    }
}
