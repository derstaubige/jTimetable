package de.bremen.jTimetable.Classes;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;

import org.h2.jdbc.JdbcSQLNonTransientException;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueDate;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueInt;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueLong;

public class TimetableEntry {
    private Integer id = 0;
    private ResourcesBlocked roomBlocked;
    private ResourcesBlocked lecturerBlocked;
    private LocalDate date;
    private Integer timeslot;
    private CoursepassLecturerSubject coursepassLecturerSubject;
    private CoursePass coursePass;
    private SQLConnectionManager sqlConnectionManager;

    /**
     * @param coursepassLecturerSubject
     * @param date
     * @param timeslot
     * @param sqlConnectionManager
     *                                  Timetableentry shall manage the t_timetable
     *                                  table. it will create and delete
     *                                  the entrys
     */
    public TimetableEntry(CoursepassLecturerSubject coursepassLecturerSubject, LocalDate date, Integer timeslot,
            SQLConnectionManager sqlConnectionManager) {
        this.coursepassLecturerSubject = coursepassLecturerSubject;
        this.date = date;
        this.timeslot = timeslot;
        this.sqlConnectionManager = sqlConnectionManager;
        this.coursePass = coursepassLecturerSubject.getCoursepass();
        loadFromDB();
    }

    /**
     * @param coursePass
     * @param date
     * @param timeslot
     * @param sqlConnectionManager
     *                             Load existing TimetableEntry by CoursePass, Date
     *                             and Timeslot
     */
    public TimetableEntry(CoursePass coursePass, LocalDate date, Integer timeslot,
            SQLConnectionManager sqlConnectionManager) {
        this.sqlConnectionManager = sqlConnectionManager;
        this.coursePass = coursePass;
        this.date = date;
        this.timeslot = timeslot;
        loadFromDB();

    }

