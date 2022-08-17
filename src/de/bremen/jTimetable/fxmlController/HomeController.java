package de.bremen.jTimetable.fxmlController;

import com.sun.org.apache.xpath.internal.operations.Bool;
import de.bremen.jTimetable.Classes.CourseofStudy;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

public class HomeController implements Initializable {
    @FXML private TableView<CourseofStudy> ActiveCoursesofStudyTableview;
    @FXML private TableColumn<CourseofStudy, Long> COSID;
    @FXML private TableColumn<CourseofStudy, String> COSDescription;
    @FXML private TableColumn<CourseofStudy, LocalDate> COSBegin;
    @FXML private TableColumn<CourseofStudy, LocalDate> COSEnd;
    @FXML private TableColumn<CourseofStudy, Boolean> COSActive;
    public HomeController(){

    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        COSID.setCellValueFactory(new PropertyValueFactory<CourseofStudy, Long>("id"));
        COSDescription.setCellValueFactory(new PropertyValueFactory<CourseofStudy, String>("caption"));
        COSBegin.setCellValueFactory(new PropertyValueFactory<CourseofStudy, LocalDate>("begin"));
        COSEnd.setCellValueFactory(new PropertyValueFactory<CourseofStudy, LocalDate>("end"));
        COSActive.setCellValueFactory(new PropertyValueFactory<CourseofStudy, Boolean>("active"));

        ActiveCoursesofStudyTableview.getItems().setAll(getActiveCoursesofStudy());
    }
    public ArrayList<CourseofStudy> getActiveCoursesofStudy() {
        ArrayList<CourseofStudy> activeCoursesofStudy = new ArrayList();
        try{
            activeCoursesofStudy = new CourseofStudy(0L).getActiveCoursesofStudy();
        } catch (SQLException e) {
            //TODo: better error handling
            System.out.println(e);
        }
        return activeCoursesofStudy;
    }
}
