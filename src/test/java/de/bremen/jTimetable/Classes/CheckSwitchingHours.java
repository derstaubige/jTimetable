package de.bremen.jTimetable.Classes;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;

import org.h2.tools.DeleteDbFiles;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CheckSwitchingHours {

    @BeforeAll
    static void initDB() {
        try {
            SQLConnectionManager sqlConnectionManager = new SQLConnectionManager("jdbc:h2:./h2Test", "sa", "");
            sqlConnectionManager.Migrate();
            // Create Some Lecturers, Rooms, Subjects, CLS and a Timetable
            for (Long i = 1L; i < 4; i++) {
                Lecturer lecturer = new Lecturer(0L, sqlConnectionManager);
                lecturer.setFirstname("Firstname");
                lecturer.setLastname("Lastname " + i.toString());
                lecturer.save();
            }

            for (Long i = 1L; i < 4; i++) {
                Room room = new Room(0L, sqlConnectionManager);
                room.setCaption("Room " + i.toString());
                room.save();
            }

            for (Long i = 1L; i < 4; i++) {
                Subject subject = new Subject(0L, sqlConnectionManager);
                subject.setCaption("Subject " + i.toString());
                subject.save();
            }

            CourseofStudy cos1 = new CourseofStudy(0L, sqlConnectionManager);
            cos1.setCaption("COS 1");
            cos1.save();

            CourseofStudy cos2 = new CourseofStudy(0L, sqlConnectionManager);
            cos2.setCaption("COS 2");
            cos2.save();

            StudySection studySection = new StudySection(0L, sqlConnectionManager);
            studySection.setDescription("Study Section 1");
            studySection.save();

            CoursePass coursepass1 = new CoursePass(0L, sqlConnectionManager);
            coursepass1.setCourseOfStudy(cos1);
            coursepass1.setCourseOfStudyCaption("COS 1");
            coursepass1.setStart(LocalDate.of(2024, 04, 01));
            coursepass1.setEnd(LocalDate.of(2024, 05, 01));
            coursepass1.setRoom(new Room(1L,sqlConnectionManager));

            CoursePass coursepass2 = new CoursePass(0L, sqlConnectionManager);
            coursepass2.setCourseOfStudy(cos2);
            coursepass2.setCourseOfStudyCaption("COS 2");
            coursepass2.setStart(LocalDate.of(2024, 04, 01));
            coursepass2.setEnd(LocalDate.of(2024, 05, 01));
            coursepass2.setRoom(new Room(1L,sqlConnectionManager));

            for(Long i = 1L ; i < 4; i++){
                CoursepassLecturerSubject cls = new CoursepassLecturerSubject(0L, sqlConnectionManager);
                cls.setCoursepass(coursepass1);
                cls.setSubject(new Subject(i, sqlConnectionManager));
                cls.setLecturer(new Lecturer(i, sqlConnectionManager));
                cls.setRoom(new Room(i, sqlConnectionManager));
                cls.setShouldHours(5L);
            }

            for(Long i = 1L ; i < 4; i++){
                CoursepassLecturerSubject cls = new CoursepassLecturerSubject(0L, sqlConnectionManager);
                cls.setCoursepass(coursepass2);
                cls.setSubject(new Subject(5L - i, sqlConnectionManager));
                cls.setLecturer(new Lecturer(5L - i, sqlConnectionManager));
                cls.setRoom(new Room(5L - i, sqlConnectionManager));
                cls.setShouldHours(5L);
            }

            Resourcemanager resourcemanager = new Resourcemanager(sqlConnectionManager);
            resourcemanager.generateInitialTimetable(coursepass1);
            resourcemanager.generateInitialTimetable(coursepass2);
            sqlConnectionManager.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testSwitch() {
        try {
            SQLConnectionManager sqlConnectionManager = new SQLConnectionManager("jdbc:h2:./h2Test", "sa", "");
            Timetable timetable = new Timetable(new CoursePass(1L, sqlConnectionManager), sqlConnectionManager);
            ArrayList<TimetableDay> listTimetableHours =  timetable.getArrayTimetableDays();
            System.out.println("STOP!");
            assertNull(new Room(1L, sqlConnectionManager).getRoomBlocks());

        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

    @AfterAll
    static void removeDB() {
        DeleteDbFiles.execute("./", "h2Test", false);
    }
}
