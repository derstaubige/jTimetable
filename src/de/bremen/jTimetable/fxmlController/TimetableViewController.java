package de.bremen.jTimetable.fxmlController;

import de.bremen.jTimetable.Classes.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
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

    /**
     * Can be called to hand parameters from the calling class to this controller. Be aware that the initialize method
     * is performed bevor this one and therefore has no access to the data that is set here.
     *
     * @param coursepass the coursepass that was selected and for which the timetable is shown
     */
    public void initData(Coursepass coursepass) {
        //Get Timetable for Coursepass
        Timetable timetable = new Timetable(coursepass);
        int inttmpRowIdx = 2;
        for (TimetableDay day : timetable.getArrayTimetableDays()) {

            grdpn_TimetableView.add(new Text(day.getDate().toString()), 0, inttmpRowIdx);

            for (TimetableHour timeslot : day.getArrayTimetableDay()) {
                JavaFXTimetableHourText tmpText =
                        new JavaFXTimetableHourText(timeslot.getLecturerName() + "\r\n" + timeslot.getSubjectCaption() + "\r\n" + timeslot.getRoomCaption(),
                                timeslot.getCoursepassLecturerSubject(), day.getDate(), timeslot.getTimeslot());

                //enables the text to be dragged
                //drag was detected, start a drag-and-drop gesture
                //https://www.youtube.com/watch?v=-TgSIr5IzQ8
                tmpText.setOnDragDetected(mouseEvent -> {
                    //allow any transfer mode
                    Dragboard db = tmpText.startDragAndDrop(TransferMode.ANY);

                    //Put a string on a dragboard
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

                    mouseEvent.consume();
                });

                //allows this field to take dropped items when they're dragged over the target
                //https://www.youtube.com/watch?v=yP_UjqnIsCk
                tmpText.setOnDragOver(dragEvent -> {
                    //accept it only if it is not dragged from the same node
                    if (dragEvent.getGestureSource() != dragEvent.getSource()) {
                        //allow for both copying and moving, whatever user chooses
                        dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    }

                    dragEvent.consume();
                });

                //handles if an element is dropped on this field
                //https://www.youtube.com/watch?v=yP_UjqnIsCk
                tmpText.setOnDragDropped(dragEvent -> {
                    JavaFXTimetableHourText source = (JavaFXTimetableHourText) dragEvent.getGestureSource();
                    JavaFXTimetableHourText target = (JavaFXTimetableHourText) dragEvent.getGestureTarget();

                    //Visually switch lessons
                    JavaFXTimetableHourText temp = new JavaFXTimetableHourText(source.getText(),
                            source.getCoursepassLecturerSubject(), source.getDay(), source.getTimeslot());
                    ((Text) dragEvent.getGestureSource()).setText(target.getText());
                    ((Text) dragEvent.getGestureTarget()).setText(temp.getText());

                    System.out.println(CoursepassLecturerSubject.cangetExchanged(source.getCoursepassLecturerSubject(),
                            source.getDay(), source.getTimeslot(), target.getCoursepassLecturerSubject(),
                            target.getDay(), target.getTimeslot()));
                });

                grdpn_TimetableView.add(tmpText, timeslot.getTimeslot() + 1, inttmpRowIdx);
            }

            inttmpRowIdx++;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    /**
     * TODO method is new :)
     * Returns the node that is in a certain field of a gridPane specified by row and column.
     *
     * @param row      which row is selected
     * @param column   which column is selected
     * @param gridPane the gridPane that is searched
     * @return Node at the specified row and column
     */
    public Node getNodeByRowColumnIndex(final int row, final int column, GridPane gridPane) {
        Node result = null;
        ObservableList<Node> children = gridPane.getChildren();

        for (Node node : children) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
                result = node;
                break;
            }
        }
        return result;
    }

    private int getRowCount(GridPane gridPane) {
        try {
            Method method = gridPane.getClass().getDeclaredMethod("getNumberOfRows");
            method.setAccessible(true);
            return (Integer) method.invoke(gridPane);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

}
