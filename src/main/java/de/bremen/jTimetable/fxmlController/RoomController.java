package de.bremen.jTimetable.fxmlController;

import javafx.application.Platform;
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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import de.bremen.jTimetable.Classes.Location;
import de.bremen.jTimetable.Classes.Room;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class RoomController implements Initializable {
    Room room;
    ResourceBundle resources;

    @FXML
    private TableView<Room> RoomTableview;
    @FXML
    private TableColumn<Room, Long> ID;
    @FXML
    private TableColumn<Room, String> Caption;
    @FXML
    private TableColumn<de.bremen.jTimetable.Classes.Location, String> TCLocation;
    @FXML
    private TableColumn<Room, Boolean> Active;
    @FXML
    private Button btnRoomEdit;
    @FXML
    private Button btnRoomNew;
    @FXML
    private CheckBox chkToogleRoom;
    @FXML
    private VBox editbox;
    @FXML
    private TextField txtID;
    @FXML
    private TextField txtCaption;
    @FXML
    private ComboBox<Location> cmbLocation;
    @FXML
    private CheckBox chkActive;
    @FXML
    private Button btnSave;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        editbox.setVisible(false);
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
            this.room.setCaption(txtCaption.getText());
            this.room.setLocation(cmbLocation.getValue());
            this.room.setActive(chkActive.isSelected());
            try {
                this.room.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
            editbox.setVisible(false);
            RoomTableview.getItems().setAll(getRoom(!chkToogleRoom.isSelected()));
        });

        Platform.runLater(() -> {

        });

        ID.setCellValueFactory(new PropertyValueFactory<Room, Long>("id"));
        Caption.setCellValueFactory(new PropertyValueFactory<Room, String>("caption"));
        TCLocation.setCellValueFactory(new PropertyValueFactory<Location, String>("locationCaption"));
        Active.setCellValueFactory(new PropertyValueFactory<Room, Boolean>("active"));

        RoomTableview.getItems().setAll(getRoom(true));
        RoomTableview.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                // SingleClick: Editor is opened
                if (click.getClickCount() == 1) {
                    editRoom();
                }
            }
        });

        chkToogleRoom.setOnAction(event -> {
            RoomTableview.getItems().setAll(getRoom(!chkToogleRoom.isSelected()));
        });
    }

    private void editRoom() {
        TableView.TableViewSelectionModel<Room> selectionModel = RoomTableview.getSelectionModel();
        ObservableList<Room> selectedItems = selectionModel.getSelectedItems();
        if (selectedItems.size() > 0) {
            // System.out.println(selectedItems.get(0).getId());
            this.room = selectedItems.get(0);
            try {
                cmbLocation.getItems().setAll(de.bremen.jTimetable.Classes.Location.getAllLocations(true));
                cmbLocation.setValue(this.room.getLocation());
            } catch (Exception e) {
                e.printStackTrace();
            }
            txtID.setText(this.room.getId().toString());
            txtID.setEditable(false);
            txtCaption.setText(this.room.getCaption());
            cmbLocation.setValue(this.room.getLocation());
            chkActive.setSelected(this.room.getActive());

            editbox.setVisible(true);
        }
    };

    private void newRoom() {
        try {
            this.room = new Room(0L);
            try {
                cmbLocation.getItems().setAll(de.bremen.jTimetable.Classes.Location.getAllLocations(true));
                cmbLocation.setValue(this.room.getLocation());
            } catch (Exception e) {
                e.printStackTrace();
            }
            txtID.setText(this.room.getId().toString());
            txtID.setEditable(false);
            txtCaption.setText(this.room.getCaption());
            cmbLocation.setValue(this.room.getLocation());
            chkActive.setSelected(this.room.getActive());

            editbox.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }

    };

    public ArrayList<Room> getRoom(Boolean activeState) {
        ArrayList<Room> activeRoom = new ArrayList<Room>();
        try {
            activeRoom = Room.getAllRooms(activeState);
        } catch (SQLException e) {
            // TODo: better error handling
            e.printStackTrace();
        }
        return activeRoom;
    }

    public void addTopmenuButtons(Scene scene) {
        HBox topmenu = (HBox) scene.lookup("#topmenu");

        Button btnNew = new Button();
        try {
            URL url = getClass().getResource("/de/bremen/jTimetable/img/add1.png");
            Image image = new Image(url.toExternalForm());
            ImageView imageView = new ImageView(image);
            btnNew.setGraphic(imageView);
            btnNew.setOnAction(e -> this.newRoom());
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
            btnEdit.setOnAction(e -> this.editRoom());
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
