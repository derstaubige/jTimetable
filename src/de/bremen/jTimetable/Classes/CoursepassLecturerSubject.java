package de.bremen.jTimetable.Classes;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

public class CoursepassLecturerSubject implements Comparable<CoursepassLecturerSubject> {
    Long id;
    Coursepass coursepass;
    Lecturer lecturer;
    Subject subject;
    public Long shouldHours;
    public Long isHours; // hours that have actually been given
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

    public static void changeCoursepassLecturerSubject(CoursepassLecturerSubject source, LocalDate sourceDay, int sourceTimeslot, CoursepassLecturerSubject target, LocalDate targetDay, int targetTimeslot){

        try{
            //change Resourcesblocked, Lecturerer and Room ID

            SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();
            ResultSet rs;

            //update source lecturer entry if the new lecturer is not 0
            if(target.lecturer.getId() != 0){

                SQLValues.add(new SQLValueLong(target.lecturer.getId()));
                SQLValues.add(new SQLValueLong(source.lecturer.getId()));
                SQLValues.add(new SQLValueDate(sourceDay));
                SQLValues.add(new SQLValueDate(sourceDay));
                SQLValues.add(new SQLValueInt(sourceTimeslot));
                SQLValues.add(new SQLValueInt(sourceTimeslot));
                rs = sqlConnectionManager.execute("update `T_RESOURCESBLOCKED` set `REFRESOURCEID` = ? where `RESOURCENAME` = 'Lecturer' and `REFRESOURCEID` = ? and STARTDATE = ? and ENDDATE = ? and STARTTIMESLOT = ? and ENDTIMESLOT  = ?;",SQLValues);
                SQLValues = new ArrayList<SQLConnectionManagerValues>();
            }

            //update source room entry if the new room is not 0
            if(target.coursepass.room.getId() != 0){
                SQLValues.add(new SQLValueLong(target.coursepass.room.getId()));
                SQLValues.add(new SQLValueLong(source.coursepass.room.getId()));
                SQLValues.add(new SQLValueDate(sourceDay));
                SQLValues.add(new SQLValueDate(sourceDay));
                SQLValues.add(new SQLValueInt(sourceTimeslot));
                SQLValues.add(new SQLValueInt(sourceTimeslot));
                rs = sqlConnectionManager.execute("update `T_RESOURCESBLOCKED` set `REFRESOURCEID` = ? where `RESOURCENAME` = 'Room' and `REFRESOURCEID` = ? and STARTDATE = ? and ENDDATE = ? and STARTTIMESLOT = ? and ENDTIMESLOT  = ?;",SQLValues);
                SQLValues = new ArrayList<SQLConnectionManagerValues>();
            }

            //update target lecturer entry if the source lecturer is not 0
            if(source.lecturer.getId() != 0){
                SQLValues.add(new SQLValueLong(source.lecturer.getId()));
                SQLValues.add(new SQLValueLong(target.lecturer.getId()));
                SQLValues.add(new SQLValueDate(targetDay));
                SQLValues.add(new SQLValueDate(targetDay));
                SQLValues.add(new SQLValueInt(targetTimeslot));
                SQLValues.add(new SQLValueInt(targetTimeslot));
                rs = sqlConnectionManager.execute("update `T_RESOURCESBLOCKED` set `REFRESOURCEID` = ? where `RESOURCENAME` = 'Lecturer' and `REFRESOURCEID` = ? and STARTDATE = ? and ENDDATE = ? and STARTTIMESLOT = ? and ENDTIMESLOT  = ?;",SQLValues);
                SQLValues = new ArrayList<SQLConnectionManagerValues>();
            }

            //update target room entry if source room is not 0
            if(source.coursepass.room.getId() != 0){

                SQLValues.add(new SQLValueLong(source.coursepass.room.getId()));
                SQLValues.add(new SQLValueLong(target.coursepass.room.getId()));
                SQLValues.add(new SQLValueDate(targetDay));
                SQLValues.add(new SQLValueDate(targetDay));
                SQLValues.add(new SQLValueInt(targetTimeslot));
                SQLValues.add(new SQLValueInt(targetTimeslot));
                rs = sqlConnectionManager.execute("update `T_RESOURCESBLOCKED` set `REFRESOURCEID` = ? where `RESOURCENAME` = 'Room' and `REFRESOURCEID` = ? and STARTDATE = ? and ENDDATE = ? and STARTTIMESLOT = ? and ENDTIMESLOT  = ?;",SQLValues);
                SQLValues = new ArrayList<SQLConnectionManagerValues>();
            }

            //change T_Timetables
            //update source if the target is not 0 / freetime
//            if(target.id != 0){
                SQLValues.add(new SQLValueLong(target.id));
                SQLValues.add(new SQLValueLong(target.coursepass.room.getId()));
                SQLValues.add(new SQLValueLong(target.lecturer.getId()));
                SQLValues.add(new SQLValueLong( source.subject.getId() ));
                SQLValues.add(new SQLValueLong(source.coursepass.getId() != 0 ? source.coursepass.getId() : target.coursepass.getId()));
                SQLValues.add(new SQLValueDate(sourceDay));
                SQLValues.add(new SQLValueInt(sourceTimeslot));
                rs = sqlConnectionManager.execute("update `T_TIMETABLES` set REFCOURSEPASSLECTURERSUBJECT = ?, REFROOMID = ?, REFLECTURER = ?, REFSUBJECT = ? where refcoursepass = ? and timetableday = ? and timeslot = ?",SQLValues);
                SQLValues = new ArrayList<SQLConnectionManagerValues>();
//            }

            //update target if the source is not 0 / freetime
//            if(source.id != 0){
                SQLValues.add(new SQLValueLong(source.id));
                SQLValues.add(new SQLValueLong(source.coursepass.room.getId()));
                SQLValues.add(new SQLValueLong(source.lecturer.getId()));
                SQLValues.add(new SQLValueLong(source.subject.getId()));
                SQLValues.add(new SQLValueLong( target.coursepass.getId() != 0 ? target.coursepass.getId() : source.coursepass.getId()));
                SQLValues.add(new SQLValueDate(targetDay));
                SQLValues.add(new SQLValueInt(targetTimeslot));
                rs = sqlConnectionManager.execute("update `T_TIMETABLES` set REFCOURSEPASSLECTURERSUBJECT = ?, REFROOMID = ?, REFLECTURER = ?, REFSUBJECT = ? where refcoursepass = ? and timetableday = ? and timeslot = ?",SQLValues);
                SQLValues = new ArrayList<SQLConnectionManagerValues>();
//            }

        }catch(Exception e){
            System.out.println("An SQLError occurred while Updating ResourceBlocked an Timetables");
            e.printStackTrace();
        }
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
            this.shouldHours = 0L;
            this.isHours = 0L;
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
            this.shouldHours = rs.getLong("shouldhours");
            this.active = rs.getBoolean("active");

            this.updateIsHours();
            this.updatePlanedHours();
        }


    }

    public void updatePlanedHours(){
        try{
            SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();
            ResultSet rs;
            LocalDate today = LocalDate.now();
            //query the planed hours
            SQLValues.clear();
            SQLValues.add(new SQLValueLong(this.coursepass.id));
            SQLValues.add(new SQLValueLong(this.subject.id));
            SQLValues.add(new SQLValueDate(today));
            rs = sqlConnectionManager.select("Select count(id) from T_Timetables where refcoursepass = ? and refsubject = ? and timetableday > ?;",SQLValues);
            rs.first();
            this.planedHours = rs.getLong(1);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void updateIsHours(){
        try{
            SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();
            ResultSet rs;
            //query the is hours
            SQLValues.clear();
            LocalDate today = LocalDate.now();
            SQLValues.add(new SQLValueLong(this.coursepass.id));
            SQLValues.add(new SQLValueLong(this.subject.id));
            SQLValues.add(new SQLValueDate(today));
            rs = sqlConnectionManager.select("Select count(id) from T_Timetables where refcoursepass = ? and refsubject = ? and timetableday < ?;",SQLValues);
            rs.first();
            this.isHours = rs.getLong(1);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void save() throws SQLException{
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        SQLValues.add(new SQLValueLong(this.coursepass.id));
        SQLValues.add(new SQLValueLong(this.lecturer.id));
        SQLValues.add(new SQLValueLong(this.subject.id));
        SQLValues.add(new SQLValueLong(this.shouldHours));
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
        if (this.shouldHours < o.shouldHours) {
            return -1;
        } else if (this.shouldHours == o.shouldHours) {
            return 0;
        } else {
            return 1;
        }
    }

    public long getLecturerID(){
        return this.lecturer.getId();
    }
    public String getLecturerFullname() { return this.lecturer.getLecturerFullName(); }
    public String getSubjectCaption() { return this.subject.getCaption(); }

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

    public Long getShouldhours() {
        return shouldHours;
    }

    public void setShouldhours(Long shouldhours) {
        this.shouldHours = shouldhours;
    }

    public Long getId() {
        return id;
    }

    public Coursepass getCoursepass() {
        return coursepass;
    }

    public void setCoursepass(Coursepass coursepass) {
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
}
