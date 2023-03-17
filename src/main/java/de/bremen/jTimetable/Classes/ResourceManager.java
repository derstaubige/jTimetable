package de.bremen.jTimetable.Classes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.time.ZoneId;
import java.util.*;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.*;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Class can generate a timetable for a coursePass and manages the resources that are necessary for this (lecturer,
 * coursePassLecturerSubject).
 *
 * @author Arne Czyborra, Loreen Roose
 */
public class ResourceManager {
    /**
     * List with the working days between two different dates in which lessons can be placed.
     */
    private ArrayList<TimetableDay> arrayTimetableDays;
    /**
     * List with CoursePassLecturerSubjects that need to be placed are in.
     */
    private ArrayList<CoursepassLecturerSubject> arrayCoursePassLecturerSubjectStack;
    /**
     * Pointer to the current position in arrayCoursePassLecturerSubjectStack.
     */
    private int positionInCoursePassLecturerSubjectStack;
    /**
     * Pointer to temporarily save the position this.arrayCoursePassLecturerSubjectStack to mark the starting point
     * of looping over the stack.
     */
    private int tmpPositionInCoursePassLecturerSubjectStack;
    /**
     * Maximum index of this.arrayCoursePassLecturerSubjectStack.
     */
    private int maxCoursePassLecturerSubjectStack;
    /**
     *
     */
    private int coursePassHours;

    //TODO generateInitialTimetable doesn't check correctly whether Lecturer is free

