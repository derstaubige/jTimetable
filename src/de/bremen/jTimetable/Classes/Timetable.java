package de.bremen.jTimetable.Classes;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.*;
import javafx.stage.FileChooser;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

public class Timetable  {
    ArrayList<TimetableDay> arrayTimetableDays;
    Coursepass coursepass;
    Lecturer lecturer;

    public Timetable(Coursepass coursepass){
        this.coursepass = coursepass;
        this.update();
    }

    public Timetable(Lecturer lecturer){
        this.lecturer = lecturer;
        try {
            this.arrayTimetableDays = lecturer.getTimetable();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void update(){
        try{
            this.arrayTimetableDays = this.coursepass.getTimetable();
        }catch (Exception e){
            //ToDO: Errorhandling
            e.printStackTrace();
        }
    }

    public ArrayList<TimetableDay> getArrayTimetableDays() {
        return arrayTimetableDays;
    }

    public void exportTimetableToFile(){
        //https://docs.oracle.com/javafx/2/ui_controls/file-chooser.htm
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
//        fileChooser.showOpenDialog();
    }

    public void deleteTimetable(){
        // Loop through all Days and Hours and Delete Resourceblocked and Timetable
        for (TimetableDay arrayTimetableDay : arrayTimetableDays) {
             for(TimetableHour timetableHour : arrayTimetableDay.getArrayTimetableDay()){
                    deleteResourceblocked(timetableHour.coursepassLecturerSubject.getLecturerID(),"Lecturer", arrayTimetableDay.date,
                            arrayTimetableDay.date, timetableHour.getTimeslot(), timetableHour.getTimeslot());
                 deleteResourceblocked(timetableHour.coursepassLecturerSubject.getLecturerID(),"Room", arrayTimetableDay.date,
                         arrayTimetableDay.date, timetableHour.getTimeslot(), timetableHour.getTimeslot());
             }
        }
        deleteTimetables(coursepass.getId());
    }

    public void addSingleHour(TimetableHour timetableHour, LocalDate day, int timeslot){
        for(TimetableDay timetableDay : getArrayTimetableDays()){
            if(timetableDay.getDate() == day){
                timetableDay.getArrayTimetableDay().set(timeslot, timetableHour);
                break;
            }
        }

        //save the change in the timetable table
        try {
            SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
            ArrayList<SQLConnectionManagerValues> SQLValues =
                    new ArrayList<SQLConnectionManagerValues>();

            SQLValues.add(new SQLValueDate(day));
            SQLValues.add(new SQLValueLong(timetableHour.getCoursepassLecturerSubject().getCoursepass().getId()));
            SQLValues.add(new SQLValueLong(timetableHour.getCoursepassLecturerSubject().getId()));
            SQLValues.add(new SQLValueLong(timetableHour.getCoursepassLecturerSubject().getRoom().getId()));
            SQLValues.add(new SQLValueLong(timetableHour.getCoursepassLecturerSubject().getLecturerID()));
            SQLValues.add(new SQLValueLong(timetableHour.getCoursepassLecturerSubject().getSubject().getId()));
            SQLValues.add(new SQLValueInt(timeslot));

            sqlConnectionManager.execute(
                    "Insert Into T_TIMETABLES (TIMETABLEDAY, REFCOURSEPASS, REFCOURSEPASSLECTURERSUBJECT, REFROOMID, REFLECTURER, REFSUBJECT, TIMESLOT) values (?, ?, ?, ?, ?, ?, ?)",
                    SQLValues);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void deleteResourceblocked(long resourceID, String resourceName, LocalDate startdate, LocalDate endDate, int startTimeslot, int endTimeslot){
//TODO ENUM ResourceName
        try {
            SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

            SQLValues.add(new SQLValueLong(resourceID));
            SQLValues.add(new SQLValueString(resourceName));
            SQLValues.add(new SQLValueDate(startdate));
            SQLValues.add(new SQLValueDate(endDate));
            SQLValues.add(new SQLValueInt(startTimeslot));
            SQLValues.add(new SQLValueInt(endTimeslot));
            sqlConnectionManager.execute("DELETE FROM T_RESOURCESBLOCKED where REFRESOURCEID = ? and " +
                    "RESOURCENAME = ? and STARTDATE = ? and ENDDATE = ? and STARTTIMESLOT = ? and ENDTIMESLOT = ?", SQLValues);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteTimetables(long coursepassID){
        try {
            SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

            SQLValues.add(new SQLValueLong(coursepassID));
            sqlConnectionManager.execute("DELETE FROM T_Timetables where refCoursepass = ?",SQLValues);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteTimetable(long coursepassID, LocalDate timetableday, int timeslot){
        try {
            SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

            SQLValues.add(new SQLValueLong(coursepassID));
            SQLValues.add(new SQLValueDate(timetableday));
            SQLValues.add(new SQLValueInt(timeslot));
            sqlConnectionManager.execute("DELETE FROM T_Timetables where refCoursepass = ? AND timetableday = ? " +
                    "AND timeslot = ?",SQLValues);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
