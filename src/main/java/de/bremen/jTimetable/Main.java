package de.bremen.jTimetable ;

import java.net.URL;
import java.util.*;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import de.bremen.jTimetable.Classes.*;
import de.bremen.jTimetable.fxmlController.BackgroundController;
import javafx.stage.StageStyle;


public class Main extends Application {
    double x, y =0;
    public static void main(String[] args){
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
        URL url = Main.class.getResource("fxml/Background.fxml");
        loader.setLocation(url);
        AnchorPane anchorPane = loader.load();

        Scene scene = new Scene(anchorPane);
        primaryStage.getIcons().add(new Image("/de/bremen/jTimetable/img/icon.png"));
        primaryStage.setTitle("jTimetable");
        primaryStage.setWidth(1024);
        primaryStage.setHeight(768);
        primaryStage.setScene(scene);
        primaryStage.show();

        BackgroundController backgroundController = loader.getController();
        backgroundController.openHomeFXML();
    }

}

