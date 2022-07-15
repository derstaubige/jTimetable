package de.bremen.jTimetable.Classes;

import java.sql.Array;
import java.time.LocalDate;
import java.util.ArrayList;

public class TimetableDay {
    LocalDate date;
    public int timeslots;
    public ArrayList<ArrayList<TimetableHour>> arrayTimetableDay;

    public TimetableDay(LocalDate date){
        // a default day has 3 timeslots
        this(date, 3);
    }

    public TimetableDay(LocalDate date, int timeslots){
        this.date = date;
        this.timeslots = timeslots;
        arrayTimetableDay = new ArrayList<>(this.timeslots);
        for(int i=0; i < timeslots;i++){
            arrayTimetableDay.add(new ArrayList<>());
        }
    }

    public void addToSlot(int timeslot, CoursepassLecturerSubject coursepassLecturerSubject){
        //TODO: Build Custom Exception if we cant place the TimetableHour in this Slot
        if(!this.checkIfSlotIsFree(timeslot)){
            return;
        }
        this.arrayTimetableDay.get(timeslot).add(new TimetableHour(timeslot, coursepassLecturerSubject));
    }

    private boolean checkIfSlotIsFree(int timeslot){
        return this.arrayTimetableDay.get(timeslot).isEmpty();
    }
}
