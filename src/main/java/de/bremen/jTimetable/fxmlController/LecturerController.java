package de.bremen.jTimetable.fxmlController;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
import de.bremen.jTimetable.Main;
import de.bremen.jTimetable.Classes.*;
import de.bremen.jTimetable.Classes.Resourcesblocked.Resourcenames;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class LecturerController implements Initializable {
    Lecturer lecturer;

    @FXML private TableView<Lecturer> LecturerTableview;
    @FXML private TableColumn<Lecturer, Long> ID;
    @FXML private TableColumn<Lecturer, String> Firstname;
    @FXML private TableColumn<Lecturer, String> Lastname;
    @FXML private TableColumn<de.bremen.jTimetable.Classes.Location, String> Location;
    @FXML private TableColumn<Lecturer, Boolean> Active;
    @FXML    private TableView<Resourcesblocked> LecturerBlockedTableview;
    @FXML
    private TableColumn<Resourcesblocked, Long> LecturerBlockedTableviewID;
    @FXML
    private TableColumn<Resourcesblocked, Long> LecturerBlockedTableviewREFRESOURCEID;
    @FXML
    private TableColumn<Resourcesblocked, String> LecturerBlockedTableviewRESOURCENAME;
    @FXML
    private TableColumn<Resourcesblocked, LocalDate> LecturerBlockedTableviewSTARTDATE;
    @FXML
    private TableColumn<Resourcesblocked, LocalDate> LecturerBlockedTableviewENDDATE;
    @FXML
    private TableColumn<Resourcesblocked, Integer> LecturerBlockedTableviewSTARTTIMESLOT;
    @FXML
    private TableColumn<Resourcesblocked, Integer> LecturerBlockedTableviewENDTIMESLOT;
    @FXML
    private TableColumn<Resourcesblocked, String> LecturerBlockedTableviewDESCRIPTION;
    @FXML private Button btnLecturerEdit;
    @FXML private Button btnLecturerNew;
    @FXML private CheckBox chkToogleLecturer;
    @FXML private VBox editbox;
    @FXML private TextField txtID;
    @FXML private TextField txtFirstname;
    @FXML private TextField txtLastname;
    @FXML private ComboBox<de.bremen.jTimetable.Classes.Location> cmbLocation;
    @FXML private CheckBox chkActive;
    @FXML private Button btnSave;
    @FXML private Button btnShowTimetable;

    @Override
    public void initialize(URL location, ResourceBundle resources)  {
        editbox.setVisible(false);
        // We need a StringConverter in order to ensure the selected item is displayed properly
        // For this sample, we only want the Person's name to be displayed
        cmbLocation.setConverter(new StringConverter<de.bremen.jTimetable.Classes.Location>() {
            @Override
            public String toString(de.bremen.jTimetable.Classes.Location location) {
                if (location == null) {
                    return "";
                }else{
                    return location.getCaption();
                }
            }

            @Override
            public de.bremen.jTimetable.Classes.Location fromString(String string) {
                return null;
            }
        });
        cmbLocation.setCellFactory(cell -> new ListCell<de.bremen.jTimetable.Classes.Location>() {

            // Create our layout here to be reused for each ListCell
            GridPane gridPane = new GridPane();
            //Label lblID = new Label();
            Label lblDescription = new Label();

            // Static block to configure our layout
            {
                // Ensure all our column widths are constant
                gridPane.getColumnConstraints().addAll(
                       // new ColumnConstraints(100, 100, 100),
                        new ColumnConstraints(200, 200, 200)
                );

                //gridPane.add(lblID, 0, 1, 1 ,1);
                gridPane.add(lblDescription, 0, 1,1 ,1);

            }


            // We override the updateItem() method in order to provide our own layout for this Cell's graphicProperty
            @Override
            protected void updateItem(de.bremen.jTimetable.Classes.Location location, boolean empty) {
                super.updateItem(location, empty);

                if (!empty && location != null) {

                    // Update our Labels
                    //lblID.setText(studySection.getId().toString());
                    lblDescription.setText(location.getCaption());

                    // Set this ListCell's graphicProperty to display our GridPane
                    setGraphic(gridPane);
                } else {
                    // Nothing to display here
                    setGraphic(null);
                }
            }
        });

        btnSave.setOnAction(event ->{
            this.lecturer.setFirstname(txtFirstname.getText());
            this.lecturer.setLastname(txtLastname.getText());
            this.lecturer.setLocation(cmbLocation.getValue());
            this.lecturer.setActive(chkActive.isSelected());
            try {
                this.lecturer.save();
            }catch (Exception e){
                e.printStackTrace();
            }
            editbox.setVisible(false);
            LecturerTableview.getItems().setAll(getLecturer(!chkToogleLecturer.isSelected()));
        });

        Platform.runLater(() -> {

        });

        ID.setCellValueFactory(new PropertyValueFactory<Lecturer, Long>("id"));
        Firstname.setCellValueFactory(new PropertyValueFactory<Lecturer, String>("Firstname"));
        Lastname.setCellValueFactory(new PropertyValueFactory<Lecturer, String>("Lastname"));
        Location.setCellValueFactory(new PropertyValueFactory<de.bremen.jTimetable.Classes.Location, String>("locationCaption"));
        Active.setCellValueFactory(new PropertyValueFactory<Lecturer, Boolean>("active"));

        LecturerTableview.getItems().setAll(getLecturer(true));
        LecturerTableview.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                //SingleClick: Editor is opened
                if (click.getClickCount() == 1) {
                    btnLecturerEdit.fire();
                }
            }
        });

        LecturerBlockedTableviewID.setCellValueFactory(new PropertyValueFactory<Resourcesblocked, Long>("ID"));
        LecturerBlockedTableviewREFRESOURCEID
                .setCellValueFactory(new PropertyValueFactory<Resourcesblocked, Long>("ID"));
        LecturerBlockedTableviewRESOURCENAME
                .setCellValueFactory(new PropertyValueFactory<Resourcesblocked, String>("ID"));
        LecturerBlockedTableviewSTARTDATE
                .setCellValueFactory(new PropertyValueFactory<Resourcesblocked, LocalDate>("ID"));
        LecturerBlockedTableviewENDDATE
                .setCellValueFactory(new PropertyValueFactory<Resourcesblocked, LocalDate>("ID"));
        LecturerBlockedTableviewSTARTTIMESLOT
                .setCellValueFactory(new PropertyValueFactory<Resourcesblocked, Integer>("ID"));
        LecturerBlockedTableviewENDTIMESLOT
                .setCellValueFactory(new PropertyValueFactory<Resourcesblocked, Integer>("ID"));
        LecturerBlockedTableviewDESCRIPTION
                .setCellValueFactory(new PropertyValueFactory<Resourcesblocked, String>("ID"));

        
        btnLecturerEdit.setOnAction(event -> {
            TableView.TableViewSelectionModel<Lecturer> selectionModel = LecturerTableview.getSelectionModel();
            ObservableList<Lecturer> selectedItems = selectionModel.getSelectedItems();
            if (selectedItems.size() > 0) {
                //System.out.println(selectedItems.get(0).getId());
                this.lecturer = selectedItems.get(0);
                try{
                    cmbLocation.getItems().setAll(de.bremen.jTimetable.Classes.Location.getAllLocations(true));
                    cmbLocation.setValue(this.lecturer.getLocation());
                }catch (Exception e){
                    e.printStackTrace();
                }
                txtID.setText(this.lecturer.getId().toString());
                txtID.setEditable(false);
                txtFirstname.setText(this.lecturer.getFirstname());
                txtLastname.setText(this.lecturer.getLastname());
                cmbLocation.setValue(this.lecturer.getLocation());
                chkActive.setSelected(this.lecturer.getActive());

                LecturerBlockedTableview.getItems().setAll(Resourcesblocked
                        .getArrayListofResourcesblockeds(this.lecturer.getId(), Resourcenames.LECTURER));
                editbox.setVisible(true);
            }
        });

        btnLecturerNew.setOnAction(event -> {
            try{
                this.lecturer = new Lecturer(0L);
                try{
                    cmbLocation.getItems().setAll(de.bremen.jTimetable.Classes.Location.getAllLocations(true));
                    cmbLocation.setValue(this.lecturer.getLocation());
                }catch (Exception e){
                    e.printStackTrace();
                }
                txtID.setText(this.lecturer.getId().toString());
                txtID.setEditable(false);
                txtFirstname.setText(this.lecturer.getFirstname());
                txtLastname.setText(this.lecturer.getLastname());
                cmbLocation.setValue(this.lecturer.getLocation());
                chkActive.setSelected(this.lecturer.getActive());

                editbox.setVisible(true);

            }catch (Exception e){
                e.printStackTrace();
            }


        });

        chkToogleLecturer.setOnAction(event -> {
            LecturerTableview.getItems().setAll(getLecturer(!chkToogleLecturer.isSelected()));
        });

        btnShowTimetable.setOnAction(event ->{
            Lecturer lecturer = LecturerTableview.getSelectionModel().getSelectedItem();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/TimetableView.fxml"), resources);
            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setTitle("Timetable for " + lecturer.getLecturerFullName());
            loader.setLocation(Main.class.getResource("fxml/TimetableView.fxml"));
            try{
                stage.setScene(new Scene(loader.load()));
                TimetableViewController controller = loader.getController();
                controller.initDataTimetable(new Timetable(lecturer));
                stage.show();
            }catch (Exception e){
                e.printStackTrace();
            }

        });
    }

    public ArrayList<Lecturer> getLecturer(Boolean activeState) {
        ArrayList<Lecturer> activeLecturer = new ArrayList<Lecturer>();
        try {
            activeLecturer = Lecturer.getAllLecturer(activeState);
        } catch (SQLException e) {
            //TODo: better error handling
            e.printStackTrace();
        }
        return activeLecturer;
    }
}
