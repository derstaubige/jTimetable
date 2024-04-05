package de.bremen.jTimetable.Classes;

import javafx.scene.text.Text;

import java.time.LocalDate;

public class JavaFXTimetableHourText extends Text {


    CoursepassLecturerSubject coursepassLecturerSubject;
    LocalDate day;
    int timeslot;
    private SQLConnectionManager sqlConnectionManager;

    public JavaFXTimetableHourText(CoursepassLecturerSubject pCoursepassLecturerSubject, LocalDate pday, int ptimeslot, SQLConnectionManager sqlConnectionManager){
        this(pCoursepassLecturerSubject, pday, ptimeslot, false, sqlConnectionManager);
    }
    public void deleteCLS(){
        this.coursepassLecturerSubject.deleteCLS(this.day,this.timeslot);
    }

    public JavaFXTimetableHourText(CoursepassLecturerSubject pCoursepassLecturerSubject, LocalDate pday, int ptimeslot, Boolean showClassname, SQLConnectionManager sqlConnectionManager){
        super();
        this.coursepassLecturerSubject = pCoursepassLecturerSubject;
        this.day = pday;
        this.timeslot = ptimeslot;
        setSqlConnectionManager(sqlConnectionManager);
        if(showClassname == true){
            super.setText(pCoursepassLecturerSubject.getCoursepass().getDescription() + "\r\n" + pCoursepassLecturerSubject.getSubjectCaption()
                    + "\r\n" + pCoursepassLecturerSubject.getRoomCaptionLocatioString());
        }else{
            super.setText(pCoursepassLecturerSubject.getSubjectCaption() + "\r\n" + pCoursepassLecturerSubject.getLecturerFullname()
                    + "\r\n" + pCoursepassLecturerSubject.getRoomCaptionLocatioString());
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
    public SQLConnectionManager getSqlConnectionManager() {
        return sqlConnectionManager;
    }
    public void setSqlConnectionManager(SQLConnectionManager sqlConnectionManager) {
        this.sqlConnectionManager = sqlConnectionManager;
    }
    
}
