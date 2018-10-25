package ru.dude.servers;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by vladimirx
 * Date: 25.10.18
 */
public class ServletusContext {

    private static Map<String, Object> resourceMap = new ConcurrentHashMap<>();

    public static Connection getConnection(String name) {
        if (resourceMap.containsKey(name)) {
            if (resourceMap.get(name) instanceof Connection) {
                return (Connection) resourceMap.get(name);
            }
        }
        return null;
    }


    public static void addConnection(String name, Connection connection) {
        resourceMap.put(name, connection);
    }


    public static Object getResource(String name) {
        if (resourceMap.containsKey(name)) {
            return (Connection) resourceMap.get(name);
        }
        return null;
    }


    public static void addResource(String name, Object obj) {
        resourceMap.put(name, obj);
    }
}
