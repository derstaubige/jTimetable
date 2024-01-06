package de.bremen.jTimetable.fxmlController;

import de.bremen.jTimetable.Classes.CoursePass;
import de.bremen.jTimetable.Main;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TableView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class BackgroundController implements Initializable {
//https://stackoverflow.com/a/61531318
    ResourceBundle resourceBundle;
    URL location;

    /**
     * FXML Elements:
     */
    @FXML
    private Label menu;
    @FXML
    private Label menuClose;
    @FXML
    private AnchorPane slider;
    @FXML
    public BorderPane brdrPnAll;
    @FXML
    public Button btnLecturer;
    @FXML
    public AnchorPane childContainer;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resourceBundle = resources;
        this.location = location;

        //set methods to open and close side menu
        menuClose.setVisible(false);
        slider.setTranslateX(-176);
        menu.setOnMouseClicked(event -> {
            TranslateTransition slide = new TranslateTransition();
            slide.setDuration(Duration.seconds(0.4));
            slide.setNode(slider);

            slide.setToX(0);
            slide.play();

            slider.setTranslateX(-176);

            slide.setOnFinished((ActionEvent e) -> {
                menu.setVisible(false);
                menuClose.setVisible(true);
            });
        });
        menuClose.setOnMouseClicked(event -> {
            TranslateTransition slide = new TranslateTransition();
            slide.setDuration(Duration.seconds(0.4));
            slide.setNode(slider);

            slide.setToX(-176);
            slide.play();

            slider.setTranslateX(0);

            slide.setOnFinished((ActionEvent e) -> {
                menu.setVisible(true);
                menuClose.setVisible(false);
            });
        });
    }

    /**
     * Open include for home view and set buttons accordingly.
     */
    @FXML
    private void openHome() {
        openHomeFXML();
    }

    /**
     * Open include for lecturer and set buttons accordingly.
     */
    @FXML
    private void openLecturer() {
        openLecturerFXML();
    }

    /**
     * Open include for study section and set buttons accordingly.
     */
    @FXML
    private void openStudySection() {
        openStudySectionFXML();
    }

    /**
     * Open include for coursePass and set buttons accordingly.
     */
    @FXML
    public void openCoursePass() {
        openCoursePassFXML();
    }

    /**
     * Open coursePassLecturerSubject as an include to edit
     * ToDo
     * @param selectedItem the coursePassLecturerSubject items will be set for this coursePass that was selected in the
     *                     tableView
     */
    public void openCoursePassLecturerSubject() {
        openCoursePassLecturerSubjectFXML();
    }

    /**
     * Open include for course of study and set buttons accordingly.
     * ToDo
     */
    @FXML
    private void openCourseOfStudy() {
        openCourseOfStudyFXML();
    }

    /**
     * Open include for subject and set buttons accordingly.
     * ToDo
     */
    @FXML
    private void openSubject() {
        try {
            //Load fmxl that will be included in center of brdrPnAll
            FXMLLoader childLoader = new FXMLLoader(getClass().getResource("/de/bremen/jTimetable/fxml/Subject.fxml"),
                    this.resourceBundle);
            Pane childNode = childLoader.load();
            //Add include
            this.childContainer.getChildren().clear();
            this.childContainer.getChildren().add(childNode);
            //Load corresponding controller class and initialize
            SubjectController subjectController = childLoader.getController();
            subjectController.initialize(this.location, this.resourceBundle);
            //Do something with the child node and controller
        } catch (IOException e) {
            System.err.println("Subject could not be loaded properly!");
        }
    }

    /**
     * Open include for room and set buttons accordingly.
     * ToDo
     */
    @FXML
    private void openRoom() {
        try {
            //Load fmxl that will be included in center of brdrPnAll
            FXMLLoader childLoader = new FXMLLoader(getClass().getResource("/de/bremen/jTimetable/fxml/Room.fxml"),
                    this.resourceBundle);
            Pane childNode = childLoader.load();
            //Add include
            this.childContainer.getChildren().clear();
            this.childContainer.getChildren().add(childNode);
            //Load corresponding controller class and initialize
            RoomController roomController = childLoader.getController();
            roomController.initialize(this.location, this.resourceBundle);
            //Do something with the child node and controller
        } catch (IOException e) {
            System.err.println("Lecturer could not be loaded properly!");
        }
    }

    /**
     * Open include for location and set buttons accordingly.
     * ToDo
     */
    @FXML
    private void openLocation() {
        try {
            //Load fmxl that will be included in center of brdrPnAll
            FXMLLoader childLoader = new FXMLLoader(getClass().getResource("/de/bremen/jTimetable/fxml/Location.fxml"),
                    this.resourceBundle);
            Pane childNode = childLoader.load();
            //Add include
            this.childContainer.getChildren().clear();
            this.childContainer.getChildren().add(childNode);
            //Load corresponding controller class and initialize
            LocationController locationController = childLoader.getController();
            locationController.initialize(this.location, this.resourceBundle);
            //Do something with the child node and controller
        } catch (IOException e) {
            System.err.println("Location could not be loaded properly!");
        }
    }

    public void openHomeFXML(){
        try {
            //Load fmxl that will be included in center of brdrPnAll
            FXMLLoader childLoader = new FXMLLoader(getClass().getResource("/de/bremen/jTimetable/fxml/Home.fxml"),
                    this.resourceBundle);
            Pane childNode = childLoader.load();
            //Add include
            this.childContainer.getChildren().clear();
            this.childContainer.getChildren().add(childNode);

            //Load corresponding Controller
            HomeController homeController = childLoader.getController();
            
            //Do something with the child node and controller:
            homeController.setBackgroundController(this);
            //Set up top menu buttons
            Scene scene = this.brdrPnAll.getScene();
            homeController.addTopmenuButtons(scene);

        } catch (IOException e) {
            System.err.println("Home could not be loaded properly!");
        }
    }

    private void openLecturerFXML(){
        try {
            //Load fmxl that will be included in center of brdrPnAll
            FXMLLoader childLoader = new FXMLLoader(getClass().getResource("/de/bremen/jTimetable/fxml/Lecturer.fxml"),
                    this.resourceBundle);
            Pane childNode = childLoader.load();
            //Add include
            this.childContainer.getChildren().clear();
            this.childContainer.getChildren().add(childNode);
            //Load corresponding controller class and initialize
            LecturerController lecturerController = childLoader.getController();
            
            //Do something with the child node and controller
            Scene scene = this.brdrPnAll.getScene();
            lecturerController.addTopmenuButtons(scene);
        } catch (IOException e) {
            System.err.println("Lecturer could not be loaded properly!");
        }
    }

    private void openStudySectionFXML(){
        try {
            //Load fmxl that will be included in center of brdrPnAll
            FXMLLoader childLoader =
                    new FXMLLoader(getClass().getResource("/de/bremen/jTimetable/fxml/StudySection.fxml"),
                            this.resourceBundle);
            Pane childNode = childLoader.load();
            //Add include
            this.childContainer.getChildren().clear();
            this.childContainer.getChildren().add(childNode);
            //Load corresponding controller class and initialize
            StudySectionController studySectionController = childLoader.getController();
            
            //Do something with the child node and controller
            Scene scene = this.brdrPnAll.getScene();
            studySectionController.addTopmenuButtons(scene);
        } catch (IOException e) {
            System.err.println("StudySection could not be loaded properly!");
        }
    }

    private void openCoursePassFXML(){
        try {
            //Load fmxl that will be included in center of brdrPnAll
            FXMLLoader childLoader =
                    new FXMLLoader(getClass().getResource("/de/bremen/jTimetable/fxml/Coursepass.fxml"),
                            this.resourceBundle);
            Pane childNode = childLoader.load();
            //Add include
            this.childContainer.getChildren().clear();
            this.childContainer.getChildren().add(childNode);
            //Load corresponding controller class and initialize
            CoursepassController coursepassController = childLoader.getController();
            
            coursepassController.setBackgroundController(this);
            //Do something with the child node and controller
            Scene scene = this.brdrPnAll.getScene();
            coursepassController.addTopmenuButtons(scene);
            
        } catch (IOException e) {
            System.err.println("CoursePass could not be loaded properly!");
        }
    }

    private void openCoursePassLecturerSubjectFXML(){
        try {
            //Load fmxl that will be included in center of brdrPnAll
            FXMLLoader childLoader =
                    new FXMLLoader(getClass().getResource("/de/bremen/jTimetable/fxml/CoursepassLecturerSubject.fxml"), this.resourceBundle);
            Pane childNode = childLoader.load();
            //Add include
            this.childContainer.getChildren().clear();
            this.childContainer.getChildren().add(childNode);
            //Load corresponding Controller
            CoursepassLecturerSubjectController coursepassLecturerSubjectController = childLoader.getController();
            
            //Do something with the child node and controller
            Scene scene = this.brdrPnAll.getScene();
            coursepassLecturerSubjectController.addTopmenuButtons(scene);
        } catch (Exception e) {
            System.err.println("Subjects for this coursePass can't be set.");
        }
    }

    private void openCourseOfStudyFXML(){        
        try {
            //Load fmxl that will be included in center of brdrPnAll
            FXMLLoader childLoader =
                    new FXMLLoader(getClass().getResource("/de/bremen/jTimetable/fxml/CourseofStudy.fxml"), this.resourceBundle);
            Pane childNode = childLoader.load();
            //Add include
            this.childContainer.getChildren().clear();
            this.childContainer.getChildren().add(childNode);
            //Load corresponding Controller
            CourseofStudyController courseofStudyController = childLoader.getController();
            //Do something with the child node and controller:            
            Scene scene = this.brdrPnAll.getScene();
            courseofStudyController.addTopmenuButtons(scene);
        } catch (Exception e) {
            System.err.println("CourseOfStudy could not be loaded properly!");
        }
    }
}
