package de.bremen.jTimetable ;

import de.bremen.jTimetable.Classes.*;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueInt;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueString;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

public class Main {
    public static void main(String[] args){
        try {
            //CourseofStudy cos = new CourseofStudy(8);
            //System.out.println(cos);

            //Coursepass cp = new Coursepass(102);

//            Room room = new Room(1L);
//            room.roomcaption = "Raum 321";
//            room.save();

//            Location location = new Location(0L);
//            location.caption = "Haus des Reichs";
//            location.active = Boolean.FALSE;
//            location.save();

//            Subject subject = new Subject(1L);
//            subject.caption = "Abgabenordnung";
//            subject.save();

//            StudySection studySection = new StudySection(3L);
//            studySection.description = "Grundstudium 1";
//            studySection.save();

//            Lecturer lecturer = new Lecturer(1L);
//            lecturer.firstname = "Bne";
//            lecturer.lastname = "Dborra";
//            lecturer.save();

//            CoursepassLecturerSubject coursepassLecturerSubject = new CoursepassLecturerSubject(1L);
//            coursepassLecturerSubject.shouldhours = 20L;
//            coursepassLecturerSubject.save();

//            CourseofStudy courseofStudy = new CourseofStudy(1L);
//            courseofStudy.begin = LocalDate.of(2022,6,8);
//            courseofStudy.caption = "hallo";
//            courseofStudy.save();

            Coursepass coursepass = new Coursepass(134L);
//            coursepass.description = "Buja";
//            coursepass.getCoursepassLecturerSubjects();
//            for(CoursepassLecturerSubject str: coursepass.arraycoursepasslecturersubject){
//                System.out.println(str.shouldhours);
//            }
//            Collections.sort(coursepass.arraycoursepasslecturersubject);
//            for(CoursepassLecturerSubject str: coursepass.arraycoursepasslecturersubject){
//                System.out.println(str.shouldhours);
//            }
//            coursepass.start = LocalDate.of(2022,6,8);
//            coursepass.end = LocalDate.of(2022,9,30);
//            coursepass.save();

//            CoursepassLecturerSubject c1 = new CoursepassLecturerSubject(1L);
//            CoursepassLecturerSubject c2 = new CoursepassLecturerSubject(2L);

            Resourcemanager resourcemanager = new Resourcemanager();
            resourcemanager.generateInitialTimetable(coursepass);

//            TimetableDay timetableDay = new TimetableDay(LocalDate.now(), 3);
//            System.out.println(timetableDay.arrayTimetableDay.get(0).isEmpty());
//            timetableDay.addToSlot(0, new CoursepassLecturerSubject(0L));
//            System.out.println(timetableDay.arrayTimetableDay.get(0).isEmpty());
            System.out.println("hi");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}