package de.bremen.jTimetable ;

import de.bremen.jTimetable.Classes.CourseofStudy;
import de.bremen.jTimetable.Classes.Coursepass;
import de.bremen.jTimetable.Classes.SQLConnectionManager;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueInt;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueString;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args){
        try {
            CourseofStudy cos = new CourseofStudy(8);
            System.out.println(cos);

            Coursepass cp = new Coursepass(102);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}