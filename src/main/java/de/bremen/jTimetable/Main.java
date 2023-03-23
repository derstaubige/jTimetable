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
import javafx.stage.StageStyle;


public class Main extends Application {
    double x, y =0;
    public static void main(String[] args){

        // https://jenkov.com/tutorials/javafx/your-first-javafx-application.html
        try{
            SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
            sqlConnectionManager.Migrate();
        }catch (Exception e){
            System.out.println(e);
        }

            Application.launch(args);
//            CourseofStudy c1 = new CourseofStudy(0L);
//            ArrayList alc1 = c1.getCoursesofStudy();
//            System.out.println("JO");



//         } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
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

//
//        Parent root = FXMLLoader.load(getClass().getResource("fxml/Background.fxml"));
//        primaryStage.initStyle(StageStyle.UNDECORATED);
//
//
//        root.setOnMousePressed(event -> {
//            x = event.getSceneX();
//            y = event.getSceneY();
//        });
//
//        root.setOnMouseDragged(event -> {
//            primaryStage.setX(event.getScreenX() - x);
//            primaryStage.setY(event.getScreenY() - y);
//        });
//
//        primaryStage.setScene(new Scene(root, 700, 400));
//        primaryStage.show();
    }

}

