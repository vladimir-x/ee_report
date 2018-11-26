package ru.dude.servers;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ServerJdbcSimple {

    public static void main(String[] args) throws Throwable {

        loadPsqlDB("localhost:5432","postgres","postgres","postgres");
        loadApps();

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
                if (request.contains("favicon")) {
                    reqIdent = "fav";
                    System.err.println(">fav");
                    writeResponse(request, "<html/>");
                } else {
                    reqIdent = request.substring(0, 10);
                    System.err.println(">req : " + reqIdent);
                    writeResponse(request, null);
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

        private void writeResponse(String request, String respInner) throws Throwable {

            String resp = respInner != null ? respInner : getAppResponse(request);


            String serverHead = "HTTP/1.1 200 OK\r\n" +
                                "Server: ServerJarSimple\r\n" +
                                "Content-Type: text/html\r\n" +
                                "Content-Length: " + resp.length() + "\r\n" +
                                "Connection: close\r\n\r\n";


            String result = serverHead + resp;
            os.write(result.getBytes());
            os.flush();
        }

        private String readInputHeaders() throws Throwable {
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while (true) {
                String s = br.readLine();
                if (s == null || s.trim().length() == 0) {
                    break;
                }
                sb.append(s);
                //System.err.println(">"+s);
            }
            return sb.toString();
        }
    }

    private static final String WEB_APP = "webapp";
    private static final Map<String, Map<String, Servletus>> apps = new HashMap<>();

    private static void loadApps() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {

        apps.clear();

        File appdir = new File(WEB_APP);
        if (appdir.exists()) {
            for (File appJar : appdir.listFiles()) {
                if (appJar.getName().endsWith(".jar")) {

                    Map<String, Servletus> appServletus = new HashMap<>();

                    URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{appJar.toURI().toURL()}, Servletus.class.getClassLoader());

                    Enumeration<JarEntry> entries = new JarFile(appJar).entries();
                    String appName = appJar.getName().substring(0, appJar.getName().length() - ".jar".length());

                    while (entries.hasMoreElements()) {
                        JarEntry je = entries.nextElement();
                        String name = je.getName();
                        if (name.endsWith(".class")) {
                            String className = name.substring(0, name.length() - ".class".length()).replaceAll("/", ".");
                            Class<?> clazz = urlClassLoader.loadClass(className);

                            if (Servletus.class.isAssignableFrom(clazz)) {
                                System.out.println("loaded: /" + appName + "/" + clazz.getSimpleName());
                                Servletus impl = (Servletus) clazz.newInstance();
                                impl.init();
                                appServletus.put(clazz.getSimpleName(), impl);
                            }
                        }
                    }
                    apps.put(appName, appServletus);
                }
            }

        }
    }

    private static String getAppResponse(String request) {
        String[] split = request.split("\\s", 3);
        String method = split[0];
        String url = split[1];
        String other = split[2];

        String[] upart = url.split("/", 4);
        String first = upart.length > 0 ? upart[0] : null;
        String appName = upart.length > 1 ? upart[1] : null;
        String servletusName = upart.length > 2 ? upart[2] : null;
        String uother = upart.length > 3 ? upart[3] : null;

        if (apps.containsKey(appName) && apps.get(appName).containsKey(servletusName)) {
            //call app
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            apps.get(appName).get(servletusName).service(request, baos);
            return new String(baos.toByteArray(), Charset.defaultCharset());
        } else {
            // not found
            return "<html><body><h1> 403 ! Not found : appName = " + appName + "; servletusName = " + servletusName + ";</h1></body></html>";
        }
    }

    private static void loadPsqlDB(String hostPort,String dbName,String user,String passwd) throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found. Include it in your library path ");
            throw new RuntimeException(e);
        }

        String dbUrl = "jdbc:postgresql://"+hostPort+"/"+dbName;

        ServletusContext.addConnection("examp-connect", DriverManager.getConnection(dbUrl,user,passwd));
        System.out.println("loaded " +dbUrl);
    }

}
