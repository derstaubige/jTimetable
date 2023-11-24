package de.bremen.jTimetable.fxmlController;

//import de.bremen.jTimetable.Resources.Resources_de;

import javafx.collections.ObservableList;
import javafx.event.Event;
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
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import de.bremen.jTimetable.Main;
import de.bremen.jTimetable.Classes.CoursePass;

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

    private ResourceBundle resources;
    private URL location;
    //private AnchorPane childContainer;
    private BackgroundController backgroundController;

    public void initialize(URL location, ResourceBundle resources) {
        // ToDo: Read prefered language out of config.properties file

        this.resources = resources;
        this.location = location;

        System.out.println("Initialize Home!");
//        Config config = new Config();
//        Locale locale = new Locale(config.getLocalLang(),config.getLocaCountry());
//        System.out.println(config.getLocaCountry() + config.getLocalLang());
//        ResourceBundle resourceBundle = ResourceBundle.getBundle("de.bremen.jTimetable.Resources.Resources", locale);
//        ResourceBundle resourceBundle = ResourceBundle.getBundle("de.bremen.jTimetable.Resources.Resources");
//
//        System.out.println(resources.getString("currency"));
//
//        // Translate everything
//        lblActiveCoursepasses.setText(resources.getString("currency"));

        CPID.setCellValueFactory(new PropertyValueFactory<CoursePass, Long>("id"));
        CPCOSCaption.setCellValueFactory(new PropertyValueFactory<CoursePass, String>("courseOfStudyCaption"));
        CPstudysection.setCellValueFactory(new PropertyValueFactory<CoursePass, String>("CPStudySection"));
        CPDescription.setCellValueFactory(new PropertyValueFactory<CoursePass, String>("description"));
        CPStart.setCellValueFactory(new PropertyValueFactory<CoursePass, LocalDate>("start"));
        CPEnd.setCellValueFactory(new PropertyValueFactory<CoursePass, LocalDate>("end"));
        CPActive.setCellValueFactory(new PropertyValueFactory<CoursePass, Boolean>("active"));

        CoursepassTableview.getItems().setAll(getCoursepass(true));

        CoursepassTableview.setOnMouseClicked(click -> {
            //DoubleClick: Editor is opened
            if (click.getClickCount() == 2) {
                editCoursePass();
            }

        });

        chkToogleCoursepass.setOnAction(event -> {
            CoursepassTableview.getItems().setAll(getCoursepass(!chkToogleCoursepass.isSelected()));
        });

    }

    /**
     * ToDo can this open more than one timetable at once/ should it be able to? Otherwise only one selected
     *  item allowed
     */
    public void showTimetable() {
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
            TimetableViewController controller = loader.getController();
            controller.initDataCoursepass(new CoursePass((selectedItems.get(0).getId())));
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Open the coursePass menu and set the id of the selected coursePass
     */
    public void editCoursePass() {
        //Get the selected items from tableView
        TableView.TableViewSelectionModel<CoursePass> selectionModel =
                CoursepassTableview.getSelectionModel();
        ObservableList<CoursePass> selectedItems = selectionModel.getSelectedItems();
        //Check if exactly one column is selected
        if (selectedItems.size() == 1) {
            //Load fxml that will be included in background with setting coursePass
            backgroundController.openCoursePass(selectedItems);
        }
    }

    public ArrayList<CoursePass> getCoursepass(Boolean activeState) {
        ArrayList<CoursePass> activeCoursepass = new ArrayList<>();
        try {
            activeCoursepass = CoursePass.getCoursePasses(activeState);
        } catch (SQLException e) {
            System.err.println("ArrayList of all coursePasses in homeController couldn't be returned.");
        }
        return activeCoursepass;
    }

    public TableView<CoursePass> getCoursePassTableview() {
        return CoursepassTableview;
    }

    public TableColumn<CoursePass, Long> getCPID() {
        return CPID;
    }

    public TableColumn<CoursePass, String> getCPCOSCaption() {
        return CPCOSCaption;
    }

    public TableColumn<CoursePass, String> getCPStudySection() {
        return CPstudysection;
    }

    public TableColumn<CoursePass, String> getCPDescription() {
        return CPDescription;
    }

    public TableColumn<CoursePass, LocalDate> getCPStart() {
        return CPStart;
    }

    public TableColumn<CoursePass, LocalDate> getCPEnd() {
        return CPEnd;
    }

    public TableColumn<CoursePass, Boolean> getCPActive() {
        return CPActive;
    }

    public Button getBtnTimetableShow() {
        return btnTimetableShow;
    }

    public Button getBtnCoursePassEdit() {
        return btnCoursepassEdit;
    }

    public CheckBox getChkToggleCoursePass() {
        return chkToogleCoursepass;
    }

    public Label getLblActiveCoursePasses() {
        return lblActiveCoursepasses;
    }

    /**
     * Setter.
     *
     * @param backgroundController Controller for the background buttons that needs to be accessed in editCoursePass
     */
    public void setBackgroundController(BackgroundController backgroundController) {
        this.backgroundController = backgroundController;
    }
}
