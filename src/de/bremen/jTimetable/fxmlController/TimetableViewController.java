package de.bremen.jTimetable.fxmlController;

import de.bremen.jTimetable.Classes.Coursepass;
import de.bremen.jTimetable.Classes.Timetable;
import de.bremen.jTimetable.Classes.TimetableDay;
import de.bremen.jTimetable.Classes.TimetableHour;
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
import java.util.HashMap;
import java.util.Map;
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
        for (TimetableDay day : timetable.getArrayTimetableDays()) {

            grdpn_TimetableView.add(new Text(day.getDate().toString()), 0, inttmpRowIdx);
            //TODO do empty slots contain a text-field? -> yes
            for (TimetableHour timeslot : day.getArrayTimetableDay()) {
                //TODO new code starts here :)
                Text text = new Text(timeslot.getLecturerName() + "\r\n" + timeslot.getSubjectCaption() + "\r\n" + timeslot.getRoomCaption());
                //TODO EventHandler setOnDragDetected is called if a field is dragged on another and it should:
                // check if the switch is valid (lecturer is available)
                // switch the text visually
                // switch the objects in the database

                //enables the text to be dragged
                //https://www.youtube.com/watch?v=-TgSIr5IzQ8
                text.setOnDragDetected(mouseEvent -> {
                    Dragboard db = text.startDragAndDrop(TransferMode.ANY);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(text.getText());
                    //TODO put row and column in content but clipBoard can only contain one String
//                    int column = GridPane.getColumnIndex(text);
//                    int row = GridPane.getRowIndex(text);
//                    content.putString(String.valueOf(column));
//                    content.putString(String.valueOf(row));

                    db.setContent(content);
                });

                //allows this field to take dropped items
                //https://www.youtube.com/watch?v=yP_UjqnIsCk
                text.setOnDragOver(dragEvent -> dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE));

                //handles if an element is dropped on this field
                //https://www.youtube.com/watch?v=yP_UjqnIsCk
                text.setOnDragDropped(dragEvent -> {
                    Dragboard db = dragEvent.getDragboard();
                    //TODO check if field is valid
                    if (db.hasString()) {
                        String newText = db.getString();
                        //TODO oldText is text the field that was dragged on had before the drag and needs to
                        // be set into the field the dragged element was in before
                        String oldText = text.getText();
                        text.setText(newText);
                    }
                });

                grdpn_TimetableView.add(text, timeslot.getTimeslot() + 1, inttmpRowIdx);
            }

            inttmpRowIdx++;
        }
        //TODO I removed my code-garbage :)
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    /**
     * TODO method is new :)
     * Returns the node that is in a certain field of a gridPane specified by row and column.
     * @param row which row is selected
     * @param column which column is selected
     * @param gridPane the gridPane that is searched
     * @return Node at the specified row and column
     */
    public Node getNodeByRowColumnIndex (final int row, final int column, GridPane gridPane) {
        Node result = null;
        ObservableList<Node> childrens = gridPane.getChildren();

        for (Node node : childrens) {
            if(GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
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
            Integer rows = (Integer) method.invoke(gridPane);
            return rows;
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return 0;
    }

}
