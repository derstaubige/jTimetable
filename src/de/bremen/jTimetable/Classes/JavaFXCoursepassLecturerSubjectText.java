package de.bremen.jTimetable.Classes;

import javafx.scene.text.Text;

import java.time.LocalDate;

public class JavaFXCoursepassLecturerSubjectText extends Text {

    CoursepassLecturerSubject coursepassLecturerSubject;

    public JavaFXCoursepassLecturerSubjectText(CoursepassLecturerSubject pCoursepassLexturerSubject){
        super("");
        this.coursepassLecturerSubject = pCoursepassLexturerSubject;
        super.setText(getTimetableTexts());
    }

    public CoursepassLecturerSubject getCoursepassLecturerSubject() {
        return coursepassLecturerSubject;
    }

    public String getTimetableTexts(){
        String tmp = coursepassLecturerSubject.subject.getCaption() + "\r\n" +
                coursepassLecturerSubject.lecturer.getLecturerFullName() + "\r\n Shouldhours " +
                coursepassLecturerSubject.getShouldHours() + "\r\n Is Hours " +
                coursepassLecturerSubject.getIsHours() + "\r\n Planed Hours " +
                coursepassLecturerSubject.getPlanedHours();
//        System.out.println(tmp);
        return tmp;
    }
}
