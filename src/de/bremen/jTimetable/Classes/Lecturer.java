package de.bremen.jTimetable.Classes;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueBoolean;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueLong;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueString;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Lecturer {
    Long id;
    public String firstname;
    public String lastname;
    Location location;
    public Boolean active;

    public Lecturer(Long id) throws SQLException {
        this.id = id;
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        if (this.id == 0){
            //load dummy object
            this.firstname = "";
            this.lastname = "";
            this.location = new Location(0L);
            this.active = Boolean.TRUE;
        }else{
            //load object from db
            SQLValues.add(new SQLValueLong(id));

            ResultSet rs = sqlConnectionManager.select("Select * from T_Lecturers where id = ?;",SQLValues);
            rs.first();
            this.id = rs.getLong("id");
            this.firstname = rs.getString("firstname");
            this.lastname = rs.getString("lastname");
            this.location = new Location(rs.getLong("reflocationID"));
            this.active = rs.getBoolean("active");
        }

    }
    public void save() throws SQLException{
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        SQLValues.add(new SQLValueString(this.firstname));
        SQLValues.add(new SQLValueString(this.lastname));
        SQLValues.add(new SQLValueLong(this.location.id));
        SQLValues.add(new SQLValueBoolean(this.active));

        if (this.id == 0) {
            // its a new object, we have to insert it
            ResultSet rs = sqlConnectionManager.execute("Insert Into `T_Lecturers` (`firstname`, `lastname`, `reflocationID`, `ACTIVE`) values (?, ?, ?, ?)",SQLValues);
            rs.first();
            this.id = rs.getLong(1);
        }else{
            // we only have to update an existing entry
            SQLValues.add(new SQLValueLong(this.id));
            ResultSet rs = sqlConnectionManager.execute("update `T_Lecturers` set `firstname` = ?, `lastname` = ?, `reflocationID` = ?, `ACTIVE` = ? where `id` = ?;",SQLValues);
        }
    }

    public String getLecturerFullName(){
        return this.lastname + ", " + this.firstname;
    }
}
