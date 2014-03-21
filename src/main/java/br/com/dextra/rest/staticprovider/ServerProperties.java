package br.com.dextra.rest.staticprovider;

import java.io.IOException;
import java.util.Properties;

public class ServerProperties {

    private static final String NAME = "webapp.properties";
    private static final ServerProperties SINGLETON = new ServerProperties();

    private final Properties properties;

    private ServerProperties() {
        this.properties = new Properties();
        try {
            this.properties.load(this.getClass().getClassLoader().getResourceAsStream(NAME));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String property(String name) {
        return SINGLETON.properties.getProperty(name);
    }

}
