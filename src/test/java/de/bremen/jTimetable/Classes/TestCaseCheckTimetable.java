package de.bremen.jTimetable.Classes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;

import org.h2.tools.DeleteDbFiles;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;

@TestMethodOrder(OrderAnnotation.class)
public class TestCaseCheckTimetable {
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

    /**
     * Creates Testdatabase with 3 Random Timetables, 3 Lecturers, 3 Rooms, 3
     * Subjects, 3 Courses of Study
     * , 3 Coursepasses and 9 CoursepassLecturerSubjects, 3 per Timetable/Coursepass
     */
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

            Timetable timetable1 = new Timetable(new CoursePass(1L, sqlConnectionManager), sqlConnectionManager,
                    resourceBundle);
            timetable1.distributeUnplanedHours();
            ArrayList<TimetableDay> listTimetableHours1 = timetable1.getArrayTimetableDays();
            Timetable timetable2 = new Timetable(new CoursePass(2L, sqlConnectionManager), sqlConnectionManager,
                    resourceBundle);
            timetable2.distributeUnplanedHours();
            ArrayList<TimetableDay> listTimetableHours2 = timetable2.getArrayTimetableDays();
            Timetable timetable3 = new Timetable(new CoursePass(3L, sqlConnectionManager), sqlConnectionManager,
                    resourceBundle);
            timetable3.distributeUnplanedHours();
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
    void deleteHourFromTimetableCP2Day1Timeslot0() {
        try {

            Timetable timetable2 = new Timetable(new CoursePass(2L, sqlConnectionManager), sqlConnectionManager,
                    resourceBundle);

            timetable2.getArrayTimetableDays().get(0).getArrayTimetableHours().get(0).coursepassLecturerSubject
                    .deleteCLS(
                            timetable2.getArrayTimetableDays().get(0).getDate(),
                            timetable2.getArrayTimetableDays().get(0).getArrayTimetableHours().get(0).getTimeslot());

            timetable2.updateCoursePassTimetable();

            assertEquals(0L, timetable2.getArrayTimetableDays()
                    .get(0).getArrayTimetableHours().get(0).getCoursepassLecturerSubject().getLecturerID());
        } catch (Exception e) {
            fail(e.getStackTrace().toString());
        }
    }

