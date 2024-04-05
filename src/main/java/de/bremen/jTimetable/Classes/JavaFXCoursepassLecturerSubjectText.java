package de.bremen.jTimetable.Classes;

import java.util.ResourceBundle;

import javafx.scene.text.Text;

public class JavaFXCoursepassLecturerSubjectText extends Text {

    CoursepassLecturerSubject coursepassLecturerSubject;
    private ResourceBundle resourceBundle;

    public JavaFXCoursepassLecturerSubjectText(CoursepassLecturerSubject pCoursepassLexturerSubject, ResourceBundle resourceBundle){
        super("");
        this.coursepassLecturerSubject = pCoursepassLexturerSubject;
        this.resourceBundle = resourceBundle;
        super.setText(getTimetableTexts());
    }

    public CoursepassLecturerSubject getCoursepassLecturerSubject() {
        return coursepassLecturerSubject;
    }

    public String getTimetableTexts(){
        Long tmpSumHours = coursepassLecturerSubject.getShouldHours() - coursepassLecturerSubject.getIsHours() -
                coursepassLecturerSubject.getPlanedHours();
        String tmp = coursepassLecturerSubject.subject.getCaption() + "\r\n" +
                coursepassLecturerSubject.lecturer.getLecturerFullName() + "\r\n" + 
                coursepassLecturerSubject.getRoomCaptionLocatioString() + "\r\n" + 
                resourceBundle.getString("javaFXCoursepassLecturerSubjectText.shouldHours") + " " + coursepassLecturerSubject.getShouldHours() + "\r\n" + 
                resourceBundle.getString("javaFXCoursepassLecturerSubjectText.isHours") + " " + coursepassLecturerSubject.getIsHours() + "\r\n" + 
                resourceBundle.getString("javaFXCoursepassLecturerSubjectText.planedHours") + " " + coursepassLecturerSubject.getPlanedHours() + "\r\n" + 
                resourceBundle.getString("javaFXCoursepassLecturerSubjectText.notplanedHours") + " " + tmpSumHours;
//        System.out.println(tmp);
        return tmp;
    }

    public void updateText(){
        this.coursepassLecturerSubject.updateallHours();
        super.setText(getTimetableTexts());
    }


}
