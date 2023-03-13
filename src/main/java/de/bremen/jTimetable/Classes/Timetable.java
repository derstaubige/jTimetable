package de.bremen.jTimetable.Classes;

import javafx.stage.FileChooser;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Class represents all timetable entries for a coursePass.
 */
public class Timetable {
    /**
     * Array that contains single TimetableDay objects that each represent one day in this timetable and consists of the
     * date and number of timeslots.
     */
    private ArrayList<TimetableDay> arrayTimetableDays;
    /**
     * CourPass for which this timetable is for. This timetable fulfills the requirements the subjects and resources
     * in this coursePass have.
     */
    private Coursepass coursepass;
    /**
     * Lecturer for which this timetable is for.
     */
    private Lecturer lecturer;

    /**
     * Constructor.
     *
     * @param coursePass the given coursePass is set for the new instance and the corresponding timetable is
     *                   loaded into the instance
     */
    public Timetable(Coursepass coursePass) {
        this.coursepass = coursePass;
        try {
            getTimetable(coursePass);
        } catch (SQLException e) {
            System.err.println("Timetable for lecturer couldn't load correctly.");
            e.printStackTrace();
        }
    }

    /**
     * Constructor.
     *
     * @param lecturer the given lecturer is set for the new instance and the corresponding timetable is
     *                 loaded into the instance
     */
    public Timetable(Lecturer lecturer) {
        this.lecturer = lecturer;
        try {
            getTimetable(lecturer);
        } catch (SQLException e) {
            System.err.println("Timetable for lecturer couldn't load correctly.");
            e.printStackTrace();
        }
    }

    /**
     * Runs database query to get resultSet with all timetable entries for given lecturer. The resultSet will
     * be loaded into this object with method loadTimetableFromResultSet().
     *
     * @param lecturer for whom the timetable is loaded
     */
    private void getTimetable(Lecturer lecturer) throws SQLException {
        //Create new SQLValues that are used for the following select statement
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();
        SQLValues.add(new SQLValueLong(lecturer.getId()));
        SQLValues.add(new SQLValueDate(LocalDate.now()));
        //Create new Connection to database
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();

        ResultSet rs = sqlConnectionManager.select(
                "Select * From T_TIMETABLES where REFLECTURER =? and TIMETABLEDAY >= ? " +
                        "ORDER BY TIMETABLEDAY, TIMESLOT ASC;",
                SQLValues);

        loadTimetableFromResultSet(rs);

        //if we haven't added CoursePassLecturerSubjects for this CoursePass yet,
        // we should return an empty array to display
        if (this.arrayTimetableDays.size() == 0) {
            TimetableDay tmpTimetableDay = new TimetableDay(LocalDate.now());
            ArrayList<TimetableHour> tmpArrayList = new ArrayList<>();
            tmpArrayList.add(new TimetableHour(0, new CoursepassLecturerSubject(0L)));
            tmpArrayList.add(new TimetableHour(1, new CoursepassLecturerSubject(0L)));
            tmpArrayList.add(new TimetableHour(2, new CoursepassLecturerSubject(0L)));
            tmpTimetableDay.setArrayTimetableDay(tmpArrayList);
            this.arrayTimetableDays.add(tmpTimetableDay);
        }
    }

