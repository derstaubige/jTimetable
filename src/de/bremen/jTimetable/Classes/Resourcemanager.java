package de.bremen.jTimetable.Classes;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.*;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class Resourcemanager {
    public ArrayList<TimetableDay> arrayTimetabledays;
    ArrayList<CoursepassLecturerSubject> arraycoursepasslecturersubject;
    int positionInCoursepassLecturerSubjectStack;
    int tmppositionInCoursepassLecturerSubjectStack;
    int maxCoursepassLecturerSubjectStack;

    public void generateInitialTimetable(Coursepass coursepass) throws SQLException {
        LocalDate startdate = coursepass.start;
        LocalDate enddate = coursepass.end;
        int Coursepasshours = 0;
        int WorkingDays = 0;
        int WorkingHours = 0;

        //TODO test
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues =
                new ArrayList<>(Collections.singleton(new SQLValueLong(coursepass.id)));
        ResultSet rs = sqlConnectionManager.select("Select * from T_TIMETABLES where REFCOURSEPASS = ?;",
                SQLValues);
        //If the ResultSet is empty, the method next will return false, if it returns true (elements
        // for the given id where found it should not generate a new timetable
        if (!rs.next()) {
//            coursepass.getCoursepassLecturerSubjects(); //should be obsolote now
            // order subjects by should hours descending, count total hours, build stack of hours
            for (int i = 0; i < coursepass.arraycoursepasslecturersubject.size(); i++) {
                Coursepasshours += coursepass.arraycoursepasslecturersubject.get(i).shouldHours;
            }

//        System.out.println(Coursepasshours);
            // check how many hours are in the coursepass object and if we need to add more hours (more than 3 per day)
            // iterate over every day between startdate and enddate / hour
            arrayTimetabledays = getWorkingDaysBetweenTwoDates(startdate, enddate);
            WorkingDays = arrayTimetabledays.size();
            //ToDo: Hardcoded default Value for the Slots per Day. Change it.
            WorkingHours = WorkingDays * 3;

            //TODO condition should be: WorkingHours < CoursePassHours --> we want to know whether less
            // timeslots are available than needed
            if (WorkingHours < Coursepasshours) {
                //ToDO: Add Custom Exception.
                //TODO have 4 hours a day to have enough timeslots
                System.out.println("Oh oh there are to many Hours for this.");
            }

            this.positionInCoursepassLecturerSubjectStack = 0;
            this.arraycoursepasslecturersubject = coursepass.arraycoursepasslecturersubject;
            this.maxCoursepassLecturerSubjectStack = this.arraycoursepasslecturersubject.size();
            if (this.maxCoursepassLecturerSubjectStack > 0) {
                this.maxCoursepassLecturerSubjectStack--;
            }
            // try to place subject, check if the lecturer and the room is free for this date / hour. if not try next subject
            for (int idxDay = 0; idxDay < arrayTimetabledays.size(); idxDay++) {
                // loop through all timeslots of this day
                for (int idxTimeslot = 0;
                     idxTimeslot < arrayTimetabledays.get(idxDay).arrayTimetableDay.size();
                     idxTimeslot++) {
                    this.tmppositionInCoursepassLecturerSubjectStack =
                            this.positionInCoursepassLecturerSubjectStack;
                    if (EvaluateCoursepassLecturerSubject(idxDay, idxTimeslot)) {
                        //we found a matching coursepasslecturersubject object
                        LocalDate Timetableday = this.arrayTimetabledays.get(idxDay).date;
                        Long refcoursepassID = this.arraycoursepasslecturersubject.get(
                                this.positionInCoursepassLecturerSubjectStack).coursepass.id;
                        Long refCoursepassLecturerSubjectId =
                                this.arraycoursepasslecturersubject.get(
                                        this.positionInCoursepassLecturerSubjectStack).id;
                        //ToDO: if we want to also manage the rooms we could do it here
                        Long refRoomId = 0L;
                        Long refLecturerId = this.arraycoursepasslecturersubject.get(
                                this.positionInCoursepassLecturerSubjectStack).lecturer.id;
                        Long refSubjectId = this.arraycoursepasslecturersubject.get(
                                this.positionInCoursepassLecturerSubjectStack).subject.id;

                        System.out.printf("%s, %s, %s\n", Timetableday, idxTimeslot,
                                this.arraycoursepasslecturersubject.get(
                                        this.positionInCoursepassLecturerSubjectStack).subject.caption);
                        //write to timetable
                        setEntryInTimetable(Timetableday, refcoursepassID,
                                refCoursepassLecturerSubjectId, refRoomId, refLecturerId,
                                refSubjectId, idxTimeslot);
                        //block lecturer and room
                        setResourcesBlocked(refLecturerId, "Lecturer", "", Timetableday,
                                Timetableday, idxTimeslot, idxTimeslot);
                        setResourcesBlocked(refRoomId, "Room", "", Timetableday, Timetableday,
                                idxTimeslot, idxTimeslot);
                        //add to the is hours count
                        this.arraycoursepasslecturersubject.get(
                                this.positionInCoursepassLecturerSubjectStack).planedHours++;
                    } else {
                        //we didnt find a matching coursepasslecturersubject, freetime?!
                        LocalDate Timetableday = this.arrayTimetabledays.get(idxDay).date;
                        Long refcoursepassID = this.arraycoursepasslecturersubject.get(
                                this.positionInCoursepassLecturerSubjectStack).coursepass.id;
                        Long refCoursepassLecturerSubjectId =  0L;
                        //ToDO: if we want to also manage the rooms we could do it here
                        Long refRoomId = 0L;
                        Long refLecturerId = 0L;
                        Long refSubjectId = 0L;
                        //write to timetable
                        setEntryInTimetable(Timetableday, refcoursepassID,
                                refCoursepassLecturerSubjectId, refRoomId, refLecturerId,
                                refSubjectId, idxTimeslot);
//                        System.out.printf("%s, %s, FREETIME!\n",
//                                this.arrayTimetabledays.get(idxDay).date, idxTimeslot);
                    }
                }
//             a subject should only occupie a day. the next day would be nice to have another subject
                if (this.positionInCoursepassLecturerSubjectStack <
                        this.maxCoursepassLecturerSubjectStack) {
                    this.positionInCoursepassLecturerSubjectStack++;
                } else {
                    this.positionInCoursepassLecturerSubjectStack = 0;
                }
            }
        }
    }

    public void setResourcesBlocked(Long REFRESOURCEID, String RESOURCENAME, String DESCRIPTION,
                                    LocalDate STARTDATE, LocalDate ENDDATE, int STARTTIMESLOT,
                                    int ENDTIMESLOT) throws SQLException {
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues =
                new ArrayList<SQLConnectionManagerValues>();

        SQLValues.add(new SQLValueLong(REFRESOURCEID));
        SQLValues.add(new SQLValueString(RESOURCENAME));
        SQLValues.add(new SQLValueString(DESCRIPTION));
        SQLValues.add(new SQLValueDate(STARTDATE));
        SQLValues.add(new SQLValueDate(ENDDATE));
        SQLValues.add(new SQLValueInt(STARTTIMESLOT));
        SQLValues.add(new SQLValueInt(ENDTIMESLOT));

        ResultSet rs = sqlConnectionManager.execute(
                "Insert Into T_RESOURCESBLOCKED  ( REFRESOURCEID, RESOURCENAME, DESCRIPTION, STARTDATE, ENDDATE, STARTTIMESLOT, ENDTIMESLOT) values (?, ?, ?, ?, ?, ?, ?)",
                SQLValues);
    }

    private void setEntryInTimetable(LocalDate TimetableDay, Long refcoursepassID,
                                     Long refCoursepassLecturerSubjectId, Long refRoomId,
                                     Long refLecturerId, Long refSubjectId, int timeslot)
            throws SQLException {
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues =
                new ArrayList<SQLConnectionManagerValues>();

        SQLValues.add(new SQLValueDate(TimetableDay));
        SQLValues.add(new SQLValueLong(refcoursepassID));
        SQLValues.add(new SQLValueLong(refCoursepassLecturerSubjectId));
        SQLValues.add(new SQLValueLong(refRoomId));
        SQLValues.add(new SQLValueLong(refLecturerId));
        SQLValues.add(new SQLValueLong(refSubjectId));
        SQLValues.add(new SQLValueInt(timeslot));

        ResultSet rs = sqlConnectionManager.execute(
                "Insert Into T_TIMETABLES (TIMETABLEDAY, REFCOURSEPASS, REFCOURSEPASSLECTURERSUBJECT, REFROOMID, REFLECTURER, REFSUBJECT, TIMESLOT) values (?, ?, ?, ?, ?, ?, ?)",
                SQLValues);
    }

    private boolean EvaluateCoursepassLecturerSubject(int idxDay, int idxTimeslot)
            throws SQLException {
        //runs through the stack of CoursepassLecturerSubjects until it does or does not find a matching Object
        // also checks if there is more hours that can be planed (shouldhours < ishours + planedhours)

        Long tmpshouldhours = this.arraycoursepasslecturersubject.get(
                this.positionInCoursepassLecturerSubjectStack).shouldHours;
        Long tmpplanedhours = this.arraycoursepasslecturersubject.get(
                this.positionInCoursepassLecturerSubjectStack).planedHours;
        Long tmpishours = this.arraycoursepasslecturersubject.get(
                this.positionInCoursepassLecturerSubjectStack).isHours;

        if (this.arraycoursepasslecturersubject.get(
                positionInCoursepassLecturerSubjectStack).lecturer.checkLecturerAvailability(this.arraycoursepasslecturersubject.get(
                        positionInCoursepassLecturerSubjectStack).lecturer.id,
                arrayTimetabledays.get(idxDay).date, idxTimeslot) &&
                tmpshouldhours > (tmpishours + tmpplanedhours)) {
            //Lecturer is Available and there are hours left to plan
            return true;
        }

        //check if we rolled over our stack size and start at 0 if we did
        if (this.positionInCoursepassLecturerSubjectStack <
                this.maxCoursepassLecturerSubjectStack) {
            this.positionInCoursepassLecturerSubjectStack++;
        } else {
            this.positionInCoursepassLecturerSubjectStack = 0;
        }

        if (this.tmppositionInCoursepassLecturerSubjectStack ==
                this.positionInCoursepassLecturerSubjectStack) {
            // we rolled over and tryed every coursepasslecturersubject in the stack and nothing fitted... that means freetime
            return false;
        }

        return EvaluateCoursepassLecturerSubject(idxDay, idxTimeslot);
        //return false;
    }



    public ArrayList<TimetableDay> getWorkingDaysBetweenTwoDates(LocalDate localstartDate,
                                                                 LocalDate localendDate) {
//        https://stackoverflow.com/questions/4600034/calculate-number-of-weekdays-between-two-dates-in-java
        //ToDO: Substract Holidays from Workingdays

        ArrayList<TimetableDay> arrayTimetabledays = new ArrayList<>();
        //default time zone
        ZoneId defaultZoneId = ZoneId.systemDefault();

        //local date + atStartOfDay() + default time zone + toInstant() = Date
        Date startDate = Date.from(localstartDate.atStartOfDay(defaultZoneId).toInstant());
        Date endDate = Date.from(localendDate.atStartOfDay(defaultZoneId).toInstant());
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);

//        int workDays = 0;

        //Return 0 if start and end are the same
        if (startCal.getTimeInMillis() == endCal.getTimeInMillis()) {
            return arrayTimetabledays;
        }

        if (startCal.getTimeInMillis() > endCal.getTimeInMillis()) {
            startCal.setTime(endDate);
            endCal.setTime(startDate);
        }

        do {
            //excluding start date

            if (startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY &&
                    startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                //TODO could add a fourth timeslot her by using the constructor of TimetableDay that allows to set the
                // number of timeslots
                arrayTimetabledays.add(new TimetableDay(
                        LocalDateTime.ofInstant(startCal.toInstant(), defaultZoneId)
                                .toLocalDate()));
                //                ++workDays;
            }
            startCal.add(Calendar.DAY_OF_MONTH, 1);
        } while (startCal.getTimeInMillis() <= endCal.getTimeInMillis()); //excluding end date

        return arrayTimetabledays;
    }
}
