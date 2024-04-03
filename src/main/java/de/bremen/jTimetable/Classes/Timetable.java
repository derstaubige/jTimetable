package de.bremen.jTimetable.Classes;

import javafx.stage.FileChooser;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.stream.IntStream;

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
    private Integer maxTimeslots = 5;

    private SQLConnectionManager sqlConnectionManager;

    /**
     * Constructor.
     *
     * @param coursePass the given coursePass is set for the new instance and the
     *                   corresponding timetable is
     *                   loaded into the instance
     */
    public Timetable(CoursePass coursePass, SQLConnectionManager sqlConnectionManager) {
        this.coursepass = coursePass;
        setSqlConnectionManager(sqlConnectionManager);
        try {
            getTimetable(coursePass);
        } catch (SQLException e) {
            System.err.println("Timetable for coursePass couldn't load correctly.");
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
    public Timetable(Lecturer lecturer, SQLConnectionManager sqlConnectionManager) {
        this.lecturer = lecturer;
        setSqlConnectionManager(sqlConnectionManager);
        try {
            getTimetable(lecturer);
        } catch (SQLException e) {
            System.err.println("Timetable for lecturer couldn't load correctly.");
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
                for (TimetableHour timetableHour : timetableDay.getArrayTimetableDay()) {
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
                                this.addSingleHour(cls, targetTimetableEntry);

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
     * TODO file export
     */
    public void exportTimetableToFile() {
        // https://docs.oracle.com/javafx/2/ui_controls/file-chooser.htm
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        // fileChooser.showOpenDialog();
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
    public void addSingleHour(CoursepassLecturerSubject cls, TimetableEntry targetTimetableEntry) {

        // save the change in the timetable table
        targetTimetableEntry.update(cls, targetTimetableEntry.getDate(), targetTimetableEntry.getTimeslot());

        try {
            updateCoursePassTimetable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param source
     * @param target
     *               Swap two CLS
     */
    public void swapHours(TimetableEntry source, TimetableEntry target) throws Exception {
        CoursepassLecturerSubject sourceCLS = source.getCoursepassLecturerSubject();
        CoursepassLecturerSubject targetCLS = target.getCoursepassLecturerSubject();

        if(CoursepassLecturerSubject.isFreeTarget(targetCLS, null, 0, sqlConnectionManager)){
            source.update(targetCLS, source.getDate(), source.getTimeslot());
            target.update(sourceCLS, target.getDate(), target.getTimeslot());
            try {
                updateCoursePassTimetable();
            } catch (Exception e) {
                e.printStackTrace();
            }            
        }else{
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
            for (TimetableHour timetableHour : arrayTimetableDay.getArrayTimetableDay()) {
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
                while (tmpTimetableday.getArrayTimetableDay().size() <= tmpTimeslot) {
                    tmpTimetableday.getArrayTimetableDay().add(tmpTimetableday.getArrayTimetableDay().size(),
                            new TimetableHour(tmpTimeslot, new CoursepassLecturerSubject(0L, getSqlConnectionManager()),
                                    getSqlConnectionManager()));
                }
                // if this timeslot is null we add a freetime
                if (tmpTimetableday.getArrayTimetableDay().get(tmpTimeslot) == null) {
                    tmpTimetableday.getArrayTimetableDay().set(tmpTimeslot,
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
                tmpTimetableDay.setArrayTimetableDay(tmpArrayList);
            }
        }

        // loop through all days and add freetimes in slots that dont have subjects jet
        for (TimetableDay tmpTimetableday : this.arrayTimetableDays) {
            // loop through all possible timeslots and check if there is already a subject
            Iterator<Integer> timeslotIterator = IntStream.range(0, maxTimeslots).boxed().iterator();
            while (timeslotIterator.hasNext()) {
                tmpTimetableday.addToSlot(timeslotIterator.next(),
                        new CoursepassLecturerSubject(0L, getSqlConnectionManager(), this.coursepass));
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
                tmpDayObject.getArrayTimetableDay().get((int) tmpTimeslot);
            } catch (Exception e) {
                tmpDayObject.setTimeslots((int) tmpTimeslot);
            }

            // add this timeslot/TimetableHour to our tmpDayObject
            tmpDayObject.getArrayTimetableDay().set((int) tmpTimeslot, new TimetableHour((int) tmpTimeslot,
                    new CoursepassLecturerSubject(resultSet.getLong("REFCOURSEPASSLECTURERSUBJECT"),
                            getSqlConnectionManager(), this.coursepass),
                    getSqlConnectionManager()));

        }
    }

}
