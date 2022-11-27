package de.bremen.jTimetable.Classes;

import javafx.scene.text.Text;

import java.time.LocalDate;

public class JavaFXTimetableHourText extends Text {


    CoursepassLecturerSubject coursepassLecturerSubject;
    LocalDate day;
    int timeslot;
    public JavaFXTimetableHourText(CoursepassLecturerSubject pCoursepassLexturerSubject, LocalDate pday, int ptimeslot){
        this(pCoursepassLexturerSubject, pday, ptimeslot, false);
    }
    public JavaFXTimetableHourText(CoursepassLecturerSubject pCoursepassLexturerSubject, LocalDate pday, int ptimeslot, Boolean showClassname){
        super();
        this.coursepassLecturerSubject = pCoursepassLexturerSubject;
        this.day = pday;
        this.timeslot = ptimeslot;
        if(showClassname == true){
            super.setText(pCoursepassLexturerSubject.getCoursepass().getDescription() + "\r\n" + pCoursepassLexturerSubject.getSubjectCaption()
                    + "\r\n" + pCoursepassLexturerSubject.getRoom().getCaption());
        }else{
            super.setText(pCoursepassLexturerSubject.getLecturerFullname() + "\r\n" + pCoursepassLexturerSubject.getSubjectCaption()
                    + "\r\n" + pCoursepassLexturerSubject.getRoom().getCaption());
        }

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
