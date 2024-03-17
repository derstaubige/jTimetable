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
                            && timetableHour.getCoursepassLecturerSubject().getCoursepass().getId() == 0L) {
                        // Freetime! Loop through clsToAddArrayList and check if one of the cls fits
                        // here
                        for (CoursepassLecturerSubject cls : clsToAddArrayList) {
                            if (CoursepassLecturerSubject.isFreeTarget(cls, timetableDay.getDate(),
                                    timetableHour.getTimeslot(), this.getSqlConnectionManager())) {
                                // LEcturer and Room are free, we can place it here
                                // delete the entry in the timetable table 
                                Timetable.deleteTimetable(timetableHour.getCoursepassLecturerSubject().getId(),
                                        timetableDay.getDate(), timetableHour.getTimeslot(), getSqlConnectionManager());

                                // save the new timetablehour
                                TimetableHour tmptimetableHour = new TimetableHour(timetableHour.getTimeslot(),
                                        cls, getSqlConnectionManager());
                                this.addSingleHour(tmptimetableHour, timetableDay.getDate(),
                                        timetableHour.getTimeslot());

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

            // redraw timetable
        }
    }

    /**
     * Method deletes the blocking for the resource that matches the parameters.
     *
     * @param resourceID    ID of the resource
     * @param resourceName  type of the resource (lecturer or room)
     * @param startDate     start of the blocking
     * @param endDate       end of the blocking
     * @param startTimeslot first timeslot of the blocking
     * @param endTimeslot   last timeslot of the blocking
     */
    public static void deleteResourceBlocked(long resourceID, ResourceNames resourceName, LocalDate startDate,
            LocalDate endDate, int startTimeslot, int endTimeslot, SQLConnectionManager sqlConnectionManager) {
        try {

            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();

            SQLValues.add(new SQLValueLong(resourceID));
            SQLValues.add(new SQLValueString(resourceName.toString()));
            SQLValues.add(new SQLValueDate(startDate));
            SQLValues.add(new SQLValueDate(endDate));
            SQLValues.add(new SQLValueInt(startTimeslot));
            SQLValues.add(new SQLValueInt(endTimeslot));
            sqlConnectionManager.execute("DELETE FROM T_RESOURCESBLOCKED where REFRESOURCEID = ? and " +
                    "RESOURCENAME = ? and STARTDATE = ? and ENDDATE = ? and STARTTIMESLOT = ? and ENDTIMESLOT = ?",
                    SQLValues);
        } catch (SQLException e) {
            System.err.println("SQLException was thrown in deleteResourceBlocked, therefor deleting " +
                    "timetable entries has side effects because resources are still blocked.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Method deletes timetable entries in database specified by coursePass, date
     * and timeslot.
     *
     * @param coursePassID given coursePassID
     * @param timetableDay given date
     * @param timeslot     given timeslot
     */
    public static void deleteTimetable(long coursePassID, LocalDate timetableDay, int timeslot,
            SQLConnectionManager sqlConnectionManager) {
        try {

            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();

            SQLValues.add(new SQLValueLong(coursePassID));
            SQLValues.add(new SQLValueDate(timetableDay));
            SQLValues.add(new SQLValueInt(timeslot));
            sqlConnectionManager.execute("DELETE FROM T_Timetables where refCoursepass = ? AND " +
                    "timetableday = ? AND timeslot = ?", SQLValues);
        } catch (SQLException e) {
            System.err.println("SQLException was thrown in deleteTimetable(coursePassID, timetableDay, timeslot)," +
                    " therefor deleting timetable entries didn't work.");
            throw new RuntimeException(e);
        }
    }

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
    public void addSingleHour(TimetableHour timetableHour, LocalDate day, int timeslot) {
        for (TimetableDay timetableDay : getArrayTimetableDays()) {
            if (timetableDay.getDate() == day) {
                // TODO use timetableHour.getTimeslot instead? --> one variable less
                timetableDay.getArrayTimetableDay().set(timeslot, timetableHour);
                break;
            }
        }
        // save the change in the timetable table
        try {

            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();

            SQLValues.add(new SQLValueDate(day));
            SQLValues.add(new SQLValueLong(timetableHour.getCoursepassLecturerSubject().getCoursepass().getId()));
            SQLValues.add(new SQLValueLong(timetableHour.getCoursepassLecturerSubject().getId()));
            SQLValues.add(new SQLValueLong(timetableHour.getCoursepassLecturerSubject().getRoom().getId()));
            SQLValues.add(new SQLValueLong(timetableHour.getCoursepassLecturerSubject().getLecturerID()));
            SQLValues.add(new SQLValueLong(timetableHour.getCoursepassLecturerSubject().getSubject().getId()));
            SQLValues.add(new SQLValueInt(timeslot));

            sqlConnectionManager.execute(
                    "Insert Into T_TIMETABLES (TIMETABLEDAY, REFCOURSEPASS, REFCOURSEPASSLECTURERSUBJECT, " +
                            "REFROOMID, REFLECTURER, REFSUBJECT, TIMESLOT) values (?, ?, ?, ?, ?, ?, ?)",
                    SQLValues);
        } catch (SQLException e) {
            System.err.println("SQLException was thrown in addSingleHour, therefor adding the lesson to this timeslot: "
                    + timetableHour.getTimeslot() + " was not successful.");
            throw new RuntimeException(e);
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
        for (TimetableDay arrayTimetableDay : arrayTimetableDays) {
            for (TimetableHour timetableHour : arrayTimetableDay.getArrayTimetableDay()) {
                if (timetableHour == null) {
                    continue;
                }
                deleteResourceBlocked(timetableHour.coursepassLecturerSubject.getLecturerID(),
                        ResourceNames.LECTURER, arrayTimetableDay.getDate(),
                        arrayTimetableDay.getDate(), timetableHour.getTimeslot(), timetableHour.getTimeslot(),
                        getSqlConnectionManager());
                deleteResourceBlocked(timetableHour.coursepassLecturerSubject.getLecturerID(),
                        ResourceNames.ROOM, arrayTimetableDay.getDate(),
                        arrayTimetableDay.getDate(), timetableHour.getTimeslot(), timetableHour.getTimeslot(),
                        getSqlConnectionManager());
            }
        }
        deleteTimetable(coursepass.getId());
    }

    /**
     * Method deletes all entries that match the given coursePassID in the database
     * table T_timetables.
     *
     * @param coursePassID ID of the coursePass for which the entries will be
     *                     deleted
     */
    public void deleteTimetable(long coursePassID) {
        try {

            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();

            SQLValues.add(new SQLValueLong(coursePassID));
            sqlConnectionManager.execute("DELETE FROM T_Timetables where refCoursepass = ?", SQLValues);
        } catch (SQLException e) {
            System.err.println("SQLException was thrown in deleteTimetable(coursePassID), therefor deleting " +
                    "timetable entries didn't work.");
            throw new RuntimeException(e);
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

        ResultSet rs = sqlConnectionManager.select("Select * From T_TIMETABLES where REFCOURSEPASS=? " +
                "ORDER BY TIMETABLEDAY, TIMESLOT ASC;", SQLValues);
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
                            new CoursepassLecturerSubject(0L, getSqlConnectionManager()), getSqlConnectionManager()));
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
                        new CoursepassLecturerSubject(0L, getSqlConnectionManager()));
            }
        }

        // grap all lecturers that are currently in this timetable
        this.lecturers = getAllLecturers();
        // sqlConnectionManager.close();
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
        int timeslotCount = 0;

        while (resultSet.next()) {

            TimetableDay tmpDayObject = null;
            long tmpTimeslot = resultSet.getLong("Timeslot");
            // If current timeslot is bigger than the timeslotCount current is the new
            // maxTimeslot
            if (tmpTimeslot > timeslotCount) {
                timeslotCount = (int) tmpTimeslot;
            }
            LocalDate tmpDate = resultSet.getDate("TIMETABLEDAY").toLocalDate();

            // Check if timetableDay object exists
            for (TimetableDay day : arrayTimetableDays) {
                // If exists select correct timetableDay, to add new Hours to it
                if (day.getDate().isEqual(tmpDate)) {
                    tmpDayObject = day;
                    // TODO another timeslot is added
                    // necessary?
                    timeslotCount++;
                    break;
                }
            }

            // If day doesn't exist, create a new object
            if (tmpDayObject == null) {
                tmpDayObject = new TimetableDay(tmpDate, timeslotCount, getSqlConnectionManager());
                // reset timeslotCount for new day
                timeslotCount = 0;
                this.arrayTimetableDays.add(tmpDayObject);
            }

            // ToDo: Check if timeslot is already filled?

            // Check if we have to add to the max timeslots per day
            // ToDo: i guess we will crash here if we don't fill up our array of empty
            // TimetableHours,
            // aka index out of bounds
            if (tmpDayObject.getTimeslots() <= tmpTimeslot) {
                tmpDayObject.setTimeslots((int) tmpTimeslot);
            }
            // System.out.println(rs.getLong("REFCOURSEPASSLECTURERSUBJECT"));
            // add this timeslot/TimetableHour to our tmpDayObject
            tmpDayObject.getArrayTimetableDay().set((int) tmpTimeslot, new TimetableHour((int) tmpTimeslot,
                    new CoursepassLecturerSubject(resultSet.getLong("REFCOURSEPASSLECTURERSUBJECT"),
                            getSqlConnectionManager()),
                    getSqlConnectionManager()));

        }
    }

    public Lecturer getLecturer() {
        return lecturer;
    }

}
