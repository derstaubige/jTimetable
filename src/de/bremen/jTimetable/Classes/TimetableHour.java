package de.bremen.jTimetable.Classes;

public class TimetableHour {
    int timeslot;
    String lecturerName;
    String subject;
    String room;
    CoursepassLecturerSubject coursepassLecturerSubject;

    public TimetableHour(int timeslot, CoursepassLecturerSubject coursepassLecturerSubject, Long RoomID ){
        this.timeslot = timeslot;
        this.coursepassLecturerSubject = coursepassLecturerSubject;
        this.lecturerName = this.coursepassLecturerSubject.lecturer.getLecturerFullName();
        this.subject = this.coursepassLecturerSubject.subject.getCaption();


    }
    public TimetableHour(int timeslot, CoursepassLecturerSubject coursepassLecturerSubject) {
        this(timeslot, coursepassLecturerSubject, 0L );
    }
}
