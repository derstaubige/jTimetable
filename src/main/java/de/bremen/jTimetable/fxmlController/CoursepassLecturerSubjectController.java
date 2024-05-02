package de.bremen.jTimetable.fxmlController;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import de.bremen.jTimetable.Main;
import de.bremen.jTimetable.Classes.*;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class CoursepassLecturerSubjectController implements Initializable {
    CoursePass coursepass;
    CoursepassLecturerSubject coursepassLecturerSubject;
    SQLConnectionManager sqlConnectionManager;

    @FXML
    public TableView<CoursepassLecturerSubject> CLSTableview;
    @FXML
    public TableColumn<CoursepassLecturerSubject, Long> TCID;
    @FXML
    public TableColumn<CoursepassLecturerSubject, String> TCLecturer;
    @FXML
    public TableColumn<CoursepassLecturerSubject, String> TCSubject;
    @FXML
    public TableColumn<CoursepassLecturerSubject, String> TCRoom;
    @FXML
    public TableColumn<CoursepassLecturerSubject, Long> TCShouldHours;
    @FXML
    public TableColumn<CoursepassLecturerSubject, Integer> TCisHours;
    @FXML
    public TableColumn<CoursepassLecturerSubject, Integer> TCPlanedHours;
    @FXML
    public TableColumn<CoursepassLecturerSubject, LocalDate> TCPlaceAfterDate;
    @FXML
    public TableColumn<CoursepassLecturerSubject, Boolean> CPActive;
    @FXML
    public Button btnCLSEdit;
    @FXML
    public Button btnCLSNew;
    @FXML
    public Button btnCLSBack;
    @FXML
    public CheckBox chkToogleCLS;
    @FXML
    public VBox editbox;
    @FXML
    public Label lblCoursepassName;
    @FXML
    public TextField txtID;
    @FXML
    public ComboBox<Lecturer> cmbLecturer;
    @FXML
    public ComboBox<Subject> cmbSubject;
    @FXML
    public ComboBox<Room> cmbRoom;
    @FXML
    public TextField txtShouldHours;
    @FXML
    public DatePicker placeAfterDay;
    @FXML
    public CheckBox chkActive;
    @FXML
    public Button btnSave;
    @FXML
    private MenuController mainMenuController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            mainMenuController.setSqlConnectionManager(sqlConnectionManager);
            TCID.setCellValueFactory(new PropertyValueFactory<CoursepassLecturerSubject, Long>("id"));
            TCLecturer.setCellValueFactory(
                    new PropertyValueFactory<CoursepassLecturerSubject, String>("LecturerFullname"));
            TCRoom.setCellValueFactory(
                    new PropertyValueFactory<CoursepassLecturerSubject, String>("RoomCaptionLocatioString"));
            TCSubject
                    .setCellValueFactory(new PropertyValueFactory<CoursepassLecturerSubject, String>("SubjectCaption"));
            TCShouldHours.setCellValueFactory(new PropertyValueFactory<CoursepassLecturerSubject, Long>("shouldHours"));
            TCisHours.setCellValueFactory(new PropertyValueFactory<CoursepassLecturerSubject, Integer>("isHours"));
            TCPlanedHours
                    .setCellValueFactory(new PropertyValueFactory<CoursepassLecturerSubject, Integer>("planedHours"));
            TCPlaceAfterDate.setCellValueFactory(
                    new PropertyValueFactory<CoursepassLecturerSubject, LocalDate>("placeAfterDay"));
            CPActive.setCellValueFactory(new PropertyValueFactory<CoursepassLecturerSubject, Boolean>("active"));
        });

        editbox.setVisible(false);

        cmbSubject.setConverter(new StringConverter<Subject>() {
            @Override
            public String toString(Subject subject) {
                if (subject == null) {
                    return "";
                } else {
                    return subject.getCaption();
                }
            }

            @Override
            public Subject fromString(String string) {
                return null;
            }
        });

        cmbSubject.setCellFactory(cell -> new ListCell<Subject>() {

            // Create our layout here to be reused for each ListCell
            GridPane gridPane = new GridPane();
            // Label lblID = new Label();
            Label lblDescription = new Label();

            // Static block to configure our layout
            {
                // Ensure all our column widths are constant
                gridPane.getColumnConstraints().addAll(
                        // new ColumnConstraints(100, 100, 100),
                        new ColumnConstraints(200, 200, 200));

                // gridPane.add(lblID, 0, 1, 1 ,1);
                gridPane.add(lblDescription, 0, 1, 1, 1);

            }

            // We override the updateItem() method in order to provide our own layout for
            // this Cell's graphicProperty
            @Override
            protected void updateItem(Subject subject, boolean empty) {
                super.updateItem(subject, empty);

                if (!empty && subject != null) {

                    // Update our Labels
                    // lblID.setText(studySection.getId().toString());
                    lblDescription.setText(subject.getCaption());

                    // Set this ListCell's graphicProperty to display our GridPane
                    setGraphic(gridPane);
                } else {
                    // Nothing to display here
                    setGraphic(null);
                }
            }
        });

        cmbRoom.setConverter(new StringConverter<Room>() {
            @Override
            public String toString(Room room) {
                if (room == null) {
                    return "";
                } else {
                    return room.getCaption() + ", " + room.getLocationCaption();
                }
            }

            @Override
            public Room fromString(String string) {
                return null;
            }
        });
        cmbRoom.setCellFactory(cell -> new ListCell<Room>() {

            // Create our layout here to be reused for each ListCell
            GridPane gridPane = new GridPane();
            // Label lblID = new Label();
            Label lblDescription = new Label();

            // Static block to configure our layout
            {
                // Ensure all our column widths are constant
                gridPane.getColumnConstraints().addAll(
                        // new ColumnConstraints(100, 100, 100),
                        new ColumnConstraints(200, 200, 200));

                // gridPane.add(lblID, 0, 1, 1 ,1);
                gridPane.add(lblDescription, 0, 1, 1, 1);

            }

            // We override the updateItem() method in order to provide our own layout for
            // this Cell's graphicProperty
            @Override
            protected void updateItem(Room room, boolean empty) {
                super.updateItem(room, empty);

                if (!empty && room != null) {

                    // Update our Labels
                    // lblID.setText(studySection.getId().toString());
                    lblDescription.setText(room.getCaption() + ", " + room.getLocationCaption());

                    // Set this ListCell's graphicProperty to display our GridPane
                    setGraphic(gridPane);
                } else {
                    // Nothing to display here
                    setGraphic(null);
                }
            }
        });

        cmbLecturer.setConverter(new StringConverter<Lecturer>() {
            @Override
            public String toString(Lecturer lecturer) {
                if (lecturer == null) {
                    return "";
                } else {
                    return lecturer.getLecturerFullName();
                }
            }

            @Override
            public Lecturer fromString(String string) {
                return null;
            }
        });

        cmbLecturer.setCellFactory(cell -> new ListCell<Lecturer>() {

            // Create our layout here to be reused for each ListCell
            GridPane gridPane = new GridPane();
            // Label lblID = new Label();
            Label lblDescription = new Label();

            // Static block to configure our layout
            {
                // Ensure all our column widths are constant
                gridPane.getColumnConstraints().addAll(
                        // new ColumnConstraints(100, 100, 100),
                        new ColumnConstraints(200, 200, 200));

                // gridPane.add(lblID, 0, 1, 1 ,1);
                gridPane.add(lblDescription, 0, 1, 1, 1);

            }

            // We override the updateItem() method in order to provide our own layout for
            // this Cell's graphicProperty
            @Override
            protected void updateItem(Lecturer lecturer, boolean empty) {
                super.updateItem(lecturer, empty);

                if (!empty && lecturer != null) {

                    // Update our Labels
                    // lblID.setText(studySection.getId().toString());
                    lblDescription.setText(lecturer.getLecturerFullName());

                    // Set this ListCell's graphicProperty to display our GridPane
                    setGraphic(gridPane);
                } else {
                    // Nothing to display here
                    setGraphic(null);
                }
            }
        });

        CLSTableview.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                // DoubleClick: Editor is opened
                if (click.getClickCount() == 1) {
                    btnCLSEdit.fire();
                }
            }
        });

        btnCLSNew.setOnAction(event -> {
            try {
                this.coursepassLecturerSubject = new CoursepassLecturerSubject(0L, getSqlConnectionManager());

                txtID.setText(this.coursepassLecturerSubject.getId().toString());
                txtID.setEditable(false);
                try {
                    cmbLecturer.getItems().setAll(Lecturer.getAllLecturer(Boolean.TRUE, getSqlConnectionManager()));
                    cmbLecturer.setValue(this.coursepassLecturerSubject.getLecturer());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    cmbSubject.getItems().setAll(Subject.getAllSubjects(Boolean.TRUE, getSqlConnectionManager()));
                    cmbSubject.setValue(this.coursepassLecturerSubject.getSubject());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    cmbRoom.getItems().setAll(Room.getAllRooms(Boolean.TRUE, getSqlConnectionManager()));
                    cmbRoom.setValue(this.coursepassLecturerSubject.getRoom());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                txtShouldHours.setText(this.coursepassLecturerSubject.getShouldHours().toString());
                chkActive.setSelected(this.coursepass.getActive());
                placeAfterDay.setValue(LocalDate.of(1970,1,1));
                editbox.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        btnCLSEdit.setOnAction(event -> {
            TableView.TableViewSelectionModel<CoursepassLecturerSubject> selectionModel = CLSTableview
                    .getSelectionModel();
            ObservableList<CoursepassLecturerSubject> selectedItems = selectionModel.getSelectedItems();
            if (selectedItems.size() > 0) {
                this.coursepassLecturerSubject = selectedItems.get(0);

                txtID.setText(this.coursepassLecturerSubject.getId().toString());
                txtID.setEditable(false);

                try {
                    cmbLecturer.getItems().setAll(Lecturer.getAllLecturer(Boolean.TRUE, getSqlConnectionManager()));
                    cmbLecturer.setValue(this.coursepassLecturerSubject.getLecturer());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    cmbSubject.getItems().setAll(Subject.getAllSubjects(Boolean.TRUE, getSqlConnectionManager()));
                    cmbSubject.setValue(this.coursepassLecturerSubject.getSubject());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    cmbRoom.getItems().setAll(Room.getAllRooms(Boolean.TRUE, getSqlConnectionManager()));
                    cmbRoom.setValue(this.coursepassLecturerSubject.getRoom());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                txtShouldHours.setText(this.coursepassLecturerSubject.getShouldHours().toString());
                chkActive.setSelected(this.coursepass.getActive());
                placeAfterDay.setValue(this.coursepassLecturerSubject.getPlaceAfterDay());
                editbox.setVisible(true);
            }
        });

        btnCLSBack.setOnAction(event -> {
            Stage stageTheEventSourceNodeBelongs = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/Coursepass.fxml"),
                    resources);
            try {
                AnchorPane anchorPane = loader.<AnchorPane>load();
                CoursepassController coursepassController = loader.<CoursepassController>getController();
                coursepassController.setSqlConnectionManager(getSqlConnectionManager());
                Scene scene = new Scene(anchorPane);
                stageTheEventSourceNodeBelongs.setScene(scene);
                coursepassController.setCoursepass(coursepass);
            } catch (Exception e) {
                // TODo: Propper Error handling
                e.printStackTrace();
            }
        });

        btnSave.setOnAction(event -> {
            this.coursepassLecturerSubject.setCoursepass(this.coursepass);
            this.coursepassLecturerSubject.setLecturer(cmbLecturer.getValue());
            this.coursepassLecturerSubject.setSubject(cmbSubject.getValue());
            this.coursepassLecturerSubject.setRoom(cmbRoom.getValue());
            this.coursepassLecturerSubject.setShouldHours(Long.parseLong(txtShouldHours.getText()));
            this.coursepassLecturerSubject.setActive(chkActive.isSelected());
            this.coursepassLecturerSubject.setPlaceAfterDay(placeAfterDay.getValue());
            try {
                this.coursepassLecturerSubject.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
            editbox.setVisible(false);
            CLSTableview.getItems().setAll(getCLS(!chkToogleCLS.isSelected()));
        });

        Platform.runLater(() -> {
            lblCoursepassName.setText(this.coursepass.getDescription());
            try {
                this.coursepass.updateCoursePassLecturerSubjects();
            } catch (Exception e) {
                e.printStackTrace();
            }
            CLSTableview.getItems().setAll(this.coursepass.getArrayCoursePassLecturerSubject());
        });
        chkToogleCLS.setOnAction(event -> {
            CLSTableview.getItems().setAll(getCLS(!chkToogleCLS.isSelected()));
        });

    }

    public ArrayList<CoursepassLecturerSubject> getCLS(Boolean activeState) {
        ArrayList<CoursepassLecturerSubject> activeSubject = new ArrayList<CoursepassLecturerSubject>();
        try {
            activeSubject = this.coursepass.getAllCLS(activeState);
        } catch (SQLException e) {
            // TODo: better error handling
            e.printStackTrace();
        }
        return activeSubject;
    }

    public void setCoursepass(CoursePass pCoursepass) {
        coursepass = pCoursepass;
    }

    public SQLConnectionManager getSqlConnectionManager() {
        return sqlConnectionManager;
    }

    public void setSqlConnectionManager(SQLConnectionManager sqlConnectionManager) {
        this.sqlConnectionManager = sqlConnectionManager;
    }

}
