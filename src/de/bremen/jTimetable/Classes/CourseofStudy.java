package de.bremen.jTimetable.Classes;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

// CREATE TABLE IF NOT EXISTS  `T_CoursesofStudy` (`id` long not null PRIMARY KEY AUTO_INCREMENT, `caption` char(60), `begin` Date, `end` Date, `active` Boolean );
public class CourseofStudy {
    long id;
    String caption;
    public LocalDate begin;
    public LocalDate end;
    boolean active;

    public CourseofStudy(long id) throws SQLException {
        //creates a courseofstudy object. if id = 0 then a new one will be created
        this.id = id;

        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        if (this.id == 0){
            //load dummy object
            this.caption = "";
            this.begin = LocalDate.of(1990,1,1);
            this.end = LocalDate.of(1990,1,1);
            this.active = Boolean.TRUE;
        }else{
            //load object from db
            SQLValues.add(new SQLValueLong(id));

            ResultSet rs = sqlConnectionManager.select("Select * from T_CoursesofStudy where id = ?;",SQLValues);
            rs.first();
            this.id = rs.getLong("id");
            this.caption = rs.getString("caption");
            this.begin = rs.getDate("begin").toLocalDate();
            this.end = rs.getDate("end").toLocalDate();
            this.active = rs.getBoolean("active");
        }



    }

    public void save() throws SQLException{
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        SQLValues.add(new SQLValueString(this.caption));
        SQLValues.add(new SQLValueDate(this.begin));
        SQLValues.add(new SQLValueDate(this.end));
        SQLValues.add(new SQLValueBoolean(this.active));

        if (this.id == 0) {
            // its a new object, we have to insert it
            ResultSet rs = sqlConnectionManager.execute("Insert Into `T_CoursesofStudy` (`caption`, `begin`, `end`, `ACTIVE`) values (?, ?, ?, ?)",SQLValues);
            rs.first();
            this.id = rs.getLong(1);
        }else{
            // we only have to update an existing entry
            SQLValues.add(new SQLValueLong(this.id));
            ResultSet rs = sqlConnectionManager.execute("update `T_CoursesofStudy` set `caption` = ?, `begin` = ?, `end` = ?, `ACTIVE` = ? where `id` = ?;",SQLValues);
        }
    }
}
