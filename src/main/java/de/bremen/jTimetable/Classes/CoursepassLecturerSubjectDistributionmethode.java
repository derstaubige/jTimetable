package de.bremen.jTimetable.Classes;

import java.util.ResourceBundle;

public enum CoursepassLecturerSubjectDistributionmethode {
    // double hours - two hours should be placed after another
    // full day - place only a full day of hours
    // normal - place a single hour
    NORMAL,
    DOUBLEHOURS,
    FULLDAY;

    public String toStringLocal(ResourceBundle resourceBundle){
        return resourceBundle.getString("CLSDM."+this);
    }
}
