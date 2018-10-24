package ru.dude.servers;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by vladimirx
 * Date: 24.10.18
 */
public class ServExample implements Servletus {
    @Override
    public void init() {
        System.out.println("init !");
    }

    @Override
    public void service(String request, OutputStream outputStream) {

        System.out.println("income request:" + request);

        try {
            outputStream.write(new String("Hello i'm alive!!!").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
