package de.bremen.jTimetable.Classes;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

import org.h2.jdbc.JdbcSQLNonTransientException;
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
    private static ResourceBundle resourceBundle = ResourceBundle.getBundle("Resources");

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

            Timetable timetable1 = new Timetable(new CoursePass(1L, sqlConnectionManager), sqlConnectionManager, resourceBundle);
            ArrayList<TimetableDay> listTimetableHours1 = timetable1.getArrayTimetableDays();
            Timetable timetable2 = new Timetable(new CoursePass(2L, sqlConnectionManager), sqlConnectionManager, resourceBundle);
            ArrayList<TimetableDay> listTimetableHours2 = timetable2.getArrayTimetableDays();
            Timetable timetable3 = new Timetable(new CoursePass(3L, sqlConnectionManager), sqlConnectionManager, resourceBundle);
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

            Timetable timetable3 = new Timetable(new CoursePass(3L, sqlConnectionManager), sqlConnectionManager, resourceBundle);

            timetable3.getArrayTimetableDays().get(0).getArrayTimetableHours().get(0).coursepassLecturerSubject.deleteCLS(
                    timetable3.getArrayTimetableDays().get(0).getDate(),
                    timetable3.getArrayTimetableDays().get(0).getArrayTimetableHours().get(0).getTimeslot());

            timetable3.updateCoursePassTimetable();

            assertEquals(0L, timetable3.getArrayTimetableDays()
                    .get(0).getArrayTimetableHours().get(0).getCoursepassLecturerSubject().getLecturerID());
        } catch (Exception e) {
            fail(e.getStackTrace().toString());
        }
    }

    @Test
    @Order(3)
    void moveHourFromTimetableCP3Day1Timeslot2L2R2toDay1Timeslot0() {
        try {

            Timetable timetable3 = new Timetable(new CoursePass(3L, sqlConnectionManager), sqlConnectionManager, resourceBundle);
            TimetableHour source = timetable3.getArrayTimetableDays().get(0).getArrayTimetableHours().get(2);
            TimetableHour target = timetable3.getArrayTimetableDays().get(0).getArrayTimetableHours().get(0);

            TimetableEntry sourceTimetableEntry = new TimetableEntry(
                    source.getCoursepassLecturerSubject(),
                    timetable3.getArrayTimetableDays().get(0).getDate(), source.getTimeslot(), sqlConnectionManager);
            TimetableEntry targetTimetableEntry = new TimetableEntry(
                    target.getCoursepassLecturerSubject(),
                    timetable3.getArrayTimetableDays().get(0).getDate(), target.getTimeslot(), sqlConnectionManager);

            if (CoursepassLecturerSubject.cangetExchanged(sourceTimetableEntry, targetTimetableEntry,
                    sqlConnectionManager)) {

                timetable3.swapHours(sourceTimetableEntry, targetTimetableEntry);
            } else {
                fail("Problem with determining if the Freetime and the Free Lecturer and Room can be Swapped");
            }

            timetable3.updateCoursePassTimetable();
            ResourcesBlocked resourcesBlocked;

            assertEquals(2, timetable3.getArrayTimetableDays().get(0).getArrayTimetableHours().get(0)
                    .getCoursepassLecturerSubject().getLecturer().getId()); // check if Day 1 Slot 0 has LecturerID 2
            assertEquals(2, timetable3.getArrayTimetableDays().get(0).getArrayTimetableHours().get(0)
                    .getCoursepassLecturerSubject().getRoom().getId()); // check if Day 1 Slot 0 has RoomID 2

            resourcesBlocked = new ResourcesBlocked(2L, ResourceNames.LECTURER,
                    timetable3.getArrayTimetableDays().get(0).getDate(),
                    timetable3.getArrayTimetableDays().get(0).getDate(), 0, 0, sqlConnectionManager);
            assertEquals(2, resourcesBlocked.getRefResourceID()); // check if resources Blocked has Lecturer 2

            resourcesBlocked = new ResourcesBlocked(2L, ResourceNames.ROOM,
                    timetable3.getArrayTimetableDays().get(0).getDate(),
                    timetable3.getArrayTimetableDays().get(0).getDate(), 0, 0, sqlConnectionManager);
            assertEquals(2, resourcesBlocked.getRefResourceID()); // check if resources Blocked has Room 2

            assertEquals(0, timetable3.getArrayTimetableDays().get(0).getArrayTimetableHours().get(2)
                    .getCoursepassLecturerSubject().getLecturer().getId()); // check if Day 1 Slot 2 has LecturerID 0

            assertEquals(0, timetable3.getArrayTimetableDays().get(0).getArrayTimetableHours().get(2)
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
            Timetable timetable3 = new Timetable(new CoursePass(3L, sqlConnectionManager), sqlConnectionManager, resourceBundle);
            TimetableHour deleteMe = timetable3.getArrayTimetableDays().get(0).getArrayTimetableHours().get(0);
            deleteMe.getCoursepassLecturerSubject().deleteCLS(timetable3.getArrayTimetableDays().get(0).getDate(),
                    0); // delete Day 1 Timeslot 0
            timetable3.updateCoursePassTimetable();

            assertEquals(0, timetable3.getArrayTimetableDays().get(0).getArrayTimetableHours().get(0)
                    .getCoursepassLecturerSubject().getLecturer().getId()); // check if Day 1 Slot 0 has LecturerID 0
            assertEquals(0, timetable3.getArrayTimetableDays().get(0).getArrayTimetableHours().get(0)
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

            Timetable timetable3 = new Timetable(new CoursePass(3L, sqlConnectionManager), sqlConnectionManager, resourceBundle);
            ResourcesBlocked resourcesBlocked;
            TimetableHour target = timetable3.getArrayTimetableDays().get(0).getArrayTimetableHours().get(0);
            LocalDate targetDate = timetable3.getArrayTimetableDays().get(0).getDate();

            CoursepassLecturerSubject source = new CoursepassLecturerSubject(8L, sqlConnectionManager,
                    target.getCoursepassLecturerSubject().getCoursepass());
            LocalDate sourceDate = timetable3.getArrayTimetableDays().get(0).getDate();

            if (CoursepassLecturerSubject.isFreeTarget(source,
                    targetDate,
                    target.getTimeslot(), sqlConnectionManager) == true) {
                TimetableEntry targetTimetableEntry = new TimetableEntry(target.getCoursepassLecturerSubject(),
                        targetDate, target.getTimeslot(), sqlConnectionManager);
                timetable3.addSingleHour(source, targetTimetableEntry);
            } else {
                fail("Source isnt free");
            }
            assertEquals(2, timetable3.getArrayTimetableDays().get(0).getArrayTimetableHours().get(0)
                    .getCoursepassLecturerSubject().getLecturer().getId()); // check if Day 1 Slot 0 has LecturerID 2
            assertEquals(2, timetable3.getArrayTimetableDays().get(0).getArrayTimetableHours().get(0)
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
        Timetable timetable1 = new Timetable(new CoursePass(1L, sqlConnectionManager), sqlConnectionManager, resourceBundle);
        Timetable timetable2 = new Timetable(new CoursePass(2L, sqlConnectionManager), sqlConnectionManager, resourceBundle);

        timetable1.deleteTimetable();
        timetable2.deleteTimetable();

        assertEquals(0, timetable1.getArrayTimetableDays().size());
        assertEquals(0, timetable2.getArrayTimetableDays().size());

        // check if ressourcesblocked are also deleted
        assertThrows(RuntimeException.class, () -> {
            new ResourcesBlocked(1L, sqlConnectionManager);
        }); // This is the first Lecturer for CP1
        assertThrows(RuntimeException.class, () -> {
            new ResourcesBlocked(2L, sqlConnectionManager);
        }); // This is the first Room for CP1

        assertThrows(RuntimeException.class, () -> {
            new ResourcesBlocked(31L, sqlConnectionManager);
        }); // This is the first Lecturer for CP2
        assertThrows(RuntimeException.class, () -> {
            new ResourcesBlocked(32L, sqlConnectionManager);
        }); // This is the first Room for CP2

    }

    @Test
    @Order(7)
    void checkIfDistributingRemainingHoursWorks() {
        Timetable timetable3 = new Timetable(new CoursePass(3L, sqlConnectionManager), sqlConnectionManager, resourceBundle);
        ResourcesBlocked resourcesBlocked;

        timetable3.distributeUnplanedHours();

        assertEquals(2, timetable3.getArrayTimetableDays().get(0).getArrayTimetableHours().get(1)
                .getCoursepassLecturerSubject().getLecturer().getId()); // check if Day 1 Slot 0 has LecturerID 2
        assertEquals(2, timetable3.getArrayTimetableDays().get(0).getArrayTimetableHours().get(1)
                .getCoursepassLecturerSubject().getRoom().getId()); // check if Day 1 Slot 0 has RoomID 2

        resourcesBlocked = new ResourcesBlocked(2L, ResourceNames.LECTURER,
                timetable3.getArrayTimetableDays().get(0).getDate(),
                timetable3.getArrayTimetableDays().get(0).getDate(), 1, 1, sqlConnectionManager);
        assertEquals(2, resourcesBlocked.getRefResourceID()); // check if resources Blocked has Lecturer 2

        resourcesBlocked = new ResourcesBlocked(2L, ResourceNames.ROOM,
                timetable3.getArrayTimetableDays().get(0).getDate(),
                timetable3.getArrayTimetableDays().get(0).getDate(), 1, 1, sqlConnectionManager);
        assertEquals(2, resourcesBlocked.getRefResourceID()); // check if resources Blocked has Room 2
    }

    @Test
    @Order(8)
    void checkIfSwappingTwoHoursFailedWhenRoomIsBlocked() {
        // Day 4 08.04.2024 Timeslot 0 Lecturer 3 Room 3, Timeslots 1 and 2 are empty
        // CP2 Lecturer 1 Romm 3 ID 4
        CoursePass coursePass2 = new CoursePass(2L, sqlConnectionManager);
        Timetable timetable2 = new Timetable(coursePass2, sqlConnectionManager, resourceBundle);
        CoursePass coursePass3 = new CoursePass(3L, sqlConnectionManager);
        Timetable timetable3 = new Timetable(coursePass3, sqlConnectionManager, resourceBundle);
        LocalDate targetDate = timetable3.getArrayTimetableDays().get(4).getDate();
        try {
            timetable2.addSingleHour(new CoursepassLecturerSubject(4L, sqlConnectionManager, coursePass2),
                    new TimetableEntry(coursePass2, targetDate, 1, sqlConnectionManager));
            CoursepassLecturerSubject cls8 = new CoursepassLecturerSubject(8L, sqlConnectionManager, coursePass3);
            TimetableEntry sourceTimetableEntry = new TimetableEntry(
                    timetable3.getArrayTimetableDays().get(5).getArrayTimetableHours().get(0)
                            .getCoursepassLecturerSubject(),
                    targetDate, 0, sqlConnectionManager);
            TimetableEntry targetTimetableEntry = new TimetableEntry(
                    cls8,
                    targetDate, 1, sqlConnectionManager);
            timetable3.swapHours(sourceTimetableEntry, targetTimetableEntry);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Hours cant be swapped");
            return;
        }
        fail("Test should have Thrown an error");
    }

    @Test
    @Order(9)
    void checkIfSwappingTwoHoursFailedWhenLecturerIsBlocked() {
        // Day 4 08.04.2024 Timeslot 0 Lecturer 3 Room 3, Timeslots 1 and 2 are empty
        // CP2 Lecturer 3 Romm 1 ID 6
        CoursePass coursePass2 = new CoursePass(2L, sqlConnectionManager);
        Timetable timetable2 = new Timetable(coursePass2, sqlConnectionManager, resourceBundle);
        CoursePass coursePass3 = new CoursePass(3L, sqlConnectionManager);
        Timetable timetable3 = new Timetable(coursePass3, sqlConnectionManager, resourceBundle);
        LocalDate targetDate = timetable3.getArrayTimetableDays().get(4).getDate();
        try {
            timetable2.addSingleHour(new CoursepassLecturerSubject(6L, sqlConnectionManager, coursePass2),
                    new TimetableEntry(coursePass2, targetDate, 1, sqlConnectionManager));
            CoursepassLecturerSubject cls8 = new CoursepassLecturerSubject(8L, sqlConnectionManager, coursePass3);
            TimetableEntry sourceTimetableEntry = new TimetableEntry(
                    timetable3.getArrayTimetableDays().get(5).getArrayTimetableHours().get(0)
                            .getCoursepassLecturerSubject(),
                    targetDate, 0, sqlConnectionManager);
            TimetableEntry targetTimetableEntry = new TimetableEntry(
                    cls8,
                    targetDate, 1, sqlConnectionManager);
            timetable3.swapHours(sourceTimetableEntry, targetTimetableEntry);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Hours cant be swapped");
            return;
        }
        fail("Test should have Thrown an error");
    }

    @Test
    @Order(10)
    void checkIfAddingAnHourFailedWhenRoomIsBlocked() {
        // Day 4 08.04.2024 Timeslot 0 Lecturer 3 Room 3 ID 7, Timeslots 1 and 2 are
        // empty
        // CP2 Lecturer 1 Romm 3 ID 4
        CoursePass coursePass2 = new CoursePass(2L, sqlConnectionManager);
        Timetable timetable2 = new Timetable(coursePass2, sqlConnectionManager, resourceBundle);
        CoursePass coursePass3 = new CoursePass(3L, sqlConnectionManager);
        Timetable timetable3 = new Timetable(coursePass3, sqlConnectionManager, resourceBundle);
        LocalDate targetDate = timetable3.getArrayTimetableDays().get(4).getDate();
        try {
            timetable2.addSingleHour(new CoursepassLecturerSubject(4L, sqlConnectionManager, coursePass2),
                    new TimetableEntry(coursePass2, targetDate, 1, sqlConnectionManager));
            CoursepassLecturerSubject cls7 = new CoursepassLecturerSubject(7L, sqlConnectionManager, coursePass3);

            TimetableEntry targetTimetableEntry = new TimetableEntry(
                    cls7,
                    targetDate, 1, sqlConnectionManager);
            timetable3.addSingleHour(cls7, targetTimetableEntry);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Error Placing Hour");
            return;
        }
        fail("Test should have Thrown an error");
    }

    @Test
    @Order(11)
    void checkIfAddingAnHourFailedWhenLecturerIsBlocked() {
        // Day 4 08.04.2024 Timeslot 0 Lecturer 3 Room 3, Timeslots 1 and 2 are empty
        // CP2 Lecturer 3 Romm 1 ID 6
        CoursePass coursePass2 = new CoursePass(2L, sqlConnectionManager);
        Timetable timetable2 = new Timetable(coursePass2, sqlConnectionManager, resourceBundle);
        CoursePass coursePass3 = new CoursePass(3L, sqlConnectionManager);
        Timetable timetable3 = new Timetable(coursePass3, sqlConnectionManager, resourceBundle);
        LocalDate targetDate = timetable3.getArrayTimetableDays().get(4).getDate();
        try {
            timetable2.addSingleHour(new CoursepassLecturerSubject(6L, sqlConnectionManager, coursePass2),
                    new TimetableEntry(coursePass2, targetDate, 1, sqlConnectionManager));
            CoursepassLecturerSubject cls7 = new CoursepassLecturerSubject(7L, sqlConnectionManager, coursePass3);

            TimetableEntry targetTimetableEntry = new TimetableEntry(
                    cls7,
                    targetDate, 1, sqlConnectionManager);
            timetable3.addSingleHour(cls7, targetTimetableEntry);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Error Placing Hour");
            return;
        }
        fail("Test should have Thrown an error");
    }

    @AfterAll
    static void removeDB() {
        DeleteDbFiles.execute("./", "h2Test", false);
    }
}
