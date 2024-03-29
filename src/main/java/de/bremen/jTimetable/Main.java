package de.bremen.jTimetable ;

import java.net.URL;
import java.util.*;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import de.bremen.jTimetable.Classes.*;
import de.bremen.jTimetable.fxmlController.HomeController;


public class Main extends Application {
    private SQLConnectionManager sqlConnectionManager;
    public static void main(String[] args){

        // https://jenkov.com/tutorials/javafx/your-first-javafx-application.html


            Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        sqlConnectionManager = new SQLConnectionManager();
        sqlConnectionManager.Migrate();

        FXMLLoader loader = new FXMLLoader();
        loader.setResources(ResourceBundle.getBundle("Resources"));
        URL url = Main.class.getResource("fxml/Home.fxml");
        loader.setLocation(url);
        AnchorPane anchorPane = loader.<AnchorPane>load();

        HomeController homeController = loader.getController();
        homeController.setSqlConnectionManager(sqlConnectionManager);
        
        Scene scene = new Scene(anchorPane);
        primaryStage.getIcons().add(new Image("/de/bremen/jTimetable/img/icon.png"));
        primaryStage.setTitle("jTimetable");
        primaryStage.setWidth(1024);
        primaryStage.setHeight(768);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


}