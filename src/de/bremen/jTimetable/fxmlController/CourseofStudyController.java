package de.bremen.jTimetable.fxmlController;

import de.bremen.jTimetable.Classes.CourseofStudy;
import de.bremen.jTimetable.Main;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CourseofStudyController implements Initializable {
    CourseofStudy cos;

    @FXML private TextField txtID;
    @FXML private TextField txtCaption;
    @FXML private DatePicker datBegin;
    @FXML private DatePicker datEnd;
    @FXML private CheckBox chkActive;
    @FXML private Button btnBack;
    @FXML private Button btnSave;



    @Override
    public void initialize(URL location, ResourceBundle resources)  {
        btnSave.setOnAction(event->{
            this.cos.setCaption(txtCaption.getText().trim());
            this.cos.setBegin(datBegin.getValue());
            this.cos.setEnd(datEnd.getValue());
            this.cos.setActive(chkActive.isSelected());

            //save changes
            try {
                this.cos.save();
            }catch (Exception e){
                //TODo: Propper Error handling
                System.out.println(e);
            }

            //redirect to home
            btnBack.fire();
        });

        Platform.runLater(() -> {
            txtID.setText("" + this.cos.getId());
            txtCaption.setText(this.cos.getCaption());
            datBegin.setValue(this.cos.getBegin());
            datEnd.setValue(this.cos.getEnd());
            chkActive.setSelected(this.cos.isActive());
            txtID.setEditable(false);
        });

        btnBack.setOnAction(event -> {
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
    }

    public void setID(CourseofStudy cos){
            this.cos = cos;
    }
}
