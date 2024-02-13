package de.bremen.jTimetable.Classes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueBoolean;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueLong;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueString;
public class Location {
    Long id;

    public String caption;
    public Boolean active;
    private SQLConnectionManager sqlConnectionManager;

    public Location(Long id, SQLConnectionManager sqlConnectionManager) {
        this.id = id;
        setSqlConnectionManager(sqlConnectionManager);
        if (this.id == 0){
            //load dummy object
            this.caption = "";
            this.active = Boolean.TRUE;
        }else{
            //load object from db
            try {
                
                ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();
                SQLValues.add(new SQLValueLong(id));

                ResultSet rs = sqlConnectionManager.select("Select * from T_Locations where id = ?;", SQLValues);
                rs.first();
                this.id = rs.getLong("id");
                this.caption = rs.getString("caption");
                this.active = rs.getBoolean("active");
            } catch (SQLException e) {
                System.err.println("Location with id: " + this.id + " could not be loaded.");
                e.printStackTrace();
            }

        }

    }

    public static ArrayList<Location> getAllLocations(Boolean pActivestate, SQLConnectionManager sqlConnectionManager) throws  SQLException{
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        SQLValues.add(new SQLValueBoolean(pActivestate));
        ResultSet rs = sqlConnectionManager.select("Select * from T_Locations where active = ?",SQLValues);
        ArrayList<Location> returnList = new ArrayList<Location>();
        while( rs.next() ){
            returnList.add(new Location(rs.getLong("id"), sqlConnectionManager));
        }
        // sqlConnectionManager.close();
        return returnList;
    }

    public void save() throws SQLException{
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        SQLValues.add(new SQLValueString(this.caption));
        SQLValues.add(new SQLValueBoolean(this.active));

        if (this.id == 0) {
            // its a new object, we have to insert it
            ResultSet rs = sqlConnectionManager.execute("Insert Into `T_Locations` (`caption`, `ACTIVE`) values (?, ?)",SQLValues);
            rs.first();
            this.id = rs.getLong(1);
        }else{
            // we only have to update an existing entry
            SQLValues.add(new SQLValueLong(this.id));
            sqlConnectionManager.execute("update `T_Locations` set `caption` = ?, `ACTIVE` = ? where `id` = ?;",SQLValues);
        }
        // sqlConnectionManager.close();
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

    public Long getId() {
        return id;
    }

    public SQLConnectionManager getSqlConnectionManager() {
        return sqlConnectionManager;
    }

    public void setSqlConnectionManager(SQLConnectionManager sqlConnectionManager) {
        this.sqlConnectionManager = sqlConnectionManager;
    }
    
}