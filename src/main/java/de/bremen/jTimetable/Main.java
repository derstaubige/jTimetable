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


public class Main extends Application {
    public static void main(String[] args){

        // https://jenkov.com/tutorials/javafx/your-first-javafx-application.html
        try( SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();){
            sqlConnectionManager.Migrate();
        }catch (Exception e){
            System.out.println(e);
        } 

            Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader();
        loader.setResources(ResourceBundle.getBundle("Resources"));
        URL url = Main.class.getResource("fxml/Home.fxml");
        loader.setLocation(url);
        AnchorPane anchorPane = loader.<AnchorPane>load();

        Scene scene = new Scene(anchorPane);
        primaryStage.getIcons().add(new Image("/de/bremen/jTimetable/img/icon.png"));
        primaryStage.setTitle("jTimetable");
        primaryStage.setWidth(1024);
        primaryStage.setHeight(768);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


}