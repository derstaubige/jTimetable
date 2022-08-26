package de.bremen.jTimetable.Classes;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueBoolean;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueLong;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueString;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

// CREATE TABLE IF NOT EXISTS  `T_StudySections` (`id` long not null PRIMARY KEY AUTO_INCREMENT, `description` Char(200), `active` Boolean );
public class StudySection {
    Long id;
    public String description;
    public Boolean active;

    public StudySection(Long id) throws SQLException {
        this.id = id;
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        if (this.id == 0){
            //load dummy object
            this.description = "";
            this.active = Boolean.TRUE;
        }else{
            //load object from db
            SQLValues.add(new SQLValueLong(id));

            ResultSet rs = sqlConnectionManager.select("Select * from T_StudySections where id = ?;",SQLValues);
            rs.first();
            this.id = rs.getLong("id");
            this.description = rs.getString("description");
            this.active = rs.getBoolean("active");
        }

    }
    public void save() throws SQLException{
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        SQLValues.add(new SQLValueString(this.description));
        SQLValues.add(new SQLValueBoolean(this.active));

        if (this.id == 0) {
            // its a new object, we have to insert it
            ResultSet rs = sqlConnectionManager.execute("Insert Into `T_StudySections` (`description`, `ACTIVE`) values (?, ?)",SQLValues);
            rs.first();
            this.id = rs.getLong(1);
        }else{
            // we only have to update an existing entry
            SQLValues.add(new SQLValueLong(this.id));
            ResultSet rs = sqlConnectionManager.execute("update `T_StudySections` set `description` = ?, `ACTIVE` = ? where `id` = ?;",SQLValues);
        }
    }

    public static ArrayList<StudySection> getStudySections(Boolean activeStatus) throws SQLException{
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();
        SQLValues.add(new SQLValueBoolean(activeStatus));
        ResultSet rs = sqlConnectionManager.select("Select * from T_StudySections where active = ? order by Description",SQLValues);
        ArrayList returnList = new ArrayList();

        while( rs.next() ){
            returnList.add(new StudySection(rs.getLong("id")));
        }
        return returnList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return this.description.trim();
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
