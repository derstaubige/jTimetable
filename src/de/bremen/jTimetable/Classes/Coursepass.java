package de.bremen.jTimetable.Classes;

import java.lang.reflect.Array;
import java.time.LocalDate;

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
