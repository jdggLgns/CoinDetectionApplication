package com.example.myapplication;

import java.util.Properties;

public class ProjectProperties {
    public static Properties getProperties() {
        Properties properties = new Properties();
        properties.setProperty("ip_services", "3.249.85.29:5003");
        return properties;
    }
    public static Properties getPropertiesServerYolo() {
        Properties properties = new Properties();
        properties.setProperty("ip_services_Yolo", "3.249.85.29:5003");
        return properties;
    }
}