    @Test
    @Order(3)
    void checkIfDeletingTimetableWorks() {
        Timetable timetable1 = new Timetable(new CoursePass(1L, sqlConnectionManager), sqlConnectionManager,
                resourceBundle);
        Timetable timetable2 = new Timetable(new CoursePass(2L, sqlConnectionManager), sqlConnectionManager,
                resourceBundle);
        Timetable timetable3 = new Timetable(new CoursePass(3L, sqlConnectionManager), sqlConnectionManager,
                resourceBundle);

        timetable1.deleteTimetable();
        timetable2.deleteTimetable();
        timetable3.deleteTimetable();

        assertEquals(0, timetable1.getArrayTimetableDays().get(0).getArrayTimetableHours().get(0)
                .getCoursepassLecturerSubject().getId());
        assertEquals(0, timetable2.getArrayTimetableDays().get(0).getArrayTimetableHours().get(0)
                .getCoursepassLecturerSubject().getId());
        assertEquals(0, timetable3.getArrayTimetableDays().get(0).getArrayTimetableHours().get(0)
                .getCoursepassLecturerSubject().getId());

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
    @Order(4)
    void createKnownState() {
        // 1 Week 01.04.2024 - 05.04.2024 , 01.04. is an Holiday
        // 2 Week 08.04.2024 - 12.04.2024
        // CoursePass 1 L3R1, L2R2, L1R3
        // CoursePass 2 L1R3, L2R2, L3R1
        // CoursePass 3 L3R3, L2R2, L1R1
        try {

            CoursePass coursePass1 = new CoursePass(1, sqlConnectionManager);
            CoursePass coursePass2 = new CoursePass(2, sqlConnectionManager);
            CoursePass coursePass3 = new CoursePass(3, sqlConnectionManager);

            Timetable timetable1 = new Timetable(coursePass1, sqlConnectionManager, resourceBundle);
            Timetable timetable2 = new Timetable(coursePass2, sqlConnectionManager, resourceBundle);
            Timetable timetable3 = new Timetable(coursePass3, sqlConnectionManager, resourceBundle);

            CoursepassLecturerSubject cls1L3R1 = new CoursepassLecturerSubject(1L, sqlConnectionManager, coursePass1);
            CoursepassLecturerSubject cls1L2R2 = new CoursepassLecturerSubject(2L, sqlConnectionManager, coursePass1);
            CoursepassLecturerSubject cls1L1R3 = new CoursepassLecturerSubject(3L, sqlConnectionManager, coursePass1);

            CoursepassLecturerSubject cls2L1R3 = new CoursepassLecturerSubject(4L, sqlConnectionManager, coursePass2);
            CoursepassLecturerSubject cls2L2R2 = new CoursepassLecturerSubject(5L, sqlConnectionManager, coursePass2);
            CoursepassLecturerSubject cls2L3R1 = new CoursepassLecturerSubject(6L, sqlConnectionManager, coursePass2);

            CoursepassLecturerSubject cls3L3R3 = new CoursepassLecturerSubject(7L, sqlConnectionManager, coursePass3);
            CoursepassLecturerSubject cls3L2R2 = new CoursepassLecturerSubject(8L, sqlConnectionManager, coursePass3);
            CoursepassLecturerSubject cls3L1R1 = new CoursepassLecturerSubject(9L, sqlConnectionManager, coursePass3);

            timetable1.addSingleHour(cls1L3R1,
                    new TimetableEntry(coursePass1, LocalDate.of(2024, 04, 04), 0, sqlConnectionManager));
            timetable1.addSingleHour(cls1L2R2,
                    new TimetableEntry(coursePass1, LocalDate.of(2024, 04, 04), 1, sqlConnectionManager));
            timetable1.addSingleHour(cls1L1R3,
                    new TimetableEntry(coursePass1, LocalDate.of(2024, 04, 04), 2, sqlConnectionManager));

            timetable1.addSingleHour(cls1L1R3,
                    new TimetableEntry(coursePass1, LocalDate.of(2024, 04, 02), 0, sqlConnectionManager));

            timetable2.addSingleHour(cls2L3R1,
                    new TimetableEntry(coursePass2, LocalDate.of(2024, 04, 02), 0, sqlConnectionManager));
            timetable2.addSingleHour(cls2L2R2,
                    new TimetableEntry(coursePass2, LocalDate.of(2024, 04, 02), 1, sqlConnectionManager));
            timetable2.addSingleHour(cls2L1R3,
                    new TimetableEntry(coursePass2, LocalDate.of(2024, 04, 02), 2, sqlConnectionManager));

            timetable3.addSingleHour(cls3L2R2,
                    new TimetableEntry(coursePass3, LocalDate.of(2024, 04, 02), 0, sqlConnectionManager));

            timetable3.addSingleHour(cls3L3R3,
                    new TimetableEntry(coursePass3, LocalDate.of(2024, 04, 03), 0, sqlConnectionManager));
            timetable3.addSingleHour(cls3L2R2,
                    new TimetableEntry(coursePass3, LocalDate.of(2024, 04, 03), 1, sqlConnectionManager));
            timetable3.addSingleHour(cls3L1R1,
                    new TimetableEntry(coursePass3, LocalDate.of(2024, 04, 03), 2, sqlConnectionManager));
            assertTrue(true);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            fail("There was an error that shouldnt have happend" + e.getMessage());
        }

    }

    @Test
    @Order(5)
    void moveHourFromTimetable3D2T0toD1T3() {
        try {

            Timetable timetable3 = new Timetable(new CoursePass(3L, sqlConnectionManager), sqlConnectionManager,
                    resourceBundle);
            TimetableHour source = timetable3.getArrayTimetableDays().get(2).getArrayTimetableHours().get(0);
            TimetableHour target = timetable3.getArrayTimetableDays().get(1).getArrayTimetableHours().get(3);

            TimetableEntry sourceTimetableEntry = new TimetableEntry(
                    source.getCoursepassLecturerSubject(),
                    timetable3.getArrayTimetableDays().get(2).getDate(), source.getTimeslot(), sqlConnectionManager);
            TimetableEntry targetTimetableEntry = new TimetableEntry(
                    target.getCoursepassLecturerSubject(),
                    timetable3.getArrayTimetableDays().get(1).getDate(), target.getTimeslot(), sqlConnectionManager);

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
    @Order(6)
    void checkIfCP3Day1Timeslot0CouldBeDeleted() {
        try {

            ResourcesBlocked resourcesBlocked;
            Timetable timetable3 = new Timetable(new CoursePass(3L, sqlConnectionManager), sqlConnectionManager,
                    resourceBundle);
            TimetableHour deleteMe = timetable3.getArrayTimetableDays().get(1).getArrayTimetableHours().get(0);
            deleteMe.getCoursepassLecturerSubject().deleteCLS(timetable3.getArrayTimetableDays().get(1).getDate(),
                    0); // delete Day 1 Timeslot 0
            timetable3.updateCoursePassTimetable();

            assertEquals(0, timetable3.getArrayTimetableDays().get(1).getArrayTimetableHours().get(0)
                    .getCoursepassLecturerSubject().getLecturer().getId()); // check if Day 1 Slot 0 has LecturerID 0
            assertEquals(0, timetable3.getArrayTimetableDays().get(1).getArrayTimetableHours().get(0)
                    .getCoursepassLecturerSubject().getRoom().getId()); // check if Day 1 Slot 0 has RoomID 0

            resourcesBlocked = new ResourcesBlocked(0L, ResourceNames.LECTURER,
                    timetable3.getArrayTimetableDays().get(1).getDate(),
                    timetable3.getArrayTimetableDays().get(1).getDate(), 0, 0, sqlConnectionManager);
            assertEquals(0, resourcesBlocked.getRefResourceID()); // check if resources Blocked has Lecturer 0

            resourcesBlocked = new ResourcesBlocked(0L, ResourceNames.ROOM,
                    timetable3.getArrayTimetableDays().get(1).getDate(),
                    timetable3.getArrayTimetableDays().get(1).getDate(), 0, 0, sqlConnectionManager);
            assertEquals(0, resourcesBlocked.getRefResourceID()); // check if resources Blocked has Room 0
        } catch (Exception e) {
            fail(e.getStackTrace().toString());
        }
    }

    @Test
    @Order(7)
    void checkIfWeCouldAddAnHourCP3Day0Timeslot2() {
        try {

            Timetable timetable3 = new Timetable(new CoursePass(3L, sqlConnectionManager), sqlConnectionManager,
                    resourceBundle);
            ResourcesBlocked resourcesBlocked;
            TimetableHour target = timetable3.getArrayTimetableDays().get(0).getArrayTimetableHours().get(2);
            LocalDate targetDate = timetable3.getArrayTimetableDays().get(0).getDate(); // should bei 02.04.2024

            CoursepassLecturerSubject source = new CoursepassLecturerSubject(8L, sqlConnectionManager,
                    target.getCoursepassLecturerSubject().getCoursepass()); // L2R2

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
    @Order(8)
    void checkIfDistributingRemainingHoursWorks() {
        Timetable timetable2 = new Timetable(new CoursePass(2L, sqlConnectionManager), sqlConnectionManager,
                resourceBundle);
        ResourcesBlocked resourcesBlocked;

        timetable2.distributeUnplanedHours();

        // run through all cls and check if there are no more unplaned hours
        for (CoursepassLecturerSubject cls : timetable2.getCoursepass().getArrayCoursePassLecturerSubject()) {
            cls.updateallHours();
            assertEquals(0, cls.getUnplanedHours());
        }
    }

    @Test
    @Order(9)
    void checkIfSwappingHourFailedWhenRoomIsBlocked() {
        // Timetable 1 02.04.2024 Timeslot 0 L1R3 CLS1
        // Timetable 2 02.04.2024 Timeslot 0 L2R2 CLS8
        // Timetable 3 03.04.2024 Timeslot 0 L3R3 CLS7

        CoursePass coursePass3 = new CoursePass(3L, sqlConnectionManager);
        Timetable timetable3 = new Timetable(coursePass3, sqlConnectionManager, resourceBundle);
        LocalDate targetDate = timetable3.getArrayTimetableDays().get(0).getDate();
        LocalDate sourceDate = timetable3.getArrayTimetableDays().get(1).getDate();

        checkIfDeletingTimetableWorks();
        createKnownState();

        try {
            TimetableEntry L3R3 = new TimetableEntry(coursePass3, sourceDate, 0, sqlConnectionManager);
            TimetableEntry L2R2 = new TimetableEntry(coursePass3, targetDate, 0, sqlConnectionManager);
            timetable3.swapHours(L3R3, L2R2);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Hours cant be swapped");
            return;
        }
        fail("Test should have Thrown an error");
    }

    @Test
    @Order(10)
    void checkIfSwappingTwoHoursFailedWhenLecturerIsBlocked() {
        // Timetable 1 02.04.2024 Timeslot 0 L1R3 CLS1
        // Timetable 2 02.04.2024 Timeslot 0 L2R2 CLS8
        // Timetable 3 03.04.2024 Timeslot 2 L1R3 CLS9

        checkIfDeletingTimetableWorks();
        createKnownState();

        CoursePass coursePass3 = new CoursePass(3L, sqlConnectionManager);
        Timetable timetable3 = new Timetable(coursePass3, sqlConnectionManager, resourceBundle);
        LocalDate sourceDate = timetable3.getArrayTimetableDays().get(1).getDate();
        LocalDate targetDate = timetable3.getArrayTimetableDays().get(0).getDate();
        try {
            TimetableEntry L1R3 = new TimetableEntry(coursePass3, sourceDate, 2, sqlConnectionManager);
            TimetableEntry L2R2 = new TimetableEntry(coursePass3, targetDate, 0, sqlConnectionManager);
            timetable3.swapHours(L1R3, L2R2);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Hours cant be swapped");
            return;
        }
        fail("Test should have Thrown an error");
    }

    @Test
    @Order(11)
    void checkIfAddingAnHourFailedWhenRoomIsBlocked() {
        // Timetable 1 02.04.2024 Timeslot 0 L1R3 CLS1
        // Timetable 2 02.04.2024 Timeslot 0 L2R2 CLS8
        // Timetable 3 03.04.2024 Timeslot 0 L3R3 CLS7

        checkIfDeletingTimetableWorks();
        createKnownState();

        CoursePass coursePass3 = new CoursePass(3L, sqlConnectionManager);
        Timetable timetable3 = new Timetable(coursePass3, sqlConnectionManager, resourceBundle);
        LocalDate targetDate = timetable3.getArrayTimetableDays().get(0).getDate();
        try {
            timetable3.addSingleHour(new CoursepassLecturerSubject(7L, sqlConnectionManager, coursePass3),
                    new TimetableEntry(coursePass3, targetDate, 0, sqlConnectionManager));
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Error Placing Hour. Target isnt Free 2024-04-02 0 Subject 1");
            return;
        }
        fail("Test should have Thrown an error");
    }

    @Test
    @Order(12)
    void checkIfAddingAnHourFailedWhenLecturerIsBlocked() {
        // Timetable 1 02.04.2024 Timeslot 0 L1R3 CLS1
        // Timetable 2 02.04.2024 Timeslot 0 L2R2 CLS8
        // Timetable 3 03.04.2024 Timeslot 3 L1R1 CLS9

        checkIfDeletingTimetableWorks();
        createKnownState();

        CoursePass coursePass3 = new CoursePass(3L, sqlConnectionManager);
        Timetable timetable3 = new Timetable(coursePass3, sqlConnectionManager, resourceBundle);
        LocalDate targetDate = timetable3.getArrayTimetableDays().get(0).getDate(); // 02.04.2024
        try {
            CoursepassLecturerSubject cls9 = new CoursepassLecturerSubject(9L, sqlConnectionManager, coursePass3);

            TimetableEntry targetTimetableEntry = new TimetableEntry(
                    cls9,
                    targetDate, 0, sqlConnectionManager);
            timetable3.addSingleHour(cls9, targetTimetableEntry);
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Error Placing Hour. Target isnt Free 2024-04-02 0 Subject 3");
            return;
        }
        fail("Test should have Thrown an error");
    }

    // setBlockingFreetext
    @Test
    @Order(13)
    void checkIfAddingBlockingHoursWorks() {
        
        checkIfDeletingTimetableWorks();
        createKnownState();

        CoursePass coursePass3 = new CoursePass(3L, sqlConnectionManager);
        Timetable timetable3 = new Timetable(coursePass3, sqlConnectionManager, resourceBundle);
        LocalDate from = timetable3.getArrayTimetableDays().get(0).getDate();
        LocalDate till = timetable3.getArrayTimetableDays().get(8).getDate();
        String freeText = "Random Blocking";

        try {
            timetable3.setBlockingFreetext(from, till, freeText);
        } catch (Exception e) {
            fail("Error that should not have happend. " + e.getLocalizedMessage());
        }

        // check if Blocking is set
        TimetableEntry timetableEntry = new TimetableEntry(coursePass3, from, 0, sqlConnectionManager);
        assertEquals(freeText, timetableEntry.getBlockingFreetext());
        timetableEntry = new TimetableEntry(coursePass3, till, 3, sqlConnectionManager);
        assertEquals(freeText, timetableEntry.getBlockingFreetext());
    }

    // check if exam is working
    @Test
    @Order(14)
    void checkIfAddingExamWorks() {
        // Timetable 3 L1R1 CLS9
        
        checkIfDeletingTimetableWorks();
        createKnownState();

        CoursePass coursePass3 = new CoursePass(3L, sqlConnectionManager);
        Timetable timetable3 = new Timetable(coursePass3, sqlConnectionManager, resourceBundle);
        LocalDate date = timetable3.getArrayTimetableDays().get(0).getDate();
        TimetableEntry targetTimetableEntry = new TimetableEntry(
                coursePass3, date, 0,
                sqlConnectionManager);
        targetTimetableEntry.setExam(true);
        targetTimetableEntry.save();

        TimetableEntry checkTimetableEntry = new TimetableEntry(coursePass3, date, 0, sqlConnectionManager);
        assertTrue(checkTimetableEntry.isExam());
    }

    @Test
    @Order(15)
    void checkIfDistributionMethodeWorks(){
                
        checkIfDeletingTimetableWorks();
        createKnownState();

        CoursePass coursePass3 = new CoursePass(3L, sqlConnectionManager);
        Timetable timetable3 = new Timetable(coursePass3, sqlConnectionManager, resourceBundle);
        try {
            CoursepassLecturerSubject cls3L3R3 = new CoursepassLecturerSubject(7L, sqlConnectionManager, coursePass3);
            cls3L3R3.setDistributionMethode(CoursepassLecturerSubjectDistributionmethode.DOUBLEHOURS);
            cls3L3R3.setShouldHours(10L);
            cls3L3R3.save();
            CoursepassLecturerSubject cls3L2R2 = new CoursepassLecturerSubject(8L, sqlConnectionManager, coursePass3);
            cls3L2R2.setDistributionMethode(CoursepassLecturerSubjectDistributionmethode.FULLDAY);
            cls3L2R2.setShouldHours(10L);
            cls3L2R2.save();
            CoursepassLecturerSubject cls3L1R1 = new CoursepassLecturerSubject(9L, sqlConnectionManager, coursePass3);
            cls3L1R1.setDistributionMethode(CoursepassLecturerSubjectDistributionmethode.NORMAL);
            cls3L1R1.setShouldHours(10L);
            cls3L1R1.save();            
            
            timetable3.deleteTimetable();
            timetable3.getCoursepass().updateCoursePassLecturerSubjects();
            timetable3.distributeUnplanedHours();
        } catch (Exception e) {
            fail("There shouldnt be an error. " + e.getLocalizedMessage());
        }
        System.out.println("jo");
    }

    @AfterAll
    static void removeDB() {
        DeleteDbFiles.execute("./", "h2Test", false);
    }
}
