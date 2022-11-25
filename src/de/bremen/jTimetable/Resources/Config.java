package de.bremen.jTimetable.Resources;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    String path;

    public Config() {
        //the base folder is ./, the root of the main.properties file
        this.path = "./config.properties";
    }

    private Properties readFile() {
        //to load application's properties, we use this class
        Properties mainProperties = new Properties();

        FileInputStream file;
        try {
            //load the file handle for main.properties
            file = new FileInputStream(this.path);

            //load all the properties from this file
            mainProperties.load(file);

            //we have loaded the properties, so close the file handle
            file.close();
        }catch(Exception e){
            e.printStackTrace();
        }

        return mainProperties;
    }

    public String getLocalLang()  {

        String string = null;
        Properties mainProperties = this.readFile();

        //retrieve the property we are intrested
        string = mainProperties.getProperty("local.lang");

        return string;
    }

    public String getLocaCountry() {

        String string = null;
        Properties mainProperties = this.readFile();

        //retrieve the property we are intrested
        string = mainProperties.getProperty("local.country");

        return string;
    }
}
