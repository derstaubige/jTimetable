package de.bremen.jTimetable.fxmlController;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import de.bremen.jTimetable.Classes.CourseofStudy;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class CourseofStudyController implements Initializable {
    CourseofStudy cos;

    @FXML
    private TextField txtID;
    @FXML
    private TextField txtCaption;
    @FXML
    private DatePicker datBegin;
    @FXML
    private DatePicker datEnd;
    @FXML
    private CheckBox chkActive;
    @FXML
    private Button btnSave;
    @FXML
    private Button ActiveCoursesofStudyButtonNew;
    @FXML
    private Button ActiveCoursesofStudyButton;
    @FXML
    private TableView<CourseofStudy> ActiveCoursesofStudyTableview;
    @FXML
    private TableColumn<CourseofStudy, Long> COSID;
    @FXML
    private TableColumn<CourseofStudy, String> COSDescription;
    @FXML
    private TableColumn<CourseofStudy, LocalDate> COSBegin;
    @FXML
    private TableColumn<CourseofStudy, LocalDate> COSEnd;
    @FXML
    private TableColumn<CourseofStudy, Boolean> COSActive;
    @FXML
    private CheckBox chkToogleActiveCourseofStudy;
    @FXML
    private HBox editbox;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        editbox.setVisible(false);

        COSID.setCellValueFactory(new PropertyValueFactory<CourseofStudy, Long>("id"));
        COSDescription.setCellValueFactory(new PropertyValueFactory<CourseofStudy, String>("caption"));
        COSBegin.setCellValueFactory(new PropertyValueFactory<CourseofStudy, LocalDate>("begin"));
        COSEnd.setCellValueFactory(new PropertyValueFactory<CourseofStudy, LocalDate>("end"));
        COSActive.setCellValueFactory(new PropertyValueFactory<CourseofStudy, Boolean>("active"));

        ActiveCoursesofStudyTableview.getItems().setAll(getCoursesofStudy(true));
        ActiveCoursesofStudyTableview.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                if (click.getClickCount() == 1) {
                    ActiveCoursesofStudyButton.fire();
                }
            }
        });

        ActiveCoursesofStudyButton.setOnAction(event -> {
            TableView.TableViewSelectionModel<CourseofStudy> selectionModel = ActiveCoursesofStudyTableview.getSelectionModel();
            ObservableList<CourseofStudy> selectedItems = selectionModel.getSelectedItems();

            if (selectedItems.size() > 0) {
                this.cos = selectedItems.get(0);
                txtID.setText("" + this.cos.getId());
                txtCaption.setText(this.cos.getCaption());
                datBegin.setValue(this.cos.getBegin());
                datEnd.setValue(this.cos.getEnd());
                chkActive.setSelected(this.cos.isActive());
                txtID.setEditable(false);
                editbox.setVisible(true);
            }
        });

        ActiveCoursesofStudyButtonNew.setOnAction(event -> {
            try{
                this.cos = new CourseofStudy(0L);
                txtID.setText("" + this.cos.getId());
                txtCaption.setText(this.cos.getCaption());
                datBegin.setValue(this.cos.getBegin());
                datEnd.setValue(this.cos.getEnd());
                chkActive.setSelected(this.cos.isActive());
                txtID.setEditable(false);
                editbox.setVisible(true);
            }catch(Exception e){
                e.printStackTrace();
            }

        });

        chkToogleActiveCourseofStudy.setOnAction(event -> {
            ActiveCoursesofStudyTableview.getItems().setAll(getCoursesofStudy(!chkToogleActiveCourseofStudy.isSelected()));
        });

        btnSave.setOnAction(event -> {
            this.cos.setCaption(txtCaption.getText().trim());
            this.cos.setBegin(datBegin.getValue());
            this.cos.setEnd(datEnd.getValue());
            this.cos.setActive(chkActive.isSelected());

            //save changes
            try {
                this.cos.save();
            } catch (Exception e) {
                //TODo: Propper Error handling
                e.printStackTrace();
            }
            editbox.setVisible(false);
            ActiveCoursesofStudyTableview.getItems().setAll(getCoursesofStudy(!chkToogleActiveCourseofStudy.isSelected()));
        });
    }

    public void setID(CourseofStudy cos) {
        this.cos = cos;
    }
    public ArrayList<CourseofStudy> getCoursesofStudy(Boolean activeState) {
        ArrayList<CourseofStudy> activeCoursesofStudy = new ArrayList<CourseofStudy>();
        try {
            activeCoursesofStudy = new CourseofStudy(0L).getCoursesofStudy(activeState);
        } catch (SQLException e) {
            //TODo: better error handling
            System.out.println(e);
        }
        return activeCoursesofStudy;
    }
}
