package de.bremen.jTimetable.Classes;

public class TimetableHour {
    int timeslot;
    CoursepassLecturerSubject coursepassLecturerSubject;

    public TimetableHour(int timeslost, CoursepassLecturerSubject coursepassLecturerSubject){
        this.timeslot = timeslost;
        this.coursepassLecturerSubject = coursepassLecturerSubject;
    }
}
