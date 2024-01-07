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
import de.bremen.jTimetable.Classes.Subject;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class SubjectController implements Initializable {
    Subject subject;
    ResourceBundle resources;

    @FXML
    private TableView<Subject> SubjectTableview;
    @FXML
    private TableColumn<Subject, Long> ID;
    @FXML
    private TableColumn<Subject, String> Caption;
    @FXML
    private TableColumn<Subject, Boolean> Active;
    @FXML
    private Button btnSubjectEdit;
    @FXML
    private Button btnSubjectNew;
    @FXML
    private CheckBox chkToogleSubject;
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
            this.subject.setCaption(txtCaption.getText());
            this.subject.setActive(chkActive.isSelected());
            try {
                this.subject.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
            editbox.setVisible(false);
            SubjectTableview.getItems().setAll(getSubject(!chkToogleSubject.isSelected()));
        });

        Platform.runLater(() -> {

        });

        ID.setCellValueFactory(new PropertyValueFactory<Subject, Long>("id"));
        Caption.setCellValueFactory(new PropertyValueFactory<Subject, String>("caption"));
        Active.setCellValueFactory(new PropertyValueFactory<Subject, Boolean>("active"));

        SubjectTableview.getItems().setAll(getSubject(true));
        SubjectTableview.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                // SingleClick: Editor is opened
                if (click.getClickCount() == 1) {
                    editSubject();
                }
            }
        });

        chkToogleSubject.setOnAction(event -> {
            SubjectTableview.getItems().setAll(getSubject(!chkToogleSubject.isSelected()));
        });
    }

    private void editSubject() {
        TableView.TableViewSelectionModel<Subject> selectionModel = SubjectTableview.getSelectionModel();
        ObservableList<Subject> selectedItems = selectionModel.getSelectedItems();
        if (selectedItems.size() > 0) {
            this.subject = selectedItems.get(0);

            txtID.setText(this.subject.getId().toString());
            txtID.setEditable(false);
            txtCaption.setText(this.subject.getCaption());
            chkActive.setSelected(this.subject.getActive());

            editbox.setVisible(true);
        }
    };

    private void newSubject() {
        try {
            this.subject = new Subject(0L);

            txtID.setText(this.subject.getId().toString());
            txtID.setEditable(false);
            txtCaption.setText(this.subject.getCaption());
            chkActive.setSelected(this.subject.getActive());

            editbox.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
        }

    };

    public ArrayList<Subject> getSubject(Boolean activeState) {
        ArrayList<Subject> activeSubject = new ArrayList<Subject>();
        try {
            activeSubject = Subject.getAllSubjects(activeState);
        } catch (SQLException e) {
            // TODo: better error handling
            e.printStackTrace();
        }
        return activeSubject;
    }

    public void addTopmenuButtons(Scene scene){
        HBox topmenu = (HBox) scene.lookup("#topmenu");
        
        Button btnNew = new Button();
        try {
            URL url = getClass().getResource("/de/bremen/jTimetable/img/add1.png");
            Image image  = new Image(url.toExternalForm());
            ImageView imageView = new ImageView(image);
            btnNew.setGraphic(imageView);
            btnNew.setOnAction(e -> this.newSubject());
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
            btnEdit.setOnAction(e -> this.editSubject());
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