    /**
     * Runs database query to get resultSet with all timetable entries for given coursePass. The resultSet will
     * be loaded into this object with method loadTimetableFromResultSet().
     *
     * @param coursePass for which the timetable is loaded
     */
    private void getTimetable(Coursepass coursePass) throws SQLException {
        //Create new SQLValues that are used for the following select statement
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();
        SQLValues.add(new SQLValueLong(coursePass.getId()));
        //Create new Connection to database
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();


        ResultSet rs = sqlConnectionManager.select("Select * From T_TIMETABLES where REFCOURSEPASS=? " +
                "ORDER BY TIMETABLEDAY, TIMESLOT ASC;", SQLValues);
        ArrayList<TimetableDay> result = new ArrayList<>();

        loadTimetableFromResultSet(rs);

        //if we haven't added CoursePassLecturerSubjects for this CoursePass yet,
        // we should return an empty array to display
        if (this.arrayTimetableDays.size() == 0) {
            Resourcemanager resourcemanager = new Resourcemanager();
            result = resourcemanager.getWorkingDaysBetweenTwoDates(coursePass.getStart(), coursePass.getEnd());
            for (TimetableDay tmpTimetableDay : result) {

                ArrayList<TimetableHour> tmpArrayList = new ArrayList<>();
                tmpArrayList.add(new TimetableHour(0, new CoursepassLecturerSubject(0L)));
                tmpArrayList.add(new TimetableHour(1, new CoursepassLecturerSubject(0L)));
                tmpArrayList.add(new TimetableHour(2, new CoursepassLecturerSubject(0L)));
                tmpTimetableDay.setArrayTimetableDay(tmpArrayList);
            }

        }
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
            //If current timeslot is bigger than the timeslotCount current is the new maxTimeslot
            if (tmpTimeslot > timeslotCount) {
                timeslotCount = (int) tmpTimeslot;
            }
            LocalDate tmpDate = resultSet.getDate("TIMETABLEDAY").toLocalDate();

            //Check if timetableDay object exists
            for (TimetableDay day : arrayTimetableDays) {
                //If exists select correct timetableDay, to add new Hours to it
                if (day.getDate().isEqual(tmpDate)) {
                    tmpDayObject = day;
                    //TODO another timeslot is added
                    // necessary?
                    timeslotCount++;
                    break;
                }
            }

            //If day doesn't exist, create a new object
            if (tmpDayObject == null) {
                tmpDayObject = new TimetableDay(tmpDate, timeslotCount);
                //reset timeslotCount for new day
                timeslotCount = 0;
                this.arrayTimetableDays.add(tmpDayObject);
            }

            //ToDo: Check if timeslot is already filled?

            //Check if we have to add to the max timeslots per day
            //ToDo: i guess we will crash here if we don't fill up our array of empty TimetableHours,
            // aka index out of bounds
            if (tmpDayObject.getTimeslots() <= tmpTimeslot) {
                tmpDayObject.setTimeslots((int) tmpTimeslot);
            }
            //System.out.println(rs.getLong("REFCOURSEPASSLECTURERSUBJECT"));
            //add this timeslot/TimetableHour to our tmpDayObject
            tmpDayObject.getArrayTimetableDay().set((int) tmpTimeslot, new TimetableHour((int) tmpTimeslot,
                    new CoursepassLecturerSubject(resultSet.getLong("REFCOURSEPASSLECTURERSUBJECT"))));

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

    /**
     * TODO file export
     */
    public void exportTimetableToFile() {
        //https://docs.oracle.com/javafx/2/ui_controls/file-chooser.htm
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        //fileChooser.showOpenDialog();
    }

    /**
     * Method adds one lesson in a timeslot to the timetable and saves the entry in the corresponding database table.
     *
     * @param timetableHour lesson that is added, consisting of timeslot, lecturer name, subject caption and room
     *                      caption
     * @param day           date at which the lesson is added
     * @param timeslot      timeslot in which the lesson is added
     */
    public void addSingleHour(TimetableHour timetableHour, LocalDate day, int timeslot) {
        for (TimetableDay timetableDay : getArrayTimetableDays()) {
            if (timetableDay.getDate() == day) {
                //TODO use timetableHour.getTimeslot instead? --> one variable less
                timetableDay.getArrayTimetableDay().set(timeslot, timetableHour);
                break;
            }
        }
        //save the change in the timetable table
        try {
            SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
            ArrayList<SQLConnectionManagerValues> SQLValues =
                    new ArrayList<>();

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
                                             LocalDate endDate, int startTimeslot, int endTimeslot) {
        try {
            SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();

            SQLValues.add(new SQLValueLong(resourceID));
            SQLValues.add(new SQLValueString(resourceName.toString()));
            SQLValues.add(new SQLValueDate(startDate));
            SQLValues.add(new SQLValueDate(endDate));
            SQLValues.add(new SQLValueInt(startTimeslot));
            SQLValues.add(new SQLValueInt(endTimeslot));
            sqlConnectionManager.execute("DELETE FROM T_RESOURCESBLOCKED where REFRESOURCEID = ? and " +
                    "RESOURCENAME = ? and STARTDATE = ? and ENDDATE = ? and STARTTIMESLOT = ? and ENDTIMESLOT = ?", SQLValues);
        } catch (SQLException e) {
            System.err.println("SQLException was thrown in deleteResourceBlocked, therefor deleting " +
                    "timetable entries has side effects because resources are still blocked.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Method deletes this timetable by deleting the blocked resources in the database through the method
     * deleteResourceBlocked() and then deleting the timetable entries through overload of this method.
     */
    public void deleteTimetable() {
        // Loop through all Days and Hours and Delete ResourceBlocked and Timetable
        for (TimetableDay arrayTimetableDay : arrayTimetableDays) {
            for (TimetableHour timetableHour : arrayTimetableDay.getArrayTimetableDay()) {
                deleteResourceBlocked(timetableHour.coursepassLecturerSubject.getLecturerID(), ResourceNames.LECTURER, arrayTimetableDay.getDate(),
                        arrayTimetableDay.getDate(), timetableHour.getTimeslot(), timetableHour.getTimeslot());
                deleteResourceBlocked(timetableHour.coursepassLecturerSubject.getLecturerID(), ResourceNames.ROOM, arrayTimetableDay.getDate(),
                        arrayTimetableDay.getDate(), timetableHour.getTimeslot(), timetableHour.getTimeslot());
            }
        }
        deleteTimetable(coursepass.getId());
    }

    /**
     * Method deletes all entries that match the given coursePassID in the database table T_timetables.
     *
     * @param coursePassID ID of the coursePass for which the entries will be deleted
     */
    public void deleteTimetable(long coursePassID) {
        try {
            SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
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
     * Method deletes timetable entries in database specified by coursePass, date and timeslot.
     *
     * @param coursePassID given coursePassID
     * @param timetableDay given date
     * @param timeslot     given timeslot
     */
    public static void deleteTimetable(long coursePassID, LocalDate timetableDay, int timeslot) {
        try {
            SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
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

}
