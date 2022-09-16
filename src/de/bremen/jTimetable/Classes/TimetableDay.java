package de.bremen.jTimetable.Classes;

import java.sql.Array;
import java.time.LocalDate;
import java.util.ArrayList;

public class TimetableDay {
    LocalDate date;
    public int timeslots;
    //ToDo: Arraylist Arraylist
    public ArrayList<TimetableHour> arrayTimetableDay;

    public TimetableDay(LocalDate date){
        // a default day has 3 timeslots
        this(date, 3);
    }

    public TimetableDay(LocalDate date, int timeslots){
        this.date = date;
        this.timeslots = timeslots;
        arrayTimetableDay = new ArrayList<>(this.timeslots);
        for(int i=0; i < timeslots;i++){
            addTimeslot();
        }
    }

    public void addToSlot(int timeslot, CoursepassLecturerSubject coursepassLecturerSubject){
        //TODO: Build Custom Exception if we cant place the TimetableHour in this Slot
        if(!this.checkIfSlotIsFree(timeslot)){
            return;
        }
        this.arrayTimetableDay.get(timeslot) = new TimetableHour(timeslot, coursepassLecturerSubject);
    }

    private boolean checkIfSlotIsFree(int timeslot){
        return this.arrayTimetableDay.get(timeslot).isEmpty();
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getTimeslots() {
        return timeslots;
    }

    public void setTimeslots(int timeslots) {
        this.timeslots = timeslots;
    }

    public ArrayList<TimetableHour> getArrayTimetableDay() {
        return arrayTimetableDay;
    }

    public void setArrayTimetableDay(ArrayList<TimetableHour> arrayTimetableDay) {
        this.arrayTimetableDay = arrayTimetableDay;
    }

}
