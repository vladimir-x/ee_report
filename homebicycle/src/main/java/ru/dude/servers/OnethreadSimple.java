package ru.dude.servers;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class OnethreadSimple {


    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(8080);
        while (true) {
            Socket s = ss.accept();
            System.err.println("Client accepted");
            s.getOutputStream().write("<html/>".getBytes());
            s.getOutputStream().flush();
            //s.getChannel().

           // s.
           // new Thread(new SocketProcessor(s)).start();
        }
    }
}
