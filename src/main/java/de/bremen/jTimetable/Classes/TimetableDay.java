package de.bremen.jTimetable.Classes;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 *
 */
public class TimetableDay {
    /**
     *
     */
    private LocalDate date;
    /**
     *
     */
    private int timeslots;
    /**
     *
     */
    private ArrayList<TimetableHour> arrayTimetableHours;
    private SQLConnectionManager sqlConnectionManager;
    private Integer lastUsedTimeslot = 0;

    /**
     * TODO problematic use of this constructor because we
     * @param date
     */
    public TimetableDay(LocalDate date, SQLConnectionManager sqlConnectionManager){
        // a default day has 3 timeslots
        this(date, 3, sqlConnectionManager);
    }

    /**
     *
     * @param date
     * @param timeslots
     */
    public TimetableDay(LocalDate date, int timeslots, SQLConnectionManager sqlConnectionManager){
        this.date = date;
        this.timeslots = timeslots;
        this.arrayTimetableHours = new ArrayList<>(this.timeslots);
        while (this.arrayTimetableHours.size() < this.timeslots){
            try {
                this.arrayTimetableHours.add(new TimetableHour(0, new CoursepassLecturerSubject(0L, sqlConnectionManager), sqlConnectionManager));                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //get last used timeslot from this day
        for(TimetableHour tmpTimetableHour : getArrayTimetableHours()){
            if(tmpTimetableHour.getCoursepassLecturerSubject().getLecturerID() != 0){
                setLastUsedTimeslot(getArrayTimetableHours().indexOf(tmpTimetableHour));
            }
        }
    }

    public void addToSlot(int timeslot, CoursepassLecturerSubject coursepassLecturerSubject) throws Exception{
        //TODO: Build Custom Exception if we cant place the TimetableHour in this Slot
        if(!this.checkIfSlotIsFree(timeslot)){
            throw new Exception("Cant place CoursepassLecturerSubject in this Timeslot");
        }
        // if timeslot > arrayTimetableDay.size add as many
        while(this.arrayTimetableHours.size() < timeslot){
            try{
                this.arrayTimetableHours.add(new TimetableHour(this.arrayTimetableHours.size() + 1, new CoursepassLecturerSubject(0L, getSqlConnectionManager(), coursepassLecturerSubject.getCoursepass()), getSqlConnectionManager()));                
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        // check if the timeslot is already part of the array, if not add it with freetime
        try {
            this.getArrayTimetableHours().get(timeslot);
            this.arrayTimetableHours.set(timeslot, new TimetableHour(timeslot, coursepassLecturerSubject, getSqlConnectionManager()));
        } catch (Exception e) {
            this.arrayTimetableHours.add(timeslot, new TimetableHour(timeslot, coursepassLecturerSubject, getSqlConnectionManager()));            
        }
    }

    private boolean checkIfSlotIsFree(int timeslot){
        try{
            // if the timeslot we want to check is bigger than the size of the arrayTimetableDay the Timeslot is free
            if(this.arrayTimetableHours.size() < timeslot){
                return true;
            }
            if(this.arrayTimetableHours.get(timeslot) == null){
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
        //All timeslots are filled with empty objects
        while(this.arrayTimetableHours.size() <= timeslots) {
            try {
                this.arrayTimetableHours.add
                        (new TimetableHour(this.arrayTimetableHours.size(), new CoursepassLecturerSubject(0L, getSqlConnectionManager()), getSqlConnectionManager()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.timeslots = timeslots;
    }

    public ArrayList<TimetableHour> getArrayTimetableHours() {
        return arrayTimetableHours;
    }

    public void setArrayTimetableHours(ArrayList<TimetableHour> arrayTimetableDay) {
        this.arrayTimetableHours = arrayTimetableDay;
    }
    public TimetableHour getTimetableHourFromArrayTimetableDay(int timeslot){
        return arrayTimetableHours.get(timeslot);
    }

    public void removeTimetableHourFromArrayTimetableDay(int timeslot){
        try{
            arrayTimetableHours.set(timeslot, new TimetableHour(timeslot, new CoursepassLecturerSubject(0L, getSqlConnectionManager()), getSqlConnectionManager()));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     *
     * @param timeslot
     * @param timetableHour
     */
    public void setTimetableHourFromArrayTimetableDay(int timeslot, TimetableHour timetableHour){
        arrayTimetableHours.set(timeslot, timetableHour);
        timetableHour.setTimeslot(timeslot);
    }

    public SQLConnectionManager getSqlConnectionManager() {
        return sqlConnectionManager;
    }

    public void setSqlConnectionManager(SQLConnectionManager sqlConnectionManager) {
        this.sqlConnectionManager = sqlConnectionManager;
    }

    public Integer getLastUsedTimeslot() {
        return lastUsedTimeslot;
    }

    public void setLastUsedTimeslot(Integer lastUsedTimeslot) {
        this.lastUsedTimeslot = lastUsedTimeslot;
    }
    
}
