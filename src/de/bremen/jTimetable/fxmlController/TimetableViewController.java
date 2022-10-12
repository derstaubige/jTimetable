package de.bremen.jTimetable.fxmlController;

import de.bremen.jTimetable.Classes.Coursepass;
import de.bremen.jTimetable.Classes.Timetable;
import de.bremen.jTimetable.Classes.TimetableDay;
import de.bremen.jTimetable.Classes.TimetableHour;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.lang.reflect.Method;
import java.net.URL;
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
        Timetable timetable = new Timetable(this.coursepass);
        Integer inttmpRowIdx = 2;
        for (TimetableDay day : timetable.getArrayTimetableDays()){
            grdpn_TimetableView.add(new Text(day.getDate().toString()), 0, inttmpRowIdx);
            for (TimetableHour timeslot : day.getArrayTimetableDay()){
                grdpn_TimetableView.add(new Text(timeslot.getLecturerName() + "\r\n" + timeslot.getSubjectCaption() + "\r\n" + timeslot.getRoomCaption()), timeslot.getTimeslot() + 1, inttmpRowIdx);
            }

            inttmpRowIdx++;
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
