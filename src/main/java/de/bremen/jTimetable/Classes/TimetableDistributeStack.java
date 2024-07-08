package de.bremen.jTimetable.Classes;

import java.util.ArrayList;
import java.util.HashMap;

public class TimetableDistributeStack {
    private CoursePass coursePass;
    private SQLConnectionManager sqlConnectionManager;
    private ArrayList<TimetableDistributeStackItem> arraylist = new ArrayList<TimetableDistributeStackItem>();

    public TimetableDistributeStack(CoursePass coursePass, SQLConnectionManager sqlConnectionManager) {
        this.coursePass = coursePass;
        this.sqlConnectionManager = sqlConnectionManager;

        this.coursePass.updateCoursePassLecturerSubjects();
        this.load();
    }

    private void load(){
        for (CoursepassLecturerSubject cls : this.coursePass.getArrayCoursePassLecturerSubject()) {
            if (cls.getUnplanedHours() > 0) {
                this.arraylist.add(new TimetableDistributeStackItem(cls, sqlConnectionManager));
            }
        }
        
        // check if some cls should start after another cls, load cls that should be put after another cls
        HashMap<Long, CoursepassLecturerSubject> hashMap = new HashMap<Long, CoursepassLecturerSubject>();
        for (TimetableDistributeStackItem timetableDistributeStackItem : arraylist) {
            if(timetableDistributeStackItem.getArrayListItems().get(0).getPlaceAfterCLS() != 0L){
                hashMap.put(timetableDistributeStackItem.getArrayListItems().get(0).getId(), timetableDistributeStackItem.getArrayListItems().get(0));
                arraylist.remove(timetableDistributeStackItem);
            }
        }

        // add those cls to the TimetableDistributeStackItems
        while(hashMap.size() > 0){
            for (TimetableDistributeStackItem timetableDistributeStackItem : arraylist) {
                for (CoursepassLecturerSubject cls : timetableDistributeStackItem.getArrayListItems()) {
                    if(hashMap.containsKey(cls.getId())){
                        Integer tmpIdx = timetableDistributeStackItem.getArrayListItems().indexOf(cls);
                        timetableDistributeStackItem.addCLS(cls, tmpIdx + 1);
                        hashMap.remove(cls.getId());
                        timetableDistributeStackItem.updateUnplanedHours();
                    }
                }
            }
        }
    }
}
