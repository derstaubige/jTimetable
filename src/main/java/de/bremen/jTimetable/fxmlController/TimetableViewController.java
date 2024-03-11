package de.bremen.jTimetable.fxmlController;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import de.bremen.jTimetable.Classes.*;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueDate;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueInt;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueLong;
import javafx.scene.text.Font;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class TimetableViewController implements Initializable {

    @FXML
    public GridPane grdpn_TimetableView;
    @FXML
    public AnchorPane anchorpane_TimetableView;
    @FXML
    public AnchorPane anchorpane_Editbox;
    @FXML
    public GridPane grdpn_Editbox;
    @FXML
    public Label savetofile;

    private Timetable timetable;
    private CoursePass coursepass;
    private SQLConnectionManager sqlConnectionManager;
    private ResourceBundle resourceBundle;

    /**
     * Can be called to hand parameters from the calling class to this controller.
     * Be aware that the initialize method
     * is performed bevor this one and therefore has no access to the data that is
     * set here.
     *
     * @param coursepass the coursepass that was selected and for which the
     *                   timetable is shown
     */
    public void initDataCoursepass(CoursePass coursepass) {
        // Get Timetable for Coursepass
        this.coursepass = coursepass;
        this.timetable = new Timetable(coursepass, getSqlConnectionManager());
        this.drawTimetable(this.timetable, true);
    }

    public void initDataTimetable(Timetable timetable) {
        this.timetable = timetable;
        this.drawTimetable(timetable, false);
    }

    private void drawTimetable(Timetable timetable, Boolean dragandDrop) {

        int inttmpRowIdx = 2;
        Calendar.getInstance();
        ZoneId.systemDefault();
        for (TimetableDay day : timetable.getArrayTimetableDays()) {
            LocalDate tmpDate = day.getDate();
            grdpn_TimetableView.add(new JavaFXTimetableDay(tmpDate, getSqlConnectionManager()), 0, inttmpRowIdx);

            for (TimetableHour timeslot : day.getArrayTimetableDay()) {
                if (timeslot == null) {
                    continue;
                }
                JavaFXTimetableHourText tmpText;
                if (dragandDrop == true) {
                    tmpText = new JavaFXTimetableHourText(timeslot.getCoursepassLecturerSubject(), day.getDate(),
                            timeslot.getTimeslot(), getSqlConnectionManager());
                } else {
                    tmpText = new JavaFXTimetableHourText(timeslot.getCoursepassLecturerSubject(), day.getDate(),
                            timeslot.getTimeslot(), true, getSqlConnectionManager());
                }
                if (dragandDrop == true) {
                    // enables the text to be dragged
                    // drag was detected, start a drag-and-drop gesture
                    // https://www.youtube.com/watch?v=-TgSIr5IzQ8
                    tmpText.setOnDragDetected(mouseEvent -> {
                        // allow any transfer mode
                        Dragboard db = tmpText.startDragAndDrop(TransferMode.ANY);

                        // Put a string on a dragboard
                        ClipboardContent content = new ClipboardContent();
                        content.putString("SWITCH");

                        db.setContent(content);

                        // check for all JavaFXTimetableHourText if they could be exchanged and color
                        // them
                        List<JavaFXTimetableHourText> timetableHourTexts = getNodesOfType(grdpn_TimetableView,
                                JavaFXTimetableHourText.class);

                        this.markSwitchableCLS(timetableHourTexts, tmpText);

                        mouseEvent.consume();
                    });

                    // allows this field to take dropped items when they're dragged over the target
                    // https://www.youtube.com/watch?v=yP_UjqnIsCk
                    tmpText.setOnDragOver(dragEvent -> {
                        // accept it only if it is not dragged from the same node
                        if (dragEvent.getGestureSource() != dragEvent.getGestureTarget()) {
                            // allow for both copying and moving, whatever user chooses
                            dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                        }

                        dragEvent.consume();
                    });

                    // handles if an element is dropped on this field
                    // https://www.youtube.com/watch?v=yP_UjqnIsCk
                    tmpText.setOnDragDropped(dragEvent -> {
                        if (dragEvent.getDragboard().getString() == "SWITCH") {
                            JavaFXTimetableHourText source = (JavaFXTimetableHourText) dragEvent.getGestureSource();
                            JavaFXTimetableHourText target = (JavaFXTimetableHourText) dragEvent.getGestureTarget();

                            if (CoursepassLecturerSubject.cangetExchanged(source.getCoursepassLecturerSubject(),
                                    source.getDay(), source.getTimeslot(), target.getCoursepassLecturerSubject(),
                                    target.getDay(), target.getTimeslot(), getSqlConnectionManager()) == true) {
                                // ger Row and Column from Source and Target
                                Integer SourceRow = GridPane.getRowIndex(source);
                                Integer SourceCol = GridPane.getColumnIndex(source);
                                Integer TargetRow = GridPane.getRowIndex(target);
                                Integer TargetCol = GridPane.getColumnIndex(target);

                                // Change Source and Target in the GridPane
                                GridPane.setRowIndex(source, TargetRow);
                                GridPane.setColumnIndex(source, TargetCol);
                                GridPane.setRowIndex(target, SourceRow);
                                GridPane.setColumnIndex(target, SourceCol);

                                CoursepassLecturerSubject.changeCoursepassLecturerSubject(
                                        source.getCoursepassLecturerSubject(),
                                        source.getDay(), source.getTimeslot(), target.getCoursepassLecturerSubject(),
                                        target.getDay(), target.getTimeslot(), getSqlConnectionManager());
                            }
                        }

                        if (dragEvent.getDragboard().getString() == "NEW") {
                            JavaFXCoursepassLecturerSubjectText source = (JavaFXCoursepassLecturerSubjectText) dragEvent
                                    .getGestureSource();
                            JavaFXTimetableHourText target = (JavaFXTimetableHourText) dragEvent.getGestureTarget();

                            if (CoursepassLecturerSubject.isFreeTarget(source.getCoursepassLecturerSubject(),
                                    target.getDay(),
                                    target.getTimeslot(), getSqlConnectionManager()) == true) {
                                // check if the target was a freetime, if not we have to delete the existing cls
                                if (target.getCoursepassLecturerSubject().getSubject().getId() != 0) {
                                    // no freetime, we have to delete the resourceblocked and the entry in the
                                    // timetable
                                    Timetable.deleteResourceBlocked(
                                            target.getCoursepassLecturerSubject().getLecturerID(),
                                            ResourceNames.LECTURER, target.getDay(), target.getDay(),
                                            target.getTimeslot(),
                                            target.getTimeslot(), getSqlConnectionManager());
                                    Timetable.deleteResourceBlocked(
                                            target.getCoursepassLecturerSubject().getRoom().getId(),
                                            ResourceNames.ROOM, target.getDay(), target.getDay(), target.getTimeslot(),
                                            target.getTimeslot(), getSqlConnectionManager());
                                }
                                // delete the entry in the timetable table
                                Timetable.deleteTimetable(source.getCoursepassLecturerSubject().getId(),
                                        target.getDay(), target.getTimeslot(), getSqlConnectionManager());

                                // save the new timetablehour
                                TimetableHour tmptimetableHour = new TimetableHour(target.getTimeslot(),
                                        source.getCoursepassLecturerSubject(), getSqlConnectionManager());
                                timetable.addSingleHour(tmptimetableHour, target.getDay(), target.getTimeslot());

                                // update visuals
                                Integer TargetRow = GridPane.getRowIndex(target);
                                Integer TargetCol = GridPane.getColumnIndex(target);
                                grdpn_TimetableView.getChildren().remove(target);
                                grdpn_TimetableView
                                        .add(new JavaFXTimetableHourText(source.getCoursepassLecturerSubject(),
                                                target.getDay(), target.getTimeslot(), getSqlConnectionManager()), TargetCol, TargetRow);
                                updateEditboxItems();
                            }
                        }
                        dragEvent.consume();
                    });

                    tmpText.setOnDragDone(dragEvent -> {
                        // check for all JavaFXTimetableHourText if they could be exchanged and color
                        // them
                        List<JavaFXTimetableHourText> timetableHourTexts = getNodesOfType(grdpn_TimetableView,
                                JavaFXTimetableHourText.class);
                        for (int i = 0; i < timetableHourTexts.size(); i++) {
                            timetableHourTexts.get(i).setFill(Color.BLACK);
                        }
                    });
                }
                grdpn_TimetableView.add(tmpText, timeslot.getTimeslot() + 1, inttmpRowIdx);
            }

            inttmpRowIdx++;
        }
        if (dragandDrop == true) {
            // Read all CoursepassLecturerSubject Objects from Coursepass and generate the
            // Labels for the Timetableview
            Integer tmpRowIdx = 0;
            Integer tmpColIdx = 0;

            coursepass.updateCoursePassLecturerSubjects();

            for (CoursepassLecturerSubject cls : coursepass.getArrayCoursePassLecturerSubject()) {
                Text tmpText = new JavaFXCoursepassLecturerSubjectText(cls, resourceBundle);

                // enables the text to be dragged
                // drag was detected, start a drag-and-drop gesture
                // https://www.youtube.com/watch?v=-TgSIr5IzQ8
                tmpText.setOnDragDetected(mouseEvent -> {
                    // allow any transfer mode
                    Dragboard db = tmpText.startDragAndDrop(TransferMode.COPY);

                    // Put a string on a dragboard
                    ClipboardContent content = new ClipboardContent();
                    content.putString("NEW");

                    db.setContent(content);

                    // check for all JavaFXTimetableHourText if they could be exchanged and color
                    // them
                    List<JavaFXTimetableHourText> timetableHourTexts = getNodesOfType(grdpn_TimetableView,
                            JavaFXTimetableHourText.class);

                    this.markNewCLS(timetableHourTexts, cls.getLecturer());
                    mouseEvent.consume();
                });

                tmpText.setOnDragDone(dragEvent -> {
                    // check for all JavaFXTimetableHourText if they could be exchanged and color
                    // them
                    List<JavaFXTimetableHourText> timetableHourTexts = getNodesOfType(grdpn_TimetableView,
                            JavaFXTimetableHourText.class);
                    for (int i = 0; i < timetableHourTexts.size(); i++) {
                        timetableHourTexts.get(i).setFill(Color.BLACK);
                    }
                });

                grdpn_Editbox.add(tmpText, tmpColIdx, tmpRowIdx);
                tmpColIdx++;
                if (tmpColIdx > timetable.getMaxTimeslots()) {
                    tmpColIdx = 0;
                    tmpRowIdx++;
                }
            }

            // Add a Trashcan to Delete Planed Hours
            Label trashcan = new Label("\uD83D\uDDD1");
            Font defaultFont = new Font(38);
            trashcan.setFont(defaultFont);
            trashcan.setTextAlignment(TextAlignment.CENTER);
            trashcan.setMinWidth(100);
            grdpn_Editbox.add(trashcan, tmpColIdx, tmpRowIdx);

            trashcan.setOnDragOver(dragEvent -> {
                // accept it only if it is not dragged from the same node
                if (dragEvent.getGestureSource() != dragEvent.getGestureTarget()) {
                    // allow for both copying and moving, whatever user chooses
                    dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }

                dragEvent.consume();
            });

            trashcan.setOnDragDropped(dragEvent -> {
                if (dragEvent.getDragboard().getString() == "SWITCH") {
                    JavaFXTimetableHourText source = (JavaFXTimetableHourText) dragEvent.getGestureSource();
                    source.deleteCLS();
                    Integer SourceRow = GridPane.getRowIndex(source);
                    Integer SourceCol = GridPane.getColumnIndex(source);
                    LocalDate tmpDate = source.getDay();
                    Integer tmpTimeslot = source.getTimeslot();
                    try {
                        CoursepassLecturerSubject tmpcoursepassLecturerSubject = new CoursepassLecturerSubject(0L, getSqlConnectionManager());
                        tmpcoursepassLecturerSubject
                                .setCoursepass(source.getCoursepassLecturerSubject().getCoursepass());
                        grdpn_TimetableView.getChildren().remove(source);

                        grdpn_TimetableView.add(
                                new JavaFXTimetableHourText(tmpcoursepassLecturerSubject, tmpDate, tmpTimeslot, getSqlConnectionManager()),
                                SourceCol, SourceRow);

                        // save freetime
                        setEntryInTimetable(tmpDate, tmpcoursepassLecturerSubject, tmpTimeslot);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // update editbox
                    updateEditboxItems();
                }
                dragEvent.consume();
            });

        }
    }

    @FXML
    private void savetoFileClicked() {
        // https://docs.oracle.com/javafx/2/ui_controls/file-chooser.htm
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) {
            try {
                PrintWriter writer;
                writer = new PrintWriter(file);
                String content = "";
                ObservableList<Node> grdpn = grdpn_TimetableView.getChildren();
                String tmpDatePlaceholder = "";

                if (coursepass != null) {
                    writer.println(coursepass.getDescription());
                }

                for (Node node : grdpn) {
                    if (node instanceof JavaFXTimetableHourText) {
                        content += (((JavaFXTimetableHourText) node).getText().replace("\r\n", " ")) + ";";
                    } else if (node instanceof JavaFXTimetableDay) {
                        writer.println(content);
                        tmpDatePlaceholder = (((JavaFXTimetableDay) node).getText().replace("\r\n", ";")) + ";";
                        content = tmpDatePlaceholder;
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
                // noinspection unchecked
                elements.add((T) node);
            }
        }
        return Collections.unmodifiableList(elements);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    /**
     * TODO method is new :)
     * Returns the node that is in a certain field of a gridPane specified by row
     * and column.
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

    private void updateEditboxItems() {
        for (Node node : grdpn_Editbox.getChildren()) {
            if (node instanceof JavaFXCoursepassLecturerSubjectText) {
                ((JavaFXCoursepassLecturerSubjectText) node).updateText();
            }
        }
    }

    private void setEntryInTimetable(LocalDate TimetableDay, CoursepassLecturerSubject coursepassLecturerSubject,
            int timeslot)
            throws SQLException {
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();
        Long refcoursepassID = coursepassLecturerSubject.getCoursepass().getId();
        Long refCoursepassLecturerSubjectId = coursepassLecturerSubject.getId();
        Long refRoomId = coursepassLecturerSubject.getRoom().getId();
        Long refLecturerId = coursepassLecturerSubject.getLecturerID();
        Long refSubjectId = coursepassLecturerSubject.getSubject().getId();

        SQLValues.add(new SQLValueDate(TimetableDay));
        SQLValues.add(new SQLValueLong(refcoursepassID));
        SQLValues.add(new SQLValueLong(refCoursepassLecturerSubjectId));
        SQLValues.add(new SQLValueLong(refRoomId));
        SQLValues.add(new SQLValueLong(refLecturerId));
        SQLValues.add(new SQLValueLong(refSubjectId));
        SQLValues.add(new SQLValueInt(timeslot));

        sqlConnectionManager.execute(
                "Insert Into T_TIMETABLES (TIMETABLEDAY, REFCOURSEPASS, REFCOURSEPASSLECTURERSUBJECT, REFROOMID, REFLECTURER, REFSUBJECT, TIMESLOT) values (?, ?, ?, ?, ?, ?, ?)",
                SQLValues);
        // sqlConnectionManager.close();
    }

    public void markNewCLS(List<JavaFXTimetableHourText> timetableHourTexts, Lecturer givingLecturer){
        givingLecturer.updateLecturerResourcesBlocked();
        for (JavaFXTimetableHourText checkingTimetableHourText : timetableHourTexts) {

            CoursepassLecturerSubject tmpCoursepassLecturerSubject = checkingTimetableHourText
                    .getCoursepassLecturerSubject();

            // check if lecturer is in freeLecturers, check if day and timeslot is in
            // givingLecturer.lecturerresourcesblocked or givinglecturer.lecturerblocked
            Boolean givingLecturerResourcesBlocked = false;
            for (ResourcesBlocked resourcesBlocked : givingLecturer.getLecturerResourcesBlocked()) {
                if (resourcesBlocked.getStartDate().isBefore(checkingTimetableHourText.getDay())
                        && resourcesBlocked.getEndDate().isAfter(checkingTimetableHourText.getDay())) {
                    givingLecturerResourcesBlocked = true;
                    break;
                }

                if (resourcesBlocked.getStartDate().isEqual(checkingTimetableHourText.getDay())
                        && resourcesBlocked.getEndDate().isEqual(checkingTimetableHourText.getDay())
                        && resourcesBlocked.getStartTimeslot().equals(checkingTimetableHourText.getTimeslot())) {
                    givingLecturerResourcesBlocked = true;
                    break;
                }
            }

            // Check for lectruer Blocked
            Boolean givingLecturerBlocked = false;
            for (LecturerBlock lecturerBlock : givingLecturer.getLecturerBlocks()) {
                if (checkingTimetableHourText.getDay().getDayOfWeek().equals(lecturerBlock.getDayNr())) {
                    givingLecturerBlocked = true;
                    break;
                }
            }

            if (givingLecturerResourcesBlocked || givingLecturerBlocked) {
                // if one or both are true, set red
                checkingTimetableHourText.setFill(Color.RED);
            } else {
                // if not set green
                checkingTimetableHourText.setFill(Color.GREEN);
            }
        }
    }

    public void markSwitchableCLS(List<JavaFXTimetableHourText> timetableHourTexts, JavaFXTimetableHourText tmpText) {
        // get list of all lecturers -> done, this.timetable.getlecturers()

        // get list of all blocks for "giving" lecturer, can get called
        // lecturer->updatelecturerresourcesblocked
        Lecturer givingLecturer = tmpText.getCoursepassLecturerSubject().getLecturer();
        givingLecturer.updateLecturerResourcesBlocked();

        ArrayList<Lecturer> freeLecturers = new ArrayList<Lecturer>();
        ArrayList<Lecturer> blockedLecturers = new ArrayList<Lecturer>();
        for (Lecturer lecturer : this.timetable.getLecturers()) {
            // the giving lecturer is allways free
            if (lecturer.getId() == givingLecturer.getId()) {
                freeLecturers.add(lecturer);
                continue;
            }

            try {
                // check if all lectures are avaidable at the "giving" time
                if (Lecturer.checkLecturerAvailability(lecturer.getId(), tmpText.getDay(), tmpText.getTimeslot(), getSqlConnectionManager())) {
                    freeLecturers.add(lecturer);
                } else {
                    // if not put lecturer in notavaidable list
                    blockedLecturers.add(lecturer);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (JavaFXTimetableHourText checkingTimetableHourText : timetableHourTexts) {

            CoursepassLecturerSubject tmpCoursepassLecturerSubject = checkingTimetableHourText
                    .getCoursepassLecturerSubject();

            // check if lecturer is in freeLecturers, check if day and timeslot is in
            // givingLecturer.lecturerresourcesblocked or givinglecturer.lecturerblocked
            Boolean givingLecturerResourcesBlocked = false;
            for (ResourcesBlocked resourcesBlocked : givingLecturer.getLecturerResourcesBlocked()) {
                if (resourcesBlocked.getStartDate().isBefore(checkingTimetableHourText.getDay())
                        && resourcesBlocked.getEndDate().isAfter(checkingTimetableHourText.getDay())) {
                    givingLecturerResourcesBlocked = true;
                    break;
                }

                if (resourcesBlocked.getStartDate().isEqual(checkingTimetableHourText.getDay())
                        && resourcesBlocked.getEndDate().isEqual(checkingTimetableHourText.getDay())
                        && resourcesBlocked.getStartTimeslot().equals(checkingTimetableHourText.getTimeslot())) {
                    givingLecturerResourcesBlocked = true;
                    break;
                }
            }

            // Check for lectruer Blocked
            Boolean givingLecturerBlocked = false;
            for (LecturerBlock lecturerBlock : givingLecturer.getLecturerBlocks()) {
                if (checkingTimetableHourText.getDay().getDayOfWeek().equals(lecturerBlock.getDayNr())) {
                    givingLecturerBlocked = true;
                    break;
                }
            }

            if (freeLecturers.contains(tmpCoursepassLecturerSubject.getLecturer()) || givingLecturerResourcesBlocked
                    || givingLecturerBlocked) {
                // if one or both are true, set red
                checkingTimetableHourText.setFill(Color.RED);
            } else {
                // if not set green
                checkingTimetableHourText.setFill(Color.GREEN);
            }

        }
    }

    public SQLConnectionManager getSqlConnectionManager() {
        return sqlConnectionManager;
    }

    public void setSqlConnectionManager(SQLConnectionManager sqlConnectionManager) {
        this.sqlConnectionManager = sqlConnectionManager;
    }
    
}
