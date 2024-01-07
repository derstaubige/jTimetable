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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;
import de.bremen.jTimetable.Main;
import de.bremen.jTimetable.Classes.*;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class CoursepassController implements Initializable {

    private BackgroundController backgroundController;
    CoursePass coursepass;
    ResourceBundle resources;

    @FXML
    private ComboBox<CourseofStudy> cmbCourseofStudy;
    @FXML
    private ComboBox<StudySection> cmbStudySections;
    @FXML
    private DatePicker datStart;
    @FXML
    private DatePicker datEnd;
    @FXML
    private TextField txtDescription;
    @FXML
    private CheckBox chkActive;
    @FXML
    private Button btnBack;
    @FXML
    private Button btnSave;
    @FXML
    private TableView<CoursePass> CoursepassTableview;
    @FXML
    private TableColumn<CoursePass, Long> CPID;
    @FXML
    private TableColumn<CoursePass, String> CPCOSCaption;
    @FXML
    private TableColumn<CoursePass, String> CPstudysection;
    @FXML
    private TableColumn<CoursePass, String> CPDescription;
    @FXML
    private TableColumn<CoursePass, LocalDate> CPStart;
    @FXML
    private TableColumn<CoursePass, LocalDate> CPEnd;
    @FXML
    private TableColumn<CoursePass, Boolean> CPActive;
    @FXML
    private CheckBox chkToogleCoursepass;
    @FXML
    private VBox editbox;
    @FXML
    private Button btnEditCLS;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.resources = resources;
        editbox.setVisible(false);
        // We need a StringConverter in order to ensure the selected item is displayed properly
        // For this sample, we only want the Person's name to be displayed
        cmbStudySections.setConverter(new StringConverter<StudySection>() {
            @Override
            public String toString(StudySection studySection) {
                if (studySection == null) {
                    return "";
                } else {
                    return studySection.getDescription();
                }
            }

            @Override
            public StudySection fromString(String string) {
                return null;
            }
        });
        cmbStudySections.setCellFactory(cell -> new ListCell<StudySection>() {

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
                gridPane.add(lblDescription, 0, 1, 1, 1);

            }


            // We override the updateItem() method in order to provide our own layout for this Cell's graphicProperty
            @Override
            protected void updateItem(StudySection studySection, boolean empty) {
                super.updateItem(studySection, empty);

                if (!empty && studySection != null) {

                    // Update our Labels
                    //lblID.setText(studySection.getId().toString());
                    lblDescription.setText(studySection.getDescription());

                    // Set this ListCell's graphicProperty to display our GridPane
                    setGraphic(gridPane);
                } else {
                    // Nothing to display here
                    setGraphic(null);
                }
            }
        });

        // We need a StringConverter in order to ensure the selected item is displayed properly
        // For this sample, we only want the Person's name to be displayed
        cmbCourseofStudy.setConverter(new StringConverter<CourseofStudy>() {
            @Override
            public String toString(CourseofStudy courseofStudy) {
                if (courseofStudy == null) {
                    return "";
                } else {
                    return courseofStudy.getCaption();
                }
            }

            @Override
            public CourseofStudy fromString(String string) {
                return null;
            }
        });
        cmbCourseofStudy.setCellFactory(cell -> new ListCell<CourseofStudy>() {

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
                gridPane.add(lblDescription, 0, 1, 1, 1);

            }


            // We override the updateItem() method in order to provide our own layout for this Cell's graphicProperty
            @Override
            protected void updateItem(CourseofStudy courseofStudy, boolean empty) {
                super.updateItem(courseofStudy, empty);

                if (!empty && courseofStudy != null) {

                    // Update our Labels
                    //lblID.setText(studySection.getId().toString());
                    lblDescription.setText(courseofStudy.getCaption());

                    // Set this ListCell's graphicProperty to display our GridPane
                    setGraphic(gridPane);
                } else {
                    // Nothing to display here
                    setGraphic(null);
                }
            }
        });

        btnSave.setOnAction(event -> {
            this.coursepass.setStudySection(cmbStudySections.getValue());
            this.coursepass.setStart(datStart.getValue());
            this.coursepass.setEnd(datEnd.getValue());
            this.coursepass.setActive(chkActive.isSelected());
            this.coursepass.setDescription(txtDescription.getText());
            this.coursepass.setCourseOfStudy(cmbCourseofStudy.getValue());
            try {
                this.coursepass.save();
            } catch (Exception e) {
                e.printStackTrace();
            }
            editbox.setVisible(false);
            CoursepassTableview.getItems().setAll(getCoursepass(!chkToogleCoursepass.isSelected()));
        });

        Platform.runLater(() -> {

        });

        CPID.setCellValueFactory(new PropertyValueFactory<CoursePass, Long>("id"));
        CPCOSCaption.setCellValueFactory(new PropertyValueFactory<CoursePass, String>("courseOfStudyCaption"));
        CPstudysection.setCellValueFactory(new PropertyValueFactory<CoursePass, String>("CPStudySection"));
        CPDescription.setCellValueFactory(new PropertyValueFactory<CoursePass, String>("description"));
        CPStart.setCellValueFactory(new PropertyValueFactory<CoursePass, LocalDate>("start"));
        CPEnd.setCellValueFactory(new PropertyValueFactory<CoursePass, LocalDate>("end"));
        CPActive.setCellValueFactory(new PropertyValueFactory<CoursePass, Boolean>("active"));

        CoursepassTableview.getItems().setAll(getCoursepass(true));
        CoursepassTableview.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent click) {
                //SingleClick: Editor is opened
                if (click.getClickCount() == 1) {
                    editCoursePass();
                }
                //DoubleClick: Timetable is shown
                if (click.getClickCount() == 2) {
                    TableView.TableViewSelectionModel<CoursePass> selectionModel = CoursepassTableview.getSelectionModel();
                    ObservableList<CoursePass> selectedItems = selectionModel.getSelectedItems();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/TimetableView.fxml"));
                    Stage stage = new Stage(StageStyle.DECORATED);
                    URL url = Main.class.getResource("fxml/TimetableView.fxml");
                    loader.setLocation(url);
                    try {
                        stage.setScene(new Scene(loader.load()));
                        stage.setTitle("Timetable");
                        TimetableViewController controller = loader.getController();
                        controller.initDataCoursepass(new CoursePass((selectedItems.get(0).getId())));
                        stage.show();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        btnEditCLS.setOnAction(event -> {
            TableView.TableViewSelectionModel<CoursePass> selectionModel = this.CoursepassTableview.getSelectionModel();
            ObservableList<CoursePass> selectedItems = selectionModel.getSelectedItems();
            if (!selectedItems.isEmpty()) {
                // backgroundController.openCoursePassLecturerSubject(selectedItems);
            }
        });

        chkToogleCoursepass.setOnAction(event -> {
            CoursepassTableview.getItems().setAll(getCoursepass(!chkToogleCoursepass.isSelected()));
        });

    }

    /**
     * Create a new coursePass.
     */
    public void newCoursePass() {
        try {
            this.coursepass = new CoursePass(0L);

            this.cmbCourseofStudy.getItems().setAll(this.coursepass.getCourseOfStudy().getCoursesofStudy(true));
            this.cmbCourseofStudy.setValue(this.coursepass.getCourseOfStudy());

            this.cmbStudySections.getItems().setAll(StudySection.getStudySections(true));

            this.datStart.setValue(this.coursepass.getStart());
            this.datEnd.setValue(this.coursepass.getEnd());
            this.txtDescription.setText(this.coursepass.getDescription());
            this.chkActive.setSelected(this.coursepass.getActive());
            //cmbCourseofStudy.setEditable(false);
            this.editbox.setVisible(true);

        } catch (Exception e) {
            System.err.println("New coursePass could not be created in controller.");
        }
    }

    /**
     * Open editBox to edit selected coursePass.
     */
    public void editCoursePass() {
        TableView.TableViewSelectionModel<CoursePass> selectionModel = this.CoursepassTableview.getSelectionModel();
        ObservableList<CoursePass> selectedItems = selectionModel.getSelectedItems();
        if (selectedItems.size() == 1) {
            //System.out.println(selectedItems.get(0).getId());
            this.coursepass = selectedItems.get(0);
            try {
                this.cmbCourseofStudy.getItems().setAll(this.coursepass.getCourseOfStudy().getCoursesofStudy(true));
                this.cmbCourseofStudy.setValue(this.coursepass.getCourseOfStudy());

                this.cmbStudySections.getItems().setAll(StudySection.getStudySections(true));
                this.cmbStudySections.setValue(this.coursepass.getStudySection());
            } catch (Exception e) {
                System.err.println(" StudySections or courses of study could not be fetched therefor coursePass " +
                        "can't be edited.");
            }
            this.datStart.setValue(this.coursepass.getStart());
            this.datEnd.setValue(this.coursepass.getEnd());
            this.txtDescription.setText(this.coursepass.getDescription());
            this.chkActive.setSelected(this.coursepass.getActive());
            //cmbCourseofStudy.setEditable(false);

            this.editbox.setVisible(true);
        }
    }

    /**
     * Creates the initial timetable for a coursePass.
     */
    public void createInitialTimetable() {
        TableView.TableViewSelectionModel<CoursePass> selectionModel = this.CoursepassTableview.getSelectionModel();
        ObservableList<CoursePass> selectedItems = selectionModel.getSelectedItems();
        if (!selectedItems.isEmpty()) {
            this.coursepass = selectedItems.get(0);

            Resourcemanager resourcemanager = new Resourcemanager();
            try {
                this.coursepass.updateCoursePassLecturerSubjects();
                resourcemanager.generateInitialTimetable(this.coursepass);
            } catch (Exception e) {
                System.err.println("Creating initial timetable didn't work.");
            }
        }
    }

    /**
     * Delete the saved timetable.
     */
    public void deleteTimetable() {
        TableView.TableViewSelectionModel<CoursePass> selectionModel = this.CoursepassTableview.getSelectionModel();
        ObservableList<CoursePass> selectedItems = selectionModel.getSelectedItems();
        if (!selectedItems.isEmpty()) {
            this.coursepass = selectedItems.get(0);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Want to delete Timetable for " + this.coursepass.getDescription());
            alert.setHeaderText("");
            alert.setContentText("Really want to delete the Timetable for " + this.coursepass.getCourseOfStudy().getCaption()
                    + " " + this.coursepass.getStudySection().getDescription() + "?");
            alert.showAndWait().ifPresent(rs -> {
                if (rs == ButtonType.OK) {
                    Timetable timetable = new Timetable(this.coursepass);
                    timetable.deleteTimetable();

                    alert.setAlertType(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setContentText("The Timetable for " + this.coursepass.getCourseOfStudy().getCaption()
                            + " " + this.coursepass.getStudySection().getDescription() + " has been deleted.");
                    alert.show();

                }
            });

        }
    }

    /**
     * Getter.
     *
     * @return current coursePass
     */
    public CoursePass getCoursepass() {
        return this.coursepass;
    }

    /**
     * Setter.
     * ToDo should this mark the row in the tableView and open the edit space automatically
     *
     * @param coursepass selected coursePass
     */
    public void setCoursepass(CoursePass coursepass) {
        this.coursepass = coursepass;
    }

    /**
     * Setter.
     *
     * @param backgroundController controller of background needs to be accessed to load coursePassLecturerSubject.
     */
    public void setBackgroundController(BackgroundController backgroundController) {
        this.backgroundController = backgroundController;
    }

    /**
     * Getter for arrayList of all coursePasses
     *
     * @param activeState true: return all active coursePasses; false: return all inactive coursePasses
     * @return returns all active OR inactive coursePasses
     */
    public ArrayList<CoursePass> getCoursepass(Boolean activeState) {
        ArrayList<CoursePass> activeCoursepass = new ArrayList<>();
        try {
            activeCoursepass = CoursePass.getCoursePasses(activeState);
        } catch (SQLException e) {
            System.err.println("Get all coursePasses failed because of an SQLException.");
        }
        return activeCoursepass;
    }

    
    public void addTopmenuButtons(Scene scene){
        HBox topmenu = (HBox) scene.lookup("#topmenu");
        
        Button btnNew = new Button();
        try {
            URL url = getClass().getResource("/de/bremen/jTimetable/img/add1.png");
            Image image  = new Image(url.toExternalForm());
            ImageView imageView = new ImageView(image);
            btnNew.setGraphic(imageView);
            btnNew.setOnAction(e -> this.newCoursePass());
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
            btnEdit.setOnAction(e -> this.editCoursePass());
            btnEdit.getStyleClass().add("menuItem");
            btnEdit.setText(resources.getString("menu.actions.edit"));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Button btncreateInitialTimetable = new Button();
        try {
            URL url = getClass().getResource("/de/bremen/jTimetable/img/data1.png");
            Image image  = new Image(url.toExternalForm());
            ImageView imageView = new ImageView(image);
            btncreateInitialTimetable.setGraphic(imageView);
            btncreateInitialTimetable.setOnAction(e -> this.createInitialTimetable());
            btncreateInitialTimetable.getStyleClass().add("menuItem");
            btncreateInitialTimetable.setText(resources.getString("menu.actions.edit"));
            
        } catch (Exception e) {
            e.printStackTrace();
        }        
        
        Button btndeleteTimetable = new Button();
        try {
            URL url = getClass().getResource("/de/bremen/jTimetable/img/data1.png");
            Image image  = new Image(url.toExternalForm());
            ImageView imageView = new ImageView(image);
            btndeleteTimetable.setGraphic(imageView);
            btndeleteTimetable.setOnAction(e -> this.deleteTimetable());
            btndeleteTimetable.getStyleClass().add("menuItem");
            btndeleteTimetable.setText(resources.getString("menu.actions.edit"));
            
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
        topmenu.getChildren().addAll(btnNew, btnEdit, btncreateInitialTimetable, btndeleteTimetable, btnClose);
    }

}

