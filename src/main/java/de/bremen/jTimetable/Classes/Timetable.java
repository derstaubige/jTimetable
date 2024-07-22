package de.bremen.jTimetable.Classes;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.*;
import static java.time.temporal.ChronoUnit.DAYS;

import java.io.BufferedReader;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.stream.IntStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.dhatim.fastexcel.BorderSide;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;

import javafx.scene.text.Text;

/**
 * Class represents all timetable entries for a coursePass.
 */
public class Timetable {

    /**
     * Array that contains single TimetableDay objects that each represent one day
     * in this timetable and consists of the
     * date and number of timeslots.
     */
    private ArrayList<TimetableDay> arrayTimetableDays;

    /**
     * CourPass for which this timetable is for. This timetable fulfills the
     * requirements the subjects and resources
     * in this coursePass have.
     */
    private CoursePass coursepass;

    /**
     * Lecturer for which this timetable is for.
     */
    private Lecturer lecturer;
    // List of Lectureres that are currently planed for this timetable
    private ArrayList<Lecturer> lecturers;

    // List of Rooms that are currently planed for this timetable
    private ArrayList<Room> rooms;
    // Set the maximum Timeslotcount to fill the timetable with freetimes
    private Integer maxTimeslots = 0;

    private SQLConnectionManager sqlConnectionManager;

    private Boolean isLecturer = false;

    private ResourceBundle resourceBundle;
    private Properties properties = new Properties();

