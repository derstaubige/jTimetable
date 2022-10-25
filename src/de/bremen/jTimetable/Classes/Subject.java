package de.bremen.jTimetable.Classes;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueBoolean;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueLong;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueString;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
public class Subject {
    Long id;
    String caption;


    Boolean active;


    public Subject(Long id) throws SQLException {
        this.id = id;
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        if (this.id == 0){
            //load dummy object
            this.caption = "FREETIME";
            this.active = Boolean.TRUE;
        }else{
            //load object from db
            SQLValues.add(new SQLValueLong(id));

            ResultSet rs = sqlConnectionManager.select("Select * from T_Subjects where id = ?;",SQLValues);
            rs.first();
            this.id = rs.getLong("id");
            this.caption = rs.getString("caption");
            this.active = rs.getBoolean("active");
        }

    }

    public void save() throws SQLException{
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        SQLValues.add(new SQLValueString(this.caption));
        SQLValues.add(new SQLValueBoolean(this.active));

        if (this.id == 0) {
            // its a new object, we have to insert it
            ResultSet rs = sqlConnectionManager.execute("Insert Into `T_Subjects` (`caption`, `ACTIVE`) values (?, ?)",SQLValues);
            rs.first();
            this.id = rs.getLong(1);
        }else{
            // we only have to update an existing entry
            SQLValues.add(new SQLValueLong(this.id));
            ResultSet rs = sqlConnectionManager.execute("update `T_Subjects` set `caption` = ?, `ACTIVE` = ? where `id` = ?;",SQLValues);
        }
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCaption() {
        return caption.trim();
    }

    public void setCaption(String caption) {
        this.caption = caption.trim();
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
    public static ArrayList<Subject> getAllSubjects(Boolean pActivestate) throws  SQLException{
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        SQLValues.add(new SQLValueBoolean(pActivestate));
        ResultSet rs = sqlConnectionManager.select("Select * from T_Subjects where active = ?",SQLValues);
        ArrayList returnList = new ArrayList();
        while( rs.next() ){
            returnList.add(new Subject(rs.getLong("id")));
        }
        return returnList;
    }
}
