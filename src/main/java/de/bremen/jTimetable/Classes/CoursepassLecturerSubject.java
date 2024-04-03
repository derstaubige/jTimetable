package de.bremen.jTimetable.Classes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.*;

public class CoursepassLecturerSubject implements Comparable<CoursepassLecturerSubject> {
    Long id;
    CoursePass coursepass;
    Lecturer lecturer;
    Subject subject;
    Room room;
    public Long shouldHours;
    public Long isHours; // hours that have actually been given
    public Long planedHours; // hours that are planed but not been given
    Boolean active;
    private SQLConnectionManager sqlConnectionManager;

    public static boolean isFreeTarget(CoursepassLecturerSubject cls, LocalDate targetDay, int targetTimeslot,
            SQLConnectionManager sqlConnectionManager) {
        try {
            if (!Lecturer.checkLecturerAvailability(cls.getLecturerID(), targetDay, targetTimeslot,
                    sqlConnectionManager)) {
                return false;
            }
            if (!Room.checkRoomAvailability(cls.getRoom().getId(), targetDay, targetTimeslot, sqlConnectionManager)) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    // TODO check if course has time (if a timetable for just the lecturer is shown)
    public static boolean cangetExchanged(TimetableEntry sourceTimetableEntry, TimetableEntry targetTimetableEntry,
            SQLConnectionManager sqlConnectionManager) {
        CoursepassLecturerSubject source = sourceTimetableEntry.getCoursepassLecturerSubject();
        LocalDate sourceDay = sourceTimetableEntry.getDate();
        int sourceTimeslot = sourceTimetableEntry.getTimeslot();
        CoursepassLecturerSubject target = targetTimetableEntry.getCoursepassLecturerSubject();
        LocalDate targetDay = targetTimetableEntry.getDate();
        int targetTimeslot = targetTimetableEntry.getTimeslot();

        // check if lecturer and room from source are free at target date and timeslot
        long sourceLecturerId = source.lecturer.getId();
        long targetLecturerId = target.lecturer.getId();

        try {
            if (!Room.checkRoomAvailability(source.getRoom().getId(), targetDay, targetTimeslot,
                    sqlConnectionManager)) {
                return false;
            }

            if (!Room.checkRoomAvailability(target.getRoom().getId(), sourceDay, sourceTimeslot,
                    sqlConnectionManager)) {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // The same lecturer can switch his/her own lessons
        if (sourceLecturerId == targetLecturerId) {
            return true;
        }
        try {
            if (!Lecturer.checkLecturerAvailability(sourceLecturerId, targetDay, targetTimeslot,
                    sqlConnectionManager)) {
                return false;
            }
            if (!Lecturer.checkLecturerAvailability(targetLecturerId, sourceDay, sourceTimeslot,
                    sqlConnectionManager)) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    // Switches two Hours
    public static void changeCoursepassLecturerSubject(CoursepassLecturerSubject source, LocalDate sourceDay,
            int sourceTimeslot, CoursepassLecturerSubject target, LocalDate targetDay, int targetTimeslot,
            SQLConnectionManager sqlConnectionManager) {

        try {
            // change Resourcesblocked, Lecturerer and Room ID
            TimetableEntry sourceTimetableEntry = new TimetableEntry(source, sourceDay, (Integer) sourceTimeslot,
                    sqlConnectionManager);
            sourceTimetableEntry.update(target, sourceDay, sourceTimeslot);

            TimetableEntry targetTimetableEntry = new TimetableEntry(target, targetDay, (Integer) targetTimeslot,
                    sqlConnectionManager);
            targetTimetableEntry.update(source, targetDay, targetTimeslot);
        } catch (Exception e) {
            System.out.println("An SQLError occurred while Updating ResourceBlocked and Timetables");
            e.printStackTrace();
        }
    }

    public CoursepassLecturerSubject(Long id, SQLConnectionManager sqlConnectionManager) throws SQLException {
        this(id, sqlConnectionManager, new CoursePass(0L, sqlConnectionManager));
    }

    public CoursepassLecturerSubject(Long id, SQLConnectionManager sqlConnectionManager, CoursePass coursePass)
            throws SQLException {
        this.id = id;
        setSqlConnectionManager(sqlConnectionManager);

        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        if (this.id == 0) {
            // load dummy object
            this.coursepass = coursePass;
            this.lecturer = new Lecturer(0L, getSqlConnectionManager());
            this.subject = new Subject(0L, getSqlConnectionManager());
            this.room = new Room(0L, getSqlConnectionManager());
            this.shouldHours = 0L;
            this.isHours = 0L;
            this.planedHours = 0L;
            this.active = Boolean.TRUE;
        } else {
            // load object from db
            SQLValues.add(new SQLValueLong(id));

            ResultSet rs = sqlConnectionManager.select("Select * from T_CoursepassesLecturerSubject where id = ?;",
                    SQLValues);
            rs.first();
            this.id = rs.getLong("id");
            this.coursepass = coursePass;
            this.lecturer = new Lecturer(rs.getLong("refLecturerID"), getSqlConnectionManager());
            this.subject = new Subject(rs.getLong("refSubjectID"), getSqlConnectionManager());
            this.room = new Room(rs.getLong("refRoomID"), getSqlConnectionManager());
            this.shouldHours = rs.getLong("shouldhours");
            this.active = rs.getBoolean("active");

            this.updateIsHours();
            this.updatePlanedHours();
        }

        // sqlConnectionManager.close();
    }

    private void updatePlanedHours() {
        try {
            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();
            LocalDate today = LocalDate.now();
            // query the planed hours
            SQLValues.clear();
            SQLValues.add(new SQLValueLong(this.coursepass.getId()));
            SQLValues.add(new SQLValueLong(this.getId()));
            SQLValues.add(new SQLValueDate(today));
            ResultSet rs = sqlConnectionManager.select(
                    "Select count(id) from T_Timetables where refcoursepass = ? and REFCOURSEPASSLECTURERSUBJECT = ? and timetableday > ?;",
                    SQLValues);
            rs.first();
            this.planedHours = rs.getLong(1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateShouldHours() {
        try {
            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();
            // query the is hours
            SQLValues.clear();
            LocalDate today = LocalDate.now();
            SQLValues.add(new SQLValueLong(this.getId()));
            SQLValues.add(new SQLValueDate(today));
            ResultSet rs = sqlConnectionManager.select(
                    "Select count(id) from T_Timetables where REFCOURSEPASSLECTURERSUBJECT  = ? and timetableday < ?;",
                    SQLValues);
            rs.first();
            this.isHours = rs.getLong(1);

            if (this.shouldHours > this.planedHours + this.isHours) {
                // TODO: Here we can think about a possibility to inform the user, that there
                // are now more planed/is hours and should hours git issue #5
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateIsHours() {
        try {
            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();
            // query the is hours
            SQLValues.clear();
            LocalDate today = LocalDate.now();
            SQLValues.add(new SQLValueLong(this.getId()));
            SQLValues.add(new SQLValueDate(today));
            ResultSet rs = sqlConnectionManager.select(
                    "Select count(id) from T_Timetables where REFCOURSEPASSLECTURERSUBJECT  = ? and timetableday < ?;",
                    SQLValues);
            rs.first();
            this.isHours = rs.getLong(1);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateallHours() {
        this.updateShouldHours();
        this.updateIsHours();
        this.updatePlanedHours();
    }

    // overrides an existing timetable entry with freetime and removes the resources
    // blocked entrys
    public void deleteCLS(LocalDate pDate, Integer pTimestamp) {
        try {
            TimetableEntry timetableEntry = new TimetableEntry(this, pDate, pTimestamp, sqlConnectionManager);
            timetableEntry.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() throws SQLException {

        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        // if we have a freetime object coursepass.id = 0, lecturer.id = 0, subject.id =
        // 0
        if (this.coursepass.getId() == 0 && this.lecturer.getId() == 0 && this.subject.id == 0) {
            // sqlConnectionManager.close();
            return;
        }

        SQLValues.add(new SQLValueLong(this.coursepass.getId()));
        SQLValues.add(new SQLValueLong(this.lecturer.getId()));
        SQLValues.add(new SQLValueLong(this.subject.getId()));
        SQLValues.add(new SQLValueLong(this.room.getId()));
        SQLValues.add(new SQLValueLong(this.shouldHours));
        SQLValues.add(new SQLValueBoolean(this.active));

        if (this.id == 0) {
            // its a new object, we have to insert it
            ResultSet rs = sqlConnectionManager.execute(
                    "Insert Into `T_CoursepassesLecturerSubject` (`refCoursePassID`, `refLecturerID`, `refSubjectID`,`REFROOMID`, `shouldhours`, `ACTIVE`) values (?, ?, ?, ?, ?, ?)",
                    SQLValues);
            rs.first();
            this.id = rs.getLong(1);
        } else {
            // we only have to update an existing entry
            SQLValues.add(new SQLValueLong(this.id));
            sqlConnectionManager.execute(
                    "update `T_CoursepassesLecturerSubject` set `refCoursePassID` = ?, `refLecturerID` = ?, `refSubjectID` = ?, `REFROOMID` = ?, `shouldhours` = ?, `ACTIVE` = ? where `id` = ?;",
                    SQLValues);
        }
        // sqlConnectionManager.close();
    }

    @Override
    public int compareTo(CoursepassLecturerSubject o) {
        if (this.shouldHours < o.shouldHours) {
            return -1;
        } else if (this.shouldHours == o.shouldHours) {
            return 0;
        } else {
            return 1;
        }
    }

    public long getLecturerID() {
        return this.lecturer.getId();
    }

    public String getLecturerFullname() {
        return this.lecturer.getLecturerFullName();
    }

    public String getSubjectCaption() {
        return this.subject.getCaption();
    }

    public Lecturer getLecturer() {
        return lecturer;
    }

    public void setLecturer(Lecturer lecturer) {
        this.lecturer = lecturer;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Long getShouldHours() {
        return shouldHours;
    }

    public void setShouldHours(Long shouldHours) {
        this.shouldHours = shouldHours;
        this.updateShouldHours();
    }

    public Long getId() {
        return id;
    }

    public CoursePass getCoursepass() {
        return coursepass;
    }

    public void setCoursepass(CoursePass coursepass) {
        this.coursepass = coursepass;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Long getIsHours() {
        return isHours;
    }

    public Long getPlanedHours() {
        return planedHours;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public SQLConnectionManager getSqlConnectionManager() {
        return sqlConnectionManager;
    }

    public void setSqlConnectionManager(SQLConnectionManager sqlConnectionManager) {
        this.sqlConnectionManager = sqlConnectionManager;
    }

    public Long getUnplanedHours() {
        return this.getShouldHours() - this.getIsHours() - this.getPlanedHours();
    }

    public String getRoomCaptionLocatioString() {
        return this.getRoom().getCaption() + ", " + this.getRoom().getLocationCaption();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof CoursepassLecturerSubject) && (this.id == ((CoursepassLecturerSubject) o).id);
    }
}
