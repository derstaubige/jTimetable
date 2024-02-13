package de.bremen.jTimetable.fxmlController;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.LocalDateStringConverter;
import de.bremen.jTimetable.Main;
import de.bremen.jTimetable.Classes.*;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class LecturerController implements Initializable {
    Lecturer lecturer;
    ResourceBundle resourceBundle;
    SQLConnectionManager sqlConnectionManager;

    @FXML
    private TableView<Lecturer> LecturerTableview;
    @FXML
    private TableColumn<Lecturer, Long> ID;
    @FXML
    private TableColumn<Lecturer, String> Firstname;
    @FXML
    private TableColumn<Lecturer, String> Lastname;
    @FXML
    private TableColumn<de.bremen.jTimetable.Classes.Location, String> Location;
    @FXML
    private TableColumn<Lecturer, Boolean> Active;
    @FXML
    private TableView<ResourcesBlocked> LecturerBlockedTableview;
    @FXML
    private TableColumn<ResourcesBlocked, Long> LecturerBlockedTableviewID;
    @FXML
    private TableColumn<ResourcesBlocked, Long> LecturerBlockedTableviewREFRESOURCEID;
    @FXML
    private TableColumn<ResourcesBlocked, String> LecturerBlockedTableviewRESOURCENAME;
    @FXML
    private TableColumn<ResourcesBlocked, LocalDate> LecturerBlockedTableviewSTARTDATE;
    @FXML
    private TableColumn<ResourcesBlocked, LocalDate> LecturerBlockedTableviewENDDATE;
    @FXML
    private TableColumn<ResourcesBlocked, Integer> LecturerBlockedTableviewSTARTTIMESLOT;
    @FXML
    private TableColumn<ResourcesBlocked, Integer> LecturerBlockedTableviewENDTIMESLOT;
    @FXML
    private TableColumn<ResourcesBlocked, String> LecturerBlockedTableviewDESCRIPTION;
    @FXML
    private TableColumn<ResourcesBlocked, Void> LecturerBlockedTableviewDelete;
    @FXML
    private Button LecturerBlockedAdd;
    @FXML
    private Button LecturerBlockedPermaAdd;
    @FXML
    private Button btnLecturerEdit;
    @FXML
    private Button btnLecturerNew;
    @FXML
    private CheckBox chkToogleLecturer;
    @FXML
    private VBox editbox;
    @FXML
    private TextField txtID;
    @FXML
    private TextField txtFirstname;
    @FXML
    private TextField txtLastname;
    @FXML
    private ComboBox<de.bremen.jTimetable.Classes.Location> cmbLocation;
    @FXML
    private CheckBox chkActive;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnShowTimetable;
    @FXML
    private ScrollPane scrollPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            editbox.setVisible(false);
            ID.setCellValueFactory(new PropertyValueFactory<Lecturer, Long>("id"));
            Firstname.setCellValueFactory(new PropertyValueFactory<Lecturer, String>("Firstname"));
            Lastname.setCellValueFactory(new PropertyValueFactory<Lecturer, String>("Lastname"));
            Location.setCellValueFactory(
                    new PropertyValueFactory<de.bremen.jTimetable.Classes.Location, String>("locationCaption"));
            Active.setCellValueFactory(new PropertyValueFactory<Lecturer, Boolean>("active"));
    
            LecturerTableview.getItems().setAll(getLecturer(true));
            LecturerBlockedTableview.setEditable(true);
        });
        // We need a StringConverter in order to ensure the selected item is displayed
        // properly
        // For this sample, we only want the Person's name to be displayed
        cmbLocation.setConverter(new StringConverter<de.bremen.jTimetable.Classes.Location>() {
            @Override
            public String toString(de.bremen.jTimetable.Classes.Location location) {
                if (location == null) {
                    return "";
                } else {
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
            protected void updateItem(de.bremen.jTimetable.Classes.Location location, boolean empty) {
                super.updateItem(location, empty);

                if (!empty && location != null) {

                    // Update our Labels
                    // lblID.setText(studySection.getId().toString());
                    lblDescription.setText(location.getCaption());

                    // Set this ListCell's graphicProperty to display our GridPane
                    setGraphic(gridPane);
                } else {
                    // Nothing to display here
                    setGraphic(null);
                }
            }
        });

        btnSave.setOnAction(event -> {
            this.lecturer.setFirstname(txtFirstname.getText());
            this.lecturer.setLastname(txtLastname.getText());
            this.lecturer.setLocation(cmbLocation.getValue());
            this.lecturer.setActive(chkActive.isSelected());
            try {
                this.lecturer.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
            editbox.setVisible(false);
            LecturerTableview.getItems().setAll(getLecturer(!chkToogleLecturer.isSelected()));
        });



        LecturerTableview.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                // SingleClick: Editor is opened
                if (click.getClickCount() == 1) {
                    btnLecturerEdit.fire();
                }
            }
        });

        LecturerBlockedTableviewID.setCellValueFactory(new PropertyValueFactory<ResourcesBlocked, Long>("ID"));
        LecturerBlockedTableviewREFRESOURCEID
                .setCellValueFactory(new PropertyValueFactory<ResourcesBlocked, Long>("reResourceID"));
        LecturerBlockedTableviewRESOURCENAME
                .setCellValueFactory(new PropertyValueFactory<ResourcesBlocked, String>("resourceName"));
        LecturerBlockedTableviewSTARTDATE
                .setCellValueFactory(new PropertyValueFactory<ResourcesBlocked, LocalDate>("startDate"));
        LecturerBlockedTableviewSTARTDATE.setCellFactory(cellData -> {
            TextFieldTableCell<ResourcesBlocked, LocalDate> textFieldTableCell = new TextFieldTableCell<ResourcesBlocked, LocalDate>(
                    new LocalDateStringConverter());
            return textFieldTableCell;
        });
        LecturerBlockedTableviewSTARTDATE.setOnEditCommit(
                e -> {
                    e.getTableView().getItems().get(e.getTablePosition().getRow()).setStartDate(e.getNewValue());
                    e.getTableView().getItems().get(e.getTablePosition().getRow()).save();
                });

        LecturerBlockedTableviewENDDATE
                .setCellValueFactory(new PropertyValueFactory<ResourcesBlocked, LocalDate>("endDate"));
        LecturerBlockedTableviewENDDATE.setCellFactory(cellData -> {
            TextFieldTableCell<ResourcesBlocked, LocalDate> textFieldTableCell = new TextFieldTableCell<ResourcesBlocked, LocalDate>(
                    new LocalDateStringConverter());
            return textFieldTableCell;
        });
        LecturerBlockedTableviewENDDATE.setOnEditCommit(
                e -> {
                    e.getTableView().getItems().get(e.getTablePosition().getRow()).setEndDate(e.getNewValue());
                    e.getTableView().getItems().get(e.getTablePosition().getRow()).save();
                });

        LecturerBlockedTableviewSTARTTIMESLOT
                .setCellValueFactory(new PropertyValueFactory<ResourcesBlocked, Integer>("startTimeslot"));
        LecturerBlockedTableviewSTARTTIMESLOT.setCellFactory(cellData -> {
            TextFieldTableCell<ResourcesBlocked, Integer> textFieldTableCell = new TextFieldTableCell<ResourcesBlocked, Integer>(
                    new IntegerStringConverter());
            return textFieldTableCell;
        });
        LecturerBlockedTableviewSTARTTIMESLOT.setOnEditCommit(
                e -> {
                    e.getTableView().getItems().get(e.getTablePosition().getRow()).setStartTimeslot(e.getNewValue());
                    e.getTableView().getItems().get(e.getTablePosition().getRow()).save();
                });

        LecturerBlockedTableviewENDTIMESLOT
                .setCellValueFactory(new PropertyValueFactory<ResourcesBlocked, Integer>("endTimeslot"));
        LecturerBlockedTableviewENDTIMESLOT.setCellFactory(cellData -> {
            TextFieldTableCell<ResourcesBlocked, Integer> textFieldTableCell = new TextFieldTableCell<ResourcesBlocked, Integer>(
                    new IntegerStringConverter());
            return textFieldTableCell;
        });
        LecturerBlockedTableviewENDTIMESLOT.setOnEditCommit(
                e -> {
                    e.getTableView().getItems().get(e.getTablePosition().getRow()).setEndTimeslot(e.getNewValue());
                    e.getTableView().getItems().get(e.getTablePosition().getRow()).save();
                });
        LecturerBlockedTableviewDESCRIPTION
                .setCellValueFactory(new PropertyValueFactory<ResourcesBlocked, String>("Description"));
        LecturerBlockedTableviewDESCRIPTION.setCellFactory(cellData -> {
            TextFieldTableCell<ResourcesBlocked, String> textFieldTableCell = new TextFieldTableCell<ResourcesBlocked, String>(
                    new StringConverter<String>() {
                        public String toString(String str) {
                            return str;
                        }

                        public String fromString(String str) {
                            return str;
                        }
                    });
            return textFieldTableCell;
        });
        LecturerBlockedTableviewDESCRIPTION.setOnEditCommit(
                e -> {
                    e.getTableView().getItems().get(e.getTablePosition().getRow()).setDescription(e.getNewValue());
                    e.getTableView().getItems().get(e.getTablePosition().getRow()).save();
                });

        LecturerBlockedTableviewDelete
                .setCellFactory(new Callback<TableColumn<ResourcesBlocked, Void>, TableCell<ResourcesBlocked, Void>>() {
                    @Override
                    public TableCell<ResourcesBlocked, Void> call(final TableColumn<ResourcesBlocked, Void> param) {
                        final TableCell<ResourcesBlocked, Void> cell = new TableCell<ResourcesBlocked, Void>() {

                            private final Button btn = new Button(
                                    resources.getString("lecturer.LecturerBlockedTableview.Delete"));

                            {
                                btn.setOnAction((ActionEvent event) -> {
                                    Alert alert = new Alert(AlertType.CONFIRMATION);
                                    alert.setTitle(resources
                                            .getString("lecturer.LecturerBlockedTableview.DeleteMessageTitle"));
                                    alert.setHeaderText("");
                                    alert.setContentText(
                                            resources.getString("lecturer.LecturerBlockedTableview.DeleteMessage"));
                                    alert.showAndWait().ifPresent(rs -> {
                                        if (rs == ButtonType.OK) {
                                            ResourcesBlocked resourcesblocked = getTableView().getItems()
                                                    .get(getIndex());
                                            try {
                                                resourcesblocked.delete();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            LecturerBlockedTableview.getItems().remove(resourcesblocked);
                                        }
                                    });
                                });
                            }

                            @Override
                            public void updateItem(Void item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    setGraphic(btn);
                                }
                            }
                        };
                        return cell;
                    }
                });

        LecturerBlockedAdd.setOnAction(event -> {
            try {
                ResourcesBlocked resourcesblocked = new ResourcesBlocked(0L, getSqlConnectionManager());
                resourcesblocked.setReResourceID(lecturer.getId());
                resourcesblocked.setResourceName(ResourceNames.LECTURER);
                resourcesblocked.setStartDate(LocalDate.now());
                resourcesblocked.setEndDate(LocalDate.now());
                resourcesblocked.save();
                LecturerBlockedTableview.getItems().add(resourcesblocked);
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        LecturerBlockedPermaAdd.setOnAction(event -> {
            // System.out.println(selectedItems.get(0).getId());
            Stage stageTheEventSourceNodeBelongs = (Stage) LecturerBlockedPermaAdd.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("fxml/LecturerBlocks.fxml"), resources);
            TableView.TableViewSelectionModel<Lecturer> selectionModel = LecturerTableview.getSelectionModel();
            ObservableList<Lecturer> selectedItems = selectionModel.getSelectedItems();
            try {
                AnchorPane anchorPane = loader.<AnchorPane>load();
                LecturerBlocksController lecturerBlocksController = loader.<LecturerBlocksController>getController();
                lecturerBlocksController.setLecturer(selectedItems.get(0));
                lecturerBlocksController.populateBlocked();
                Scene scene = new Scene(anchorPane);
                stageTheEventSourceNodeBelongs.setScene(scene);
            } catch (Exception e) {
                // TODo: Propper Error handling
                e.printStackTrace();
            }
        });

        btnLecturerEdit.setOnAction(event -> {
            TableView.TableViewSelectionModel<Lecturer> selectionModel = LecturerTableview.getSelectionModel();
            ObservableList<Lecturer> selectedItems = selectionModel.getSelectedItems();
            if (selectedItems.size() > 0) {
                // System.out.println(selectedItems.get(0).getId());
                this.lecturer = selectedItems.get(0);
                try {
                    cmbLocation.getItems().setAll(
                            de.bremen.jTimetable.Classes.Location.getAllLocations(true, getSqlConnectionManager()));
                    cmbLocation.setValue(this.lecturer.getLocation());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                txtID.setText(this.lecturer.getId().toString());
                txtID.setEditable(false);
                txtFirstname.setText(this.lecturer.getFirstname());
                txtLastname.setText(this.lecturer.getLastname());
                cmbLocation.setValue(this.lecturer.getLocation());
                chkActive.setSelected(this.lecturer.getActive());

                LecturerBlockedTableview.getItems().setAll(ResourcesBlocked
                        .getArrayListofResourcesblocked(this.lecturer.getId(), ResourceNames.LECTURER,
                                getSqlConnectionManager()));
                editbox.setVisible(true);
            }

        });

        btnLecturerNew.setOnAction(event -> {
            try {
                this.lecturer = new Lecturer(0L, getSqlConnectionManager());
                try {
                    cmbLocation.getItems().setAll(
                            de.bremen.jTimetable.Classes.Location.getAllLocations(true, getSqlConnectionManager()));
                    cmbLocation.setValue(this.lecturer.getLocation());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                txtID.setText(this.lecturer.getId().toString());
                txtID.setEditable(false);
                txtFirstname.setText(this.lecturer.getFirstname());
                txtLastname.setText(this.lecturer.getLastname());
                cmbLocation.setValue(this.lecturer.getLocation());
                chkActive.setSelected(this.lecturer.getActive());

                editbox.setVisible(true);

            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        chkToogleLecturer.setOnAction(event -> {
            LecturerTableview.getItems().setAll(getLecturer(!chkToogleLecturer.isSelected()));
        });

        btnShowTimetable.setOnAction(event -> {
            Lecturer lecturer = LecturerTableview.getSelectionModel().getSelectedItem();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/TimetableView.fxml"), resources);
            Stage stage = new Stage(StageStyle.DECORATED);
            stage.setTitle("Timetable for " + lecturer.getLecturerFullName());
            loader.setLocation(Main.class.getResource("fxml/TimetableView.fxml"));
            try {
                stage.setScene(new Scene(loader.load()));
                TimetableViewController controller = loader.getController();
                controller.initDataTimetable(new Timetable(lecturer, getSqlConnectionManager()));
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }



    public ArrayList<Lecturer> getLecturer(Boolean activeState) {
        return Lecturer.getAllLecturer(activeState, getSqlConnectionManager());
    }

    public SQLConnectionManager getSqlConnectionManager() {
        return sqlConnectionManager;
    }

    public void setSqlConnectionManager(SQLConnectionManager sqlConnectionManager) {
        this.sqlConnectionManager = sqlConnectionManager;
    }

}
