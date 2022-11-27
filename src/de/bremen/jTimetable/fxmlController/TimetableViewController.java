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
import javafx.stage.Stage;
import javafx.stage.Window;

import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;
import java.util.List;

public class TimetableViewController implements Initializable {

    @FXML    public GridPane grdpn_TimetableView;
    @FXML public AnchorPane  anchorpane_TimetableView;
    @FXML public AnchorPane anchorpane_Editbox;

    @FXML public GridPane grdpn_Editbox;

    @FXML    public Label savetofile;
    private Timetable timetable;
    private Coursepass coursepass;
    /**
     * Can be called to hand parameters from the calling class to this controller. Be aware that the initialize method
     * is performed bevor this one and therefore has no access to the data that is set here.
     *
     * @param coursepass the coursepass that was selected and for which the timetable is shown
     */
    public void initDataCoursepass(Coursepass coursepass) {
        //Get Timetable for Coursepass
        this.coursepass = coursepass;
        this.timetable = new Timetable(coursepass);
        this.drawTimetable(this.timetable, true);
    }

    public void initDataTimetable(Timetable timetable){
        this.timetable = timetable;
        this.drawTimetable(timetable, false);
    }

    private void drawTimetable(Timetable timetable, Boolean dragandDrop){

        int inttmpRowIdx = 2;
        Calendar cal = Calendar.getInstance();
        ZoneId defaultZoneId = ZoneId.systemDefault();
        for (TimetableDay day : timetable.getArrayTimetableDays()) {
            Date tmpDate = Date.from(day.getDate().atStartOfDay(defaultZoneId).toInstant());
            grdpn_TimetableView.add(new Text(new SimpleDateFormat("EEEE").format(tmpDate) + "\r\n" + day.getDate().toString()), 0, inttmpRowIdx);

            for (TimetableHour timeslot : day.getArrayTimetableDay()) {
                if(timeslot == null){
                    continue;
                }
                JavaFXTimetableHourText tmpText;
                if(dragandDrop == true) {
                    tmpText =
                            new JavaFXTimetableHourText(timeslot.getCoursepassLecturerSubject(), day.getDate(),
                                    timeslot.getTimeslot());
                }else{
                    tmpText =
                            new JavaFXTimetableHourText(timeslot.getCoursepassLecturerSubject(), day.getDate(),
                                    timeslot.getTimeslot(),true);
                }
                if(dragandDrop == true) {
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
                        for (int i = 0; i < timetableHourTexts.size(); i++) {
                            CoursepassLecturerSubject tmpCoursepassLecturerSubject = timetableHourTexts.get(i).getCoursepassLecturerSubject();
                            try {
                                if (tmpCoursepassLecturerSubject.cangetExchanged(
                                        tmpText.getCoursepassLecturerSubject(),
                                        tmpText.getDay(), tmpText.getTimeslot(), timetableHourTexts.get(i).getCoursepassLecturerSubject(),
                                        timetableHourTexts.get(i).getDay(), timetableHourTexts.get(i).getTimeslot()
                                ) == true) {
                                    timetableHourTexts.get(i).setFill(Color.GREEN);
                                } else {
                                    timetableHourTexts.get(i).setFill(Color.RED);
                                }
                            } catch (Exception e) {
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
                        if (dragEvent.getDragboard().getString() == "SWITCH") {
                            JavaFXTimetableHourText source = (JavaFXTimetableHourText) dragEvent.getGestureSource();
                            JavaFXTimetableHourText target = (JavaFXTimetableHourText) dragEvent.getGestureTarget();

                            if (CoursepassLecturerSubject.cangetExchanged(source.getCoursepassLecturerSubject(),
                                    source.getDay(), source.getTimeslot(), target.getCoursepassLecturerSubject(),
                                    target.getDay(), target.getTimeslot()) == true) {
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

                        if (dragEvent.getDragboard().getString() == "NEW") {
                            JavaFXCoursepassLecturerSubjectText source = (JavaFXCoursepassLecturerSubjectText)
                                    dragEvent.getGestureSource();
                            JavaFXTimetableHourText target = (JavaFXTimetableHourText) dragEvent.getGestureTarget();

                            if (CoursepassLecturerSubject.isFreeTarget(source.getCoursepassLecturerSubject(), target.getDay(),
                                    target.getTimeslot()) == true) {
                                //check if the target was a freetime, if not we have to delete the existing cls
                                if (target.getCoursepassLecturerSubject().getSubject().getId() != 0) {
                                    //no freetime, we have to delete the resourceblocked and the entry in the timetable
                                    //TODO ENUM ResourceName
                                    Timetable.deleteResourceblocked(target.getCoursepassLecturerSubject().getLecturerID(),
                                            "Lecturer", target.getDay(), target.getDay(), target.getTimeslot(),
                                            target.getTimeslot());
                                    Timetable.deleteResourceblocked(target.getCoursepassLecturerSubject().getRoom().getId(),
                                            "Room", target.getDay(), target.getDay(), target.getTimeslot(),
                                            target.getTimeslot());
                                }
                                //delete the entry in the timetable table
                                Timetable.deleteTimetable(source.getCoursepassLecturerSubject().getId(),
                                        target.getDay(), target.getTimeslot());

                                //save the new timetablehour
                                TimetableHour tmptimetableHour = new TimetableHour(target.getTimeslot(),
                                        source.getCoursepassLecturerSubject());
                                timetable.addSingleHour(tmptimetableHour, target.getDay(), target.getTimeslot());

                                //update visuals

                                this.emptyGridpanes();
                                //this.drawTimetable();
                            }
                        }


                        dragEvent.consume();

                    });

                    tmpText.setOnDragDone(dragEvent -> {
                        //check for all JavaFXTimetableHourText if they could be exchanged and color them
                        List<JavaFXTimetableHourText> timetableHourTexts = getNodesOfType(grdpn_TimetableView, JavaFXTimetableHourText.class);
                        for (int i = 0; i < timetableHourTexts.size(); i++) {
                            timetableHourTexts.get(i).setFill(Color.BLACK);
                        }
                    });
                }
                grdpn_TimetableView.add(tmpText, timeslot.getTimeslot() + 1, inttmpRowIdx);
            }

            inttmpRowIdx++;
        }
        if(dragandDrop == true) {
            //Read all CoursepassLecturerSubject Objects from Coursepass and generate the Labels for the Timetableview
            Integer tmpRowIdx = 0;
            Integer tmpColIdx = 0;
            try {
                coursepass.updateCoursepassLecturerSubjects();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            for (CoursepassLecturerSubject cls : coursepass.getArraycoursepasslecturersubject()) {
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
                    for (int i = 0; i < timetableHourTexts.size(); i++) {
                        CoursepassLecturerSubject tmpCoursepassLecturerSubject =
                                timetableHourTexts.get(i).getCoursepassLecturerSubject();
                        try {
                            if (CoursepassLecturerSubject.isFreeTarget(cls, timetableHourTexts.get(i).getDay(),
                                    timetableHourTexts.get(i).getTimeslot()) == true) {
                                timetableHourTexts.get(i).setFill(Color.GREEN);
                            } else {
                                timetableHourTexts.get(i).setFill(Color.RED);
                            }
                        } catch (Exception e) {
                            System.out.println("Error while determing if a Lectrurer is availdable at a given date");
                            e.printStackTrace();
                        }
                    }
                    mouseEvent.consume();
                });


                tmpText.setOnDragDone(dragEvent -> {
                    //check for all JavaFXTimetableHourText if they could be exchanged and color them
                    List<JavaFXTimetableHourText> timetableHourTexts = getNodesOfType(grdpn_TimetableView, JavaFXTimetableHourText.class);
                    for (int i = 0; i < timetableHourTexts.size(); i++) {
                        timetableHourTexts.get(i).setFill(Color.BLACK);
                    }
                });

                grdpn_Editbox.add(tmpText, tmpColIdx, tmpRowIdx);
                tmpColIdx++;
                if (tmpColIdx > 9) {
                    tmpColIdx = 0;
                    tmpRowIdx++;
                }
            }
            //Add a Trashcan to Delete Planed Hours
        }
    }

    @FXML
    private void savetoFileClicked(){
        //https://docs.oracle.com/javafx/2/ui_controls/file-chooser.htm
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) {
            try {
                PrintWriter writer;
                writer = new PrintWriter(file);
                Integer tmpRowCounter = 1;
                Integer tmpRowCounterNow = 1;
                JavaFXTimetableHourText tmpDateNode;
                String content = "";
                ObservableList<Node> grdpn = grdpn_TimetableView.getChildren();
                Node[][] gridPaneArray = null;
                gridPaneArray = new Node[/*nbLines*/][/*nbColumns*/];
                for(Node node : grdpn)
                {
                    gridPaneArray[GridPane.getRowIndex(node)][GridPane.getColumnIndex(node)] = node;
                }
                for(Node node : grdpn){
                    if (node instanceof JavaFXTimetableHourText) {
                        tmpRowCounterNow = (Integer) node.getProperties().get("gridpane-row");
                        if(tmpRowCounter.compareTo(tmpRowCounterNow) < 0) {
                            content += ";\r\n";
                            tmpDateNode = (JavaFXTimetableHourText) gridPaneArray[tmpRowCounterNow][0];
                            content += tmpDateNode.getText().replace("\r\n",";");
                            writer.println(content);
                            content = "";
                        }
                        content += (((JavaFXTimetableHourText)node).getText().replace("\r\n",""));
                        content += ";";
                    }
                }

                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

    private void emptyGridpanes(){
        for (int i = 1 ; i <= getRowCount(grdpn_TimetableView) ; i++){
            int finalI = i;
            grdpn_TimetableView.getChildren().removeIf(node -> GridPane.getRowIndex(node) == finalI);
        }
        for (int i = 0 ; i <= getRowCount(grdpn_Editbox) ; i++){
            if(i == 0){
                grdpn_Editbox.getChildren().removeIf(node -> GridPane.getRowIndex(node) == null);
            }else{
                int finalI = i;
                grdpn_Editbox.getChildren().removeIf(node -> GridPane.getRowIndex(node) == finalI);
            }

        }
    }

}
