package de.bremen.jTimetable.Classes;

import javafx.scene.text.Text;

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
        Long tmpSumHours = coursepassLecturerSubject.getShouldHours() - coursepassLecturerSubject.getIsHours() -
                coursepassLecturerSubject.getPlanedHours();
        String tmp = coursepassLecturerSubject.subject.getCaption() + "\r\n" +
                coursepassLecturerSubject.lecturer.getLecturerFullName() + "\r\n Shouldhours " +
                coursepassLecturerSubject.getShouldHours() + "\r\n Is Hours " +
                coursepassLecturerSubject.getIsHours() + "\r\n Planed Hours " +
                coursepassLecturerSubject.getPlanedHours() + "\r\n Not planned Hours " + tmpSumHours;
//        System.out.println(tmp);
        return tmp;
    }

    public void updateText(){
        this.coursepassLecturerSubject.updateallHours();
        super.setText(getTimetableTexts());
    }


}
