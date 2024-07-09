package de.bremen.jTimetable.Classes;

import java.util.ArrayList;

public class TimetableDistributeStackItem {
    private ArrayList<CoursepassLecturerSubject> arrayListItems = new ArrayList<CoursepassLecturerSubject>();
    private Long unplanedHours = 0L;
    private SQLConnectionManager sqlConnectionManager;
    

    public TimetableDistributeStackItem(CoursepassLecturerSubject cls, SQLConnectionManager sqlConnectionManager) {
        this.arrayListItems.add(cls);
        this.sqlConnectionManager = sqlConnectionManager;
    }


    public void updateUnplanedHours(){
        unplanedHours = 0L;
        for (CoursepassLecturerSubject cls : arrayListItems) {
            cls.updateallHours();
            unplanedHours += cls.getUnplanedHours();
        }
    }

    public Long getUnplanedHours(){
        return this.unplanedHours;
    }

    public void addCLS(CoursepassLecturerSubject cls){
        this.arrayListItems.add(cls);
    }

    public void addCLS(CoursepassLecturerSubject cls, Integer idx){
        this.arrayListItems.add(idx, cls);
    }

    public void removeCLS(CoursepassLecturerSubject cls){
        this.arrayListItems.remove(cls);
    }


    public ArrayList<CoursepassLecturerSubject> getArrayListItems() {
        return arrayListItems;
    }    
}

