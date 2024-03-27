package de.bremen.jTimetable.Classes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;

import org.h2.tools.DeleteDbFiles;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CheckTimetable {

    @BeforeAll
    static void initDB() {
        DeleteDbFiles.execute("./", "h2Test", false);
        try {
            SQLConnectionManager sqlConnectionManager = new SQLConnectionManager("jdbc:h2:./h2Test", "sa", "");
            sqlConnectionManager.Migrate();

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    void setupDatabase() {
        try {

            SQLConnectionManager sqlConnectionManager = new SQLConnectionManager("jdbc:h2:./h2Test", "sa", "");
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

            CourseofStudy cos3 = new CourseofStudy(0L, sqlConnectionManager);
            cos3.setCaption("COS 3");
            cos3.save();

            StudySection studySection = new StudySection(0L, sqlConnectionManager);
            studySection.setDescription("Study Section 1");
            studySection.save();

            CoursePass coursepass1 = new CoursePass(0L, sqlConnectionManager);
            coursepass1.setCourseOfStudy(cos1);
            coursepass1.setCourseOfStudyCaption("COS 1");
            coursepass1.setDescription("COS 1");
            coursepass1.setStart(LocalDate.of(2024, 04, 01));
            coursepass1.setEnd(LocalDate.of(2024, 05, 01));
            coursepass1.setRoom(new Room(1L, sqlConnectionManager));
            coursepass1.save();

            CoursePass coursepass2 = new CoursePass(0L, sqlConnectionManager);
            coursepass2.setCourseOfStudy(cos2);
            coursepass2.setCourseOfStudyCaption("COS 2");
            coursepass2.setDescription("COS 2");
            coursepass2.setStart(LocalDate.of(2024, 04, 01));
            coursepass2.setEnd(LocalDate.of(2024, 05, 01));
            coursepass2.setRoom(new Room(1L, sqlConnectionManager));
            coursepass2.save();

            CoursePass coursepass3 = new CoursePass(0L, sqlConnectionManager);
            coursepass3.setCourseOfStudy(cos3);
            coursepass3.setCourseOfStudyCaption("COS 3");
            coursepass3.setDescription("COS 3");
            coursepass3.setStart(LocalDate.of(2024, 04, 01));
            coursepass3.setEnd(LocalDate.of(2024, 05, 01));
            coursepass3.setRoom(new Room(1L, sqlConnectionManager));
            coursepass3.save();

            for (Long i = 1L; i < 4; i++) {
                CoursepassLecturerSubject cls = new CoursepassLecturerSubject(0L, sqlConnectionManager);
                cls.setCoursepass(coursepass1);
                cls.setSubject(new Subject(i, sqlConnectionManager));
                cls.setLecturer(new Lecturer(4L - i, sqlConnectionManager));
                cls.setRoom(new Room(i, sqlConnectionManager));
                cls.setShouldHours(5L);
                cls.save();
            }

            for (Long i = 1L; i < 4; i++) {
                CoursepassLecturerSubject cls = new CoursepassLecturerSubject(0L, sqlConnectionManager);
                cls.setCoursepass(coursepass2);
                cls.setSubject(new Subject(4L - i, sqlConnectionManager));
                cls.setLecturer(new Lecturer(i, sqlConnectionManager));
                cls.setRoom(new Room(4L - i, sqlConnectionManager));
                cls.setShouldHours(5L);
                cls.save();
            }

            for (Long i = 1L; i < 4; i++) {
                CoursepassLecturerSubject cls = new CoursepassLecturerSubject(0L, sqlConnectionManager);
                cls.setCoursepass(coursepass3);
                cls.setSubject(new Subject(i, sqlConnectionManager));
                cls.setLecturer(new Lecturer(4L - i, sqlConnectionManager));
                cls.setRoom(new Room(4L - i, sqlConnectionManager));
                cls.setShouldHours(5L);
                cls.save();
            }

            Resourcemanager resourcemanager = new Resourcemanager(sqlConnectionManager);
            resourcemanager.generateInitialTimetable(new CoursePass(1L, sqlConnectionManager));
            resourcemanager.generateInitialTimetable(new CoursePass(2L, sqlConnectionManager));
            resourcemanager.generateInitialTimetable(new CoursePass(3L, sqlConnectionManager));
            sqlConnectionManager.close();

        } catch (Exception e) {
            fail(e.getStackTrace().toString());
        }
    }

    @Test
    void loadThreeTimetables() {
        try {
            SQLConnectionManager sqlConnectionManager = new SQLConnectionManager("jdbc:h2:./h2Test", "sa", "");
            Timetable timetable1 = new Timetable(new CoursePass(1L, sqlConnectionManager), sqlConnectionManager);
            ArrayList<TimetableDay> listTimetableHours1 = timetable1.getArrayTimetableDays();
            Timetable timetable2 = new Timetable(new CoursePass(2L, sqlConnectionManager), sqlConnectionManager);
            ArrayList<TimetableDay> listTimetableHours2 = timetable2.getArrayTimetableDays();
            Timetable timetable3 = new Timetable(new CoursePass(3L, sqlConnectionManager), sqlConnectionManager);
            ArrayList<TimetableDay> listTimetableHours3 = timetable3.getArrayTimetableDays();

            assertNotNull(timetable1);
            assertNotNull(timetable2);
            assertNotNull(timetable3);

        } catch (Exception e) {
            fail(e.getStackTrace().toString());
        }
    }

    @Test
    void deleteHourFromTimetableCP3Day1Timeslot0() {
        try {
            SQLConnectionManager sqlConnectionManager = new SQLConnectionManager("jdbc:h2:./h2Test", "sa", "");
            Timetable timetable3 = new Timetable(new CoursePass(3L, sqlConnectionManager), sqlConnectionManager);
            timetable3.getArrayTimetableDays().get(0).getArrayTimetableDay().get(0).coursepassLecturerSubject.deleteCLS(
                    timetable3.getArrayTimetableDays().get(0).getDate(),
                    timetable3.getArrayTimetableDays().get(0).getArrayTimetableDay().get(0).getTimeslot());
            timetable3.updateCoursePassTimetable();
            assertEquals(new CoursepassLecturerSubject(0L, sqlConnectionManager), timetable3.getArrayTimetableDays().get(0).getArrayTimetableDay().get(0).getCoursepassLecturerSubject());
        } catch (Exception e) {
            fail(e.getStackTrace().toString());
        }
    }

    @AfterAll
    static void removeDB() {
        DeleteDbFiles.execute("./", "h2Test", false);
    }
}
