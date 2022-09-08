package de.bremen.jTimetable.fxmlController;

import de.bremen.jTimetable.Classes.Coursepass;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class TimetableViewController implements Initializable {

    @FXML
    public ScrollPane scrllpn_TimetableView;

    @FXML
    public GridPane grdpn_TimetableView;

    private Coursepass coursepass;

    /**
     * Can be called to hand parameters from the calling class to this controller. Be aware that the initialize method
     * is performed bevor this one and therefore has no access to the data that is set here.
     *
     * @param coursepass the coursepass that was selected and for which the timetable is shown
     */
    public void initData(Coursepass coursepass) {
        this.coursepass = coursepass;
        //Get Timetable for Coursepass
        try {
            ArrayList<ArrayList<SQLConnectionManagerValues>> rs = this.coursepass.getTimetable();
            //Timeslot -1 == date
            int timeslot = -1;
            for (ArrayList<SQLConnectionManagerValues> row : rs) {
                int idx = 1;
                Node date = null;
                Node room = null;
                Node lecturer = null;
                Node subject = null;

                for (SQLConnectionManagerValues value : row) {
                    //Field, only one Timeslot
                    switch (idx) {
                        case 1: {
                            //Display date
                            date = new Label(value.toString());
                            break;
                        }
                        case 2: {
                            //Room Name
                            room = new Label(value.toString());
                            break;
                        }
                        case 3: {
                            //Lecturer
                            //TODO lecturer has first and lastname as value
                            lecturer = new Label(value.toString());
                            break;
                        }
                        case 4: {
                            //Subject
                            subject = new Label(value.toString());
                            break;
                        }
                        default: {
                            System.out.println("Something went wrong while displaying timetable.");
                        }
                    }

                    //TODO doesn't work if day uses four timeslots
                    //TODO add timeslots next to each other
                    switch (timeslot) {
                        case -1: {

                        }
                    }
                    timeslot++;
                    if (timeslot > 2) {
                        //Add Row for each day: Date, Slot 1, Slot 2, Slot 3, Slot 4
                        grdpn_TimetableView.addRow(1, date, room, lecturer, subject);
                        timeslot = -1;
                    }

                    idx++;

                }
            }
            //Create new Element for each Date and Timeslot


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

}
