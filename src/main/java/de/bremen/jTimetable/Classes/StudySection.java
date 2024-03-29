package de.bremen.jTimetable.Classes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueBoolean;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueLong;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueString;

public class StudySection {
    Long id;
    public String description;
    public Boolean active;
    private SQLConnectionManager sqlConnectionManager;

    public StudySection(Long id, SQLConnectionManager sqlConnectionManager) throws SQLException {
        this.id = id;
        setSqlConnectionManager(sqlConnectionManager);
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        if (this.id == 0) {
            // load dummy object
            this.description = "";
            this.active = Boolean.TRUE;
        } else {
            // load object from db
            SQLValues.add(new SQLValueLong(id));

            ResultSet rs = sqlConnectionManager.select("Select * from T_StudySections where id = ?;", SQLValues);
            rs.first();
            this.id = rs.getLong("id");
            this.description = rs.getString("description");
            this.active = rs.getBoolean("active");
        }
        // sqlConnectionManager.close();
    }

    public void save() throws SQLException {
        try  {
            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

            SQLValues.add(new SQLValueString(this.description));
            SQLValues.add(new SQLValueBoolean(this.active));

            if (this.id == 0) {
                // its a new object, we have to insert it
                ResultSet rs = sqlConnectionManager
                        .execute("Insert Into `T_StudySections` (`description`, `ACTIVE`) values (?, ?)", SQLValues);
                rs.first();
                this.id = rs.getLong(1);
            } else {
                // we only have to update an existing entry
                SQLValues.add(new SQLValueLong(this.id));
                sqlConnectionManager.execute("update `T_StudySections` set `description` = ?, `ACTIVE` = ? where `id` = ?;",
                        SQLValues);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static ArrayList<StudySection> getStudySections(Boolean activeStatus, SQLConnectionManager sqlConnectionManager) throws SQLException {
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();
        SQLValues.add(new SQLValueBoolean(activeStatus));
        ResultSet rs = sqlConnectionManager
                .select("Select * from T_StudySections where active = ? order by Description", SQLValues);
        ArrayList<StudySection> returnList = new ArrayList<StudySection>();

        while (rs.next()) {
            returnList.add(new StudySection(rs.getLong("id"), sqlConnectionManager));
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

    public static ArrayList<StudySection> getAllStudySections(Boolean pActivestate, SQLConnectionManager sqlConnectionManager) throws SQLException {
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        SQLValues.add(new SQLValueBoolean(pActivestate));
        ResultSet rs = sqlConnectionManager.select("Select * from T_StudySections where active = ?", SQLValues);
        ArrayList<StudySection> returnList = new ArrayList<StudySection>();
        while (rs.next()) {
            returnList.add(new StudySection(rs.getLong("id"), sqlConnectionManager));
        }
        // sqlConnectionManager.close();
        return returnList;
    }

    public SQLConnectionManager getSqlConnectionManager() {
        return sqlConnectionManager;
    }

    public void setSqlConnectionManager(SQLConnectionManager sqlConnectionManager) {
        this.sqlConnectionManager = sqlConnectionManager;
    }

}
