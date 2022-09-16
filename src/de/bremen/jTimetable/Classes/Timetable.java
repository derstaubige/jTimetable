package de.bremen.jTimetable.Classes;

import java.util.ArrayList;

public class Timetable  {
    ArrayList<ArrayList<TimetableDay>> arrayTimetableDays;
    Coursepass coursepass;

    public Timetable(Coursepass coursepass){
        this.coursepass = coursepass;
        this.arrayTimetableDays = this.coursepass.getTimetable();
    }
}
