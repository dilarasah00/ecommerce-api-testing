package com.company.project.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigurationReader {

    private static final Properties properties = new Properties();

    static {
        try (InputStream is = ConfigurationReader.class.getClassLoader().getResourceAsStream("configuration.properties")) {
            if (is == null) {
                throw new RuntimeException("configuration.properties not found in classpath");
            }
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration.properties", e);
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
