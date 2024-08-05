package de.bremen.jTimetable.Classes;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueBoolean;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueDate;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueInt;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueLong;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueString;

public class TimetableHour {
    int timeslot;
    String lecturerName;
    String subjectCaption;
    String roomCaption;
    LocalDate date;
    CoursepassLecturerSubject coursepassLecturerSubject;
    SQLConnectionManager sqlConnectionManager;

    public TimetableHour(int timeslot, CoursepassLecturerSubject coursepassLecturerSubject, Long RoomID,
            SQLConnectionManager sqlConnectionManager, LocalDate date) {
        this.timeslot = timeslot;
        this.coursepassLecturerSubject = coursepassLecturerSubject;
        this.lecturerName = this.coursepassLecturerSubject.lecturer.getLecturerFullName();
        this.subjectCaption = this.coursepassLecturerSubject.subject.getCaption();
        this.roomCaption = this.coursepassLecturerSubject.getRoom().getCaption();
        this.date = date;
        this.setSqlConnectionManager(sqlConnectionManager);
    }

    public TimetableHour(int timeslot, CoursepassLecturerSubject coursepassLecturerSubject,
            SQLConnectionManager sqlConnectionManager, LocalDate date) {
        this(timeslot, coursepassLecturerSubject, 0L, sqlConnectionManager, date);
    }

    public void updateTimetabhleHoursCLS(CoursepassLecturerSubject cls){
        this.coursepassLecturerSubject = cls;
        this.lecturerName = this.coursepassLecturerSubject.lecturer.getLecturerFullName();
        this.subjectCaption = this.coursepassLecturerSubject.subject.getCaption();
        this.roomCaption = this.coursepassLecturerSubject.getRoom().getCaption();       
    }

    public void updateTimetableHourFromDB(){
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        SQLValues.add(new SQLValueDate(this.date));
        SQLValues.add(new SQLValueInt(this.timeslot));

        try {
            ResultSet rs = this.sqlConnectionManager.select("select REFCOURSEPASSLECTURERSUBJECT from T_TIMETABLES  where timetableday = ? and timeslot = ?", SQLValues);
            Long clsID = rs.getLong("REFCOURSEPASSLECTURERSUBJECT");
    
            if (clsID != this.coursepassLecturerSubject.getId()){
                this.coursepassLecturerSubject = new CoursepassLecturerSubject(clsID, sqlConnectionManager);
                this.lecturerName = this.coursepassLecturerSubject.lecturer.getLecturerFullName();
                this.subjectCaption = this.coursepassLecturerSubject.subject.getCaption();
                this.roomCaption = this.coursepassLecturerSubject.getRoom().getCaption();
            }            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getTimeslot() {
        return timeslot;
    }

    public void setTimeslot(int timeslot) {
        this.timeslot = timeslot;
    }

    public String getLecturerName() {
        return lecturerName;
    }

    public void setLecturerName(String lecturerName) {
        this.lecturerName = lecturerName;
    }

    public String getSubjectCaption() {
        return subjectCaption;
    }

    public void setSubjectCaption(String subjectCaption) {
        this.subjectCaption = subjectCaption;
    }

    public String getRoomCaption() {
        return roomCaption;
    }

    public void setRoomCaption(String roomCaption) {
        this.roomCaption = roomCaption;
    }

    public CoursepassLecturerSubject getCoursepassLecturerSubject() {
        return coursepassLecturerSubject;
    }

    public SQLConnectionManager getSqlConnectionManager() {
        return sqlConnectionManager;
    }

    public void setSqlConnectionManager(SQLConnectionManager sqlConnectionManager) {
        this.sqlConnectionManager = sqlConnectionManager;
    }

}
