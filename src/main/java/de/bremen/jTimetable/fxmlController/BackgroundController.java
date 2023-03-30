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
import javafx.scene.Parent;
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
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class BackgroundController implements Initializable {

    public Button btnCoursePassEdit;
    public Button btnNew;
    public BorderPane brdrPnAll;
    ResourceBundle resourceBundle;

    @FXML
    public Parent homeView;

    @FXML
    private HomeController homeViewController;

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
    @FXML
    private AnchorPane courseOfStudyView;
    @FXML
    private AnchorPane coursePassView;
    @FXML
    private AnchorPane coursePassLecturerSubjectView;
    @FXML
    private AnchorPane lecturerView;
    @FXML
    private AnchorPane locationView;
    @FXML
    private AnchorPane roomView;
    @FXML
    private AnchorPane studySectionView;
    @FXML
    private AnchorPane subjectView;
    @FXML
    private AnchorPane timetableView;

    private AnchorPane currentlyVisible = new AnchorPane();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resourceBundle = resources;
        homeViewController.initialize(location, resources);
        btnNew.setDisable(true);

        //Initialise includes
        homeView.setVisible(true);
        courseOfStudyView.setVisible(false);
        coursePassView.setVisible(false);
        coursePassLecturerSubjectView.setVisible(false);
        lecturerView.setVisible(false);
        locationView.setVisible(false);
        roomView.setVisible(false);
        studySectionView.setVisible(false);
        subjectView.setVisible(false);
        timetableView.setVisible(false);

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

        homeViewController.getBtnTimetableShow().setOnAction(event -> {
            TableView.TableViewSelectionModel<CoursePass> selectionModel = homeViewController.getCoursePassTableview().getSelectionModel();
            ObservableList<CoursePass> selectedItems = selectionModel.getSelectedItems();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/TimetableView.fxml"), resources);
            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setTitle("Timetable for " + selectedItems.get(0).getCourseOfStudy().getCaption() + " " +
                    selectedItems.get(0).getStudySection().getDescription());
            URL url = Main.class.getResource("fxml/TimetableView.fxml");
            loader.setLocation(url);
            try {
                stage.setScene(new Scene(loader.load()));
                TimetableViewController controller = loader.getController();
                controller.initDataCoursepass(new CoursePass((selectedItems.get(0).getId())));
                stage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        homeViewController.getBtnCoursePassEdit().setOnAction(event -> {
            TableView.TableViewSelectionModel<CoursePass> selectionModel = homeViewController.getCoursePassTableview().getSelectionModel();
            ObservableList<CoursePass> selectedItems = selectionModel.getSelectedItems();
            if (selectedItems.size() > 0) {
                //System.out.println(selectedItems.get(0).getId());
                Stage stageTheEventSourceNodeBelongs = (Stage) ((Node) event.getSource()).getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/Coursepass.fxml"), resources);

                try {
                    AnchorPane anchorPane = loader.load();
                    CoursepassController coursepassController = loader.getController();
                    coursepassController.setCoursepass(new CoursePass(selectedItems.get(0).getId()));
                    Scene scene = new Scene(anchorPane);
                    stageTheEventSourceNodeBelongs.setScene(scene);
                } catch (Exception e) {
                    //TODo: Proper Error handling
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     *
     * @param visible
     */
    private void handleIncludes(AnchorPane visible) {
        currentlyVisible.setVisible(false);
        visible.setVisible(true);
        currentlyVisible = visible;
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
        //System.out.println(selectedItems.get(0).getId());
        Stage stageTheEventSourceNodeBelongs = (Stage) menuBar.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/CourseofStudy.fxml"), this.resourceBundle);

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
        Stage stageTheEventSourceNodeBelongs = (Stage) menuBar.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/Home.fxml"), this.resourceBundle);

        try {
            AnchorPane anchorPane = loader.load();
            Scene scene = new Scene(anchorPane);
            stageTheEventSourceNodeBelongs.setScene(scene);
        } catch (Exception e) {
            //TODo: Proper Error handling
            e.printStackTrace();
        }
    }

    @FXML
    private void openCoursePass() {
        Stage stageTheEventSourceNodeBelongs = (Stage) menuBar.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/Coursepass.fxml"), this.resourceBundle);

        try {
            AnchorPane anchorPane = loader.load();
            Scene scene = new Scene(anchorPane);
            stageTheEventSourceNodeBelongs.setScene(scene);
        } catch (Exception e) {
            //TODo: Proper Error handling
            e.printStackTrace();
        }
    }

    @FXML
    private void openLecturer() {
        Stage stageTheEventSourceNodeBelongs = (Stage) menuBar.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/Lecturer.fxml"), this.resourceBundle);

        try {
            AnchorPane anchorPane = loader.<AnchorPane>load();
            Scene scene = new Scene(anchorPane);
            stageTheEventSourceNodeBelongs.setScene(scene);
        } catch (Exception e) {
            //TODo: Proper Error handling
            e.printStackTrace();
        }
    }

    @FXML
    private void openSubject() {
        Stage stageTheEventSourceNodeBelongs = (Stage) menuBar.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/Subject.fxml"), this.resourceBundle);

        try {
            AnchorPane anchorPane = loader.load();
            Scene scene = new Scene(anchorPane);
            stageTheEventSourceNodeBelongs.setScene(scene);
        } catch (Exception e) {
            //TODo: Proper Error handling
            e.printStackTrace();
        }
    }

    @FXML
    private void openRoom() {
        Stage stageTheEventSourceNodeBelongs = (Stage) menuBar.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/Room.fxml"), this.resourceBundle);

        try {
            AnchorPane anchorPane = loader.<AnchorPane>load();
            Scene scene = new Scene(anchorPane);
            stageTheEventSourceNodeBelongs.setScene(scene);
        } catch (Exception e) {
            //TODo: Propper Error handling
            e.printStackTrace();
        }
    }

    @FXML
    private void openLocation() {
        Stage stageTheEventSourceNodeBelongs = (Stage) menuBar.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/Location.fxml"), this.resourceBundle);

        try {
            AnchorPane anchorPane = loader.<AnchorPane>load();
            Scene scene = new Scene(anchorPane);
            stageTheEventSourceNodeBelongs.setScene(scene);
        } catch (Exception e) {
            //TODo: Propper Error handling
            e.printStackTrace();
        }
    }

    @FXML
    private void openStudySection() {
        Stage stageTheEventSourceNodeBelongs = (Stage) menuBar.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/StudySection.fxml"), this.resourceBundle);

        try {
            AnchorPane anchorPane = loader.<AnchorPane>load();
            Scene scene = new Scene(anchorPane);
            stageTheEventSourceNodeBelongs.setScene(scene);
        } catch (Exception e) {
            //TODo: Propper Error handling
            e.printStackTrace();
        }
    }
}
