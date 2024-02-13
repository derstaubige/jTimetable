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
import de.bremen.jTimetable.Classes.SQLConnectionManager;
import de.bremen.jTimetable.Classes.Subject;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class SubjectController implements Initializable {
    Subject subject;
    SQLConnectionManager sqlConnectionManager;

    @FXML    private TableView<Subject> SubjectTableview;
    @FXML    private TableColumn<Subject, Long> ID;
    @FXML    private TableColumn<Subject, String> Caption;
    @FXML    private TableColumn<Subject, Boolean> Active;
    @FXML    private Button btnSubjectEdit;
    @FXML    private Button btnSubjectNew;
    @FXML    private CheckBox chkToogleSubject;
    @FXML    private VBox editbox;
    @FXML private TextField txtID;
    @FXML private TextField txtCaption;
    @FXML private CheckBox chkActive;
    @FXML private Button btnSave;

    @Override
    public void initialize(URL location, ResourceBundle resources)  {
        Platform.runLater(() -> {
            editbox.setVisible(false);
            ID.setCellValueFactory(new PropertyValueFactory<Subject, Long>("id"));
            Caption.setCellValueFactory(new PropertyValueFactory<Subject, String>("caption"));
            Active.setCellValueFactory(new PropertyValueFactory<Subject, Boolean>("active"));
    
            SubjectTableview.getItems().setAll(getSubject(true));
        });   
        // We need a StringConverter in order to ensure the selected item is displayed properly
        // For this sample, we only want the Person's name to be displayed
       
        btnSave.setOnAction(event ->{
            this.subject.setCaption(txtCaption.getText());
            this.subject.setActive(chkActive.isSelected());
            try {
                this.subject.save();
            }catch (Exception e){
                e.printStackTrace();
            }
            editbox.setVisible(false);
            SubjectTableview.getItems().setAll(getSubject(!chkToogleSubject.isSelected()));
        });



       
        SubjectTableview.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                //SingleClick: Editor is opened
                if (click.getClickCount() == 1) {
                    btnSubjectEdit.fire();
                }
            }
        });

        btnSubjectEdit.setOnAction(event -> {
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
        });

        btnSubjectNew.setOnAction(event -> {
            try{
                this.subject = new Subject(0L, getSqlConnectionManager());

                txtID.setText(this.subject.getId().toString());
                txtID.setEditable(false);
                txtCaption.setText(this.subject.getCaption());
                chkActive.setSelected(this.subject.getActive());

                editbox.setVisible(true);

            }catch (Exception e){
                e.printStackTrace();
            }


        });

        chkToogleSubject.setOnAction(event -> {
            SubjectTableview.getItems().setAll(getSubject(!chkToogleSubject.isSelected()));
        });
    }

    public ArrayList<Subject> getSubject(Boolean activeState) {
        ArrayList<Subject> activeSubject = new ArrayList<Subject>();
        try {
            activeSubject = Subject.getAllSubjects(activeState, getSqlConnectionManager());
        } catch (SQLException e) {
            //TODo: better error handling
            e.printStackTrace();
        }
        return activeSubject;
    }

    public SQLConnectionManager getSqlConnectionManager() {
        return sqlConnectionManager;
    }

    public void setSqlConnectionManager(SQLConnectionManager sqlConnectionManager) {
        this.sqlConnectionManager = sqlConnectionManager;
    }
    
}
