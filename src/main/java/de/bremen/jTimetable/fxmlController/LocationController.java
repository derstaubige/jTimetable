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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import de.bremen.jTimetable.Classes.Location;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class LocationController implements Initializable {
    Location location;
    ResourceBundle resources;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        editbox.setVisible(false);
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

        Platform.runLater(() -> {

        });

        ID.setCellValueFactory(new PropertyValueFactory<Location, Long>("id"));
        Caption.setCellValueFactory(new PropertyValueFactory<Location, String>("Caption"));
        Active.setCellValueFactory(new PropertyValueFactory<Location, Boolean>("active"));

        LocationTableview.getItems().setAll(getLocation(true));
        LocationTableview.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                // SingleClick: Editor is opened
                if (click.getClickCount() == 1) {
                    editLocation();
                }
            }
        });

        chkToogleLocation.setOnAction(event -> {
            LocationTableview.getItems().setAll(getLocation(!chkToogleLocation.isSelected()));
        });
    }

    private void editLocation() {
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
    };

    private void newLocation() {
        try {
            this.location = new Location(0L);

            txtID.setText(this.location.getId().toString());
            txtID.setEditable(false);
            txtCaption.setText(this.location.getCaption());
            chkActive.setSelected(this.location.getActive());

            editbox.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }

    };

    public ArrayList<Location> getLocation(Boolean activeState) {
        ArrayList<Location> activeLocation = new ArrayList<Location>();
        try {
            activeLocation = Location.getAllLocations(activeState);
        } catch (SQLException e) {
            // TODo: better error handling
            e.printStackTrace();
        }
        return activeLocation;
    }
    
    public void addTopmenuButtons(Scene scene) {
        HBox topmenu = (HBox) scene.lookup("#topmenu");

        Button btnNew = new Button();
        try {
            URL url = getClass().getResource("/de/bremen/jTimetable/img/add1.png");
            Image image = new Image(url.toExternalForm());
            ImageView imageView = new ImageView(image);
            btnNew.setGraphic(imageView);
            btnNew.setOnAction(e -> this.newLocation());
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
            btnEdit.setOnAction(e -> this.editLocation());
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