    /**
     * Constructor.
     *
     * @param coursePass the given coursePass is set for the new instance and the
     *                   corresponding timetable is
     *                   loaded into the instance
     */
    public Timetable(CoursePass coursePass, SQLConnectionManager sqlConnectionManager, ResourceBundle resourceBundle) {
        this.coursepass = coursePass;
        this.resourceBundle = resourceBundle;
        setSqlConnectionManager(sqlConnectionManager);
        try {
            FileResourcesUtils fileResourcesUtils = new FileResourcesUtils();
            properties.load(new BufferedReader(
                    new InputStreamReader(fileResourcesUtils.getFileFromResourceAsStream("/Config.properties"))));
            setMaxTimeslots(Integer.parseInt(properties.getProperty("maxTimetableSlotsPerDay")));
            getTimetable(coursePass);
        } catch (SQLException e) {
            System.err.println("Timetable for coursePass couldn't load correctly.");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor.
     *
     * @param lecturer the given lecturer is set for the new instance and the
     *                 corresponding timetable is
     *                 loaded into the instance
     */
    public Timetable(Lecturer lecturer, SQLConnectionManager sqlConnectionManager, ResourceBundle resourceBundle) {
        this.lecturer = lecturer;
        this.resourceBundle = resourceBundle;
        this.coursepass = new CoursePass(0L, sqlConnectionManager);
        setIsLecturer(true);
        setSqlConnectionManager(sqlConnectionManager);
        try {
            FileResourcesUtils fileResourcesUtils = new FileResourcesUtils();
            properties.load(new BufferedReader(
                    new InputStreamReader(fileResourcesUtils.getFileFromResourceAsStream("/Config.properties"))));
            setMaxTimeslots(Integer.parseInt(properties.getProperty("maxTimetableSlotsPerDay")));
            getTimetable(lecturer);
        } catch (SQLException e) {
            System.err.println("Timetable for lecturer couldn't load correctly.");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveCLStoFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("CLS File");
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        fileChooser.setInitialFileName(coursepass.getCourseOfStudyCaption() + "_CLS_" + date.format(formatter));

        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Excel Datei", "*.xlsx"));
        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) {

            String fileLocation = file.getAbsolutePath();
            try (OutputStream os = Files.newOutputStream(Paths.get(fileLocation));
                    Workbook wb = new Workbook(os, "MyApplication", "1.0")) {
                Worksheet ws = wb.newWorksheet("Blatt 1");
                Integer xlsRowCounter = 2;
                ws.value(xlsRowCounter, 1, coursepass.getCourseOfStudyCaption());
                xlsRowCounter++;
                ws.value(xlsRowCounter, 1, resourceBundle.getString("timetableview.subject"));
                ws.value(xlsRowCounter, 2, resourceBundle.getString("timetableview.lecturer"));
                ws.value(xlsRowCounter, 3, resourceBundle.getString("timetableview.shouldHours"));
                ws.value(xlsRowCounter, 4, resourceBundle.getString("timetableview.planedHours"));
                ws.value(xlsRowCounter, 5, resourceBundle.getString("javaFXCoursepassLecturerSubjectText.examHours"));
                xlsRowCounter++;
                for (CoursepassLecturerSubject cls : coursepass.getArrayCoursePassLecturerSubject()) {
                    ws.value(xlsRowCounter, 1, cls.getSubjectCaption());
                    ws.value(xlsRowCounter, 2, cls.getLecturerFullname());
                    ws.value(xlsRowCounter, 3, cls.getShouldHours());
                    ws.value(xlsRowCounter, 4, cls.getPlanedHours() + cls.getIsHours());
                    ws.value(xlsRowCounter, 5, cls.getExamHours());
                    xlsRowCounter++;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void setBlockingFreetext(LocalDate from, LocalDate till, String text) throws Exception {
        Integer startIndex = -1;
        Integer endIndex = -1;
        for (TimetableDay timetableDay : this.getArrayTimetableDays()) {
            if (timetableDay.getDate().isEqual(from)) {
                startIndex = this.arrayTimetableDays.indexOf(timetableDay);
            }
            if (timetableDay.getDate().isEqual(till)) {
                endIndex = this.arrayTimetableDays.indexOf(timetableDay);
            }
        }
        if (startIndex == -1 || endIndex == -1) {
            throw new Exception(resourceBundle.getString("timetableview.blockFreetext.ErrorDatesnotFound"));
        }
        CoursepassLecturerSubject cls = new CoursepassLecturerSubject(0L, sqlConnectionManager);
        cls.setCoursepass(coursepass);
        for (Integer i = startIndex; i <= endIndex; i++) {
            for (TimetableHour timetableHour : arrayTimetableDays.get(i).getArrayTimetableHours()) {
                TimetableEntry timetableEntry = new TimetableEntry(
                        timetableHour.getCoursepassLecturerSubject(),
                        arrayTimetableDays.get(i).getDate(),
                        timetableHour.getTimeslot(), sqlConnectionManager);
                timetableEntry.setBlockingFreetext(text);
                timetableEntry.setCoursepassLecturerSubject(cls);
                timetableEntry.save();
            }
        }
    }

    public void updateCoursePassTimetable() throws Exception {
        this.getTimetable(this.getCoursepass());
    }

    public void distributeUnplanedHours() {

        Integer maxTimetableSlotsUsedForInitialTimetable = Integer
                .parseInt(properties.getProperty("maxTimetableSlotsUsedForInitialTimetable")) - 1;

        TimetableDistributeStack timetableDistributeStack = new TimetableDistributeStack(coursepass,
                sqlConnectionManager);

        this.coursepass.updateCoursePassLecturerSubjects();

        if (timetableDistributeStack.size() > 0) {
            while (maxTimetableSlotsUsedForInitialTimetable < getMaxTimeslots()) {

                // Loop through the Timetable and Check all Timeslots for Freetime
                for (TimetableDay timetableDay : this.getArrayTimetableDays()) {
                    for (TimetableHour timetableHour : timetableDay.getArrayTimetableHours()) {
                        if (timetableHour != null
                                && timetableHour.getCoursepassLecturerSubject().getId() == 0L
                                && timetableHour.getTimeslot() <= maxTimetableSlotsUsedForInitialTimetable) {
                            // Freetime! Loop through clsToAddArrayList and check if one of the cls fits
                            // here
                            for (TimetableDistributeStackItem stackItem : timetableDistributeStack.getArraylist()) {
                                CoursepassLecturerSubject cls = stackItem.getArrayListItems().get(0);
                                if (CoursepassLecturerSubject.isFreeTarget(cls, timetableDay.getDate(),
                                        timetableHour.getTimeslot(), this.getSqlConnectionManager())
                                        && timetableDay.getDate().isAfter(cls.getPlaceAfterDay())) {
                                    // LEcturer and Room are free, we can place it here
                                    TimetableEntry targetTimetableEntry = new TimetableEntry(cls,
                                            timetableDay.getDate(),
                                            timetableHour.getTimeslot(), sqlConnectionManager);
                                    // delete the entry in the timetable table
                                    // save the new timetablehour
                                    try {
                                        this.addSingleHour(cls, targetTimetableEntry);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    // check if we now have distributed all unplaned hours
                                    cls.updateallHours();
                                    if (cls.getUnplanedHours() <= 0) {
                                        // no unplaned hours left, remove this cls from clsToAddArrayList
                                        stackItem.getArrayListItems().remove(cls);
                                        if (stackItem.getArrayListItems().size() <= 0) {
                                            timetableDistributeStack.getArraylist().remove(stackItem);
                                        }
                                    }
                                    timetableDistributeStack.sortStackUnplanedHours();
                                    break;
                                }
                            }
                        }
                    }
                }
                try {
                    updateCoursePassTimetable();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                maxTimetableSlotsUsedForInitialTimetable++;
            }
            // redraw timetable
        }
    }

    /**
     * Exports this Timetable to a XLSX File
     */
    public void exportTimetableToFile() {
        // https://docs.oracle.com/javafx/2/ui_controls/file-chooser.htm
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (this.isLecturer) {
            fileChooser.setInitialFileName(
                    this.getLecturer().getLecturerFullName() + "_" + date.format(formatter));
        } else {
            fileChooser.setInitialFileName(coursepass.getCourseOfStudyCaption() + "_" + date.format(formatter));
        }
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("Excel Datei", "*.xlsx"));
        File file = fileChooser.showSaveDialog(new Stage());
        if (file != null) {
            try {
                String fileLocation = file.getAbsolutePath();

                try (OutputStream os = Files.newOutputStream(Paths.get(fileLocation));
                        Workbook wb = new Workbook(os, "MyApplication", "1.0")) {
                    Worksheet ws = wb.newWorksheet("Blatt 1");

                    Integer xlsRowCounter = 0;
                    Integer tmpDOW = 9; // what was the last day? used to determin if a new week has started
                    Integer tmpMaxSlots = 0; // the max/last Timeslot that isnt freetime for the week
                    Integer tmpCol = 0;
                    Integer rowOffset = 0;
                    GregorianCalendar calendar = new GregorianCalendar();

                    for (TimetableDay tmpDay : getArrayTimetableDays()) {
                        calendar.setTime(Date.from(tmpDay.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                        if (tmpDay.getDate().getDayOfWeek().getValue() < tmpDOW) {
                            // new week new Line
                            xlsRowCounter += (rowOffset - xlsRowCounter + 1);
                            if (isLecturer) {
                                ws.value(xlsRowCounter, 1, lecturer.getLecturerFullName());
                            } else {
                                ws.value(xlsRowCounter, 1, coursepass.getCourseOfStudyCaption());
                            }
                            ws.range(xlsRowCounter, 1, xlsRowCounter, 6).merge();
                            ws.range(xlsRowCounter, 1, xlsRowCounter, 6).style().borderStyle("thin").set();
                            xlsRowCounter++;

                            if (isLecturer) {
                                tmpMaxSlots = getMaxTimeslots() - 1;
                            } else {
                                tmpMaxSlots = tmpDay.getMaxUsedTimeslotForThisWeek(coursepass);
                            }

                            // write all the new line stuff like calenderweek, timeslots and so on
                            ws.value(xlsRowCounter, 1, "KW: " + calendar.get(Calendar.WEEK_OF_YEAR));

                            // borderize
                            // border calendar week and days
                            ws.range(xlsRowCounter, 1, xlsRowCounter, 1).style().merge()
                                    .borderStyle(BorderSide.RIGHT, "thin")
                                    .borderStyle(BorderSide.TOP, "thin").set();
                            ws.range(xlsRowCounter + 1, 1, xlsRowCounter + 1, 1).style().merge()
                                    .borderStyle(BorderSide.RIGHT, "thin")
                                    .borderStyle(BorderSide.BOTTOM, "thin").set();
                            for (int i = 0; i <= tmpMaxSlots; i++) {
                                ws.value(xlsRowCounter + 2 + i * 3, 1,
                                        resourceBundle.getString("timetableview.slot" + (i + 1)));
                                ws.range(xlsRowCounter + 2 + i * 3 + 2, 1, xlsRowCounter + 2 + i * 3 + 2, 6).style()
                                        .borderStyle(BorderSide.BOTTOM, "thin").set();
                            }

                            // Monday
                            ws.range(xlsRowCounter, 2, xlsRowCounter, 2).style().merge()
                                    .borderStyle(BorderSide.LEFT, "thin").borderStyle(BorderSide.TOP, "thin")
                                    .borderStyle(BorderSide.RIGHT, "thin").bold().set();
                            ws.range(xlsRowCounter + 1, 2, xlsRowCounter + 1, 2).style().merge()
                                    .borderStyle(BorderSide.LEFT, "thin").borderStyle(BorderSide.BOTTOM, "thin")
                                    .borderStyle(BorderSide.RIGHT, "thin").bold().set();
                            for (int i = 0; i <= tmpMaxSlots; i++) {
                                ws.range(xlsRowCounter + 2 + i * 3, 2, xlsRowCounter + 2 + i * 3 + 1, 2).style()
                                        .borderStyle(BorderSide.LEFT, "thin").set();
                                ws.range(xlsRowCounter + 2 + i * 3 + 2, 2, xlsRowCounter + 2 + i * 3 + 2, 2).style()
                                        .borderStyle(BorderSide.BOTTOM, "thin").borderStyle(BorderSide.LEFT, "thin")
                                        .set();
                            }

                            ws.range(xlsRowCounter, 3, xlsRowCounter, 3).style().merge()
                                    .borderStyle(BorderSide.LEFT, "thin").borderStyle(BorderSide.TOP, "thin")
                                    .borderStyle(BorderSide.RIGHT, "thin").bold().set();
                            ws.range(xlsRowCounter + 1, 3, xlsRowCounter + 1, 3).style().merge()
                                    .borderStyle(BorderSide.LEFT, "thin").borderStyle(BorderSide.BOTTOM, "thin")
                                    .borderStyle(BorderSide.RIGHT, "thin").bold().set();
                            for (int i = 0; i <= tmpMaxSlots; i++) {
                                ws.range(xlsRowCounter + 2 + i * 3, 3, xlsRowCounter + 2 + i * 3 + 1, 3).style()
                                        .borderStyle(BorderSide.LEFT, "thin").set();
                                ws.range(xlsRowCounter + 2 + i * 3 + 2, 3, xlsRowCounter + 2 + i * 3 + 2, 3).style()
                                        .borderStyle(BorderSide.BOTTOM, "thin").borderStyle(BorderSide.LEFT, "thin")
                                        .set();
                            }

                            ws.range(xlsRowCounter, 4, xlsRowCounter, 4).style().merge()
                                    .borderStyle(BorderSide.LEFT, "thin").borderStyle(BorderSide.TOP, "thin")
                                    .borderStyle(BorderSide.RIGHT, "thin").bold().set();
                            ws.range(xlsRowCounter + 1, 4, xlsRowCounter + 1, 4).style().merge()
                                    .borderStyle(BorderSide.LEFT, "thin").borderStyle(BorderSide.BOTTOM, "thin")
                                    .borderStyle(BorderSide.RIGHT, "thin").bold().set();
                            for (int i = 0; i <= tmpMaxSlots; i++) {
                                ws.range(xlsRowCounter + 2 + i * 3, 4, xlsRowCounter + 2 + i * 3 + 1, 4).style()
                                        .borderStyle(BorderSide.LEFT, "thin").set();
                                ws.range(xlsRowCounter + 2 + i * 3 + 2, 4, xlsRowCounter + 2 + i * 3 + 2, 4).style()
                                        .borderStyle(BorderSide.BOTTOM, "thin").borderStyle(BorderSide.LEFT, "thin")
                                        .set();
                            }

                            ws.range(xlsRowCounter, 5, xlsRowCounter, 5).style().merge()
                                    .borderStyle(BorderSide.LEFT, "thin").borderStyle(BorderSide.TOP, "thin")
                                    .borderStyle(BorderSide.RIGHT, "thin").bold().set();
                            ws.range(xlsRowCounter + 1, 5, xlsRowCounter + 1, 5).style().merge()
                                    .borderStyle(BorderSide.LEFT, "thin").borderStyle(BorderSide.BOTTOM, "thin")
                                    .borderStyle(BorderSide.RIGHT, "thin").bold().set();
                            for (int i = 0; i <= tmpMaxSlots; i++) {
                                ws.range(xlsRowCounter + 2 + i * 3, 5, xlsRowCounter + 2 + i * 3 + 1, 5).style()
                                        .borderStyle(BorderSide.LEFT, "thin").set();
                                ws.range(xlsRowCounter + 2 + i * 3 + 2, 5, xlsRowCounter + 2 + i * 3 + 2, 5).style()
                                        .borderStyle(BorderSide.BOTTOM, "thin").borderStyle(BorderSide.LEFT, "thin")
                                        .set();
                            }

                            ws.range(xlsRowCounter, 6, xlsRowCounter, 6).style().merge()
                                    .borderStyle(BorderSide.LEFT, "thin").borderStyle(BorderSide.TOP, "thin")
                                    .bold().set();
                            ws.range(xlsRowCounter + 1, 6, xlsRowCounter + 1, 6).style().merge()
                                    .borderStyle(BorderSide.LEFT, "thin").borderStyle(BorderSide.BOTTOM, "thin")
                                    .bold().set();
                            for (int i = 0; i <= tmpMaxSlots; i++) {
                                ws.range(xlsRowCounter + 2 + i * 3, 6, xlsRowCounter + 2 + i * 3 + 1, 6).style()
                                        .borderStyle(BorderSide.LEFT, "thin").set();
                                ws.range(xlsRowCounter + 2 + i * 3 + 2, 6, xlsRowCounter + 2 + i * 3 + 2, 6).style()
                                        .borderStyle(BorderSide.BOTTOM, "thin").borderStyle(BorderSide.LEFT, "thin")
                                        .set();
                            }

                        }
                        tmpDOW = tmpDay.getDate().getDayOfWeek().getValue();
                        tmpCol = 1 + tmpDOW;
                        ws.value(xlsRowCounter, tmpCol, tmpDay.getDate().format(DateTimeFormatter.ofPattern("EEEE")));
                        ws.value(xlsRowCounter + 1, tmpCol,
                                tmpDay.getDate().format(DateTimeFormatter.ofPattern("dd.MM.uuuu")));
                        rowOffset = xlsRowCounter + 2;
                        for (TimetableHour tmpHour : tmpDay.getArrayTimetableHours()) {
                            TimetableEntry timetableEntry = new TimetableEntry(coursepass, tmpDay.getDate(),
                                    tmpHour.getTimeslot(), sqlConnectionManager);
                            if (tmpHour.getCoursepassLecturerSubject().getLecturerID() != 0
                                    || timetableEntry.getBlockingFreetext() != null) {
                                if (timetableEntry.isExam()) {
                                    ws.value(rowOffset, tmpCol,
                                            resourceBundle.getString("timetableview.exam") + ": "
                                                    + tmpHour.getCoursepassLecturerSubject().getSubject().getCaption());
                                } else if (timetableEntry.getBlockingFreetext() != null) {
                                    ws.value(rowOffset, tmpCol,
                                            resourceBundle.getString("timetableview.blockFreetext") + ": "
                                                    + timetableEntry.getBlockingFreetext());
                                } else {
                                    ws.value(rowOffset, tmpCol,
                                            tmpHour.getCoursepassLecturerSubject().getSubject().getCaption());
                                }
                                if (isLecturer) {
                                    ws.value(rowOffset + 1, tmpCol,
                                            tmpHour.getCoursepassLecturerSubject().getCoursepass()
                                                    .getCourseOfStudyCaption());
                                } else if (timetableEntry.getBlockingFreetext() != null) {

                                } else {
                                    ws.value(rowOffset + 1, tmpCol,
                                            tmpHour.getCoursepassLecturerSubject().getLecturerFullname());
                                }
                                if (timetableEntry.getBlockingFreetext() != null) {
                                } else {
                                    ws.value(rowOffset + 2, tmpCol,
                                            tmpHour.getCoursepassLecturerSubject().getRoom().getCaption());
                                }
                            }
                            rowOffset += 3;
                        }
                        ws.pageOrientation("landscape");
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Method adds one lesson in a timeslot to the timetable and saves the entry in
     * the corresponding database table.
     *
     * @param timetableHour lesson that is added, consisting of timeslot, lecturer
     *                      name, subject caption and room
     *                      caption
     * @param day           date at which the lesson is added
     * @param timeslot      timeslot in which the lesson is added
     */
    public void addSingleHour(CoursepassLecturerSubject cls, TimetableEntry targetTimetableEntry) throws Exception {

        // save the change in the timetable table

        if (CoursepassLecturerSubject.isFreeTarget(cls, targetTimetableEntry.getDate(),
                targetTimetableEntry.getTimeslot(), sqlConnectionManager)) {
            targetTimetableEntry.update(cls, targetTimetableEntry.getDate(), targetTimetableEntry.getTimeslot(),
                    targetTimetableEntry.isExam());
        } else {
            throw new Exception("Error Placing Hour. Target isnt Free " + targetTimetableEntry.getDate() + " "
                    + targetTimetableEntry.getTimeslot() + " " + cls.getSubjectCaption());
        }
        updateCoursePassTimetable();
    }

    /**
     * @param source
     * @param target
     *               Swap two CLS
     */
    public void swapHours(TimetableEntry source, TimetableEntry target) throws Exception {
        CoursepassLecturerSubject sourceCLS = source.getCoursepassLecturerSubject();
        CoursepassLecturerSubject targetCLS = target.getCoursepassLecturerSubject();
        Boolean sourceIsExam = source.isExam();
        Boolean targetIsExam = target.isExam();

        if (CoursepassLecturerSubject.isFreeTarget(sourceCLS, target.getDate(), target.getTimeslot(),
                sqlConnectionManager)
                && CoursepassLecturerSubject.isFreeTarget(targetCLS, source.getDate(), source.getTimeslot(),
                        sqlConnectionManager)) {
            source.update(targetCLS, source.getDate(), source.getTimeslot(), targetIsExam);
            target.update(sourceCLS, target.getDate(), target.getTimeslot(), sourceIsExam);
            try {
                updateCoursePassTimetable();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new Exception("Hours cant be swapped");
        }
    }

    /**
     * Method deletes this timetable by deleting the blocked resources in the
     * database through the method
     * deleteResourceBlocked() and then deleting the timetable entries through
     * overload of this method.
     */
    public void deleteTimetable() {
        // Loop through all Days and Hours and Delete ResourceBlocked and Timetable
        TimetableEntry timetableEntry;
        for (TimetableDay arrayTimetableDay : arrayTimetableDays) {
            for (TimetableHour timetableHour : arrayTimetableDay.getArrayTimetableHours()) {
                if (timetableHour == null) {
                    continue;
                }
                timetableEntry = new TimetableEntry(this.getCoursepass(), arrayTimetableDay.getDate(),
                        timetableHour.getTimeslot(), sqlConnectionManager);
                timetableEntry.delete();
            }
        }
        try {
            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();

            SQLValues.add(new SQLValueLong(coursepass.getId()));
            sqlConnectionManager.execute("DELETE FROM T_Timetables where REFcoursepass = ?", SQLValues);
            updateCoursePassTimetable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Getter for this.arrayTimetableDays
     *
     * @return this.arrayTimetableDays
     */
    public ArrayList<TimetableDay> getArrayTimetableDays() {
        return arrayTimetableDays;
    }

    public ArrayList<Lecturer> getLecturers() {
        return lecturers;
    }

    public SQLConnectionManager getSqlConnectionManager() {
        return sqlConnectionManager;
    }

    public void setSqlConnectionManager(SQLConnectionManager sqlConnectionManager) {
        this.sqlConnectionManager = sqlConnectionManager;
    }

    public Integer getMaxTimeslots() {
        return maxTimeslots;
    }

    public Lecturer getLecturer() {
        return lecturer;
    }

    public ArrayList<Room> getRooms() {
        return rooms;
    }

    public CoursePass getCoursepass() {
        return coursepass;
    }

    /**
     * Runs database query to get resultSet with all timetable entries for given
     * lecturer. The resultSet will
     * be loaded into this object with method loadTimetableFromResultSet().
     *
     * @param lecturer for whom the timetable is loaded
     */
    private void getTimetable(Lecturer lecturer) throws SQLException {
        // Create new SQLValues that are used for the following select statement
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();
        SQLValues.add(new SQLValueLong(lecturer.getId()));
        SQLValues.add(new SQLValueDate(LocalDate.now()));

        ResultSet rs = sqlConnectionManager.select(
                "Select * From T_TIMETABLES where REFLECTURER =? and TIMETABLEDAY >= ? " +
                        "ORDER BY TIMETABLEDAY, TIMESLOT ASC;",
                SQLValues);

        loadTimetableFromResultSet(rs);

        // sqlConnectionManager.close();
    }

    /**
     * Runs database query to get resultSet with all timetable entries for given
     * coursePass. The resultSet will
     * be loaded into this object with method loadTimetableFromResultSet().
     *
     * @param coursePass for which the timetable is loaded
     */
    private void getTimetable(CoursePass coursePass) throws SQLException {
        // Create new SQLValues that are used for the following select statement
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();
        SQLValues.add(new SQLValueLong(coursePass.getId()));
        // Create new Connection to database

        ResultSet rs = sqlConnectionManager.select(
                "Select * From T_TIMETABLES where REFCOURSEPASS=? ORDER BY TIMETABLEDAY, TIMESLOT ASC;", SQLValues);
        ArrayList<TimetableDay> result = new ArrayList<>();

        loadTimetableFromResultSet(rs);

        // if we haven't added CoursePassLecturerSubjects for this CoursePass yet,
        // we should return an empty array to display
        if (this.arrayTimetableDays.size() == 0) {
            Resourcemanager resourcemanager = new Resourcemanager(getSqlConnectionManager());
            result = resourcemanager.getWorkingDaysBetweenTwoDates(coursePass.getStart(), coursePass.getEnd());
            for (TimetableDay tmpTimetableDay : result) {

                ArrayList<TimetableHour> tmpArrayList = new ArrayList<>();
                Iterator<Integer> timeslotIterator = IntStream.range(0, maxTimeslots).boxed().iterator();
                while (timeslotIterator.hasNext()) {
                    tmpArrayList.add(new TimetableHour(timeslotIterator.next(),
                            new CoursepassLecturerSubject(0L, getSqlConnectionManager(), this.coursepass),
                            getSqlConnectionManager()));
                }
                tmpTimetableDay.setArrayTimetableHours(tmpArrayList);
                this.arrayTimetableDays.add(tmpTimetableDay);
            }
        }

        // grap all lecturers that are currently in this timetable
        this.lecturers = getAllLecturers();

        // grap all rooms that are currently in this timetable
        this.rooms = getAllRooms();

        // sqlConnectionManager.close();
    }

    private ArrayList<Room> getAllRooms() {
        ArrayList<Room> rooms = new ArrayList<Room>();
        try {
            // Create new SQLValues that are used for the following select statement
            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();
            SQLValues.add(new SQLValueLong(this.coursepass.getId()));
            // Create new Connection to database

            ResultSet rs = sqlConnectionManager
                    .select("SELECT DISTINCT refroomid FROM T_TIMETABLES where refcoursepass = ?;", SQLValues);

            Room tmpRoom;

            while (rs.next()) {
                tmpRoom = new Room(rs.getLong("refroomid"), getSqlConnectionManager());
                rooms.add(tmpRoom);
                tmpRoom.updateRoomBlocks();
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return rooms;
    }

    private ArrayList<Lecturer> getAllLecturers() {
        ArrayList<Lecturer> lecturers = new ArrayList<Lecturer>();
        try {
            // Create new SQLValues that are used for the following select statement
            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();
            SQLValues.add(new SQLValueLong(this.coursepass.getId()));
            // Create new Connection to database

            ResultSet rs = sqlConnectionManager
                    .select("SELECT DISTINCT reflecturer FROM T_TIMETABLES where refcoursepass = ?;", SQLValues);

            while (rs.next()) {
                lecturers.add(new Lecturer(rs.getLong("reflecturer"), getSqlConnectionManager()));
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return lecturers;
    }

    /**
     * Loads all already planned hours from a resultSet into this object.
     *
     * @param resultSet contains timetable entries from a database
     */
    private void loadTimetableFromResultSet(ResultSet resultSet) throws SQLException {
        this.arrayTimetableDays = getWorkingDaysBetweenTwoDates(this.coursepass.getStart(), this.coursepass.getEnd());

        // loop through all days and add freetimes in slots that dont have subjects jet
        for (TimetableDay tmpTimetableday : this.arrayTimetableDays) {
            // loop through all possible timeslots and check if there is already a subject
            Iterator<Integer> timeslotIterator = IntStream.range(0, maxTimeslots).boxed().iterator();
            while (timeslotIterator.hasNext()) {
                Integer tmpTimeslot = timeslotIterator.next();
                while (tmpTimetableday.getArrayTimetableHours().size() <= tmpTimeslot) {
                    tmpTimetableday.getArrayTimetableHours().add(tmpTimetableday.getArrayTimetableHours().size(),
                            new TimetableHour(tmpTimeslot, new CoursepassLecturerSubject(0L, getSqlConnectionManager()),
                                    getSqlConnectionManager()));
                }

                tmpTimetableday.getArrayTimetableHours().set(tmpTimeslot,
                        new TimetableHour(tmpTimeslot, new CoursepassLecturerSubject(0L, getSqlConnectionManager()),
                                getSqlConnectionManager()));

            }
        }
        while (resultSet.next()) {

            TimetableDay tmpDayObject = null;
            long tmpTimeslot = resultSet.getLong("Timeslot");
            LocalDate tmpDate = resultSet.getDate("TIMETABLEDAY").toLocalDate();

            // Check if timetableDay object exists
            for (TimetableDay day : arrayTimetableDays) {
                // If exists select correct timetableDay, to add new Hours to it
                if (day.getDate().isEqual(tmpDate)) {
                    tmpDayObject = day;
                    break;
                }
            }

            // If day doesn't exist, create a new object
            if (tmpDayObject == null) {
                tmpDayObject = new TimetableDay(tmpDate, (int) tmpTimeslot, getSqlConnectionManager());

                this.arrayTimetableDays.add(tmpDayObject);
            }

            // Check if we have to add to the max timeslots per day
            try {
                tmpDayObject.getArrayTimetableHours().get((int) tmpTimeslot);
            } catch (Exception e) {
                tmpDayObject.setTimeslots((int) tmpTimeslot);
            }

            if (isLecturer) {
                // add this timeslot/TimetableHour to our tmpDayObject
                tmpDayObject.getArrayTimetableHours().set((int) tmpTimeslot, new TimetableHour((int) tmpTimeslot,
                        new CoursepassLecturerSubject(resultSet.getLong("REFCOURSEPASSLECTURERSUBJECT"),
                                getSqlConnectionManager(), new CoursePass(0L, sqlConnectionManager)),
                        getSqlConnectionManager()));
            } else {
                // add this timeslot/TimetableHour to our tmpDayObject
                tmpDayObject.getArrayTimetableHours().set((int) tmpTimeslot, new TimetableHour((int) tmpTimeslot,
                        new CoursepassLecturerSubject(resultSet.getLong("REFCOURSEPASSLECTURERSUBJECT"),
                                getSqlConnectionManager(), this.coursepass),
                        getSqlConnectionManager()));

            }

        }
    }

    private ArrayList<TimetableDay> getWorkingDaysBetweenTwoDates(LocalDate localstartDate,
            LocalDate localendDate) {
        // https://stackoverflow.com/questions/4600034/calculate-number-of-weekdays-between-two-dates-in-java

        // https://schegge.de/2020/01/kalenderspielereien-mit-java/#:~:text=Feiertage%20mit%20einem%20festen%20Datum,%2D%2D01%2D01%22).

        ArrayList<TimetableDay> arrayTimetabledays = new ArrayList<>();
        // default time zone
        ZoneId defaultZoneId = ZoneId.systemDefault();

        // local date + atStartOfDay() + default time zone + toInstant() = Date
        Date startDate = Date.from(localstartDate.atStartOfDay(defaultZoneId).toInstant());
        Date endDate = Date.from(localendDate.atStartOfDay(defaultZoneId).toInstant());
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);

        // int workDays = 0;

        // Return 0 if start and end are the same
        if (startCal.getTimeInMillis() == endCal.getTimeInMillis()) {
            return arrayTimetabledays;
        }

        if (startCal.getTimeInMillis() > endCal.getTimeInMillis()) {
            startCal.setTime(endDate);
            endCal.setTime(startDate);
        }

        do {
            // excluding start date

            if (startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY &&
                    startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY &&
                    !this.isHoliday(LocalDateTime.ofInstant(startCal.toInstant(), defaultZoneId)
                            .toLocalDate())) {

                arrayTimetabledays.add(new TimetableDay(
                        LocalDateTime.ofInstant(startCal.toInstant(), defaultZoneId)
                                .toLocalDate(),
                        4, getSqlConnectionManager()));
                // ++workDays;
            }
            startCal.add(Calendar.DAY_OF_MONTH, 1);
        } while (startCal.getTimeInMillis() <= endCal.getTimeInMillis()); // excluding end date

        return arrayTimetabledays;
    }

    private LocalDate getEasterDate(int year) {
        int a = year % 19;
        int b = year % 4;
        int c = year % 7;
        int H1 = year / 100;
        int H2 = year / 400;
        int N = 4 + H1 - H2;
        int M = 15 + H1 - H2 - (8 * H1 + 13) / 25;
        int d = (19 * a + M) % 30;
        int e = (2 * b + 4 * c + 6 * d + N) % 7;
        int f = (c + 11 * d + 22 * e) / 451;
        int tage = 22 + d + e - 7 * f;
        return LocalDate.of(year, 3, 1).plus(tage - 1, DAYS);
    }

    private Map<LocalDate, String> getHolydaysMap(int year) {
        Map<LocalDate, String> holidays = new HashMap<>();
        holidays.put(MonthDay.parse("--01-01").atYear(year), "Neujahr");
        holidays.put(getEasterDate(year).plus(-2, DAYS), "Karfreitag");
        holidays.put(getEasterDate(year).plus(0, DAYS), "Ostersonntag");
        holidays.put(getEasterDate(year).plus(1, DAYS), "Ostermonntag");
        holidays.put(getEasterDate(year).plus(39, DAYS), "Himmelfahrt");
        holidays.put(getEasterDate(year).plus(49, DAYS), "Pfingstsonntag");
        holidays.put(getEasterDate(year).plus(50, DAYS), "Pfingstmontag");
        holidays.put(MonthDay.parse("--05-01").atYear(year), "Tag der Arbeit");
        holidays.put(MonthDay.parse("--10-03").atYear(year), "Tag der Einheit");
        holidays.put(MonthDay.parse("--10-31").atYear(year), "Reformationstag");
        holidays.put(MonthDay.parse("--12-24").atYear(year), "Heiligabend");
        holidays.put(MonthDay.parse("--12-25").atYear(year), "1. Weihnachtstag");
        holidays.put(MonthDay.parse("--12-26").atYear(year), "2. Weihnachtstag");
        holidays.put(MonthDay.parse("--12-31").atYear(year), "Sylvester");
        return holidays;
    }

    public boolean isHoliday(LocalDate date) {
        return getHolydaysMap(date.getYear()).containsKey(date);
    }

    public Boolean getIsLecturer() {
        return isLecturer;
    }

    public void setIsLecturer(Boolean isLecturer) {
        this.isLecturer = isLecturer;
    }

    public void setMaxTimeslots(Integer maxTimeslots) {
        this.maxTimeslots = maxTimeslots;
    }

}
