package de.bremen.jTimetable.Classes;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueLong;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

// CREATE TABLE IF NOT EXISTS  `T_Rooms` (`id` long not null PRIMARY KEY AUTO_INCREMENT, `roomcaption`char(200) , `refRoomID` long,  `active` Boolean );
public class Room {
    Long id;
    String roomcaption;
    Location location;
    Boolean active;

    public Room(Long id) throws SQLException {
        this.id = id;
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        if (this.id == 0){
            // create new coursepass object
            ResultSet rs = sqlConnectionManager.insert("Insert Into `T_Rooms` (`roomcaption`, `refRoomID`, `ACTIVE`) values ('', 0,  True)",SQLValues);
            //ResultSet rs = sqlConnectionManager.select("select max(id) as id from T_Rooms",SQLValues);
            rs.first();
            this.id = rs.getLong(1);
        }
        SQLValues.add(new SQLValueLong(id));

        ResultSet rs = sqlConnectionManager.select("Select * from T_Rooms where id = ?;",SQLValues);
        while(rs.next()){
            this.id = rs.getLong("id");
            this.roomcaption = rs.getString("roomcaption");
            this.location = new Location(rs.getLong("refRoomID"));
            this.active = rs.getBoolean("active");
        }
    }
}
