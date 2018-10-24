package ru.dude.servers;

import java.io.OutputStream;

/**
 * Created by vladimirx
 * Date: 24.10.18
 */
public interface Servletus {

    void init();

    void service(String request,OutputStream os);
}
