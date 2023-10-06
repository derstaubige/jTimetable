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
import javafx.scene.image.ImageView;
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

    @FXML
    public Button btnCoursePassEdit;
    @FXML
    public Button btnNew;
    @FXML
    public BorderPane brdrPnAll;
    @FXML
    public Button btnLecturer;
    @FXML
    public AnchorPane childContainer;

    ResourceBundle resourceBundle;
    URL location;

    /**
     * FXML Elements:
     */
    @FXML
    private MenuBar menuBar;

    @FXML
    private ImageView exit;

    @FXML
    private Label menu;

    @FXML
    private Label menuClose;

    @FXML
    private AnchorPane slider;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resourceBundle = resources;
        this.location = location;
        //  homeViewController.initialize(location, resources);
        btnNew.setDisable(true);
        openHome();


        exit.setOnMouseClicked(event -> System.exit(0));

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
     * TODO item doesn't exist
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

    @FXML
    private void closeButtonAction() {
        // get a handle to the stage
        Stage stage = (Stage) menuBar.getScene().getWindow();
        // do what you have to do
        stage.close();
    }

    @FXML
    private void openCourseOfStudy() {
        //TODO kann ich das hier genauso laden wie bei den anderen?
        //System.out.println(selectedItems.get(0).getId());
        Stage stageTheEventSourceNodeBelongs = (Stage) menuBar.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/CourseofStudy.fxml"),
                this.resourceBundle);

        try {
            AnchorPane anchorPane = loader.load();
            loader.getController();
            //courseofStudyController.setID(new CourseofStudy(selectedItems.get(0).getId()));
            Scene scene = new Scene(anchorPane);
            stageTheEventSourceNodeBelongs.setScene(scene);
        } catch (Exception e) {
            //TODo: Proper Error handling
            e.printStackTrace();
        }
    }

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
            homeController.initialize(this.location, this.resourceBundle);
            //Do something with the child node and controller:
            //Open the selected timetable in a new window
            //TODO in include as well
            homeController.getBtnTimetableShow().setOnAction(event -> {
                TableView.TableViewSelectionModel<CoursePass> selectionModel =
                        homeController.getCoursePassTableview().getSelectionModel();
                ObservableList<CoursePass> selectedItems = selectionModel.getSelectedItems();

                //TODO does this still work?
                FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/Home.fxml"),
                        this.resourceBundle);
                Stage stage = new Stage(StageStyle.DECORATED);
                stage.setTitle(
                        "Timetable for " + selectedItems.get(0).getCourseOfStudy().getCaption() +
                                " " +
                                selectedItems.get(0).getStudySection().getDescription());
                URL url = Main.class.getResource("fxml/TimetableView.fxml");
                loader.setLocation(url);
                try {
                    stage.setScene(new Scene(loader.load()));
                    TimetableViewController controller = loader.getController();
                    controller.initDataCoursepass(new CoursePass((selectedItems.get(0).getId())));
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });

            homeController.getBtnCoursePassEdit().setOnAction(event -> {
                TableView.TableViewSelectionModel<CoursePass> selectionModel =
                        homeController
                                .getCoursePassTableview().getSelectionModel();
                ObservableList<CoursePass> selectedItems = selectionModel.getSelectedItems();
                if (selectedItems.size() > 0) {
                    //System.out.println(selectedItems.get(0).getId());
                    Stage stageTheEventSourceNodeBelongs =
                            (Stage) ((Node) event.getSource()).getScene().getWindow();
                    FXMLLoader loader =
                            new FXMLLoader(Main.class.getResource("fxml/Coursepass.fxml"),
                                    this.resourceBundle);

                    try {
                        AnchorPane anchorPane = loader.load();
                        CoursepassController coursepassController = loader.getController();
                        coursepassController.setCoursepass(
                                new CoursePass(selectedItems.get(0).getId()));
                        Scene scene = new Scene(anchorPane);
                        stageTheEventSourceNodeBelongs.setScene(scene);
                    } catch (Exception e) {
                        //TODo: Proper Error handling
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            System.err.println("Home could not be loaded properly!");
            e.printStackTrace();
        }
    }

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
        } catch (IOException e) {
            System.err.println("Coursepass could not be loaded properly!");
            e.printStackTrace();
        }
    }

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
        } catch (IOException e) {
            System.err.println("Lecturer could not be loaded properly!");
            e.printStackTrace();
        }
    }

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
            e.printStackTrace();
        }
    }

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
            e.printStackTrace();
        }
    }

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
            e.printStackTrace();
        }
    }

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
        } catch (IOException e) {
            System.err.println("StudySection could not be loaded properly!");
            e.printStackTrace();
        }
    }
}