    /**
     * Generates an initial timetable for the given coursePass. Therefor it evaluates the hours the course needs to
     * attend to and plans them according to the lecturers' availability.
     *
     * @param coursePass for whom the timetable is generated
     */
    public void generateInitialTimetable(CoursePass coursePass) {
        LocalDate startDate = coursePass.getStart();
        LocalDate endDate = coursePass.getEnd();
        this.coursePassHours = 0;
        int workingDays;
        int workingHours;
        int maxTimeslotsPerDay = 3;

        try {
            SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
            ArrayList<SQLConnectionManagerValues> SQLValues =
                    new ArrayList<>(Collections.singleton(new SQLValueLong(coursePass.getId())));
            ResultSet rs = sqlConnectionManager.select(
                    "Select * from T_TIMETABLES where REFCOURSEPASS = ?;",
                    SQLValues);

            //If the ResultSet is empty, the method next will return false, if it returns true
            // elements for the given id where found, and we should not generate a new timetable
            if (!rs.next()) {
                //Order subjects by should hours descending,
                // build stack of hours
                // count total hours, TODO this is the only out one out of the three we actually do here
                for (int i = 0; i < coursePass.arrayCoursePassLecturerSubject.size(); i++) {
                    coursePassHours += coursePass.arrayCoursePassLecturerSubject.get(i).shouldHours;
                }

                //Check how many hours are in the coursePass object and if we need to add more hours
                // (more than 3 per day)
                arrayTimetableDays = getWorkingDaysBetweenTwoDates(startDate, endDate);
                workingDays = arrayTimetableDays.size();
                workingHours = workingDays * maxTimeslotsPerDay;

                //Check if we have to add timeslots to fit all hours
                while (workingHours < this.coursePassHours) {
                    //We have to Add Timeslots per Day if we have more shouldHours than timeslots
                    maxTimeslotsPerDay++;
                    workingHours = workingDays * maxTimeslotsPerDay;
                }

                //Start at the beginning of the stack to fit all hours into timetable
                this.positionInCoursePassLecturerSubjectStack = 0;
                this.arrayCoursePassLecturerSubjectStack =
                        coursePass.getArrayCoursePassLecturerSubject();
                //Define last index of the stack
                this.maxCoursePassLecturerSubjectStack =
                        this.arrayCoursePassLecturerSubjectStack.size();
                if (this.maxCoursePassLecturerSubjectStack > 0) {
                    this.maxCoursePassLecturerSubjectStack--;
                }

                //TODO if we still have shouldHours we need to add a timeslot
                // --> Problem: do we need to check whether timeslot is already blocked or is it possible to
                // just override?
                //TODO doesn't work
                while (this.coursePassHours > 0) {
                    planLessons(maxTimeslotsPerDay);
                    if (this.coursePassHours > 0) {
                        maxTimeslotsPerDay++;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println(
                    "No initial timetable could be generated for CoursePass with the id: " +
                            coursePass.getId() + " because the database query failed.");
            e.printStackTrace();
        }
    }

    /**
     * Method plans the timetable for a coursePass by checking the lecturers availability and saving
     * entries in the database table T_TIMETABLES.
     *
     * @param maxTimeslotsPerDay maximum timeslots that can be set for one day
     */
    private void planLessons(int maxTimeslotsPerDay) {
        //Try to place subject
        // --> check if the lecturer and the room is free for this date / hour, if not try next subject
        for (int idxDay = 0; idxDay < this.arrayTimetableDays.size(); idxDay++) {
            //Loop through all timeslots of this day
            for (int idxTimeslot = 0; idxTimeslot < maxTimeslotsPerDay; idxTimeslot++) {
                //Save current position in stack
                this.tmpPositionInCoursePassLecturerSubjectStack =
                        this.positionInCoursePassLecturerSubjectStack;

                if (evaluateCoursePassLecturerSubject(idxDay, idxTimeslot)) {
                    //We found a matching coursePassLecturerSubject object
                    LocalDate timetableDay = this.arrayTimetableDays.get(idxDay).getDate();
                    Long refCoursePassID = this.arrayCoursePassLecturerSubjectStack.get
                            (this.positionInCoursePassLecturerSubjectStack).coursepass.getId();
                    Long refCoursePassLecturerSubjectId =
                            this.arrayCoursePassLecturerSubjectStack.get
                                    (this.positionInCoursePassLecturerSubjectStack).getId();
                    //TODO: if we want to also manage the rooms we could do it here
                    Long refRoomId = 0L;
                    Long refLecturerId = this.arrayCoursePassLecturerSubjectStack.get
                            (this.positionInCoursePassLecturerSubjectStack).getLecturer().getId();
                    Long refSubjectId = this.arrayCoursePassLecturerSubjectStack.get
                            (this.positionInCoursePassLecturerSubjectStack).getSubject().getId();

                    //Write to timetable
                    setEntryInTimetable(timetableDay, refCoursePassID,
                            refCoursePassLecturerSubjectId, refRoomId,
                            refLecturerId, refSubjectId, idxTimeslot);
                    //If a lesson is inserted in the database there is one hour less to plan
                    this.coursePassHours--;
                    //Block lecturer and room
                    ResourcesBlocked.setResourcesBlocked(refLecturerId, ResourceNames.LECTURER,
                            ResourceNames.LECTURER.toString(), timetableDay, timetableDay,
                            idxTimeslot, idxTimeslot);
                    ResourcesBlocked.setResourcesBlocked(refRoomId, ResourceNames.ROOM
                            , ResourceNames.LECTURER.toString(), timetableDay, timetableDay,
                            idxTimeslot, idxTimeslot);

                    //Add to the is hours count
                    //TODO method to increment in coursePassLecturerSubject
                    this.arrayCoursePassLecturerSubjectStack.get
                            (this.positionInCoursePassLecturerSubjectStack).planedHours++;

                    //Set pointer to the next subject, so they are alternating in the timetable
                    if (this.positionInCoursePassLecturerSubjectStack <
                            this.maxCoursePassLecturerSubjectStack) {
                        this.positionInCoursePassLecturerSubjectStack++;
                    } else {
                        this.positionInCoursePassLecturerSubjectStack = 0;
                    }
                    //TODO necessary? Because we reset this in the beginning of the loop
                    this.tmpPositionInCoursePassLecturerSubjectStack =
                            this.positionInCoursePassLecturerSubjectStack;
                } else {
                    //We didn't find a matching coursePassLecturerSubject, free time?!
                    LocalDate timetableDay = this.arrayTimetableDays.get(idxDay).getDate();
                    Long refCoursePassID = this.arrayCoursePassLecturerSubjectStack.get
                            (this.positionInCoursePassLecturerSubjectStack).getCoursepass().getId();
                    //Set an "empty"/free time object in this slot
                    Long refCoursePassLecturerSubjectId = 0L;
                    Long refRoomId = 0L;
                    Long refLecturerId = 0L;
                    Long refSubjectId = 0L;
                    //Write to timetable
                    setEntryInTimetable(timetableDay, refCoursePassID,
                            refCoursePassLecturerSubjectId, refRoomId, refLecturerId,
                            refSubjectId, idxTimeslot);
                }
            }

        }
    }

    /**
     * Check whether there is an entry for the current coursePassLecturerSubject object at this date
     * in this timeslot in the database because that would mean, that the lecturer is not available
     * because they are already teaching this coursePass.
     *
     * @param idxDay   index of current date in this.arrayTimetableDays
     * @param timeslot timeslot to be checked
     * @return true if this lecturer teaches this coursePass at this date in this timeslot, fales if not
     * @throws SQLException is thrown if database query fails
     */
    private boolean isLessonAlreadySet(int idxDay, int timeslot) throws SQLException {
        long refCoursePassLecturerSubjectID =
                this.arrayCoursePassLecturerSubjectStack.get
                        (this.positionInCoursePassLecturerSubjectStack).getId();
        LocalDate timetableDay = this.arrayTimetableDays.get(idxDay).getDate();

        //Database query
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues =
                new ArrayList<>();

        SQLValues.add(new SQLValueDate(timetableDay));
        SQLValues.add(new SQLValueLong(refCoursePassLecturerSubjectID));
        SQLValues.add(new SQLValueInt(timeslot));

        ResultSet rs = sqlConnectionManager.execute(
                "Select * from T_TIMETABLES where TIMETABLEDAY = '?' and REFCOURSEPASSLECTURERSUBJECT = ? and timeslot = ? ",
                SQLValues);
        //rs.next() returns true if select query has result, lesson is available if rs is empty
        return !rs.next();
    }

    /**
     * Inserts a timetable entry into the database table T_TIMETABLES.
     *
     * @param timetableDay                   date for which the entry is set
     * @param refCoursePassID                reference to the coursePass that this entry is for
     * @param refCoursePassLecturerSubjectID reference to CoursePassLecturerSubject
     *                                       (which CoursePass needs to attend how many lessons of
     *                                       which subject taught by which lecturer)
     * @param refRoomId                      reference to the room that this lesson takes place in
     * @param refLecturerId                  reference to the lecturer that teaches during this entry
     * @param refSubjectId                   reference to the subject that is taught in this entry
     * @param timeslot                       timeslot this lesson is set in
     */
    private void setEntryInTimetable(LocalDate timetableDay, Long refCoursePassID,
                                     Long refCoursePassLecturerSubjectID, Long refRoomId,
                                     Long refLecturerId, Long refSubjectId, int timeslot) {
        try {
            SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
            ArrayList<SQLConnectionManagerValues> SQLValues =
                    new ArrayList<>();

            SQLValues.add(new SQLValueDate(timetableDay));
            SQLValues.add(new SQLValueLong(refCoursePassID));
            SQLValues.add(new SQLValueLong(refCoursePassLecturerSubjectID));
            SQLValues.add(new SQLValueLong(refRoomId));
            SQLValues.add(new SQLValueLong(refLecturerId));
            SQLValues.add(new SQLValueLong(refSubjectId));
            SQLValues.add(new SQLValueInt(timeslot));

            sqlConnectionManager.execute(
                    "Insert Into T_TIMETABLES (TIMETABLEDAY, REFCOURSEPASS, REFCOURSEPASSLECTURERSUBJECT, " +
                            "REFROOMID, REFLECTURER, REFSUBJECT, TIMESLOT) values (?, ?, ?, ?, ?, ?, ?)",
                    SQLValues);
        } catch (SQLException e) {
            System.err.println(
                    "Setting a new timetable entry into the database table T_TIMETABLES wasn't successful.");
            e.printStackTrace();
        }
    }

    /**
     * Runs through the entire stack of CoursePassLecturerSubjects until it does or does not find a matching object
     * that can be placed into the given timeslot. Meaning the lecturer is available and there are hours for this
     * subject left to plan.
     *
     * @param idxDay      index of the day in arrayTimetableDays at which we want to plan the lesson
     * @param idxTimeslot timeslot at which the lesson is supposed to be planned
     * @return true if lecturer is available and there are hours left to plan for the current coursePassLecturerSubject
     */
    private boolean evaluateCoursePassLecturerSubject(int idxDay, int idxTimeslot) {
        Long tmpShouldHours = this.arrayCoursePassLecturerSubjectStack.get
                (this.positionInCoursePassLecturerSubjectStack).getShouldHours();
        Long tmpPlanedHours = this.arrayCoursePassLecturerSubjectStack.get
                (this.positionInCoursePassLecturerSubjectStack).getPlanedHours();
        Long tmpIsHours = this.arrayCoursePassLecturerSubjectStack.get
                (this.positionInCoursePassLecturerSubjectStack).getIsHours();

        //If method returns true the lesson is already set with
        //TODO technically no new timetable entry necessary
        // but needs to be checked for every coursePassLecturerSubjectObject
        try {
            if (!isLessonAlreadySet(idxDay, idxTimeslot)) {
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Calling method isLessonAlreadySet failed in method " +
                    "evaluateCoursePassLecturerSubject. For lecturer id: " +
                    this.arrayCoursePassLecturerSubjectStack.get
                            (positionInCoursePassLecturerSubjectStack).lecturer.getId() +
                    " at date: " +
                    arrayTimetableDays.get(idxDay).getDate().toString() + " and timeslot: " +
                    idxTimeslot + " .");
            e.printStackTrace();
        }

        //Check if current coursePassLecturerSubject fits
        try {
            if (Lecturer.checkLecturerAvailability(this.arrayCoursePassLecturerSubjectStack.get
                            (positionInCoursePassLecturerSubjectStack).getLecturer().getId(),
                    arrayTimetableDays.get(idxDay).getDate(), idxTimeslot) &&
                    tmpShouldHours > (tmpIsHours + tmpPlanedHours)) {
                //Lecturer is Available and there are hours left to plan
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Calling method checkLecturerAvailability failed in method " +
                    "evaluateCoursePassLecturerSubject. For lecturer id: " +
                    this.arrayCoursePassLecturerSubjectStack.get
                            (positionInCoursePassLecturerSubjectStack).lecturer.getId() +
                    " at date: " +
                    arrayTimetableDays.get(idxDay).getDate().toString() + " and timeslot: " +
                    idxTimeslot + " .");
            e.printStackTrace();
        }

        //Check if we rolled over our stack size and start at 0 if we did, if we are not at the end of our stack set
        // pointer forward
        if (this.positionInCoursePassLecturerSubjectStack <
                this.maxCoursePassLecturerSubjectStack) {
            this.positionInCoursePassLecturerSubjectStack++;
        } else {
            this.positionInCoursePassLecturerSubjectStack = 0;
        }

        //We rolled over and tried every coursePassLecturerSubject in the stack and nothing fitted
        // ... that means free time and ensures whe don't create and endless loop
        if (this.tmpPositionInCoursePassLecturerSubjectStack ==
                this.positionInCoursePassLecturerSubjectStack) {
            return false;
        }

        return evaluateCoursePassLecturerSubject(idxDay, idxTimeslot);
    }

    /**
     * Method calculates a list of every working day between start and end date.
     * TODO Excluding start and end date.? --> tut es ja rein optisch nicht
     * <a href="https://stackoverflow.com/questions/4600034/calculate-number-of-weekdays-between-two-dates-in-java">...</a>
     * <a href="https://schegge.de/2020/01/kalenderspielereien-mit-java/#:~:text=Feiertage%20mit%20einem%20festen%20Datum,%2D%2D01%2D01%22">...</a>
     *
     * @param localStartDate start date of the timeframe
     * @param localEndDate   end date of the timeframe
     * @return ArrayList with all the workingDays between start and end date (excluding weekends and holidays)
     */
    public ArrayList<TimetableDay> getWorkingDaysBetweenTwoDates(LocalDate localStartDate,
                                                                 LocalDate localEndDate) {
        ArrayList<TimetableDay> arrayTimetableDays = new ArrayList<>();
        //default time zone
        ZoneId defaultZoneId = ZoneId.systemDefault();

        //local date + atStartOfDay() + default time zone + toInstant() = Date
        Date startDate = Date.from(localStartDate.atStartOfDay(defaultZoneId).toInstant());
        Date endDate = Date.from(localEndDate.atStartOfDay(defaultZoneId).toInstant());
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);

        //Return 0 if start and end are the same
        if (startCal.getTimeInMillis() == endCal.getTimeInMillis()) {
            return arrayTimetableDays;
        }

        if (startCal.getTimeInMillis() > endCal.getTimeInMillis()) {
            startCal.setTime(endDate);
            endCal.setTime(startDate);
        }

        do {//excluding start date
            if (startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY &&
                    startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY &&
                    !this.isHoliday(LocalDateTime.ofInstant(startCal.toInstant(), defaultZoneId)
                            .toLocalDate())) {

                arrayTimetableDays.add(new TimetableDay
                        (LocalDateTime.ofInstant(startCal.toInstant(), defaultZoneId)
                                .toLocalDate(), 4));
            }
            startCal.add(Calendar.DAY_OF_MONTH, 1);
        } while (startCal.getTimeInMillis() <= endCal.getTimeInMillis()); //excluding end date

        return arrayTimetableDays;
    }

    /**
     * Method to calculate the date of Easter sunday.
     *
     * @param year for which easter is calculated
     * @return the date of ester sunday for the given year
     */
    private LocalDate getEasterDate(int year) {
        int a = year % 19;
        int b = year % 4;
        int c = year % 7;
        int H1 = year / 100;
        int H2 = year / 400;
        int N = 4 + H1 - H2;
        int M = 15 + H1 - H2 - (8 * H1 + 13) / 25;
        int d = (19 * a + M) % 30;
        int e = (2 * b + 4 * c + 6 * d + N) % 7;
        int f = (c + 11 * d + 22 * e) / 451;
        int tage = 22 + d + e - 7 * f;
        return LocalDate.of(year, 3, 1).plus(tage - 1, DAYS);
    }

    /**
     * Method helps isHoliday to define whether a date is a holiday by creating a map with fixed holidays and
     * calculating variable holidays using the method getEasterDate.
     *
     * @param year for which the holidays are calculated
     * @return map with the date of the holiday as the key and name of the holiday as the value
     */
    private Map<LocalDate, String> getHolidaysMap(int year) {
        Map<LocalDate, String> holidays = new HashMap<>();
        holidays.put(MonthDay.parse("--01-01").atYear(year), "Neujahr");
        holidays.put(getEasterDate(year).plus(-2, DAYS), "Karfreitag");
        holidays.put(getEasterDate(year).plus(0, DAYS), "Ostersonntag");
        holidays.put(getEasterDate(year).plus(1, DAYS), "Ostermonntag");
        holidays.put(getEasterDate(year).plus(39, DAYS), "Himmelfahrt");
        holidays.put(getEasterDate(year).plus(49, DAYS), "Pfingsten");
        holidays.put(MonthDay.parse("--05-01").atYear(year), "Tag der Arbeit");
        //TODO why august?
        holidays.put(MonthDay.parse("--08-03").atYear(year), "Tag der Einheit");
        holidays.put(MonthDay.parse("--08-31").atYear(year), "Reformationstag");
        holidays.put(MonthDay.parse("--12-24").atYear(year), "Heiligabend");
        holidays.put(MonthDay.parse("--12-25").atYear(year), "1. Weihnachtstag");
        holidays.put(MonthDay.parse("--12-26").atYear(year), "2. Weihnachtstag");
        holidays.put(MonthDay.parse("--12-31").atYear(year), "Sylvester");
        return holidays;
    }

    /**
     * Determines whether the given date is a holiday in this year by using the method getHolidaysMap.
     *
     * @param date is checked whether it's a holiday
     * @return true if date is holiday, false if not
     */
    public boolean isHoliday(LocalDate date) {
        return getHolidaysMap(date.getYear()).containsKey(date);
    }
}
