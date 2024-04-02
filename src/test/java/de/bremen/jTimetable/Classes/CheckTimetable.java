package de.bremen.jTimetable.Classes;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDate;
import java.util.ArrayList;

import org.h2.tools.DeleteDbFiles;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;

@TestMethodOrder(OrderAnnotation.class)
public class CheckTimetable {
    private static SQLConnectionManager sqlConnectionManager;

    @BeforeAll
    static void initDB() {
        DeleteDbFiles.execute("./", "h2Test", false);
        try {
            sqlConnectionManager = new SQLConnectionManager("jdbc:h2:./h2Test", "sa", "");
            sqlConnectionManager.Migrate();

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    @Order(1)
    void setupDatabase() {
        try {

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

            Timetable timetable1 = new Timetable(new CoursePass(1L, sqlConnectionManager), sqlConnectionManager);
            ArrayList<TimetableDay> listTimetableHours1 = timetable1.getArrayTimetableDays();
            Timetable timetable2 = new Timetable(new CoursePass(2L, sqlConnectionManager), sqlConnectionManager);
            ArrayList<TimetableDay> listTimetableHours2 = timetable2.getArrayTimetableDays();
            Timetable timetable3 = new Timetable(new CoursePass(3L, sqlConnectionManager), sqlConnectionManager);
            ArrayList<TimetableDay> listTimetableHours3 = timetable3.getArrayTimetableDays();

            assertNotEquals(0, timetable1.getArrayTimetableDays().size());
            assertNotEquals(0, timetable2.getArrayTimetableDays().size());
            assertNotEquals(0, timetable3.getArrayTimetableDays().size());
        } catch (Exception e) {
            fail(e.getStackTrace().toString());
        }
    }

    @Test
    @Order(2)
    void deleteHourFromTimetableCP3Day1Timeslot0() {
        try {

            Timetable timetable3 = new Timetable(new CoursePass(3L, sqlConnectionManager), sqlConnectionManager);

            timetable3.getArrayTimetableDays().get(0).getArrayTimetableDay().get(0).coursepassLecturerSubject.deleteCLS(
                    timetable3.getArrayTimetableDays().get(0).getDate(),
                    timetable3.getArrayTimetableDays().get(0).getArrayTimetableDay().get(0).getTimeslot());

            timetable3.updateCoursePassTimetable();

            assertEquals(0L, timetable3.getArrayTimetableDays()
                    .get(0).getArrayTimetableDay().get(0).getCoursepassLecturerSubject().getLecturerID());
        } catch (Exception e) {
            fail(e.getStackTrace().toString());
        }
    }

    @Test
    @Order(3)
    void moveHourFromTimetableCP3Day1Timeslot2L2R2toDay1Timeslot0() {
        try {

            Timetable timetable3 = new Timetable(new CoursePass(3L, sqlConnectionManager), sqlConnectionManager);
            TimetableHour source = timetable3.getArrayTimetableDays().get(0).getArrayTimetableDay().get(0);
            TimetableHour target = timetable3.getArrayTimetableDays().get(0).getArrayTimetableDay().get(2);

            if (CoursepassLecturerSubject.cangetExchanged(source.getCoursepassLecturerSubject(),
                    timetable3.getArrayTimetableDays().get(0).getDate(), source.getTimeslot(),
                    target.getCoursepassLecturerSubject(), timetable3.getArrayTimetableDays().get(0).getDate(),
                    target.getTimeslot(), sqlConnectionManager)) {
                CoursepassLecturerSubject.changeCoursepassLecturerSubject(source.getCoursepassLecturerSubject(),
                        timetable3.getArrayTimetableDays().get(0).getDate(), source.getTimeslot(),
                        target.getCoursepassLecturerSubject(), timetable3.getArrayTimetableDays().get(0).getDate(),
                        target.getTimeslot(), sqlConnectionManager);
            } else {
                fail("Problem with determining if the Freetime and the Free Lecturer and Room can be Swapped");
            }

            timetable3.updateCoursePassTimetable();
            ResourcesBlocked resourcesBlocked;

            assertEquals(2, timetable3.getArrayTimetableDays().get(0).getArrayTimetableDay().get(0)
                    .getCoursepassLecturerSubject().getLecturer().getId()); // check if Day 1 Slot 0 has LecturerID 2
            assertEquals(2, timetable3.getArrayTimetableDays().get(0).getArrayTimetableDay().get(0)
                    .getCoursepassLecturerSubject().getRoom().getId()); // check if Day 1 Slot 0 has RoomID 2

            resourcesBlocked = new ResourcesBlocked(2L, ResourceNames.LECTURER,
                    timetable3.getArrayTimetableDays().get(0).getDate(),
                    timetable3.getArrayTimetableDays().get(0).getDate(), 0, 0, sqlConnectionManager);
            assertEquals(2, resourcesBlocked.getRefResourceID()); // check if resources Blocked has Lecturer 2

            resourcesBlocked = new ResourcesBlocked(2L, ResourceNames.ROOM,
                    timetable3.getArrayTimetableDays().get(0).getDate(),
                    timetable3.getArrayTimetableDays().get(0).getDate(), 0, 0, sqlConnectionManager);
            assertEquals(2, resourcesBlocked.getRefResourceID()); // check if resources Blocked has Room 2

            assertEquals(0, timetable3.getArrayTimetableDays().get(0).getArrayTimetableDay().get(2)
                    .getCoursepassLecturerSubject().getLecturer().getId()); // check if Day 1 Slot 2 has LecturerID 0

            assertEquals(0, timetable3.getArrayTimetableDays().get(0).getArrayTimetableDay().get(2)
                    .getCoursepassLecturerSubject().getRoom().getId()); // check if Day 1 Slot 2 has RoomID 0

            resourcesBlocked = new ResourcesBlocked(0L, ResourceNames.LECTURER,
                    timetable3.getArrayTimetableDays().get(0).getDate(),
                    timetable3.getArrayTimetableDays().get(0).getDate(), 2, 2, sqlConnectionManager);
            assertEquals(0, resourcesBlocked.getRefResourceID()); // check if resources Blocked has Lecturer 0

            resourcesBlocked = new ResourcesBlocked(0L, ResourceNames.ROOM,
                    timetable3.getArrayTimetableDays().get(0).getDate(),
                    timetable3.getArrayTimetableDays().get(0).getDate(), 2, 2, sqlConnectionManager);
            assertEquals(0, resourcesBlocked.getRefResourceID()); // check if resources Blocked has Room 0

        } catch (Exception e) {
            fail(e.getStackTrace().toString());
        }
    }

    @Test
    @Order(4)
    void checkIfCP3Day1Timeslot0CouldBeDeleted() {
        try {

            ResourcesBlocked resourcesBlocked;
            Timetable timetable3 = new Timetable(new CoursePass(3L, sqlConnectionManager), sqlConnectionManager);
            TimetableHour deleteMe = timetable3.getArrayTimetableDays().get(0).getArrayTimetableDay().get(0);
            deleteMe.getCoursepassLecturerSubject().deleteCLS(timetable3.getArrayTimetableDays().get(0).getDate(),
                    0); // delete Day 1 Timeslot 0
            timetable3.updateCoursePassTimetable();

            assertEquals(0, timetable3.getArrayTimetableDays().get(0).getArrayTimetableDay().get(0)
                    .getCoursepassLecturerSubject().getLecturer().getId()); // check if Day 1 Slot 0 has LecturerID 0
            assertEquals(0, timetable3.getArrayTimetableDays().get(0).getArrayTimetableDay().get(0)
                    .getCoursepassLecturerSubject().getRoom().getId()); // check if Day 1 Slot 0 has RoomID 0

            resourcesBlocked = new ResourcesBlocked(0L, ResourceNames.LECTURER,
                    timetable3.getArrayTimetableDays().get(0).getDate(),
                    timetable3.getArrayTimetableDays().get(0).getDate(), 0, 0, sqlConnectionManager);
            assertEquals(0, resourcesBlocked.getRefResourceID()); // check if resources Blocked has Lecturer 0

            resourcesBlocked = new ResourcesBlocked(0L, ResourceNames.ROOM,
                    timetable3.getArrayTimetableDays().get(0).getDate(),
                    timetable3.getArrayTimetableDays().get(0).getDate(), 0, 0, sqlConnectionManager);
            assertEquals(0, resourcesBlocked.getRefResourceID()); // check if resources Blocked has Room 0
        } catch (Exception e) {
            fail(e.getStackTrace().toString());
        }
    }

    @Test
    @Order(5)
    void checkIfWeCouldAddAnHourCP3Day1Timeslot0() {
        try {

            Timetable timetable3 = new Timetable(new CoursePass(3L, sqlConnectionManager), sqlConnectionManager);
            ResourcesBlocked resourcesBlocked;
            TimetableHour target = timetable3.getArrayTimetableDays().get(0).getArrayTimetableDay().get(0);
            LocalDate targetDate = timetable3.getArrayTimetableDays().get(0).getDate();

            CoursepassLecturerSubject source = new CoursepassLecturerSubject(8L,
                    sqlConnectionManager, new CoursePass(3L, sqlConnectionManager));

            if (CoursepassLecturerSubject.isFreeTarget(source,
                    targetDate,
                    target.getTimeslot(), sqlConnectionManager) == true) {
                // check if the target was a freetime, if not we have to delete the existing cls
                if (target.getCoursepassLecturerSubject().getSubject().getId() != 0) {
                    // no freetime, we have to delete the resourceblocked and the entry in the
                    // timetable
                    Timetable.deleteResourceBlocked(
                            target.getCoursepassLecturerSubject().getLecturerID(),
                            ResourceNames.LECTURER, targetDate, targetDate,
                            target.getTimeslot(),
                            target.getTimeslot(), sqlConnectionManager);
                    Timetable.deleteResourceBlocked(
                            target.getCoursepassLecturerSubject().getRoom().getId(),
                            ResourceNames.ROOM, targetDate, targetDate, target.getTimeslot(),
                            target.getTimeslot(), sqlConnectionManager);
                }
                // delete the entry in the timetable table
                Timetable.deleteTimetable(source.getId(),
                        targetDate, target.getTimeslot(), sqlConnectionManager);
                TimetableHour tmptimetableHour = new TimetableHour(target.getTimeslot(),
                        source, sqlConnectionManager);
                timetable3.addSingleHour(tmptimetableHour, targetDate, target.getTimeslot());
                timetable3.updateCoursePassTimetable();
            } else {
                fail("Source isnt free");
            }
            assertEquals(2, timetable3.getArrayTimetableDays().get(0).getArrayTimetableDay().get(0)
                    .getCoursepassLecturerSubject().getLecturer().getId()); // check if Day 1 Slot 0 has LecturerID 2
            assertEquals(2, timetable3.getArrayTimetableDays().get(0).getArrayTimetableDay().get(0)
                    .getCoursepassLecturerSubject().getRoom().getId()); // check if Day 1 Slot 0 has RoomID 2

            resourcesBlocked = new ResourcesBlocked(2L, ResourceNames.LECTURER,
                    timetable3.getArrayTimetableDays().get(0).getDate(),
                    timetable3.getArrayTimetableDays().get(0).getDate(), 0, 0, sqlConnectionManager);
            assertEquals(2, resourcesBlocked.getRefResourceID()); // check if resources Blocked has Lecturer 2

            resourcesBlocked = new ResourcesBlocked(2L, ResourceNames.ROOM,
                    timetable3.getArrayTimetableDays().get(0).getDate(),
                    timetable3.getArrayTimetableDays().get(0).getDate(), 0, 0, sqlConnectionManager);
            assertEquals(2, resourcesBlocked.getRefResourceID()); // check if resources Blocked has Room 2

        } catch (Exception e) {
            fail(e.getStackTrace().toString());
        }

    }

    @Test
    @Order(6)
    void checkIfDeletingTimetableWorks() {
        Timetable timetable1 = new Timetable(new CoursePass(1L, sqlConnectionManager), sqlConnectionManager);
        Timetable timetable2 = new Timetable(new CoursePass(2L, sqlConnectionManager), sqlConnectionManager);
        
        timetable1.deleteTimetable();
        timetable2.deleteTimetable();
        
        assertEquals(0, timetable1.getArrayTimetableDays().size());
        assertEquals(0, timetable2.getArrayTimetableDays().size());
    }
    
    @Test
    @Order(7)
    void checkIfDistributingRemainingHoursWorks() {

    }

    @Test
    void checkIfSwappingTwoHoursFailed() {

    }

    @Test
    void checkIfAddingAnHourFailed() {

    }


    @AfterAll
    static void removeDB() {
        // DeleteDbFiles.execute("./", "h2Test", false);
    }
}
