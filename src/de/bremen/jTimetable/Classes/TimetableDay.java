package de.bremen.jTimetable.Classes;

import java.sql.Array;
import java.time.LocalDate;
import java.util.ArrayList;

public class TimetableDay {
    LocalDate date;
    public int timeslots;
    public ArrayList<TimetableHour> arrayTimetableDay;

    public TimetableDay(LocalDate date){
        // a default day has 3 timeslots
        this(date, 3);
    }

    public TimetableDay(LocalDate date, int timeslots){
        this.date = date;
        this.timeslots = timeslots;
        this.arrayTimetableDay = new ArrayList<>(this.timeslots);
        while (this.arrayTimetableDay.size() < this.timeslots){
            this.arrayTimetableDay.add(null);
        }
    }

    public void addToSlot(int timeslot, CoursepassLecturerSubject coursepassLecturerSubject){
        //TODO: Build Custom Exception if we cant place the TimetableHour in this Slot
        if(!this.checkIfSlotIsFree(timeslot)){
            return;
        }
        this.arrayTimetableDay.add(timeslot, new TimetableHour(timeslot, coursepassLecturerSubject));
    }

    private boolean checkIfSlotIsFree(int timeslot){
        try{
            if(this.arrayTimetableDay.get(timeslot) == null){
                return true;
            }else {
                return false;
            }
        }catch (IndexOutOfBoundsException e){
            return true;
        }

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
        while(this.arrayTimetableDay.size() <= timeslots) {
            try {
                this.arrayTimetableDay.add(new TimetableHour(this.arrayTimetableDay.size(), new CoursepassLecturerSubject(0L)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.timeslots = timeslots;
    }

    public ArrayList<TimetableHour> getArrayTimetableDay() {
        return arrayTimetableDay;
    }

    public void setArrayTimetableDay(ArrayList<TimetableHour> arrayTimetableDay) {
        this.arrayTimetableDay = arrayTimetableDay;
    }
    public TimetableHour getTimetableHourfromarrayTimetableDay(int timeslot){
        return arrayTimetableDay.get(timeslot);
    }

    public void removeTimetableHourfromarrayTimetableDay(int timeslot){
        try{
            arrayTimetableDay.set(timeslot, new TimetableHour(timeslot, new CoursepassLecturerSubject(0L)));
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void setTimetableHourfromarrayTimetableDay(int timeslot, TimetableHour timetableHour){
        arrayTimetableDay.set(timeslot, timetableHour);
        timetableHour.setTimeslot(timeslot);
    }
}
