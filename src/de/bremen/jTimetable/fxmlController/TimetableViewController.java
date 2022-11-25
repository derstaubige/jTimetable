package de.bremen.jTimetable.fxmlController;

import de.bremen.jTimetable.Classes.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;

public class TimetableViewController implements Initializable {

    @FXML    public GridPane grdpn_TimetableView;
    @FXML public AnchorPane  anchorpane_TimetableView;
    @FXML public AnchorPane anchorpane_Editbox;

    @FXML public GridPane grdpn_Editbox;

    @FXML    public Label savetofile;
    private Timetable timetable;
    /**
     * Can be called to hand parameters from the calling class to this controller. Be aware that the initialize method
     * is performed bevor this one and therefore has no access to the data that is set here.
     *
     * @param coursepass the coursepass that was selected and for which the timetable is shown
     */
    public void initData(Coursepass coursepass) {
        //Get Timetable for Coursepass
        timetable = new Timetable(coursepass);
        int inttmpRowIdx = 2;
        Calendar cal = Calendar.getInstance();
        ZoneId defaultZoneId = ZoneId.systemDefault();
        for (TimetableDay day : timetable.getArrayTimetableDays()) {
            Date tmpDate = Date.from(day.getDate().atStartOfDay(defaultZoneId).toInstant());
            grdpn_TimetableView.add(new Text(new SimpleDateFormat("EEEE").format(tmpDate) + "\r\n" + day.getDate().toString()), 0, inttmpRowIdx);

            for (TimetableHour timeslot : day.getArrayTimetableDay()) {
                JavaFXTimetableHourText tmpText =
                        new JavaFXTimetableHourText(timeslot.getCoursepassLecturerSubject(), day.getDate(),
                                timeslot.getTimeslot());

                //enables the text to be dragged
                //drag was detected, start a drag-and-drop gesture
                //https://www.youtube.com/watch?v=-TgSIr5IzQ8
                tmpText.setOnDragDetected(mouseEvent -> {
                    //allow any transfer mode
                    Dragboard db = tmpText.startDragAndDrop(TransferMode.ANY);

                    //Put a string on a dragboard
                    ClipboardContent content = new ClipboardContent();
                    content.putString("SWITCH");

                    db.setContent(content);

                    //check for all JavaFXTimetableHourText if they could be exchanged and color them
                    List<JavaFXTimetableHourText> timetableHourTexts = getNodesOfType(grdpn_TimetableView, JavaFXTimetableHourText.class);
                    for(int i = 0; i < timetableHourTexts.size(); i++){
                        CoursepassLecturerSubject tmpCoursepassLecturerSubject = timetableHourTexts.get(i).getCoursepassLecturerSubject();
                        try{
                            if(tmpCoursepassLecturerSubject.cangetExchanged(
                                    tmpText.getCoursepassLecturerSubject(),
                                    tmpText.getDay(), tmpText.getTimeslot(), timetableHourTexts.get(i).getCoursepassLecturerSubject(),
                                    timetableHourTexts.get(i).getDay(), timetableHourTexts.get(i).getTimeslot()
                            ) == true){
                                timetableHourTexts.get(i).setFill(Color.GREEN);
                            }else{
                                timetableHourTexts.get(i).setFill(Color.RED);
                            }
                        }catch(Exception e){
                            System.out.println("Error while determing if a Lectrurer is availdable at a given date");
                            e.printStackTrace();
                        }


                    }
                    mouseEvent.consume();
                });

                //allows this field to take dropped items when they're dragged over the target
                //https://www.youtube.com/watch?v=yP_UjqnIsCk
                tmpText.setOnDragOver(dragEvent -> {
                    //accept it only if it is not dragged from the same node
                    if (dragEvent.getGestureSource() != dragEvent.getGestureTarget()) {
                        //allow for both copying and moving, whatever user chooses
                        dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    }

                    dragEvent.consume();
                });

                //handles if an element is dropped on this field
                //https://www.youtube.com/watch?v=yP_UjqnIsCk
                tmpText.setOnDragDropped(dragEvent -> {
                    if(dragEvent.getDragboard().getString() == "SWITCH"){
                        JavaFXTimetableHourText source = (JavaFXTimetableHourText) dragEvent.getGestureSource();
                        JavaFXTimetableHourText target = (JavaFXTimetableHourText) dragEvent.getGestureTarget();

                        if(CoursepassLecturerSubject.cangetExchanged(source.getCoursepassLecturerSubject(),
                                source.getDay(), source.getTimeslot(), target.getCoursepassLecturerSubject(),
                                target.getDay(), target.getTimeslot()) == true){
                            //ger Row and Column from Source and Target
                            Integer SourceRow = GridPane.getRowIndex(source);
                            Integer SourceCol = GridPane.getColumnIndex(source);
                            Integer TargetRow = GridPane.getRowIndex(target);
                            Integer TargetCol = GridPane.getColumnIndex(target);

                            //Change Source and Target in the GridPane
                            GridPane.setRowIndex(source, TargetRow);
                            GridPane.setColumnIndex(source, TargetCol);
                            GridPane.setRowIndex(target, SourceRow);
                            GridPane.setColumnIndex(target, SourceCol);

                            CoursepassLecturerSubject.changeCoursepassLecturerSubject(source.getCoursepassLecturerSubject(),
                                    source.getDay(), source.getTimeslot(), target.getCoursepassLecturerSubject(),
                                    target.getDay(), target.getTimeslot());
                        }
                    }

                    if(dragEvent.getDragboard().getString() == "NEW"){
                        JavaFXCoursepassLecturerSubjectText source = (JavaFXCoursepassLecturerSubjectText)
                                dragEvent.getGestureSource();
                        JavaFXTimetableHourText target = (JavaFXTimetableHourText) dragEvent.getGestureTarget();

                        if(CoursepassLecturerSubject.isFreeTarget(source.getCoursepassLecturerSubject(),target.getDay(),
                                target.getTimeslot()) == true){
                            //check if the target was a freetime, if not we have to delete the existing cls
                            if(target.getCoursepassLecturerSubject().getSubject().getId() != 0){
                                //no freetime, we have to delete the resourceblocked and the entry in the timetable
                                //TODO ENUM ResourceName
                                Timetable.deleteResourceblocked(target.getCoursepassLecturerSubject().getLecturerID(),
                                        "Lecturer",target.getDay(),target.getDay(),target.getTimeslot(),
                                        target.getTimeslot());
                                Timetable.deleteResourceblocked(target.getCoursepassLecturerSubject().getRoom().getId(),
                                        "Room",target.getDay(),target.getDay(),target.getTimeslot(),
                                        target.getTimeslot());
                            }
                            //delete the entry in the timetable table
                            Timetable.deleteTimetable(source.getCoursepassLecturerSubject().getId(),
                                    target.getDay(), target.getTimeslot());

                            //save the new timetablehour
                            TimetableHour tmptimetableHour = new TimetableHour(target.getTimeslot(),
                                    source.getCoursepassLecturerSubject());
                            timetable.addSingleHour(tmptimetableHour,target.getDay(), target.getTimeslot());

                            //update visuals
                            //ger Row and Column from Source and Target
                            Integer TargetRow = GridPane.getRowIndex(target);
                            Integer TargetCol = GridPane.getColumnIndex(target);


                            //Change Source and Target in the GridPane
                            GridPane.setRowIndex(source, TargetRow);
                            GridPane.setColumnIndex(source, TargetCol);
                        }
                    }


                    dragEvent.consume();

                });

                tmpText.setOnDragDone( dragEvent -> {
                    //check for all JavaFXTimetableHourText if they could be exchanged and color them
                    List<JavaFXTimetableHourText> timetableHourTexts = getNodesOfType(grdpn_TimetableView, JavaFXTimetableHourText.class);
                    for(int i = 0; i < timetableHourTexts.size(); i++){
                        timetableHourTexts.get(i).setFill(Color.BLACK);
                    }
                });

                grdpn_TimetableView.add(tmpText, timeslot.getTimeslot() + 1, inttmpRowIdx);
            }

            inttmpRowIdx++;
        }

        //Read all CoursepassLecturerSubject Objects from Coursepass and generate die Labels for the Timetableview
        Integer tmpRowIdx = 0;
        Integer tmpColIdx = 0;
        try {
            coursepass.updateCoursepassLecturerSubjects();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        for(CoursepassLecturerSubject cls : coursepass.getArraycoursepasslecturersubject()){
            Text tmpText = new JavaFXCoursepassLecturerSubjectText(cls);

            //enables the text to be dragged
            //drag was detected, start a drag-and-drop gesture
            //https://www.youtube.com/watch?v=-TgSIr5IzQ8
            tmpText.setOnDragDetected(mouseEvent -> {
                //allow any transfer mode
                Dragboard db = tmpText.startDragAndDrop(TransferMode.COPY);

                //Put a string on a dragboard
                ClipboardContent content = new ClipboardContent();
                content.putString("NEW");

                db.setContent(content);

                //check for all JavaFXTimetableHourText if they could be exchanged and color them
                List<JavaFXTimetableHourText> timetableHourTexts = getNodesOfType(grdpn_TimetableView,
                        JavaFXTimetableHourText.class);
                for(int i = 0; i < timetableHourTexts.size(); i++){
                    CoursepassLecturerSubject tmpCoursepassLecturerSubject =
                            timetableHourTexts.get(i).getCoursepassLecturerSubject();
                    try{
                        if(CoursepassLecturerSubject.isFreeTarget(cls, timetableHourTexts.get(i).getDay(),
                                timetableHourTexts.get(i).getTimeslot()) == true){
                            timetableHourTexts.get(i).setFill(Color.GREEN);
                        }else{
                            timetableHourTexts.get(i).setFill(Color.RED);
                        }
                    }catch(Exception e){
                        System.out.println("Error while determing if a Lectrurer is availdable at a given date");
                        e.printStackTrace();
                    }
                }
                mouseEvent.consume();
            });


            tmpText.setOnDragDone( dragEvent -> {
                //check for all JavaFXTimetableHourText if they could be exchanged and color them
                List<JavaFXTimetableHourText> timetableHourTexts = getNodesOfType(grdpn_TimetableView, JavaFXTimetableHourText.class);
                for(int i = 0; i < timetableHourTexts.size(); i++){
                    timetableHourTexts.get(i).setFill(Color.BLACK);
                }
            });

            grdpn_Editbox.add(tmpText, tmpColIdx, tmpRowIdx );
            tmpColIdx++;
            if (tmpColIdx > 9){
                tmpColIdx = 0;
                tmpRowIdx++;
            }
        }
        //Add a Trashcan to Delete Planed Hours
    }

    @FXML
    private void savetoFileClicked(){
        final FileChooser fileChooser = new FileChooser();
        //File file = fileChooser.showOpenDialog(stage);

        timetable.exportTimetableToFile();
    }


    private <T> List<T> getNodesOfType(Pane parent, Class<T> type) {
        List<T> elements = new ArrayList<>();
        for (Node node : parent.getChildren()) {
            if (node instanceof Pane) {
                elements.addAll(getNodesOfType((Pane) node, type));
            } else if (type.isAssignableFrom(node.getClass())) {
                //noinspection unchecked
                elements.add((T) node);
            }
        }
        return Collections.unmodifiableList(elements);
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
