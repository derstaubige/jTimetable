package de.bremen.jTimetable.fxmlController;

import de.bremen.jTimetable.Classes.Coursepass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class TimetableViewController implements Initializable {

    @FXML
    public ScrollPane scrllpn_TimetableView;

    @FXML
    public GridPane grdpn_TimetableView;

    private Coursepass coursepass;

    /**
     * Can be called to hand parameters from the calling class to this controller
     *
     * @param coursepass the coursepass that was selected and for which the timetable is shown
     */
    public void initData(Coursepass coursepass) {
        this.coursepass = coursepass;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

}
