package ru.dude.servers;

import java.net.ServerSocket;
import java.net.Socket;

public class OnethreadSimple {

    public static void main(String[] args) throws Exception {
        ServerSocket ss = new ServerSocket(8080);
        while (true) {
            Socket s = ss.accept();
            System.err.println("Client accepted");
            String head = "HTTP/1.1 200 OK\n Connection: close\n\n";
            String resp = "<html><body><h1>OnethreadSimple hello</h1></body></html>";
            s.getOutputStream().write((head + resp).getBytes());
            s.close();
        }
    }
}
