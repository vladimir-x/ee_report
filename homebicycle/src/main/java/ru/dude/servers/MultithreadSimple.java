package ru.dude.servers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MultithreadSimple {

    public static void main(String[] args) throws Throwable {

        ServerSocket ss = new ServerSocket(8080);
        while (true) {
            System.err.println("ready...");
            Socket s = ss.accept();
            System.err.println("Client accepted " + s);
            new Thread(new SocketProcessor(s)).start();

            System.err.println("end.");
        }
    }

    private static class SocketProcessor implements Runnable {

        private Socket s;
        private InputStream is;
        private OutputStream os;

        private SocketProcessor(Socket s) throws Throwable {
            this.s = s;
            this.is = s.getInputStream();
            this.os = s.getOutputStream();
        }

        public void run() {
            String reqIdent = "";
            try {
                String request = readInputHeaders();
                if (request.contains("favicon")){
                    reqIdent = "fav";
                    System.err.println(">fav");
                    writeResponse("<html/>");
                }else {
                    reqIdent = request.substring(0,10);
                    System.err.println(">req : " + reqIdent);
                    writeResponse("<html><body><h1>Hello from MultithreadSimple</h1></body></html>");
                }
            } catch (Throwable t) {
                /*do nothing*/
            } finally {
                try {
                    s.close();
                } catch (Throwable t) {
                    /*do nothing*/
                }
            }
            System.err.println("Client processing finished : " + reqIdent);
        }

        private void writeResponse(String s) throws Throwable {
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Server: MultithreadSimple\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Content-Length: " + s.length() + "\r\n" +
                    "Connection: close\r\n\r\n";


            String result = response + s;
            os.write(result.getBytes());
            os.flush();
        }

        private String readInputHeaders() throws Throwable {
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while(true) {
                String s = br.readLine();
                if(s == null || s.trim().length() == 0) {
                    break;
                }
                sb.append(s);
            }
            return sb.toString();
        }
    }
}
