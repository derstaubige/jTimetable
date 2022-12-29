package de.bremen.jTimetable.Classes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.*;

public class Resourcesblocked {
    
    Long ID;
    Long REFRESOURCEID;
    String RESOURCENAME;
    LocalDate STARTDATE;
    LocalDate ENDDATE;
    Integer STARTTIMESLOT;
    Integer ENDTIMESLOT;
    String DESCRIPTION;

    public enum Resourcenames {
        ROOM,
        LECTURER
    }

    public Resourcesblocked(Long resourcesblockedID) throws SQLException {
        this.ID = resourcesblockedID;
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        if (this.ID == 0) {
            // load dummy object
            this.REFRESOURCEID = 0L;
            this.RESOURCENAME = Resourcenames.LECTURER.toString();
            this.STARTDATE = LocalDate.of(1990, 1, 1);
            this.ENDDATE = LocalDate.of(1990, 1, 1);
            this.STARTTIMESLOT = 0;
            this.ENDTIMESLOT = 0;
            this.DESCRIPTION = "";
        } else {
            // load object from db
            SQLValues.add(new SQLValueLong(ID));

            ResultSet rs = sqlConnectionManager.select("Select * from T_Resourcesblocked where id = ?;", SQLValues);
            rs.first();
            this.ID = rs.getLong("id");
            this.REFRESOURCEID = rs.getLong("REFRESOURCEID");
            this.RESOURCENAME = rs.getString("RESOURCENAME").trim();
            this.STARTDATE = rs.getDate("STARTDATE").toLocalDate();
            this.ENDDATE = rs.getDate("ENDDATE").toLocalDate();
            this.STARTTIMESLOT = rs.getInt("STARTTIMESLOT");
            this.ENDTIMESLOT = rs.getInt("ENDTIMESLOT");
            this.DESCRIPTION = rs.getString("DESCRIPTION").trim();
        }

    }
    
