package de.bremen.jTimetable.fxmlController;

//import com.sun.org.apache.xpath.internal.operations.Bool;

import de.bremen.jTimetable.Classes.CourseofStudy;
import de.bremen.jTimetable.Classes.Coursepass;
import de.bremen.jTimetable.Main;
import de.bremen.jTimetable.Resources.Config;
//import de.bremen.jTimetable.Resources.Resources_de;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML    private TableView<Coursepass> CoursepassTableview;
    @FXML    private TableColumn<Coursepass, Long> CPID;
    @FXML    private TableColumn<Coursepass, String> CPCOSCaption;
    @FXML    private TableColumn<Coursepass, String> CPstudysection;
    @FXML    private TableColumn<Coursepass, String> CPDescription;
    @FXML    private TableColumn<Coursepass, LocalDate> CPStart;
    @FXML    private TableColumn<Coursepass, LocalDate> CPEnd;
    @FXML    private TableColumn<Coursepass, Boolean> CPActive;
    @FXML    private Button btnCoursepassEdit;
    @FXML    private Button btnTimetableShow;
    @FXML    private CheckBox chkToogleCoursepass;
    @FXML private Label lblActiveCoursepasses;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // ToDo: Read prefered language out of config.properties file

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

        CPID.setCellValueFactory(new PropertyValueFactory<Coursepass, Long>("id"));
        CPCOSCaption.setCellValueFactory(new PropertyValueFactory<Coursepass, String>("courseofstudycaption"));
        CPstudysection.setCellValueFactory(new PropertyValueFactory<Coursepass, String>("CPstudysection"));
        CPDescription.setCellValueFactory(new PropertyValueFactory<Coursepass, String>("description"));
        CPStart.setCellValueFactory(new PropertyValueFactory<Coursepass, LocalDate>("start"));
        CPEnd.setCellValueFactory(new PropertyValueFactory<Coursepass, LocalDate>("end"));
        CPActive.setCellValueFactory(new PropertyValueFactory<Coursepass, Boolean>("active"));

        CoursepassTableview.getItems().setAll(getCoursepass(true));
        btnTimetableShow.setOnAction(event -> {
            TableView.TableViewSelectionModel<Coursepass> selectionModel = CoursepassTableview.getSelectionModel();
            ObservableList<Coursepass> selectedItems = selectionModel.getSelectedItems();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/TimetableView.fxml"), resources);
            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setTitle("Timetable for " + selectedItems.get(0).getCourseofstudy().getCaption() + " " +
                    selectedItems.get(0).getStudysection().getDescription());
            URL url = Main.class.getResource("fxml/TimetableView.fxml");
            loader.setLocation(url);
            try {
                stage.setScene(new Scene(loader.load()));
                TimetableViewController controller = loader.getController();
                controller.initData(new Coursepass((selectedItems.get(0).getId())));
                stage.show();
            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
        });
        CoursepassTableview.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                //DoubleClick: Editor is opened
                if (click.getClickCount() == 2) {
                    btnCoursepassEdit.fire();
                }

            }
        });

        btnCoursepassEdit.setOnAction(event -> {
            TableView.TableViewSelectionModel<Coursepass> selectionModel = CoursepassTableview.getSelectionModel();
            ObservableList<Coursepass> selectedItems = selectionModel.getSelectedItems();
            if (selectedItems.size() > 0) {
                //System.out.println(selectedItems.get(0).getId());
                Stage stageTheEventSourceNodeBelongs = (Stage) ((Node) event.getSource()).getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/Coursepass.fxml"), resources);

                try {
                    AnchorPane anchorPane = loader.<AnchorPane>load();
                    CoursepassController coursepassController = loader.<CoursepassController>getController();
                    coursepassController.setCoursepass(new Coursepass(selectedItems.get(0).getId()));
                    Scene scene = new Scene(anchorPane);
                    stageTheEventSourceNodeBelongs.setScene(scene);
                } catch (Exception e) {
                    //TODo: Propper Error handling
                    e.printStackTrace();
                }
            }
        });

        chkToogleCoursepass.setOnAction(event -> {
            CoursepassTableview.getItems().setAll(getCoursepass(!chkToogleCoursepass.isSelected()));
        });

    }



    public ArrayList<Coursepass> getCoursepass(Boolean activeState) {
        ArrayList<Coursepass> activeCoursepass = new ArrayList();
        try {
            activeCoursepass = new Coursepass(0L).getCoursepasses(activeState);
        } catch (SQLException e) {
            //TODo: better error handling
            System.out.println(e);
        }
        return activeCoursepass;
    }
}
