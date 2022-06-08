package de.bremen.jTimetable.Classes;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueLong;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

// CREATE TABLE IF NOT EXISTS  `T_StudySections` (`id` long not null PRIMARY KEY AUTO_INCREMENT, `description` Char(200), `active` Boolean );
public class StudySection {
    Long id;
    String description;
    Boolean active;

    public StudySection(Long id) throws SQLException {
        this.id = id;
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        if (this.id == 0){
            // create new coursepass object
            ResultSet rs = sqlConnectionManager.insert("Insert Into `T_StudySections` ( `DESCRIPTION`, `ACTIVE`) values ('', True)",SQLValues);
            //ResultSet rs = sqlConnectionManager.select("select max(id) as id from T_StudySections",SQLValues);
            rs.first();
            this.id = rs.getLong(1);
        }
        SQLValues.add(new SQLValueLong(id));

        ResultSet rs = sqlConnectionManager.select("Select * from T_StudySections where id = ?;",SQLValues);
        while(rs.next()){
            this.id = rs.getLong("id");
            this.active = rs.getBoolean("active");
            this.description = "";
        }
    }
}
