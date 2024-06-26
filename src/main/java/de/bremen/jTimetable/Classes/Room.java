package de.bremen.jTimetable.Classes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueBoolean;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueDate;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueLong;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueString;

public class Room {
    Long id;
    String caption;
    Location location;
    Boolean active;
    // String locationCaption;
    private SQLConnectionManager sqlConnectionManager;
    // Arraylist of ResourcesBlocked for this Room
    private ArrayList<ResourcesBlocked> roomBlocks;

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
            this.caption = rs.getString("roomcaption").trim();
            this.location = new Location(rs.getLong("refLocationID"), getSqlConnectionManager());
            this.active = rs.getBoolean("active");

        }
        // sqlConnectionManager.close();
    }

    public void updateRoomBlocks() {
        this.roomBlocks = ResourcesBlocked.getArrayListofResourcesblocked(getId(), ResourceNames.ROOM, true, false,
                sqlConnectionManager);
    }

    public boolean isRoomAvailable(LocalDate date, Integer timeslot) {

        this.updateRoomBlocks();

        if (this.id == 0) { // "no room" is allways free
            return true;
        }

        LocalDate resourcesBlockedStartDate;
        LocalDate resourcesBlockedEndDate;
        int resourcesBlockedStartTimeslot;
        int resourcesBlockedEndTimeslot;

        for (ResourcesBlocked resourcesBlocked : this.roomBlocks) {

            resourcesBlockedStartDate = resourcesBlocked.getStartDate();
            resourcesBlockedEndDate = resourcesBlocked.getEndDate();
            resourcesBlockedStartTimeslot = resourcesBlocked.getStartTimeslot();
            resourcesBlockedEndTimeslot = resourcesBlocked.getEndTimeslot();

            if(date.isBefore(resourcesBlockedEndDate) && date.isAfter(resourcesBlockedStartDate)){
                return false;
            }

            // If the date we want to check is the same date as the start of the blocked
            // date range,
            // we can check if the blocking starts after the timeslot we want to reserve or
            // ends before it
            if (resourcesBlockedStartDate.compareTo(date) == 0) {
                if (resourcesBlockedStartTimeslot <= timeslot) {
                    if (resourcesBlockedEndDate.compareTo(date) == 0 && resourcesBlockedEndTimeslot < timeslot) {
                        // when a lecturer just can't make it to the 2nd timeslot he should be able
                        // to teach at 3rd timeslot
                        continue;
                    } else {
                        // sqlConnectionManager.close();
                        return false;
                    }
                } else {
                    continue;
                }
            }

            // If the date we want to check is the same date as the end of the blocked date
            // range, we can check if the
            // blocking ends before the timestamp we want to reserve
            if (resourcesBlockedEndDate.compareTo(date) == 0) {
                if (timeslot <= resourcesBlockedEndTimeslot) {
                    // sqlConnectionManager.close();
                    return false;
                } else {
                    continue;
                }
            }
        }
        return true;
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

    public static ArrayList<Room> getAllRooms(Boolean pActivestate, SQLConnectionManager sqlConnectionManager)
            throws SQLException {
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

    public ArrayList<ResourcesBlocked> getRoomBlocks() {
        return roomBlocks;
    }

}
