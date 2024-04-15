package de.bremen.jTimetable.fxmlController;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import de.bremen.jTimetable.Main;
import de.bremen.jTimetable.Classes.*;
import javafx.scene.text.Font;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class TimetableViewController implements Initializable {

    @FXML
    public GridPane grdpn_TimetableView;
    @FXML
    public ScrollPane scrollpane_TimetableView;
    @FXML
    public AnchorPane anchorpane_Editbox;
    @FXML
    public GridPane grdpn_Editbox;
    @FXML
    public Button savetofile;
    @FXML
    private Label lbl_Slot1;
    @FXML
    private Button btnDistributeUnplanedHours;

    private Timetable timetable;
    private CoursePass coursepass;
    private SQLConnectionManager sqlConnectionManager;
    private ResourceBundle resourceBundle;
    private Boolean isLecturer = false;

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
        this.timetable = new Timetable(coursepass, getSqlConnectionManager(), resourceBundle);
        this.drawTimetable(this.timetable, true);
    }

    public void initDataTimetable(Timetable timetable) {
        this.timetable = timetable;
        this.isLecturer = true;
        this.timetable.setIsLecturer(isLecturer);
        this.drawTimetable(timetable, false);
    }

    private void drawTimetable(Timetable timetable, Boolean dragandDrop) {

        int inttmpRowIdx = 2;
        Calendar.getInstance();
        ZoneId.systemDefault();
        for (TimetableDay day : timetable.getArrayTimetableDays()) {
            LocalDate tmpDate = day.getDate();
            grdpn_TimetableView.add(new JavaFXTimetableDay(tmpDate, getSqlConnectionManager()), 0, inttmpRowIdx);

            for (TimetableHour timeslot : day.getArrayTimetableHours()) {
                if (timeslot == null) {
                    continue;
                }
                JavaFXTimetableHourText tmpText;
                if (dragandDrop == true) {
                    tmpText = new JavaFXTimetableHourText(timeslot.getCoursepassLecturerSubject(),
                            new TimetableEntry(timeslot.getCoursepassLecturerSubject(), day.getDate(),
                                    timeslot.getTimeslot(), false, sqlConnectionManager),
                            getSqlConnectionManager());
                } else {
                    tmpText = new JavaFXTimetableHourText(timeslot.getCoursepassLecturerSubject(),
                            new TimetableEntry(timeslot.getCoursepassLecturerSubject(), day.getDate(),
                                    timeslot.getTimeslot(), false, sqlConnectionManager),
                            true, getSqlConnectionManager());
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

                            TimetableEntry sourceTimetableEntry = new TimetableEntry(
                                    source.getCoursepassLecturerSubject(),
                                    source.getDay(), source.getTimeslot(), false, sqlConnectionManager);
                            TimetableEntry targetTimetableEntry = new TimetableEntry(
                                    target.getCoursepassLecturerSubject(),
                                    target.getDay(), target.getTimeslot(), false, sqlConnectionManager);

                            if (CoursepassLecturerSubject.cangetExchanged(sourceTimetableEntry, targetTimetableEntry,
                                    getSqlConnectionManager()) == true) {
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

                                try {
                                    this.timetable.swapHours(sourceTimetableEntry, targetTimetableEntry);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            } else {
                                // System.out.println("Error switching Hours");
                            }
                        }

                        if (dragEvent.getDragboard().getString() == "NEW") {
                            JavaFXCoursepassLecturerSubjectText source = (JavaFXCoursepassLecturerSubjectText) dragEvent
                                    .getGestureSource();
                            JavaFXTimetableHourText target = (JavaFXTimetableHourText) dragEvent.getGestureTarget();

                            if (CoursepassLecturerSubject.isFreeTarget(source.getCoursepassLecturerSubject(),
                                    target.getDay(),
                                    target.getTimeslot(), getSqlConnectionManager()) == true) {
                                TimetableEntry targetTimetableEntry = new TimetableEntry(
                                        target.getCoursepassLecturerSubject(), target.getDay(), target.getTimeslot(),
                                        false,
                                        sqlConnectionManager);
                                try {
                                    timetable.addSingleHour(source.getCoursepassLecturerSubject(),
                                            targetTimetableEntry);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                // update visuals
                                Integer TargetRow = GridPane.getRowIndex(target);
                                Integer TargetCol = GridPane.getColumnIndex(target);
                                grdpn_TimetableView.getChildren().remove(target);
                                grdpn_TimetableView
                                        .add(new JavaFXTimetableHourText(source.getCoursepassLecturerSubject(),
                                                target.getDay(), target.getTimeslot(), getSqlConnectionManager()),
                                                TargetCol, TargetRow);
                                updateEditboxItems();
                            }

                        }
                        if (dragEvent.getDragboard().getString() == "EXAM") {
                            VBox examBox = (VBox) dragEvent.getGestureSource();
                            ComboBox examComboBox = (ComboBox) examBox.getChildren().get(1);
                            CoursepassLecturerSubject cls = (CoursepassLecturerSubject) examComboBox.getValue(); // get
                                                                                                                 // selected
                                                                                                                 // cls
                            if (cls == null) { // es erfolgte keine Auswahl
                                return;
                            }

                            JavaFXTimetableHourText target = (JavaFXTimetableHourText) dragEvent.getGestureTarget();

                            TimetableEntry targetTimetableEntry = new TimetableEntry(
                                    target.getCoursepassLecturerSubject(), target.getDay(), target.getTimeslot(),
                                    true,
                                    sqlConnectionManager);
                            targetTimetableEntry.setExam(true);
                            targetTimetableEntry.save();
                            try {
                                timetable.addSingleHour(cls,
                                        targetTimetableEntry);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            // update visuals
                            Integer TargetRow = GridPane.getRowIndex(target);
                            Integer TargetCol = GridPane.getColumnIndex(target);
                            grdpn_TimetableView.getChildren().remove(target);
                            grdpn_TimetableView
                                    .add(new JavaFXTimetableHourText(cls,
                                            targetTimetableEntry, getSqlConnectionManager()),
                                            TargetCol, TargetRow);
                            updateEditboxItems();
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
            Integer tmpColIdx = 1;

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

                    this.markNewCLS(timetableHourTexts, cls);
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
            // Add a ExamButton to Plan Exams
            Label examLabel = new Label("\uD83D\uDCD3 " + resourceBundle.getString("timetableview.exam"));
            Font defaultFont = new Font(38);
            examLabel.setFont(defaultFont);
            examLabel.setTextAlignment(TextAlignment.CENTER);
            examLabel.setMinWidth(100);

            ComboBox comboBox = new ComboBox<CoursepassLecturerSubject>();
            comboBox.getItems().addAll(timetable.getCoursepass().getArrayCoursePassLecturerSubject());

            VBox examVBox = new VBox(examLabel, comboBox);
            grdpn_Editbox.add(examVBox, tmpColIdx, tmpRowIdx);

            examVBox.setOnDragDetected(mouseEvent -> {
                // allow any transfer mode
                Dragboard db = examVBox.startDragAndDrop(TransferMode.ANY);

                // Put a string on a dragboard
                ClipboardContent content = new ClipboardContent();
                content.putString("EXAM");

                db.setContent(content);

                // check for all JavaFXTimetableHourText if they could be exchanged and color
                // them
                List<JavaFXTimetableHourText> timetableHourTexts = getNodesOfType(grdpn_TimetableView,
                        JavaFXTimetableHourText.class);
                if(comboBox.getValue() != null){
                    this.markNewCLS(timetableHourTexts, (CoursepassLecturerSubject) comboBox.getValue());
                }
                mouseEvent.consume();
            });

            
            examVBox.setOnDragDone(dragEvent -> {
                // check for all JavaFXTimetableHourText if they could be exchanged and color
                // them
                List<JavaFXTimetableHourText> timetableHourTexts = getNodesOfType(grdpn_TimetableView,
                        JavaFXTimetableHourText.class);
                for (int i = 0; i < timetableHourTexts.size(); i++) {
                    timetableHourTexts.get(i).setFill(Color.BLACK);
                }
            });

            tmpColIdx++;
            if (tmpColIdx > timetable.getMaxTimeslots()) {
                tmpColIdx = 0;
                tmpRowIdx++;
            }

            // Add a Trashcan to Delete Planed Hours
            Label trashcan = new Label("\uD83D\uDDD1");
            defaultFont = new Font(38);
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
                        CoursepassLecturerSubject tmpcoursepassLecturerSubject = new CoursepassLecturerSubject(0L,
                                getSqlConnectionManager());
                        tmpcoursepassLecturerSubject
                                .setCoursepass(source.getCoursepassLecturerSubject().getCoursepass());
                        grdpn_TimetableView.getChildren().remove(source);

                        grdpn_TimetableView.add(
                                new JavaFXTimetableHourText(tmpcoursepassLecturerSubject, tmpDate, tmpTimeslot,
                                        getSqlConnectionManager()),
                                SourceCol, SourceRow);

                        // save freetime
                        TimetableEntry timetableEntry = new TimetableEntry(tmpcoursepassLecturerSubject, tmpDate,
                                tmpTimeslot, false, sqlConnectionManager);
                        timetableEntry.save();

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
    private void distributeUnplanedHoursClicked(ActionEvent event) {
        this.timetable.distributeUnplanedHours();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(resourceBundle.getString("coursepass.inittimetable.successtitle"));
        alert.setContentText(resourceBundle.getString("coursepass.inittimetable.successmessage"));
        alert.show();
        this.reloadWindow(event);
    };

    private void reloadWindow(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/TimetableView.fxml"), resourceBundle);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle("Timetable for " + coursepass.getCourseOfStudy().getCaption() + " " +
                coursepass.getStudySection().getDescription());
        URL url = Main.class.getResource("fxml/TimetableView.fxml");
        loader.setLocation(url);
        try {
            stage.setScene(new Scene(loader.load()));
            TimetableViewController timetableViewController = loader.getController();
            timetableViewController.setSqlConnectionManager(getSqlConnectionManager());
            timetableViewController
                    .initDataCoursepass(new CoursePass((coursepass.getId()), getSqlConnectionManager()));
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void savetoFileClicked() {
        this.timetable.exportTimetableToFile();
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
        btnDistributeUnplanedHours.setOnAction(event -> {
            this.distributeUnplanedHoursClicked(event);
        });
        Platform.runLater(() -> {
            if (this.isLecturer) {
                anchorpane_Editbox.setVisible(false);
            }
        });
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

    public void markNewCLS(List<JavaFXTimetableHourText> timetableHourTexts, CoursepassLecturerSubject cls) {
        Lecturer givingLecturer = cls.getLecturer();
        givingLecturer.updateLecturerResourcesBlocked();

        Room givingRoom = cls.getRoom();
        givingRoom.updateRoomBlocks();

        for (JavaFXTimetableHourText checkingTimetableHourText : timetableHourTexts) {
            // check if lecturer is in freeLecturers, check if day and timeslot is in
            LocalDate dateToCheck = checkingTimetableHourText.getDay();
            Integer timeslotToCheck = checkingTimetableHourText.getTimeslot();

            // Check for lectruer Blocked
            Boolean givingLecturerBlocked = false;
            try {
                if (Lecturer.checkLecturerAvailability(givingLecturer.getId(), dateToCheck, timeslotToCheck,
                        sqlConnectionManager) == false) {
                    givingLecturerBlocked = true;
                }
            } catch (Exception e) {
                // TODO: handle exception
            }

            // ToDo: Check for room blocked!
            Boolean givingRoomBlocked = false;
            if (givingRoom.isRoomAvailable(dateToCheck, timeslotToCheck) == false) {
                givingRoomBlocked = true;
            }

            if (givingLecturerBlocked || givingRoomBlocked) {
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
        Room givingRoom = tmpText.getCoursepassLecturerSubject().getRoom();
        Room targetRoom;

        givingLecturer.updateLecturerResourcesBlocked();
        givingRoom.updateRoomBlocks();

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
                if (Lecturer.checkLecturerAvailability(lecturer.getId(), tmpText.getDay(), tmpText.getTimeslot(),
                        getSqlConnectionManager())) {
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

            // Check if givingLectruer is Blocked
            Boolean givingLecturerBlocked = false;
            if (givingLecturer.checkifLecturerisBlocked(checkingTimetableHourText.getDay(),
                    checkingTimetableHourText.getTimeslot())) {
                givingLecturerBlocked = true;
            }

            // check if rooms are free
            targetRoom = checkingTimetableHourText.getCoursepassLecturerSubject().getRoom();
            Boolean givingRoomBlocked = false;
            Boolean targetRoomBlocked = false;
            if (givingRoom.isRoomAvailable(checkingTimetableHourText.getDay(),
                    checkingTimetableHourText.getTimeslot()) == false) {
                givingRoomBlocked = true;
            }

            if (targetRoom.isRoomAvailable(tmpText.getDay(), tmpText.getTimeslot()) == false) {
                targetRoomBlocked = true;
            }

            if (freeLecturers.contains(tmpCoursepassLecturerSubject.getLecturer()) == false
                    || givingLecturerBlocked || givingRoomBlocked || targetRoomBlocked) {
                // something isnt avaidable
                checkingTimetableHourText.setFill(Color.RED);
            } else {
                // everything is possible
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
