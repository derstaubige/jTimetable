package de.bremen.jTimetable.fxmlController;

import de.bremen.jTimetable.Classes.Coursepass;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.lang.reflect.Method;
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
                Node firstname = null;
                Node lastname = null;
                Node subject = null;
                Node slot1 = null;
                Node slot2 = null;
                Node slot3 = null;

                for (SQLConnectionManagerValues value : row) {
                    //Field, only one Timeslot
                    switch (idx) {
                        case 1: {
                            //Display date
                            date = new Label(value.toString());
                            break;
                        }
//                        case 2: {
//                            //Room Name
//                            room = new Label(value.toString());
//                            break;
//                        }
                        case 2: {
                            //Lecturer firstname
                            firstname = new Label(value.toString());
                            break;
                        }
                        case 3: {
                            //Lecturer lastname
                            lastname = new Label(value.toString());
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
                    switch (timeslot) {
                        case 0: {
                            if ((firstname != null) && (lastname != null) && (subject != null)) {
                                slot1 = new VBox(firstname, lastname, subject);
                            }
                            break;
                        }
                        case 1: {
                            if ((firstname != null) && (lastname != null) && (subject != null)) {
                                slot2 = new VBox(firstname, lastname, subject);
                            }
                            break;
                        }
                        case 2: {
                            if ((firstname != null) && (lastname != null) && (subject != null)) {
                                slot3 = new VBox(firstname, lastname, subject);
                            }
                            break;
                        }
                        default: {
                         //   System.out.println("To many timeslots.");
                        }
                    }
                    timeslot++;
                    if (timeslot > 2) {
                        //Add Row for each day: Date, Slot 1, Slot 2, Slot 3
                        if ((slot1 != null) && (slot2 != null) &&
                                (slot3 != null)) {
                            grdpn_TimetableView.addRow(2, date, slot1, slot2, slot3);

                        }
                        slot1 = null;
                        slot2 = null;
                        slot3 = null;
                        timeslot = -1;
                    }
                    System.out.println(grdpn_TimetableView);

                    idx++;

                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    private int getRowCount(GridPane gridPane) {
        try {
            Method method = gridPane.getClass().getDeclaredMethod("getNumberOfRows");
            method.setAccessible(true);
            Integer rows = (Integer) method.invoke(gridPane);
            return rows;
        }catch (Exception e) {
            System.out.println(e.toString());
        }
        return 0;
    }

}