    private void loadFromDB() {

        try {
            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();
            SQLValues.add(new SQLValueDate(date));
            SQLValues.add(new SQLValueLong(coursePass.getId()));
            SQLValues.add(new SQLValueInt(timeslot));
            ResultSet rs = sqlConnectionManager.select(
                    "Select * from T_timetables where timetableday = ? and refCoursepass = ? and timeslot = ?;",
                    SQLValues);
            rs.first();
            this.coursepassLecturerSubject = new CoursepassLecturerSubject(rs.getLong("REFCOURSEPASSLECTURERSUBJECT"),
                    sqlConnectionManager, this.coursePass);
            this.id = rs.getInt("id");
        } catch (JdbcSQLNonTransientException e) {
            // we didnt find a entry at this day/timestamp.... sooo its freetime or a new
            // entry
            try {
                if (this.coursepassLecturerSubject == null) {
                    this.coursepassLecturerSubject = new CoursepassLecturerSubject(0L, sqlConnectionManager,
                            this.coursePass);
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.roomBlocked = new ResourcesBlocked(coursepassLecturerSubject.getRoom().getId(),
                ResourceNames.ROOM, date, date, timeslot, timeslot, sqlConnectionManager);
        this.lecturerBlocked = new ResourcesBlocked(coursepassLecturerSubject.getLecturerID(),
                ResourceNames.LECTURER, date, date, timeslot, timeslot, sqlConnectionManager);
    }

    public void update(CoursepassLecturerSubject coursepassLecturerSubject, LocalDate date, Integer timeslot) {

        // delete old TimetableEntry and resources blocked
        delete();

        // create new ResourcesBlocked lecturer and room
        this.lecturerBlocked = new ResourcesBlocked(coursepassLecturerSubject.getLecturerID(),
                ResourceNames.LECTURER, date, date, timeslot, timeslot, sqlConnectionManager);
        this.lecturerBlocked.save();

        this.roomBlocked = new ResourcesBlocked(coursepassLecturerSubject.getRoom().getId(),
                ResourceNames.ROOM, date, date, timeslot, timeslot, sqlConnectionManager);
        this.roomBlocked.save();

        this.coursepassLecturerSubject = coursepassLecturerSubject;
        this.date = date;
        this.timeslot = timeslot;
        this.coursePass = coursepassLecturerSubject.getCoursepass();

        save();
    }

    /**
     * Sets selected Timetable Entry to Freetime
     */
    public void delete() {
        try {
            // delete ResourcesBlocked lecturer and room
            this.lecturerBlocked.delete();
            this.roomBlocked.delete();

            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();
            SQLValues.add(new SQLValueLong(0L));
            SQLValues.add(new SQLValueLong(0L));
            SQLValues.add(new SQLValueLong(0L));
            SQLValues.add(new SQLValueLong(0L));
            SQLValues.add(new SQLValueLong(this.coursePass.getId()));
            SQLValues.add(new SQLValueDate(this.date));
            SQLValues.add(new SQLValueInt(this.timeslot));
            sqlConnectionManager.execute(
                    "update `T_TIMETABLES` set REFCOURSEPASSLECTURERSUBJECT = ?, REFROOMID = ?, REFLECTURER = ?, REFSUBJECT = ? where refcoursepass = ? and timetableday = ? and timeslot = ?",
                    SQLValues);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        this.lecturerBlocked.save();
        this.roomBlocked.save();
        try {
            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();
            if (this.coursePass.getId() == 0) {
                throw new Exception("Coursepass cant be ID 0!");
            }

            // is this day and timeslot free?
            if (id == 0) {
                // insert new TimetableEntry
                SQLValues.add(new SQLValueDate(this.date));
                SQLValues.add(new SQLValueLong(this.coursePass.getId()));
                SQLValues.add(new SQLValueLong(this.coursepassLecturerSubject.getId()));
                SQLValues.add(new SQLValueLong(this.coursepassLecturerSubject.getRoom().getId()));
                SQLValues.add(new SQLValueLong(this.coursepassLecturerSubject.getLecturerID()));
                SQLValues.add(new SQLValueLong(this.coursepassLecturerSubject.getSubject().getId()));
                SQLValues.add(new SQLValueInt(this.timeslot));

                ResultSet rs = sqlConnectionManager.execute(
                        "Insert Into T_TIMETABLES (TIMETABLEDAY, REFCOURSEPASS, REFCOURSEPASSLECTURERSUBJECT, REFROOMID, REFLECTURER, REFSUBJECT, TIMESLOT) values (?, ?, ?, ?, ?, ?, ?)",
                        SQLValues);
                rs.first();
                this.id = rs.getInt(1);

            } else {
                // update existing entry

                SQLValues.add(new SQLValueLong(this.coursepassLecturerSubject.getId()));
                SQLValues.add(new SQLValueLong(this.coursepassLecturerSubject.getRoom().getId()));
                SQLValues.add(new SQLValueLong(this.coursepassLecturerSubject.getLecturerID()));
                SQLValues.add(new SQLValueLong(this.coursepassLecturerSubject.getSubject().getId()));
                SQLValues.add(new SQLValueLong(this.coursePass.getId()));
                SQLValues.add(new SQLValueDate(this.date));
                SQLValues.add(new SQLValueInt(this.timeslot));
                SQLValues.add(new SQLValueInt(this.id));
                sqlConnectionManager.execute(
                        "update `T_TIMETABLES` set REFCOURSEPASSLECTURERSUBJECT = ?, REFROOMID = ?, REFLECTURER = ?, REFSUBJECT = ? , refcoursepass = ? , timetableday = ? , timeslot = ? where id = ?",
                        SQLValues);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ResourcesBlocked getRoomBlocked() {
        return roomBlocked;
    }

    public ResourcesBlocked getLecturerBlocked() {
        return lecturerBlocked;
    }

    public LocalDate getDate() {
        return date;
    }

    public Integer getTimeslot() {
        return timeslot;
    }

    public CoursepassLecturerSubject getCoursepassLecturerSubject() {
        return coursepassLecturerSubject;
    }

    public SQLConnectionManager getSqlConnectionManager() {
        return sqlConnectionManager;
    }

}
