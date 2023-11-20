package de.bremen.jTimetable.fxmlController;

import de.bremen.jTimetable.Classes.CoursePass;
import de.bremen.jTimetable.Main;
import javafx.animation.TranslateTransition;
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
    public Button btnNew;
    @FXML
    public BorderPane brdrPnAll;
    @FXML
    public Button btnLecturer;
    @FXML
    public AnchorPane childContainer;
    @FXML
    public Button btnEdit;
    @FXML
    public Button btnShow;
    @FXML
    public Button btnCreateIniTimetable;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resourceBundle = resources;
        this.location = location;

        //Initially home view is displayed
        openHome();

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
     * Handle action related to "About" menu item.
     *
     * @param event Event on "About" menu item.
     */
    @FXML
    private void handleAboutAction(final ActionEvent event) {
        provideAboutFunctionality();
    }

    /**
     * Handle action related to input (in this case specifically only responds to
     * keyboard event CTRL-A).
     *
     * @param event Input event.
     */
    @FXML
    private void handleKeyInput(final InputEvent event) {
        if (event instanceof KeyEvent) {
            final KeyEvent keyEvent = (KeyEvent) event;
            if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.A) {
                provideAboutFunctionality();
            }
        }
    }

    /**
     * Perform functionality associated with "About" menu selection or CTRL-A.
     */
    private void provideAboutFunctionality() {
        System.out.println("You clicked on About!");
    }

    /**
     * Closes the entire program.
     */
    @FXML
    private void closeButtonAction() {
        System.exit(0);
    }

    /**
     * Open include for home view and set buttons accordingly.
     */
    @FXML
    private void openHome() {
        try {
            //Load fmxl that will be included in center of brdrPnAll
            FXMLLoader childLoader = new FXMLLoader(getClass().getResource("../fxml/Home.fxml"),
                    this.resourceBundle);
            Pane childNode = childLoader.load();
            //Add include
            this.childContainer.getChildren().clear();
            this.childContainer.getChildren().add(childNode);
            //Load corresponding Controller
            HomeController homeController = childLoader.getController();
            //homeController.initialize(this.location, this.resourceBundle);
            //Do something with the child node and controller:
            homeController.setChildContainer(this.childContainer);
            //Set up top menu buttons
            btnCreateIniTimetable.setVisible(false);
            btnEdit.setOnAction(e -> homeController.editCoursePass());
            btnNew.setDisable(true);
            //Open the selected timetable in a new window
            btnShow.setOnAction(e -> homeController.showTimetable());

        } catch (IOException e) {
            System.err.println("Home could not be loaded properly!");
        }
    }

    /**
     * Open include for lecturer and set buttons accordingly.
     */
    @FXML
    private void openLecturer() {
        try {
            //Load fmxl that will be included in center of brdrPnAll
            FXMLLoader childLoader = new FXMLLoader(getClass().getResource("../fxml/Lecturer.fxml"),
                    this.resourceBundle);
            Pane childNode = childLoader.load();
            //Add include
            this.childContainer.getChildren().clear();
            this.childContainer.getChildren().add(childNode);
            //Load corresponding controller class and initialize
            LecturerController lecturerController = childLoader.getController();
            lecturerController.initialize(this.location, this.resourceBundle);
            //Do something with the child node and controller
            btnNew.setDisable(false);
            btnNew.setOnAction(event -> lecturerController.newLecturer());
            btnEdit.setOnAction(event -> lecturerController.editLecturer());
            //Show timetable of lecturer
            btnShow.setOnAction(event -> lecturerController.showLecturerTimetable());
        } catch (IOException e) {
            System.err.println("Lecturer could not be loaded properly!");
        }
    }

    /**
     * Open include for study section and set buttons accordingly.
     */
    @FXML
    private void openStudySection() {
        try {
            //Load fmxl that will be included in center of brdrPnAll
            FXMLLoader childLoader =
                    new FXMLLoader(getClass().getResource("../fxml/StudySection.fxml"),
                            this.resourceBundle);
            Pane childNode = childLoader.load();
            //Add include
            this.childContainer.getChildren().clear();
            this.childContainer.getChildren().add(childNode);
            //Load corresponding controller class and initialize
            StudySectionController studySectionController = childLoader.getController();
            studySectionController.initialize(this.location, this.resourceBundle);
            //Do something with the child node and controller
            btnNew.setDisable(false);
            btnShow.setDisable(true);
            btnNew.setOnAction(event -> studySectionController.newStudySection());
            btnEdit.setOnAction(event -> studySectionController.editStudySection());
        } catch (IOException e) {
            System.err.println("StudySection could not be loaded properly!");
        }
    }

    /**
     * Open include for coursePass and set buttons accordingly.
     * ToDo
     */
    @FXML
    private void openCoursePass() {
        try {
            //Load fmxl that will be included in center of brdrPnAll
            FXMLLoader childLoader =
                    new FXMLLoader(getClass().getResource("../fxml/Coursepass.fxml"),
                            this.resourceBundle);
            Pane childNode = childLoader.load();
            //Add include
            this.childContainer.getChildren().clear();
            this.childContainer.getChildren().add(childNode);
            //Load corresponding controller class and initialize
            CoursepassController coursepassController = childLoader.getController();
            coursepassController.initialize(this.location, this.resourceBundle);
            //Do something with the child node and controller
            btnNew.setDisable(false);
            btnShow.setDisable(true);
            btnNew.setOnAction(event -> coursepassController.newCoursePass());
            btnEdit.setOnAction(event -> coursepassController.editCoursePass());
        } catch (IOException e) {
            System.err.println("CoursePass could not be loaded properly!");
        }
    }

    /**
     * Open include for course of study and set buttons accordingly.
     * ToDo
     */
    @FXML
    private void openCourseOfStudy() {
        try {
            //Load fmxl that will be included in center of brdrPnAll
            FXMLLoader childLoader =
                    new FXMLLoader(getClass().getResource("../fxml/CourseofStudy.fxml"), this.resourceBundle);
            Pane childNode = childLoader.load();
            //Add include
            this.childContainer.getChildren().clear();
            this.childContainer.getChildren().add(childNode);
            //Load corresponding Controller
            CourseofStudyController courseofStudyController = childLoader.getController();
            courseofStudyController.initialize(this.location, this.resourceBundle);
            //Do something with the child node and controller:
        } catch (Exception e) {
            System.err.println("CourseOfStudy could not be loaded properly!");
        }
    }

    /**
     * Open include for subject and set buttons accordingly.
     * ToDo
     */
    @FXML
    private void openSubject() {
        try {
            //Load fmxl that will be included in center of brdrPnAll
            FXMLLoader childLoader = new FXMLLoader(getClass().getResource("../fxml/Subject.fxml"),
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
            FXMLLoader childLoader = new FXMLLoader(getClass().getResource("../fxml/Room.fxml"),
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
            FXMLLoader childLoader = new FXMLLoader(getClass().getResource("../fxml/Location.fxml"),
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
}
