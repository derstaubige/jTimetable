package de.bremen.jTimetable.Classes;

public class TimetableHour {
    int timeslot;
    String lecturerName;
    String subjectCaption;
    String roomCaption;
    CoursepassLecturerSubject coursepassLecturerSubject;
    SQLConnectionManager sqlConnectionManager;

    public TimetableHour(int timeslot, CoursepassLecturerSubject coursepassLecturerSubject, Long RoomID, SQLConnectionManager sqlConnectionManager ){
        this.timeslot = timeslot;
        this.coursepassLecturerSubject = coursepassLecturerSubject;
        this.lecturerName = this.coursepassLecturerSubject.lecturer.getLecturerFullName();
        this.subjectCaption = this.coursepassLecturerSubject.subject.getCaption();
        this.roomCaption = this.coursepassLecturerSubject.getRoom().getCaption();
    }
    public TimetableHour(int timeslot, CoursepassLecturerSubject coursepassLecturerSubject, SQLConnectionManager sqlConnectionManager) {
        this(timeslot, coursepassLecturerSubject, 0L , sqlConnectionManager);
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
