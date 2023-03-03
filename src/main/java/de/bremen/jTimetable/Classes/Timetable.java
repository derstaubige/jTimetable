package de.bremen.jTimetable.Classes;

import javafx.stage.FileChooser;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.*;

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
     *
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
        this.update();
    }

    public Timetable(Lecturer lecturer) {
        this.lecturer = lecturer;
        try {
            this.arrayTimetableDays = lecturer.getTimetable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update() {
        try {
            this.arrayTimetableDays = this.coursepass.getTimetable();
        } catch (Exception e) {
            //ToDO: Errorhandling
            e.printStackTrace();
        }
    }

    /**
     * Getter for this.arrayTimetableDays
     * @return
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
