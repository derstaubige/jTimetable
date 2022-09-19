package de.bremen.jTimetable.Classes;

import java.util.ArrayList;

public class Timetable  {
    ArrayList<TimetableDay> arrayTimetableDays;
    Coursepass coursepass;

    public Timetable(Coursepass coursepass){
        this.coursepass = coursepass;
        try{
            this.arrayTimetableDays = this.coursepass.getTimetable();
        }catch (Exception e){
            //ToDO: Errorhandling
            System.out.println(e);
        }
    }

    public ArrayList<TimetableDay> getArrayTimetableDays() {
        return arrayTimetableDays;
    }

}
