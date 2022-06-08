package de.bremen.jTimetable.Classes;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueLong;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

// CREATE TABLE IF NOT EXISTS  `T_Subjects` (`id` long not null PRIMARY KEY AUTO_INCREMENT, `caption` char(200), `active` Boolean);
public class Subject {
    Long id;
    String caption;
    Boolean active;

    public Subject(Long id) throws SQLException {
        this.id = id;
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        if (this.id == 0){
            ResultSet rs = sqlConnectionManager.insert("Insert Into `T_Subjects` (`caption`, ACTIVE`) values ('', True)",SQLValues);
            rs.first();
            this.id = rs.getLong(1);
        }
        SQLValues.add(new SQLValueLong(id));

        ResultSet rs = sqlConnectionManager.select("Select * from T_Subjects where id = ?;",SQLValues);
        while(rs.next()){
            this.id = rs.getLong("id");
            this.caption = rs.getString("caption");
            this.active = rs.getBoolean("active");
        }
    }
}
