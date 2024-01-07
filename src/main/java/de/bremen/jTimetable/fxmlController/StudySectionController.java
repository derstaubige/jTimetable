package de.bremen.jTimetable.fxmlController;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import de.bremen.jTimetable.Classes.StudySection;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class StudySectionController implements Initializable {
    StudySection studySection;
    ResourceBundle resources;

    @FXML
    private TableView<StudySection> StudySectionTableview;
    @FXML
    private TableColumn<StudySection, Long> ID;
    @FXML
    private TableColumn<StudySection, String> Description;
    @FXML
    private TableColumn<StudySection, Boolean> Active;
    @FXML
    private CheckBox chkToogleStudySection;
    @FXML
    private VBox editbox;
    @FXML
    private TextField txtID;
    @FXML
    private TextField txtDescription;
    @FXML
    private CheckBox chkActive;
    @FXML
    private Button btnSave;

    @Override
    public void initialize(URL StudySection, ResourceBundle resources) {
        this.resources = resources;
        editbox.setVisible(false);
        // We need a StringConverter in order to ensure the selected item is displayed properly
        // For this sample, we only want the Person's name to be displayed

        btnSave.setOnAction(event -> {
            this.studySection.setDescription(txtDescription.getText());
            this.studySection.setActive(chkActive.isSelected());
            try {
                this.studySection.save();
            } catch (Exception e) {
                System.err.println("Saving study section did´n't work.");
            }
            editbox.setVisible(false);
            StudySectionTableview.getItems().setAll(getStudySection(!chkToogleStudySection.isSelected()));
        });

        Platform.runLater(() -> {});

        ID.setCellValueFactory(new PropertyValueFactory<StudySection, Long>("id"));
        Description.setCellValueFactory(new PropertyValueFactory<StudySection, String>("description"));
        Active.setCellValueFactory(new PropertyValueFactory<StudySection, Boolean>("active"));

        StudySectionTableview.getItems().setAll(getStudySection(true));
        StudySectionTableview.setOnMouseClicked(click -> {
            //Double Click: Editor is opened
            if (click.getClickCount() == 2) {
                editStudySection();
            }
        });
        chkToogleStudySection.setOnAction(event -> {
            StudySectionTableview.getItems().setAll(getStudySection(!chkToogleStudySection.isSelected()));
        });
    }

    /**
     * Create new study section.
     */
    public void newStudySection() {
        try {
            this.studySection = new StudySection(0L);

            txtID.setText(this.studySection.getId().toString());
            txtID.setEditable(false);
            txtDescription.setText(this.studySection.getDescription());
            chkActive.setSelected(this.studySection.getActive());

            editbox.setVisible(true);

        } catch (Exception e) {
            System.err.println("New study section could not be created.");
        }
    }

    /**
     * Open selected study section and editBox to edit it.
     */
    public void editStudySection() {
        TableView.TableViewSelectionModel<StudySection> selectionModel = StudySectionTableview.getSelectionModel();
        ObservableList<StudySection> selectedItems = selectionModel.getSelectedItems();
        if (selectedItems.size() == 1) {
            this.studySection = selectedItems.get(0);

            txtID.setText(this.studySection.getId().toString());
            txtID.setEditable(false);
            txtDescription.setText(this.studySection.getDescription());
            chkActive.setSelected(this.studySection.getActive());

            editbox.setVisible(true);
        }
    }

    /**
     * Get all study Section.
     *
     * @param activeState true: show all active study sections; false: show all inactive study sections
     * @return ArrayList of all active OR inactive study sections
     */
    public ArrayList<StudySection> getStudySection(Boolean activeState) {
        ArrayList<StudySection> activeStudySection = new ArrayList<>();
        try {
            activeStudySection = StudySection.getAllStudySections(activeState);
        } catch (SQLException e) {
            System.err.println("Method getAllStudySections failed in getStudySection");
        }
        return activeStudySection;
    }

    
    public void addTopmenuButtons(Scene scene){
        HBox topmenu = (HBox) scene.lookup("#topmenu");
        
        Button btnNew = new Button();
        try {
            URL url = getClass().getResource("/de/bremen/jTimetable/img/add1.png");
            Image image  = new Image(url.toExternalForm());
            ImageView imageView = new ImageView(image);
            btnNew.setGraphic(imageView);
            btnNew.setOnAction(e -> this.newStudySection());
            btnNew.getStyleClass().add("menuItem");
            btnNew.setText(resources.getString("menu.actions.new"));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Button btnEdit = new Button();
        try {
            URL url = getClass().getResource("/de/bremen/jTimetable/img/edit1.png");
            Image image  = new Image(url.toExternalForm());
            ImageView imageView = new ImageView(image);
            btnEdit.setGraphic(imageView);
            btnEdit.setOnAction(e -> this.editStudySection());
            btnEdit.getStyleClass().add("menuItem");
            btnEdit.setText(resources.getString("menu.actions.edit"));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Button btnClose = new Button();
        try {
            URL url = getClass().getResource("/de/bremen/jTimetable/img/exit.png");
            Image image  = new Image(url.toExternalForm());
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
