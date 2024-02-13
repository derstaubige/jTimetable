package de.bremen.jTimetable.fxmlController;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import de.bremen.jTimetable.Classes.Location;
import de.bremen.jTimetable.Classes.SQLConnectionManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class LocationController implements Initializable {
    Location location;
    SQLConnectionManager sqlConnectionManager;

    @FXML
    private TableView<Location> LocationTableview;
    @FXML
    private TableColumn<Location, Long> ID;
    @FXML
    private TableColumn<Location, String> Caption;
    @FXML
    private TableColumn<Location, Boolean> Active;
    @FXML
    private Button btnLocationEdit;
    @FXML
    private Button btnLocationNew;
    @FXML
    private CheckBox chkToogleLocation;
    @FXML
    private VBox editbox;
    @FXML
    private TextField txtID;
    @FXML
    private TextField txtCaption;
    @FXML
    private CheckBox chkActive;
    @FXML
    private Button btnSave;
    @FXML 
    private MenuController mainMenuController;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            mainMenuController.setSqlConnectionManager(sqlConnectionManager);
            editbox.setVisible(false);
            ID.setCellValueFactory(new PropertyValueFactory<Location, Long>("id"));
            Caption.setCellValueFactory(new PropertyValueFactory<Location, String>("Caption"));
            Active.setCellValueFactory(new PropertyValueFactory<Location, Boolean>("active"));

            LocationTableview.getItems().setAll(getLocation(true));
        });

        // We need a StringConverter in order to ensure the selected item is displayed
        // properly
        // For this sample, we only want the Person's name to be displayed

        btnSave.setOnAction(event -> {
            this.location.setCaption(txtCaption.getText());
            this.location.setActive(chkActive.isSelected());
            try {
                this.location.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
            editbox.setVisible(false);
            LocationTableview.getItems().setAll(getLocation(!chkToogleLocation.isSelected()));
        });

        LocationTableview.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                // SingleClick: Editor is opened
                if (click.getClickCount() == 1) {
                    btnLocationEdit.fire();
                }
            }
        });

        btnLocationEdit.setOnAction(event -> {
            TableView.TableViewSelectionModel<Location> selectionModel = LocationTableview.getSelectionModel();
            ObservableList<Location> selectedItems = selectionModel.getSelectedItems();
            if (selectedItems.size() > 0) {
                this.location = selectedItems.get(0);

                txtID.setText(this.location.getId().toString());
                txtID.setEditable(false);
                txtCaption.setText(this.location.getCaption());
                chkActive.setSelected(this.location.getActive());

                editbox.setVisible(true);
            }
        });

        btnLocationNew.setOnAction(event -> {
            try {
                this.location = new Location(0L, getSqlConnectionManager());

                txtID.setText(this.location.getId().toString());
                txtID.setEditable(false);
                txtCaption.setText(this.location.getCaption());
                chkActive.setSelected(this.location.getActive());

                editbox.setVisible(true);

            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        chkToogleLocation.setOnAction(event -> {
            LocationTableview.getItems().setAll(getLocation(!chkToogleLocation.isSelected()));
        });
    }

    public ArrayList<Location> getLocation(Boolean activeState) {
        ArrayList<Location> activeLocation = new ArrayList<Location>();
        try {
            activeLocation = Location.getAllLocations(activeState, getSqlConnectionManager());
        } catch (SQLException e) {
            // TODo: better error handling
            e.printStackTrace();
        }
        return activeLocation;
    }

    public SQLConnectionManager getSqlConnectionManager() {
        return sqlConnectionManager;
    }

    public void setSqlConnectionManager(SQLConnectionManager sqlConnectionManager) {
        this.sqlConnectionManager = sqlConnectionManager;
    }

}
