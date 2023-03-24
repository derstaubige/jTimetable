package de.bremen.jTimetable.fxmlController;

import de.bremen.jTimetable.Main;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.InputEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class BackgroundController implements Initializable {
    @FXML
    private MenuBar menuBar;

    ResourceBundle resourceBundle;

    @FXML
    private ImageView Exit;

    @FXML
    private Label Menu;

    @FXML
    private Label MenuClose;

    @FXML
    private AnchorPane slider;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //menuBar.setFocusTraversable(true);
        //this.resourceBundle = resources;

        Exit.setOnMouseClicked(event -> {
            System.exit(0);
        });
        slider.setTranslateX(-176);
        Menu.setOnMouseClicked(event -> {
            TranslateTransition slide = new TranslateTransition();
            slide.setDuration(Duration.seconds(0.4));
            slide.setNode(slider);

            slide.setToX(0);
            slide.play();

            slider.setTranslateX(-176);

            slide.setOnFinished((ActionEvent e)-> {
                Menu.setVisible(false);
                MenuClose.setVisible(true);
            });
        });

        MenuClose.setOnMouseClicked(event -> {
            TranslateTransition slide = new TranslateTransition();
            slide.setDuration(Duration.seconds(0.4));
            slide.setNode(slider);

            slide.setToX(-176);
            slide.play();

            slider.setTranslateX(0);

            slide.setOnFinished((ActionEvent e)-> {
                Menu.setVisible(true);
                MenuClose.setVisible(false);
            });
        });
    }
    /**
     * Handle action related to "About" menu item.
     * TODO item doesn't exist
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
    private void openCourseofStudy() {
        //System.out.println(selectedItems.get(0).getId());
        Stage stageTheEventSourceNodeBelongs = (Stage) menuBar.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/CourseofStudy.fxml"), this.resourceBundle);

        try {
            AnchorPane anchorPane = loader.<AnchorPane>load();
            loader.<CourseofStudyController>getController();
            //courseofStudyController.setID(new CourseofStudy(selectedItems.get(0).getId()));
            Scene scene = new Scene(anchorPane);
            stageTheEventSourceNodeBelongs.setScene(scene);
        } catch (Exception e) {
            //TODo: Propper Error handling
            e.printStackTrace();
        }
    }

    @FXML
    private void openHome() {
        Stage stageTheEventSourceNodeBelongs = (Stage) menuBar.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/Home.fxml"), this.resourceBundle);

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
    private void openCoursepass() {
        Stage stageTheEventSourceNodeBelongs = (Stage) menuBar.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/Coursepass.fxml"), this.resourceBundle);

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
    private void openLecturer() {
        Stage stageTheEventSourceNodeBelongs = (Stage) menuBar.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/Lecturer.fxml"), this.resourceBundle);

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
    private void openSubject() {
        Stage stageTheEventSourceNodeBelongs = (Stage) menuBar.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/Subject.fxml"), this.resourceBundle);

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
