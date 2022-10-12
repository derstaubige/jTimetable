package de.bremen.jTimetable.Classes;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

public class CourseofStudy {
    long id;
    public String caption;
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
            this.caption = rs.getString("caption").trim();
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

    public ArrayList<CourseofStudy> getCoursesofStudy(Boolean activeStatus) throws SQLException{
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();
        SQLValues.add(new SQLValueBoolean(activeStatus));
        ResultSet rs = sqlConnectionManager.select("Select * from T_CoursesofStudy where active = ?",SQLValues);
        ArrayList returnList = new ArrayList();
//        rs.last();
//        System.out.println(rs.getRow() + " active Courses of Study");
//        rs.first();
        while( rs.next() ){
            returnList.add(new CourseofStudy(rs.getLong("id")));
        }
        return returnList;
    }

    public void setCaption(String pCaption){
        this.caption = pCaption.trim();
    }

    public String getCaption(){
        return this.caption.trim();
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDate getBegin() {
        return begin;
    }

    public void setBegin(LocalDate begin) {
        this.begin = begin;
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
