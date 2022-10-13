package de.bremen.jTimetable.Classes;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

public class CoursepassLecturerSubject implements Comparable<CoursepassLecturerSubject> {
    Long id;
    Coursepass coursepass;
    Lecturer lecturer;
    Subject subject;
    public Long shouldhours;
    public Long ishours; // hours that have actually been given
    public Long planedHours; //hours that are planed but not been given
    Boolean active;

    //TODO check if course has time (if a timetable for just the lecturer is shown)
    public static boolean cangetExchanged(CoursepassLecturerSubject source, LocalDate sourceDay, int sourceTimeslot, CoursepassLecturerSubject target, LocalDate targetDay, int targetTimeslot){
        //check if lecturer and room from source are free at target date and timeslot
        long sourceLecturerId = source.lecturer.getId();
        long targetLecturerId = target.lecturer.getId();
        //TODO rooms not considered
        //The same lecturer can switch his/her own lessons
        if (sourceLecturerId == targetLecturerId) {
            return true;
        }
        try{
            if(!source.lecturer.checkLecturerAvailability(sourceLecturerId,targetDay,targetTimeslot)){
                return false;
            }
            if(!target.lecturer.checkLecturerAvailability(targetLecturerId,sourceDay,sourceTimeslot)){
                return false;
            }
        }catch (SQLException e){
            //ToDo: better error handling --> check
            System.out.println("An SQLError occurred while checking if two lessons can be exchanged.");
            e.printStackTrace();
        }
        return true;
    }

    public CoursepassLecturerSubject(Long id) throws SQLException  {
        this.id = id;
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        if (this.id == 0){
            //load dummy object
            this.coursepass = new Coursepass(0L);
            this.lecturer = new Lecturer(0L);
            this.subject = new Subject(0L);
            this.shouldhours = 0L;
            this.ishours = 0L;
            this.planedHours = 0L;
            this.active = Boolean.TRUE;
        }else{
            //load object from db
            SQLValues.add(new SQLValueLong(id));

            ResultSet rs = sqlConnectionManager.select("Select * from T_CoursepassesLecturerSubject where id = ?;",SQLValues);
            rs.first();
            this.id = rs.getLong("id");
            this.coursepass = new Coursepass(rs.getLong("refCoursePassID"));
            this.lecturer = new Lecturer(rs.getLong("refLecturerID"));
            this.subject = new Subject(rs.getLong("refSubjectID"));
            this.shouldhours = rs.getLong("shouldhours");
            this.active = rs.getBoolean("active");

            //query the is hours
            SQLValues.clear();
            LocalDate today = LocalDate.now();
            SQLValues.add(new SQLValueLong(this.coursepass.id));
            SQLValues.add(new SQLValueLong(this.subject.id));
            SQLValues.add(new SQLValueDate(today));
            rs = sqlConnectionManager.select("Select count(id) from T_Timetables where refcoursepass = ? and refsubject = ? and timetableday < ?;",SQLValues);
            rs.first();
            this.ishours = rs.getLong(1);

            //query the planed hours
            SQLValues.clear();
            SQLValues.add(new SQLValueLong(this.coursepass.id));
            SQLValues.add(new SQLValueLong(this.subject.id));
            SQLValues.add(new SQLValueDate(today));
            rs = sqlConnectionManager.select("Select count(id) from T_Timetables where refcoursepass = ? and refsubject = ? and timetableday > ?;",SQLValues);
            rs.first();
            this.planedHours = rs.getLong(1);
        }


    }
    public void save() throws SQLException{
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        SQLValues.add(new SQLValueLong(this.coursepass.id));
        SQLValues.add(new SQLValueLong(this.lecturer.id));
        SQLValues.add(new SQLValueLong(this.subject.id));
        SQLValues.add(new SQLValueLong(this.shouldhours));
        SQLValues.add(new SQLValueBoolean(this.active));

        if (this.id == 0) {
            // its a new object, we have to insert it
            ResultSet rs = sqlConnectionManager.execute("Insert Into `T_CoursepassesLecturerSubject` (`refCoursePassID`, `refLecturerID`, `refSubjectID`, `shouldhours`, `ACTIVE`) values (?, ?, ?, ?, ?)",SQLValues);
            rs.first();
            this.id = rs.getLong(1);
        }else{
            // we only have to update an existing entry
            SQLValues.add(new SQLValueLong(this.id));
            ResultSet rs = sqlConnectionManager.execute("update `T_CoursepassesLecturerSubject` set `refCoursePassID` = ?, `refLecturerID` = ?, `refSubjectID` = ?, shouldhours` = ?, `ACTIVE` = ? where `id` = ?;",SQLValues);
        }
    }

    @Override
    public int compareTo(CoursepassLecturerSubject o) {
        if (this.shouldhours < o.shouldhours) {
            return -1;
        } else if (this.shouldhours == o.shouldhours) {
            return 0;
        } else {
            return 1;
        }
    }
}
