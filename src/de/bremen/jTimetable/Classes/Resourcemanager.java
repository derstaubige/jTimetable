package de.bremen.jTimetable.Classes;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;
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
    public void generateInitialTimetable( Coursepass coursepass){
        LocalDate startdate = coursepass.start;
        LocalDate enddate = coursepass.end;
        int Coursepasshours = 0;
        int WorkingDays = 0;
        int WorkingHours = 0;


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
        int positionInCoursepassLecturerSubjectStack = 0;
        int maxCoursepassLecturerSubjectStack = coursepass.arraycoursepasslecturersubject.size();
        // try to place subject, check if the lecturer and the room is free for this date / hour. if not try next subject
        for(int idxDay = 0; idxDay < arrayTimetabledays.size(); idxDay++){



            // a subject should only occupie a day. the next day would be nice to have another subject

            if(positionInCoursepassLecturerSubjectStack < maxCoursepassLecturerSubjectStack) {
                positionInCoursepassLecturerSubjectStack++;
            }else{
                positionInCoursepassLecturerSubjectStack = 0;
            }
        }
        // if placing is possible, remove hour from stack of hours

    }
    private boolean checkLecturerAvailability(long lecturerID) throws SQLException {
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();
        SQLValues.add(new SQLValueLong(lecturerID));

        ResultSet rs = sqlConnectionManager.select("Select * from T_RESOURCESBLOCKED where Resourcename = `Lecturer` and refresourceid = ?;", SQLValues);
        rs.first();

        this.id = rs.getLong("id");
        this.courseofstudy = new CourseofStudy(rs.getLong("REFCOURSEOFSTUDYID"));
        this.studysection = new StudySection(rs.getLong("REFSTUDYSECTIONID"));
        this.start = rs.getDate("start").toLocalDate();
        this.end = rs.getDate("end").toLocalDate();
        this.active = rs.getBoolean("active");
        this.description = rs.getString("description");
        this.room = new Room(rs.getLong("refRoomID"));
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
