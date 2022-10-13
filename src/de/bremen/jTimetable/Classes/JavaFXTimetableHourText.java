package de.bremen.jTimetable.Classes;

import javafx.scene.text.Text;

import java.time.LocalDate;

public class JavaFXTimetableHourText extends Text {


    CoursepassLecturerSubject coursepassLecturerSubject;
    LocalDate day;
    int timeslot;
    public JavaFXTimetableHourText(String pText, CoursepassLecturerSubject pCoursepassLexturerSubject, LocalDate pday, int ptimeslot){
        super(pText);
        this.coursepassLecturerSubject = pCoursepassLexturerSubject;
        this.day = pday;
        this.timeslot = ptimeslot;
    }

    public LocalDate getDay() {
        return day;
    }

    public int getTimeslot() {
        return timeslot;
    }

    public CoursepassLecturerSubject getCoursepassLecturerSubject() {
        return coursepassLecturerSubject;
    }
}
