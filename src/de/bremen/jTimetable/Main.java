package de.bremen.jTimetable ;

import de.bremen.jTimetable.Classes.*;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueInt;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueString;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class Main extends Application {
    public static void main(String[] args){
//        try {
            //CourseofStudy cos = new CourseofStudy(8);
            //System.out.println(cos);

            //Coursepass cp = new Coursepass(102);

//            Room room = new Room(1L);
//            room.roomcaption = "Raum 321";
//            room.save();

//            Location location = new Location(0L);
//            location.caption = "Haus des Reichs";
//            location.active = Boolean.FALSE;
//            location.save();

//            Subject subject = new Subject(1L);
//            subject.caption = "Abgabenordnung";
//            subject.save();

//            StudySection studySection = new StudySection(3L);
//            studySection.description = "Grundstudium 1";
//            studySection.save();

//            Lecturer lecturer = new Lecturer(1L);
//            lecturer.firstname = "Bne";
//            lecturer.lastname = "Dborra";
//            lecturer.save();

//            CoursepassLecturerSubject coursepassLecturerSubject = new CoursepassLecturerSubject(1L);
//            coursepassLecturerSubject.shouldhours = 20L;
//            coursepassLecturerSubject.save();

//            CourseofStudy courseofStudy = new CourseofStudy(0L);
//            courseofStudy.begin = LocalDate.of(2022,9,8);
//            courseofStudy.end = LocalDate.of(2022,9,30);
//            courseofStudy.caption = "hallo";
//            courseofStudy.save();

            //Coursepass coursepass = new Coursepass(134L);
//            coursepass.description = "Buja";
//            coursepass.getCoursepassLecturerSubjects();
//            for(CoursepassLecturerSubject str: coursepass.arraycoursepasslecturersubject){
//                System.out.println(str.shouldhours);
//            }
//            Collections.sort(coursepass.arraycoursepasslecturersubject);
//            for(CoursepassLecturerSubject str: coursepass.arraycoursepasslecturersubject){
//                System.out.println(str.shouldhours);
//            }
//            coursepass.start = LocalDate.of(2022,6,8);
//            coursepass.end = LocalDate.of(2022,9,30);
//            coursepass.save();

//            CoursepassLecturerSubject c1 = new CoursepassLecturerSubject(1L);
//            CoursepassLecturerSubject c2 = new CoursepassLecturerSubject(2L);



//            TimetableDay timetableDay = new TimetableDay(LocalDate.now(), 3);
//            System.out.println(timetableDay.arrayTimetableDay.get(0).isEmpty());
//            timetableDay.addToSlot(0, new CoursepassLecturerSubject(0L));
//            System.out.println(timetableDay.arrayTimetableDay.get(0).isEmpty());

            //truncate resourcesblocked and t_timetables


//            SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
//            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();
//            sqlConnectionManager.execute("TRUNCATE table T_resourcesblocked",SQLValues);
//            sqlConnectionManager.execute("TRUNCATE table T_timetables",SQLValues);
//
//            Coursepass coursepass1 = new Coursepass(134L);
//            Coursepass coursepass2 = new Coursepass(135L);
//
//            Resourcemanager resourcemanager = new Resourcemanager();
//            resourcemanager.setResourcesBlocked(1L,"Lecturer","Urlaub",LocalDate.of(2022,9,5), LocalDate.of(2022,9,7),0,99);
//            resourcemanager.setResourcesBlocked(2L,"Lecturer","Urlaub",LocalDate.of(2022,9,6), LocalDate.of(2022,9,6),0,99);
//            resourcemanager.generateInitialTimetable(coursepass1);
//            resourcemanager.generateInitialTimetable(coursepass2);
//
//            System.out.println("hi");

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