package de.bremen.jTimetable.Classes;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueInt;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueLong;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

// CREATE TABLE IF NOT EXISTS  `T_CoursesofStudy` (`id` long not null PRIMARY KEY AUTO_INCREMENT, `caption` char(60), `begin` Date, `end` Date, `active` Boolean );
public class CourseofStudy {
    long id;
    String caption;
    LocalDate begin;
    LocalDate end;
    boolean active;

    public CourseofStudy(long id) throws SQLException {
        //creates a courseofstudy object. if id = 0 then a new one will be created
        this.id = id;

        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        if (this.id == 0){
            // create new courseofstudy object
            sqlConnectionManager.insert("Insert Into `T_CoursesofStudy` (`caption`, `begin`, `end`, `active`) values ('', '1990-01-01', '1990-01-01', True)",SQLValues);
            ResultSet rs = sqlConnectionManager.select("select max(id) as id from T_CoursesofStudy",SQLValues);
            this.id = rs.getLong("id");
        }
        SQLValues.add(new SQLValueLong(id));

        ResultSet rs = sqlConnectionManager.select("Select * from T_CoursesofStudy where id = ?",SQLValues);
        this.id = rs.getLong("id");
        this.caption = rs.getString("caption");
        this.begin = rs.getDate("begin").toLocalDate();
        this.end = rs.getDate("end").toLocalDate();
        this.active = rs.getBoolean("active");


    }
}
