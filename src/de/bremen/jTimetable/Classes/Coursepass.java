package de.bremen.jTimetable.Classes;

import java.lang.reflect.Array;
import java.time.LocalDate;
// CREATE TABLE IF NOT EXISTS  `T_Coursepasses` (`id` long not null PRIMARY KEY AUTO_INCREMENT, `refCourseofStudyID` long, `refStudySectionID` long, `start` Date, `end` Date, `active` Boolean,  `description` Char(200), `refRommID` long );
public class Coursepass {
    long id;
    CourseofStudy courseofstudy;
    StudySection studysection;
    LocalDate start;
    LocalDate end;
    boolean active;
    char description;
    Room room;
    CoursepassLecturerSubject[] arraycoursepasslecturersubject;

    public static void main(String[] args){

    }
}
