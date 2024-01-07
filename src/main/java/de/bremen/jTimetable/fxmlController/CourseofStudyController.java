package de.bremen.jTimetable.fxmlController;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    ResourceBundle resources;

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
        this.resources = resources;
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
                    editCOS();
                }
            }
        });

        chkToogleActiveCourseofStudy.setOnAction(event -> {
            ActiveCoursesofStudyTableview.getItems()
                    .setAll(getCoursesofStudy(!chkToogleActiveCourseofStudy.isSelected()));
        });

        btnSave.setOnAction(event -> {
            this.cos.setCaption(txtCaption.getText().trim());
            this.cos.setBegin(datBegin.getValue());
            this.cos.setEnd(datEnd.getValue());
            this.cos.setActive(chkActive.isSelected());

            // save changes
            try {
                this.cos.save();
            } catch (Exception e) {
                // TODo: Propper Error handling
                e.printStackTrace();
            }
            editbox.setVisible(false);
            ActiveCoursesofStudyTableview.getItems()
                    .setAll(getCoursesofStudy(!chkToogleActiveCourseofStudy.isSelected()));
        });
    }

    private void editCOS() {
        TableView.TableViewSelectionModel<CourseofStudy> selectionModel = ActiveCoursesofStudyTableview
                .getSelectionModel();
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
    };

    private void newCOS() {
        try {
            this.cos = new CourseofStudy(0L);
            txtID.setText("" + this.cos.getId());
            txtCaption.setText(this.cos.getCaption());
            datBegin.setValue(this.cos.getBegin());
            datEnd.setValue(this.cos.getEnd());
            chkActive.setSelected(this.cos.isActive());
            txtID.setEditable(false);
            editbox.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

    };

    public void setID(CourseofStudy cos) {
        this.cos = cos;
    }

    public ArrayList<CourseofStudy> getCoursesofStudy(Boolean activeState) {
        ArrayList<CourseofStudy> activeCoursesofStudy = new ArrayList<CourseofStudy>();
        try {
            activeCoursesofStudy = new CourseofStudy(0L).getCoursesofStudy(activeState);
        } catch (SQLException e) {
            // TODo: better error handling
            System.out.println(e);
        }
        return activeCoursesofStudy;
    }

    public void addTopmenuButtons(Scene scene) {
        HBox topmenu = (HBox) scene.lookup("#topmenu");

        Button btnNew = new Button();
        try {
            URL url = getClass().getResource("/de/bremen/jTimetable/img/add1.png");
            Image image = new Image(url.toExternalForm());
            ImageView imageView = new ImageView(image);
            btnNew.setGraphic(imageView);
            btnNew.setOnAction(e -> this.newCOS());
            btnNew.getStyleClass().add("menuItem");
            btnNew.setText(resources.getString("menu.actions.new"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        Button btnEdit = new Button();
        try {
            URL url = getClass().getResource("/de/bremen/jTimetable/img/edit1.png");
            Image image = new Image(url.toExternalForm());
            ImageView imageView = new ImageView(image);
            btnEdit.setGraphic(imageView);
            btnEdit.setOnAction(e -> this.editCOS());
            btnEdit.getStyleClass().add("menuItem");
            btnEdit.setText(resources.getString("menu.actions.edit"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        Button btnClose = new Button();
        try {
            URL url = getClass().getResource("/de/bremen/jTimetable/img/exit.png");
            Image image = new Image(url.toExternalForm());
            ImageView imageView = new ImageView(image);
            btnClose.setGraphic(imageView);
            btnClose.setOnAction(e -> System.exit(0));
            btnClose.getStyleClass().add("menuItem");
            btnClose.setText(resources.getString("menu.file.exit"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        topmenu.getChildren().clear();
        topmenu.getChildren().addAll(btnNew, btnEdit, btnClose);
    }
}
