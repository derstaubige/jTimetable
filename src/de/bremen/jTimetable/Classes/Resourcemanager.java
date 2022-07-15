package de.bremen.jTimetable.Classes;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueDate;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueLong;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Resourcemanager {
    public ArrayList<TimetableDay> arrayTimetabledays ;
    ArrayList<CoursepassLecturerSubject> arraycoursepasslecturersubject;
    int positionInCoursepassLecturerSubjectStack;
    int tmppositionInCoursepassLecturerSubjectStack;
    int maxCoursepassLecturerSubjectStack;
    public void generateInitialTimetable( Coursepass coursepass) throws SQLException{
        LocalDate startdate = coursepass.start;
        LocalDate enddate = coursepass.end;
        int Coursepasshours = 0;
        int WorkingDays = 0;
        int WorkingHours = 0;

        coursepass.getCoursepassLecturerSubjects();
        // order subjects by should hours descending, count total hours, build stack of hours
        for(int i = 0; i < coursepass.arraycoursepasslecturersubject.size(); i++){
            Coursepasshours += coursepass.arraycoursepasslecturersubject.get(i).shouldhours;
        }

//        System.out.println(Coursepasshours);
        // check how many hours are in the coursepass object and if we need to add more hours (more than 3 per day)
        // iterate over every day between startdate and enddate / hour
        arrayTimetabledays = getWorkingDaysBetweenTwoDates(startdate, enddate);
        WorkingDays = arrayTimetabledays.size();
        //ToDo: Hardcoded default Value for the Slots per Day. Change it.
        WorkingHours = WorkingDays * 3;

        if(WorkingHours > Coursepasshours){
            //ToDO: Add Custom Exception.
            System.out.println("Oh oh there are to many Hours for this.");
        }
//        System.out.println(WorkingDays);
        this.positionInCoursepassLecturerSubjectStack = 0;
        this.arraycoursepasslecturersubject = coursepass.arraycoursepasslecturersubject;
        this.maxCoursepassLecturerSubjectStack = this.arraycoursepasslecturersubject.size();
        if(this.maxCoursepassLecturerSubjectStack > 0){
            this.maxCoursepassLecturerSubjectStack--;
        }
        // try to place subject, check if the lecturer and the room is free for this date / hour. if not try next subject
        for(int idxDay = 0; idxDay < arrayTimetabledays.size(); idxDay++){
            // loop through all timeslots of this day
            for(int idxTimeslot = 0; idxTimeslot < arrayTimetabledays.get(idxDay).arrayTimetableDay.size(); idxTimeslot++){
                this.tmppositionInCoursepassLecturerSubjectStack = this.positionInCoursepassLecturerSubjectStack;
                if(EvaluateCoursepassLecturerSubject(idxDay, idxTimeslot)){
                    //we found a matching coursepasslecturersubject object
                    System.out.printf("%s, %s, %s\n", this.arrayTimetabledays.get(idxDay).date ,idxTimeslot, this.arraycoursepasslecturersubject.get(this.positionInCoursepassLecturerSubjectStack).subject.caption);
                }else{
                    //we didnt find a matching coursepasslecturersubject, freetime?!
                    System.out.println("FREETIME!");
                }
            }
//             a subject should only occupie a day. the next day would be nice to have another subject
            if(this.positionInCoursepassLecturerSubjectStack < this.maxCoursepassLecturerSubjectStack) {
                this.positionInCoursepassLecturerSubjectStack++;
            }else{
                this.positionInCoursepassLecturerSubjectStack = 0;
            }
        }
        // if placing is possible, remove hour from stack of hours

    }
    private boolean EvaluateCoursepassLecturerSubject(int idxDay, int idxTimeslot) throws SQLException{
        if(checkLecturerAvailability(this.arraycoursepasslecturersubject.get(positionInCoursepassLecturerSubjectStack).lecturer.id, arrayTimetabledays.get(idxDay).date, idxTimeslot)  ){
            return true;
        }

        if(this.positionInCoursepassLecturerSubjectStack < this.maxCoursepassLecturerSubjectStack) {
            this.positionInCoursepassLecturerSubjectStack++;
        }else{
            this.positionInCoursepassLecturerSubjectStack = 0;
        }

        if(this.tmppositionInCoursepassLecturerSubjectStack == this.positionInCoursepassLecturerSubjectStack){
            // we rolled over and tryed every coursepasslecturersubject in the stack and nothing fitted... that means freetime
            return false;
        }

        EvaluateCoursepassLecturerSubject(idxDay, idxTimeslot);
        return false;
    }
    public boolean checkLecturerAvailability(long lecturerID, LocalDate date, int timeslot) throws SQLException {
        LocalDate startdate;
        LocalDate enddate;
        Integer starttimeslot;
        Integer endtimeslot;

        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();
        SQLValues.add(new SQLValueLong(lecturerID));
        SQLValues.add(new SQLValueDate(date));
        SQLValues.add(new SQLValueDate(date));

        ResultSet rs = sqlConnectionManager.select("Select * from T_RESOURCESBLOCKED where Resourcename = 'Lecturer' and refresourceid = ? and STARTDATE <= ? and ENDDATE >= ?;", SQLValues);
        while(rs.next()){
            startdate = rs.getDate("startdate").toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            enddate = rs.getDate("enddate").toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            starttimeslot = rs.getInt("starttimeslot");
            endtimeslot = rs.getInt("endtimeslot");

            // if the date we want to check is the same date as the start of the blocked date range, we can check if the blocking starts after the timestamp we want to reserve
            if(startdate == date ){
                if( timeslot >= starttimeslot){
                    return false;
                }else{
                    continue;
                }
            }

            // if the date we want to check is the same date as the end of the blocked date range, we can check if the blocking ends before the timestamp we want to reserve
            if(enddate == date){
                if(timeslot <= endtimeslot){
                    return false;
                }else {
                    continue;
                }

            }

            return false;
        }
        // there is no blocking
        return true;
    }
    public ArrayList<TimetableDay> getWorkingDaysBetweenTwoDates(LocalDate localstartDate, LocalDate localendDate) {
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
            startCal.add(Calendar.DAY_OF_MONTH, 1);
            if (startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && startCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                arrayTimetabledays.add(new TimetableDay(LocalDateTime.ofInstant(startCal.toInstant(), defaultZoneId).toLocalDate()));
                //                ++workDays;
            }
        } while (startCal.getTimeInMillis() < endCal.getTimeInMillis()); //excluding end date

        return arrayTimetabledays;
    }
}
