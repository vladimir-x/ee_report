package ru.dude.servers;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by vladimirx
 * Date: 24.10.18
 */
public class ServJdbcExample implements Servletus {
    @Override
    public void init() {
        System.out.println("init ServJdbcExample !");
    }

    @Override
    public void service(String request, OutputStream outputStream) {

        try {
            outputStream.write(new String("Working ServJdbcExample ...").getBytes());

            String connectName = "examp-connect";

            Connection c = ServletusContext.getConnection(connectName);
            if (c != null) {
                try {

                    Statement st = c.createStatement();
                    boolean r = st.execute("select 1");
                    st.close();
                    outputStream.write(new String("Using: " + connectName + " " + r).getBytes());

                } catch (SQLException e) {
                    outputStream.write(new String("Error: " + connectName + ": " + e.getMessage()).getBytes());
                    e.printStackTrace();
                }
            } else {
                outputStream.write(new String("Error: " + connectName + " not found").getBytes());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
