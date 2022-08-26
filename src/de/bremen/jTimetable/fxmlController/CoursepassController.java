package de.bremen.jTimetable.fxmlController;

import de.bremen.jTimetable.Classes.Coursepass;
import de.bremen.jTimetable.Classes.StudySection;
import de.bremen.jTimetable.Main;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.ResourceBundle;

public class CoursepassController implements Initializable {
    Coursepass coursepass;
    @FXML private TextField txtCourseofStudyCaption;
    @FXML private ComboBox<StudySection> cmbStudySections;
    @FXML private DatePicker datStart;
    @FXML private DatePicker datEnd;
    @FXML private TextField txtDescription;
    @FXML private CheckBox chkActive;
    @FXML private Button btnBack;
    @FXML private Button btnSave;

    @Override
    public void initialize(URL location, ResourceBundle resources)  {
        // We need a StringConverter in order to ensure the selected item is displayed properly
        // For this sample, we only want the Person's name to be displayed
        cmbStudySections.setConverter(new StringConverter<StudySection>() {
            @Override
            public String toString(StudySection studySection) {
                if (studySection == null) {
                    return "";
                }else{
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
                gridPane.add(lblDescription, 0, 1,1 ,1);

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

        btnBack.setOnAction(event ->{
            Stage stageTheEventSourceNodeBelongs = (Stage) ((Node)event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader();
            URL url = Main.class.getResource("fxml/Home.fxml");
            loader.setLocation(url);
            try{
                AnchorPane anchorPane = loader.<AnchorPane>load();
                Scene scene = new Scene(anchorPane);
                stageTheEventSourceNodeBelongs.setScene(scene);
            }catch (Exception e ){
                //TODo: Propper Error handling
                System.out.println(e);
            }
        });

        btnSave.setOnAction(event ->{
            this.coursepass.setStudysection(cmbStudySections.getValue());
            this.coursepass.setStart(datStart.getValue());
            this.coursepass.setEnd(datEnd.getValue());
            this.coursepass.setActive(chkActive.isSelected());
            this.coursepass.setDescription(txtDescription.getText());
            try {
                this.coursepass.save();
            }catch (Exception e){
                //ToDo Fix Exeptionhandling
                System.out.println(e);
            }
            btnBack.fire();

        });

        Platform.runLater(() -> {
            txtCourseofStudyCaption.setText(this.coursepass.getCourseofstudycaption());
            try{
                cmbStudySections.getItems().setAll(StudySection.getStudySections(true));
                cmbStudySections.setValue(this.coursepass.getStudysection());
            }catch (Exception e){
                //ToDo: fix error handling
                System.out.println(e);
            }
            datStart.setValue(this.coursepass.getStart());
            datEnd.setValue(this.coursepass.getEnd());
            txtDescription.setText(this.coursepass.getDescription());
            chkActive.setSelected(this.coursepass.getActive());
            txtCourseofStudyCaption.setEditable(false);
        });
    }

    public Coursepass getCoursepass() {
        return coursepass;
    }

    public void setCoursepass(Coursepass coursepass) {
        this.coursepass = coursepass;
    }
}
