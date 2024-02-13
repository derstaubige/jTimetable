package de.bremen.jTimetable.fxmlController;

import javafx.application.Platform;

//import de.bremen.jTimetable.Resources.Resources_de;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import de.bremen.jTimetable.Main;
import de.bremen.jTimetable.Classes.CoursePass;
import de.bremen.jTimetable.Classes.SQLConnectionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML
    private TableView<CoursePass> CoursepassTableview;
    @FXML
    private TableColumn<CoursePass, Long> CPID;
    @FXML
    private TableColumn<CoursePass, String> CPCOSCaption;
    @FXML
    private TableColumn<CoursePass, String> CPstudysection;
    @FXML
    private TableColumn<CoursePass, String> CPDescription;
    @FXML
    private TableColumn<CoursePass, LocalDate> CPStart;
    @FXML
    private TableColumn<CoursePass, LocalDate> CPEnd;
    @FXML
    private TableColumn<CoursePass, Boolean> CPActive;
    @FXML
    private Button btnTimetableShow;
    @FXML
    private Button btnCoursepassEdit;
    @FXML
    private CheckBox chkToogleCoursepass;
    @FXML
    private Label lblActiveCoursepasses;
    @FXML 
    private MenuController mainMenuController;

    private SQLConnectionManager sqlConnectionManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            mainMenuController.setSqlConnectionManager(sqlConnectionManager);
            CPID.setCellValueFactory(new PropertyValueFactory<CoursePass, Long>("id"));
            CPCOSCaption.setCellValueFactory(new PropertyValueFactory<CoursePass, String>("courseOfStudyCaption"));
            CPstudysection.setCellValueFactory(new PropertyValueFactory<CoursePass, String>("CPStudySection"));
            CPDescription.setCellValueFactory(new PropertyValueFactory<CoursePass, String>("description"));
            CPStart.setCellValueFactory(new PropertyValueFactory<CoursePass, LocalDate>("start"));
            CPEnd.setCellValueFactory(new PropertyValueFactory<CoursePass, LocalDate>("end"));
            CPActive.setCellValueFactory(new PropertyValueFactory<CoursePass, Boolean>("active"));

            CoursepassTableview.getItems().setAll(getCoursepass(true));
        });
        // ToDo: Read prefered language out of config.properties file

        btnTimetableShow.setOnAction(event -> {
            TableView.TableViewSelectionModel<CoursePass> selectionModel = CoursepassTableview.getSelectionModel();
            ObservableList<CoursePass> selectedItems = selectionModel.getSelectedItems();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/TimetableView.fxml"), resources);
            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setTitle("Timetable for " + selectedItems.get(0).getCourseOfStudy().getCaption() + " " +
                    selectedItems.get(0).getStudySection().getDescription());
            URL url = Main.class.getResource("fxml/TimetableView.fxml");
            loader.setLocation(url);
            try {
                stage.setScene(new Scene(loader.load()));
                TimetableViewController timetableViewController = loader.getController();
                timetableViewController.setSqlConnectionManager(getSqlConnectionManager());
                timetableViewController
                        .initDataCoursepass(new CoursePass((selectedItems.get(0).getId()), getSqlConnectionManager()));
                stage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        CoursepassTableview.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                // DoubleClick: Editor is opened
                if (click.getClickCount() == 2) {
                    btnCoursepassEdit.fire();
                }

            }
        });

        btnCoursepassEdit.setOnAction(event -> {
            TableView.TableViewSelectionModel<CoursePass> selectionModel = CoursepassTableview.getSelectionModel();
            ObservableList<CoursePass> selectedItems = selectionModel.getSelectedItems();
            if (selectedItems.size() > 0) {
                // System.out.println(selectedItems.get(0).getId());
                Stage stageTheEventSourceNodeBelongs = (Stage) ((Node) event.getSource()).getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/Coursepass.fxml"), resources);
                try {
                    AnchorPane anchorPane = loader.<AnchorPane>load();
                    CoursepassController coursepassController = loader.<CoursepassController>getController();
                    coursepassController
                            .setCoursepass(new CoursePass(selectedItems.get(0).getId(), getSqlConnectionManager()));
                    coursepassController.setSqlConnectionManager(getSqlConnectionManager());
                    Scene scene = new Scene(anchorPane);
                    stageTheEventSourceNodeBelongs.setScene(scene);
                } catch (Exception e) {
                    // TODo: Propper Error handling
                    e.printStackTrace();
                }
            }
        });

        chkToogleCoursepass.setOnAction(event -> {
            CoursepassTableview.getItems().setAll(getCoursepass(!chkToogleCoursepass.isSelected()));
        });

    }

    public ArrayList<CoursePass> getCoursepass(Boolean activeState) {
        ArrayList<CoursePass> activeCoursepass = new ArrayList<CoursePass>();
        try {
            activeCoursepass = CoursePass.getCoursePasses(activeState, getSqlConnectionManager());
        } catch (SQLException e) {
            // TODo: better error handling
            e.printStackTrace();

        }
        return activeCoursepass;
    }

    public SQLConnectionManager getSqlConnectionManager() {
        return sqlConnectionManager;
    }

    public void setSqlConnectionManager(SQLConnectionManager sqlConnectionManager) {
        this.sqlConnectionManager = sqlConnectionManager;
    }

}
