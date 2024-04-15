package de.bremen.jTimetable.Classes;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.*;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.stream.IntStream;
import java.io.FileInputStream;
import org.dhatim.fastexcel.BorderSide;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;

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
            properties.load(new FileInputStream(
                    Thread.currentThread().getContextClassLoader().getResource("").getPath() + "Config.properties"));
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
        setSqlConnectionManager(sqlConnectionManager);
        setMaxTimeslots(Integer.parseInt(properties.getProperty("maxTimetableSlotsPerDay")));
        try {
            properties.load(new FileInputStream(
                    Thread.currentThread().getContextClassLoader().getResource("").getPath() + "Config.properties"));
            getTimetable(lecturer);
        } catch (SQLException e) {
            System.err.println("Timetable for lecturer couldn't load correctly.");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateCoursePassTimetable() throws Exception {
        this.getTimetable(this.getCoursepass());
    }

    public void distributeUnplanedHours() {

        this.coursepass.updateCoursePassLecturerSubjects();
        ArrayList<CoursepassLecturerSubject> clsToAddArrayList = new ArrayList<CoursepassLecturerSubject>();
        for (CoursepassLecturerSubject cls : this.coursepass.getArrayCoursePassLecturerSubject()) {
            if (cls.getUnplanedHours() > 0) {
                clsToAddArrayList.add(cls);
            }
        }

        if (clsToAddArrayList.size() > 0) {
            // Loop through the Timetable and Check all Timeslots for Freetime
            for (TimetableDay timetableDay : this.getArrayTimetableDays()) {
                for (TimetableHour timetableHour : timetableDay.getArrayTimetableHours()) {
                    if (timetableHour != null
                            && timetableHour.getCoursepassLecturerSubject().getId() == 0L) {
                        // Freetime! Loop through clsToAddArrayList and check if one of the cls fits
                        // here
                        for (CoursepassLecturerSubject cls : clsToAddArrayList) {
                            if (CoursepassLecturerSubject.isFreeTarget(cls, timetableDay.getDate(),
                                    timetableHour.getTimeslot(), this.getSqlConnectionManager())) {
                                // LEcturer and Room are free, we can place it here
                                TimetableEntry targetTimetableEntry = new TimetableEntry(cls, timetableDay.getDate(),
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
                                    clsToAddArrayList.remove(cls);
                                }
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
                            ws.value(xlsRowCounter, 1, coursepass.getCourseOfStudyCaption());
                            ws.range(xlsRowCounter, 1, xlsRowCounter, 6).merge();
                            ws.range(xlsRowCounter, 1, xlsRowCounter, 6).style().borderStyle("thin").set();
                            xlsRowCounter++;
                            tmpMaxSlots = tmpDay.getMaxUsedTimeslotForThisWeek(coursepass);

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
                            if (tmpHour.getCoursepassLecturerSubject().getLecturerID() != 0) {
                                ws.value(rowOffset, tmpCol,
                                        tmpHour.getCoursepassLecturerSubject().getSubject().getCaption());
                                ws.value(rowOffset + 1, tmpCol,
                                        tmpHour.getCoursepassLecturerSubject().getLecturerFullname());
                                ws.value(rowOffset + 2, tmpCol,
                                        tmpHour.getCoursepassLecturerSubject().getRoom().getCaption());
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
            targetTimetableEntry.update(cls, targetTimetableEntry.getDate(), targetTimetableEntry.getTimeslot());
        } else {
            throw new Exception("Error Placing Hour");
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

        if (CoursepassLecturerSubject.isFreeTarget(sourceCLS, target.getDate(), target.getTimeslot(),
                sqlConnectionManager)
                && CoursepassLecturerSubject.isFreeTarget(targetCLS, source.getDate(), source.getTimeslot(),
                        sqlConnectionManager)) {
            source.update(targetCLS, source.getDate(), source.getTimeslot());
            target.update(sourceCLS, target.getDate(), target.getTimeslot());
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

        // if we haven't added CoursePassLecturerSubjects for this CoursePass yet,
        // we should return an empty array to display
        if (this.arrayTimetableDays.size() == 0) {
            TimetableDay tmpTimetableDay = new TimetableDay(LocalDate.now(), getSqlConnectionManager());
            ArrayList<TimetableHour> tmpArrayList = new ArrayList<>();
            Iterator<Integer> timeslotIterator = IntStream.range(0, maxTimeslots).boxed().iterator();
            while (timeslotIterator.hasNext()) {
                tmpArrayList.add(new TimetableHour(timeslotIterator.next(),
                        new CoursepassLecturerSubject(0L, getSqlConnectionManager()), getSqlConnectionManager()));
            }
            this.arrayTimetableDays.add(tmpTimetableDay);
        }

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
                // if this timeslot is null we add a freetime
                if (tmpTimetableday.getArrayTimetableHours().get(tmpTimeslot) == null) {
                    tmpTimetableday.getArrayTimetableHours().set(tmpTimeslot,
                            new TimetableHour(tmpTimeslot, new CoursepassLecturerSubject(0L, getSqlConnectionManager()),
                                    getSqlConnectionManager()));
                }
            }
        }

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
        this.arrayTimetableDays = new ArrayList<>();

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

            // add this timeslot/TimetableHour to our tmpDayObject
            tmpDayObject.getArrayTimetableHours().set((int) tmpTimeslot, new TimetableHour((int) tmpTimeslot,
                    new CoursepassLecturerSubject(resultSet.getLong("REFCOURSEPASSLECTURERSUBJECT"),
                            getSqlConnectionManager(), this.coursepass),
                    getSqlConnectionManager()));

        }
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
