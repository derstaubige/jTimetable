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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class Resourcemanager {
    public ArrayList<TimetableDay> arrayTimetabledays;
    ArrayList<CoursepassLecturerSubject> arrayCoursepassLecturerSubject;
    int positionInCoursepassLecturerSubjectStack;
    int tmppositionInCoursepassLecturerSubjectStack;
    int maxCoursepassLecturerSubjectStack;
    private SQLConnectionManager sqlConnectionManager;
    private Properties properties = new Properties();

    public Resourcemanager(SQLConnectionManager sqlConnectionManager) {
        setSqlConnectionManager(sqlConnectionManager);
        try {
            FileResourcesUtils fileResourcesUtils = new FileResourcesUtils();
            properties.load(new BufferedReader(
                    new InputStreamReader(fileResourcesUtils.getFileFromResourceAsStream("/Config.properties"))));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateInitialTimetable(CoursePass coursepass) throws SQLException {
        LocalDate startdate = coursepass.getStart();
        LocalDate enddate = coursepass.getEnd();
        int Coursepasshours = 0;
        int WorkingDays = 0;
        int WorkingHours = 0;
        int MaxTimeslotsperDay = Integer
                .parseInt(properties.getProperty("maxTimetableSlotsUsedForInitialTimetable", "5")); // How many Timeslot
                                                                                                    // should be used
                                                                                                    // max per Day
        int timeslotsPerDay = Integer.parseInt(properties.getProperty("maxTimetableSlotsPerDay", "5"));

        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>(
                Collections.singleton(new SQLValueLong(coursepass.getId())));
        ResultSet rs = sqlConnectionManager.select("Select * from T_TIMETABLES where REFCOURSEPASS = ?;",
                SQLValues);
        // If the ResultSet is empty, the method next will return false, if it returns
        // true
        // (elements for the given id where found, it should not generate a new
        // timetable)
        if (!rs.next()) {
            // coursepass.getCoursepassLecturerSubjects(); //should be obsolote now
            // order subjects by should hours descending, count total hours, build stack of
            // hours
            for (int i = 0; i < coursepass.arrayCoursePassLecturerSubject.size(); i++) {
                Coursepasshours += coursepass.arrayCoursePassLecturerSubject.get(i).shouldHours;
            }

            // check if enddate is after startdate, if not exit funktion and inform user
            // the value 0 if the argument Date is equal to this Date; a value less than 0
            // if this Date is before the Date argument; and a value greater than 0 if this
            // Date is after the Date argument.
            if (enddate.compareTo(startdate) < 0) {
                // sqlConnectionManager.close();
                return;
            }

            // check how many hours are in the coursepass object and if we need to add more
            // hours (more than 3 per day)
            // iterate over every day between startdate and enddate / hour
            arrayTimetabledays = getWorkingDaysBetweenTwoDates(startdate, enddate);

            // check if there are days in with there could be education and inform user
            if (arrayTimetabledays.size() == 0) {
                // sqlConnectionManager.close();
                return;
            }
            WorkingDays = arrayTimetabledays.size();
            WorkingHours = WorkingDays * MaxTimeslotsperDay;

            if (Boolean.parseBoolean(properties.getProperty("extendGivenSlotsforInitialTimetable", "true"))) {
                while (WorkingHours < Coursepasshours) {
                    // We have to Add Timeslots per Day if we have more shouldhours than timeslots
                    MaxTimeslotsperDay++;
                    WorkingHours = WorkingDays * MaxTimeslotsperDay;
                }
            }

            this.positionInCoursepassLecturerSubjectStack = 0;
            this.arrayCoursepassLecturerSubject = coursepass.arrayCoursePassLecturerSubject;
            // sort the arraycoursepasslecturersubject by unplaned Hours
            Collections.sort(this.arrayCoursepassLecturerSubject,
                    (o1, o2) -> o2.getUnplanedHours().compareTo(o1.getUnplanedHours()));
            this.maxCoursepassLecturerSubjectStack = this.arrayCoursepassLecturerSubject.size();
            if (this.maxCoursepassLecturerSubjectStack > 0) {
                this.maxCoursepassLecturerSubjectStack--;
            }
            // try to place subject, check if the lecturer and the room is free for this
            // date / hour. if not try next subject
            for (int idxDay = 0; idxDay < arrayTimetabledays.size(); idxDay++) {
                // loop through all timeslots of this day
                for (int idxTimeslot = 0; idxTimeslot < Math.max(MaxTimeslotsperDay, timeslotsPerDay); idxTimeslot++) {
                    this.tmppositionInCoursepassLecturerSubjectStack = this.positionInCoursepassLecturerSubjectStack;

                    if (EvaluateCoursepassLecturerSubject(idxDay, idxTimeslot) && idxTimeslot < MaxTimeslotsperDay) {
                        // we found a matching coursepasslecturersubject object
                        LocalDate timetableday = this.arrayTimetabledays.get(idxDay).getDate();

                        TimetableEntry timetableEntry = new TimetableEntry(this.arrayCoursepassLecturerSubject.get(
                                this.positionInCoursepassLecturerSubjectStack), timetableday, (Integer) idxTimeslot,
                                sqlConnectionManager);
                        timetableEntry.save();

                        // add to the is hours count
                        this.arrayCoursepassLecturerSubject.get(
                                this.positionInCoursepassLecturerSubjectStack).planedHours++;

                        Long tmpLastCLSID = this.arrayCoursepassLecturerSubject
                                .get(positionInCoursepassLecturerSubjectStack).getId();
                        // resort the arraycoursepasslecturersubject
                        Collections.sort(this.arrayCoursepassLecturerSubject,
                                (o1, o2) -> o2.getUnplanedHours().compareTo(o1.getUnplanedHours()));
                        if (tmpLastCLSID == this.arrayCoursepassLecturerSubject
                                .get(0).getId()) {
                            // its still the same cls ontop of the stack. we dont want 2 cls in a arrow
                            // reset the position in stack
                            this.tmppositionInCoursepassLecturerSubjectStack = 1;
                            this.positionInCoursepassLecturerSubjectStack = 1;
                        } else {
                            // reset the position in stack
                            this.tmppositionInCoursepassLecturerSubjectStack = 0;
                            this.positionInCoursepassLecturerSubjectStack = 0;
                        }
                    } else {
                        // we didnt find a matching coursepasslecturersubject, freetime?!
                        LocalDate timetableday = this.arrayTimetabledays.get(idxDay).getDate();
                        TimetableEntry timetableEntry = new TimetableEntry(
                                new CoursepassLecturerSubject(0L, sqlConnectionManager, coursepass),
                                timetableday, (Integer) idxTimeslot, sqlConnectionManager);
                        timetableEntry.save();
                    }
                }

            }
        }
        // sqlConnectionManager.close();
    }

    private boolean EvaluateCoursepassLecturerSubject(int idxDay, int idxTimeslot)
            throws SQLException {
        // runs through the stack of CoursepassLecturerSubjects until it does or does
        // not find a matching Object
        // also checks if there is more hours that can be planed (shouldhours < ishours
        // + planedhours)

        Long tmpshouldhours = this.arrayCoursepassLecturerSubject.get(
                this.positionInCoursepassLecturerSubjectStack).shouldHours;
        Long tmpplanedhours = this.arrayCoursepassLecturerSubject.get(
                this.positionInCoursepassLecturerSubjectStack).planedHours;
        Long tmpishours = this.arrayCoursepassLecturerSubject.get(
                this.positionInCoursepassLecturerSubjectStack).isHours;

        if (Lecturer.checkLecturerAvailability(this.arrayCoursepassLecturerSubject.get(
                positionInCoursepassLecturerSubjectStack).lecturer.getId(),
                arrayTimetabledays.get(idxDay).getDate(), idxTimeslot, getSqlConnectionManager())

                &&

                this.arrayCoursepassLecturerSubject.get(positionInCoursepassLecturerSubjectStack).getRoom()
                        .isRoomAvailable(
                                arrayTimetabledays.get(idxDay).getDate(), idxTimeslot)
                &&
                tmpshouldhours > (tmpishours + tmpplanedhours) &&
                arrayTimetabledays.get(idxDay).getDate().isAfter(this.arrayCoursepassLecturerSubject
                        .get(positionInCoursepassLecturerSubjectStack).getPlaceAfterDay())) {
            // Lecturer and Room are Available and there are hours left to plan
            return true;
        }

        // check if we rolled over our stack size and start at 0 if we did
        if (this.positionInCoursepassLecturerSubjectStack < this.maxCoursepassLecturerSubjectStack) {
            this.positionInCoursepassLecturerSubjectStack++;
        } else {
            this.positionInCoursepassLecturerSubjectStack = 0;
        }

        if (this.tmppositionInCoursepassLecturerSubjectStack == this.positionInCoursepassLecturerSubjectStack) {
            // we rolled over and tryed every coursepasslecturersubject in the stack and
            // nothing fitted... that means freetime
            return false;
        }

        return EvaluateCoursepassLecturerSubject(idxDay, idxTimeslot);
        // return false;
    }

    public ArrayList<TimetableDay> getWorkingDaysBetweenTwoDates(LocalDate localstartDate,
            LocalDate localendDate) {
        // https://stackoverflow.com/questions/4600034/calculate-number-of-weekdays-between-two-dates-in-java

        // https://schegge.de/2020/01/kalenderspielereien-mit-java/#:~:text=Feiertage%20mit%20einem%20festen%20Datum,%2D%2D01%2D01%22).

        ArrayList<TimetableDay> arrayTimetabledays = new ArrayList<>();
        // default time zone
        ZoneId defaultZoneId = ZoneId.systemDefault();

        // local date + atStartOfDay() + default time zone + toInstant() = Date
        Date startDate = Date.from(localstartDate.atStartOfDay(defaultZoneId).toInstant());
        Date endDate = Date.from(localendDate.atStartOfDay(defaultZoneId).toInstant());
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);

        // int workDays = 0;

        // Return 0 if start and end are the same
        if (startCal.getTimeInMillis() == endCal.getTimeInMillis()) {
            return arrayTimetabledays;
        }

        if (startCal.getTimeInMillis() > endCal.getTimeInMillis()) {
            startCal.setTime(endDate);
            endCal.setTime(startDate);
        }

        do {
            // excluding start date

            if (startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY &&
                    startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY &&
                    !this.isHoliday(LocalDateTime.ofInstant(startCal.toInstant(), defaultZoneId)
                            .toLocalDate())) {

                arrayTimetabledays.add(new TimetableDay(
                        LocalDateTime.ofInstant(startCal.toInstant(), defaultZoneId)
                                .toLocalDate(),
                        4, getSqlConnectionManager()));
                // ++workDays;
            }
            startCal.add(Calendar.DAY_OF_MONTH, 1);
        } while (startCal.getTimeInMillis() <= endCal.getTimeInMillis()); // excluding end date

        return arrayTimetabledays;
    }

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

    private Map<LocalDate, String> getHolydaysMap(int year) {
        Map<LocalDate, String> holidays = new HashMap<>();
        holidays.put(MonthDay.parse("--01-01").atYear(year), "Neujahr");
        holidays.put(getEasterDate(year).plus(-2, DAYS), "Karfreitag");
        holidays.put(getEasterDate(year).plus(0, DAYS), "Ostersonntag");
        holidays.put(getEasterDate(year).plus(1, DAYS), "Ostermonntag");
        holidays.put(getEasterDate(year).plus(39, DAYS), "Himmelfahrt");
        holidays.put(getEasterDate(year).plus(49, DAYS), "Pfingstsonntag");
        holidays.put(getEasterDate(year).plus(50, DAYS), "Pfingstmontag");
        holidays.put(MonthDay.parse("--05-01").atYear(year), "Tag der Arbeit");
        holidays.put(MonthDay.parse("--10-03").atYear(year), "Tag der Einheit");
        holidays.put(MonthDay.parse("--10-31").atYear(year), "Reformationstag");
        holidays.put(MonthDay.parse("--12-24").atYear(year), "Heiligabend");
        holidays.put(MonthDay.parse("--12-25").atYear(year), "1. Weihnachtstag");
        holidays.put(MonthDay.parse("--12-26").atYear(year), "2. Weihnachtstag");
        holidays.put(MonthDay.parse("--12-31").atYear(year), "Sylvester");
        return holidays;
    }

    public boolean isHoliday(LocalDate date) {
        return getHolydaysMap(date.getYear()).containsKey(date);
    }

    public SQLConnectionManager getSqlConnectionManager() {
        return sqlConnectionManager;
    }

    public void setSqlConnectionManager(SQLConnectionManager sqlConnectionManager) {
        this.sqlConnectionManager = sqlConnectionManager;
    }

}