    public void save() {
        try {
            SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

            SQLValues.add(new SQLValueLong(this.REFRESOURCEID));
            SQLValues.add(new SQLValueString(this.RESOURCENAME));
            SQLValues.add(new SQLValueDate(this.STARTDATE));
            SQLValues.add(new SQLValueDate(this.ENDDATE));
            SQLValues.add(new SQLValueInt(this.STARTTIMESLOT));
            SQLValues.add(new SQLValueInt(this.ENDTIMESLOT));
            SQLValues.add(new SQLValueString(this.DESCRIPTION));
            if (this.ID == 0) {
                // its a new object, we have to insert it
                ResultSet rs = sqlConnectionManager.execute(
                        "Insert Into `T_RESOURCESBLOCKED` (`REFRESOURCEID`, `RESOURCENAME`, `STARTDATE`, `ENDDATE`, `STARTTIMESLOT`, `ENDTIMESLOT`, `DESCRIPTION`) values (?, ?, ?, ?, ?, ?, ?)",
                        SQLValues);
                rs.first();
                this.ID = rs.getLong(1);
            } else {
                // we only have to update an existing entry
                SQLValues.add(new SQLValueLong(this.ID));
                sqlConnectionManager.execute(
                        "update `T_RESOURCESBLOCKED` set `REFRESOURCEID` = ?, `RESOURCENAME` = ?, `STARTDATE` = ?, `ENDDATE` = ?, `STARTTIMESLOT` = ?, `ENDTIMESLOT` = ?, `DESCRIPTION` = ? where `id` = ?;",
                        SQLValues);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void delete() throws SQLException {
        if (this.ID != 0) {
            SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();
            SQLValues.add(new SQLValueLong(this.ID));
            sqlConnectionManager.execute(
                    "delete from `T_RESOURCESBLOCKED` where `id` = ?;",
                    SQLValues);
        }
    }

    public static ArrayList<Resourcesblocked> getArrayListofResourcesblocked(Long resourceID,
            Resourcenames resourcename) {
        return getArrayListofResourcesblocked(resourceID, resourcename, false, true);
    }

    public static ArrayList<Resourcesblocked> getArrayListofResourcesblocked(Long resourceID,
            Resourcenames resourcename, Boolean showPassed, Boolean withoutLesson) {
        ArrayList<Resourcesblocked> returnListe = new ArrayList<Resourcesblocked>();

        try {
            SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();
            SQLValues.add(new SQLValueLong(resourceID));
            SQLValues.add(new SQLValueString(resourcename.toString()));

            String SQLString = "Select * from T_Resourcesblocked where REFRESOURCEID = ? and RESOURCENAME = ?";

            if (showPassed == false) {
                SQLString = SQLString + " and ENDDATE >= '" + LocalDate.now().toString() + "'";
            }

            if (withoutLesson == true) {
                SQLString = SQLString + " and DESCRIPTION NOT LIKE 'LESSON%'";
            }

            SQLString = SQLString + ";";
            
            ResultSet rs = sqlConnectionManager.select(SQLString, SQLValues);

            while (rs.next()) {
                returnListe.add(new Resourcesblocked(rs.getLong("ID")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnListe;
    }

    public static void setResourcesBlocked(Long REFRESOURCEID, String RESOURCENAME, String DESCRIPTION,
            LocalDate STARTDATE, LocalDate ENDDATE, int STARTTIMESLOT,
            int ENDTIMESLOT) throws SQLException {
        Resourcesblocked resourcesblocked = new Resourcesblocked(0L);
        resourcesblocked.setREFRESOURCEID(REFRESOURCEID);
        resourcesblocked.setRESOURCENAME(RESOURCENAME);
        resourcesblocked.setDESCRIPTION(DESCRIPTION);
        resourcesblocked.setSTARTDATE(STARTDATE);
        resourcesblocked.setENDDATE(ENDDATE);
        resourcesblocked.setSTARTTIMESLOT(STARTTIMESLOT);
        resourcesblocked.setENDTIMESLOT(ENDTIMESLOT);

        resourcesblocked.save();
    }

    public void get(Long resourceID, Resourcenames resourcename, LocalDate startDate, LocalDate endDate,
            Integer startTimeslot, Integer endTimeslot) {
        try {
            SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();
            SQLValues.add(new SQLValueLong(resourceID));
            SQLValues.add(new SQLValueString(resourcename.toString()));
            SQLValues.add(new SQLValueDate(startDate));
            SQLValues.add(new SQLValueDate(endDate));

            ResultSet rs = sqlConnectionManager.select(
                    "Select * from T_Resourcesblocked where REFRESOURCEID = ? and RESOURCENAME = ? and startdate = ? and enddate = ? and starttimeslot = ? and endtimeslot = ?;",
                    SQLValues);
            rs.first();
            this.setID(rs.getLong("ID"));
            this.setREFRESOURCEID(rs.getLong("REFRESOURCEID"));
            this.setRESOURCENAME(rs.getString("RESOURCENAME"));
            this.setSTARTDATE(rs.getDate("startdate").toLocalDate());
            this.setENDDATE(rs.getDate("enddate").toLocalDate());
            this.setSTARTTIMESLOT(rs.getInt("starttimeslot"));
            this.setENDTIMESLOT(rs.getInt("endtimeslot"));
            this.setDESCRIPTION(rs.getString("DESCRIPTION"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Long getID() {
        return ID;
    }

    public void setID(Long iD) {
        ID = iD;
    }

    public Long getREFRESOURCEID() {
        return REFRESOURCEID;
    }

    public void setREFRESOURCEID(Long rEFRESOURCEID) {
        REFRESOURCEID = rEFRESOURCEID;
    }

    public String getRESOURCENAME() {
        return RESOURCENAME.trim();
    }

    public void setRESOURCENAME(String rESOURCENAME) {
        RESOURCENAME = rESOURCENAME;
    }

    public LocalDate getSTARTDATE() {
        return STARTDATE;
    }

    public void setSTARTDATE(LocalDate sTARTDATE) {
        STARTDATE = sTARTDATE;
    }

    public LocalDate getENDDATE() {
        return ENDDATE;
    }

    public void setENDDATE(LocalDate eNDDATE) {
        ENDDATE = eNDDATE;
    }

    public Integer getSTARTTIMESLOT() {
        return STARTTIMESLOT;
    }

    public void setSTARTTIMESLOT(Integer sTARTTIMESLOT) {
        STARTTIMESLOT = sTARTTIMESLOT;
    }

    public Integer getENDTIMESLOT() {
        return ENDTIMESLOT;
    }

    public void setENDTIMESLOT(Integer eNDTIMESLOT) {
        ENDTIMESLOT = eNDTIMESLOT;
    }

    public String getDESCRIPTION() {
        return DESCRIPTION.trim();
    }

    public void setDESCRIPTION(String dESCRIPTION) {
        DESCRIPTION = dESCRIPTION;
    }

    
}
