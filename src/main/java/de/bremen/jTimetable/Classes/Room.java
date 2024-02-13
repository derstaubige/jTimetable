package de.bremen.jTimetable.Classes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueBoolean;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueLong;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueString;

public class Room {
    Long id;
    String caption;
    Location location;
    Boolean active;
    // String locationCaption;
    private SQLConnectionManager sqlConnectionManager;

    public Room(Long id, SQLConnectionManager sqlConnectionManager) throws SQLException {
        this.id = id;
        setSqlConnectionManager(sqlConnectionManager);
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        if (this.id == 0) {
            // load dummy object
            this.caption = "";
            this.location = new Location(0L, getSqlConnectionManager());
            this.active = Boolean.TRUE;
        } else {
            // load object from db
            SQLValues.add(new SQLValueLong(id));

            ResultSet rs = sqlConnectionManager.select("Select * from T_Rooms where id = ?;", SQLValues);
            rs.first();
            this.id = rs.getLong("id");
            this.caption = rs.getString("roomcaption");
            this.location = new Location(rs.getLong("refLocationID"), getSqlConnectionManager());
            this.active = rs.getBoolean("active");

        }
        // sqlConnectionManager.close();
    }

    public void save() throws SQLException {
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        SQLValues.add(new SQLValueString(this.caption));
        SQLValues.add(new SQLValueLong(this.location.id));
        SQLValues.add(new SQLValueBoolean(this.active));

        if (this.id == 0) {
            // its a new object, we have to insert it
            ResultSet rs = sqlConnectionManager.execute(
                    "Insert Into `T_Rooms` (`roomcaption`, `refLocationID`, `ACTIVE`) values (?, ?, ?)", SQLValues);
            rs.first();
            this.id = rs.getLong(1);
        } else {
            // we only have to update an existing entry
            SQLValues.add(new SQLValueLong(this.id));
            sqlConnectionManager.execute(
                    "update `T_Rooms` set `roomcaption` = ?, `refLocationID` = ?, `ACTIVE` = ? where `id` = ?;",
                    SQLValues);
        }
        // sqlConnectionManager.close();
    }

    public static ArrayList<Room> getAllRooms(Boolean pActivestate, SQLConnectionManager sqlConnectionManager) throws SQLException {
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        SQLValues.add(new SQLValueBoolean(pActivestate));
        ResultSet rs = sqlConnectionManager.select("Select * from T_Rooms where active = ?", SQLValues);
        ArrayList<Room> returnList = new ArrayList<Room>();
        while (rs.next()) {
            returnList.add(new Room(rs.getLong("id"), sqlConnectionManager));
        }
        // sqlConnectionManager.close();
        return returnList;
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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getLocationCaption() {
        return location.getCaption();
    }

    public SQLConnectionManager getSqlConnectionManager() {
        return sqlConnectionManager;
    }

    public void setSqlConnectionManager(SQLConnectionManager sqlConnectionManager) {
        this.sqlConnectionManager = sqlConnectionManager;
    }

}
