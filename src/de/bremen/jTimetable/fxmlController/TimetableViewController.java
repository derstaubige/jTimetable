package de.bremen.jTimetable.fxmlController;

import de.bremen.jTimetable.Classes.*;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.*;
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
                JavaFXTimetableHourText tmpText = new JavaFXTimetableHourText(timeslot.getLecturerName() + "\r\n" + timeslot.getSubjectCaption() + "\r\n" + timeslot.getRoomCaption(), timeslot.getCoursepassLecturerSubject(), day.getDate(), timeslot.getTimeslot());
                tmpText.setOnDragDetected(new EventHandler<MouseEvent>() {
                    public void handle(MouseEvent event) {
                        /* drag was detected, start a drag-and-drop gesture*/
                        /* allow any transfer mode */
                        Dragboard db = tmpText.startDragAndDrop(TransferMode.ANY);

                        /* Put a string on a dragboard */
                        ClipboardContent content = new ClipboardContent();
                        content.putString(tmpText.getProperties().get("gridpane-column").toString() + "," + tmpText.getProperties().get("gridpane-row").toString());
//                        System.out.println(tmpText.getText());
//                        System.out.println(tmpText.getDay());
//                        System.out.println(tmpText.getTimeslot());
//                        // we can access the great array to pull data from there, no need to store it over and over i guess
//                        System.out.println(timetable.getArrayTimetableDays().get(0).getArrayTimetableDay().get(0).getSubjectCaption());
//                        System.out.println(tmpText.getProperties().get("gridpane-column"));
//                        System.out.println(tmpText.getProperties().get("gridpane-row"));
                        db.setContent(content);

                        event.consume();
                    }
                });

                tmpText.setOnDragOver(new EventHandler<DragEvent>() {
                    public void handle(DragEvent event) {
                        /* data is dragged over the target */
                        /* accept it only if it is not dragged from the same node */
                        if ( event.getGestureSource() != event.getSource()) {
                            /* allow for both copying and moving, whatever user chooses */
                            event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                        }

                        event.consume();
                    }
                });

                tmpText.setOnDragDropped(new EventHandler<DragEvent>()  {
                    public void handle(DragEvent event){
                        JavaFXTimetableHourText source = (JavaFXTimetableHourText) event.getGestureSource();
                        JavaFXTimetableHourText target = (JavaFXTimetableHourText) event.getGestureTarget();

                        System.out.println(source.getCoursepassLecturerSubject().cangetExchanged(source.getCoursepassLecturerSubject(), source.getDay(), source.getTimeslot(), target.getCoursepassLecturerSubject(), target.getDay(), target.getTimeslot()));
                    }
                });


                grdpn_TimetableView.add(tmpText, timeslot.getTimeslot() + 1, inttmpRowIdx);
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
