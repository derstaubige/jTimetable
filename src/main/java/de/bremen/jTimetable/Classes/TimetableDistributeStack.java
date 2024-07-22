package de.bremen.jTimetable.Classes;

import java.util.ArrayList;
import java.util.Collections;
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
        this.sortStackUnplanedHours();
    }

    public void sortStackUnplanedHours() {
        for (TimetableDistributeStackItem timetableDistributeStackItem : this.arraylist) {
            timetableDistributeStackItem.updateUnplanedHours();
        }
        // sort the arraycoursepasslecturersubject by unplaned Hours
        Collections.sort(this.arraylist,
                (o1, o2) -> o2.getUnplanedHours().compareTo(o1.getUnplanedHours()));
    }

    public int size(){
        return this.arraylist.size();
    }

    private void load() {
        for (CoursepassLecturerSubject cls : this.coursePass.getArrayCoursePassLecturerSubject()) {
            if (cls.getUnplanedHours() > 0) {
                this.arraylist.add(new TimetableDistributeStackItem(cls, sqlConnectionManager));
            }
        }

        // check if some cls should start after another cls, load cls that should be put
        // after another cls
        HashMap<Long, CoursepassLecturerSubject> hashMap = new HashMap<Long, CoursepassLecturerSubject>();
        for (Integer i = arraylist.size() - 1; i >= 0; i--) {
            TimetableDistributeStackItem timetableDistributeStackItem = arraylist.get(i);
            if (timetableDistributeStackItem.getArrayListItems().get(0).getPlaceAfterCLS() != 0L) {
                hashMap.put(timetableDistributeStackItem.getArrayListItems().get(0).getPlaceAfterCLS(),
                        timetableDistributeStackItem.getArrayListItems().get(0));
                arraylist.remove(timetableDistributeStackItem);
            }
        }

        // add those cls to the TimetableDistributeStackItems
        while (hashMap.size() > 0) {
            for (TimetableDistributeStackItem timetableDistributeStackItem : arraylist) {
                for (CoursepassLecturerSubject cls : timetableDistributeStackItem.getArrayListItems()) {
                    if (hashMap.containsKey(cls.getId())) {
                        Integer tmpIdx = timetableDistributeStackItem.getArrayListItems().indexOf(cls);
                        timetableDistributeStackItem.addCLS(hashMap.get(cls.getId()), tmpIdx + 1);
                        hashMap.remove(cls.getId());
                        timetableDistributeStackItem.updateUnplanedHours();
                        break;
                    }
                }
            }
        }
    }

    public ArrayList<TimetableDistributeStackItem> getArraylist() {
        return arraylist;
    }

    
}
