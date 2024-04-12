package de.bremen.jTimetable.Classes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.*;

/**
 * Class represents object of a blocked resource (either LECTURER or ROOM) that
 * can be stored in a database and
 * provides actions for editing these objects.
 */
public class ResourcesBlocked {

    /**
     * Static method that creates a new resourcesBlocked-instance and setts all
     * values at once.
     * Afterwards it calls method save() to save the changes in the database.
     *
     * @param REFRESOURCEID
     * @param RESOURCENAME
     * @param DESCRIPTION
     * @param STARTDATE
     * @param ENDDATE
     * @param STARTTIMESLOT
     * @param ENDTIMESLOT
     * @throws SQLException
     */
    public static void setResourcesBlocked(Long REFRESOURCEID, ResourceNames RESOURCENAME, String DESCRIPTION,
            LocalDate STARTDATE, LocalDate ENDDATE, int STARTTIMESLOT,
            int ENDTIMESLOT, SQLConnectionManager sqlConnectionManager) throws SQLException {
        ResourcesBlocked resourcesblocked = new ResourcesBlocked(0L, sqlConnectionManager);
        resourcesblocked.setRefResourceID(REFRESOURCEID);
        resourcesblocked.setResourceName(RESOURCENAME);
        resourcesblocked.setDescription(DESCRIPTION);
        resourcesblocked.setStartDate(STARTDATE);
        resourcesblocked.setEndDate(ENDDATE);
        resourcesblocked.setStartTimeslot(STARTTIMESLOT);
        resourcesblocked.setEndTimeslot(ENDTIMESLOT);

        resourcesblocked.save();
    }

    public static ArrayList<ResourcesBlocked> getArrayListofResourcesblocked(Long resourceID,
            ResourceNames resourcename, Boolean showPassed, Boolean withoutLesson,
            SQLConnectionManager sqlConnectionManager) {
        ArrayList<ResourcesBlocked> returnListe = new ArrayList<ResourcesBlocked>();

        try {

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
                returnListe.add(new ResourcesBlocked(rs.getLong("ID"), sqlConnectionManager));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnListe;
    }

    /**
     * Primary key, not null, auto_increment --> is initially 0
     */
    private Long ID = 0L;
    /**
     * Reference to the resource (either Lecturer or Room)
     */
    private Long refResourceID;
    /**
     * Type of the resource (Lecturer or Room)
     */
    private ResourceNames resourceName;
    /**
     * Date at which the blocking starts.
     */
    private LocalDate startDate;
    /**
     * Date at which the blocking ends.
     */
    private LocalDate endDate;
    /**
     * First timeslot of the blocking.
     */
    private Integer startTimeslot;

    /**
     * Ending timeslot of the blocking.
     */
    private Integer endTimeslot;

    /**
     * Description is optional and can describe the matter of the blocking (e.g.:
     * "LESSON" or "VACATION")
     */
    private String description;

    private SQLConnectionManager sqlConnectionManager;

    /**
     * Constructor that only sets the ID.
     *
     * @param resourcesBlockedID id the object has in database (0 if it's not stored
     *                           in the db yet)
     */
    public ResourcesBlocked(Long resourcesBlockedID, SQLConnectionManager sqlConnectionManager) throws SQLException {
        this.ID = resourcesBlockedID;
        setSqlConnectionManager(sqlConnectionManager);
        // establish connection

        // id == 0 if object doesn't exist in database
        if (this.ID == 0) {
            // load dummy object
            this.refResourceID = 0L;
            this.resourceName = ResourceNames.LECTURER;
            this.startDate = LocalDate.of(1990, 1, 1);
            this.endDate = LocalDate.of(1990, 1, 1);
            this.startTimeslot = 0;
            this.endTimeslot = 0;
            this.description = "";
        } else {
            // load object from db
            // Array with values that will be stored in the database
            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();
            SQLValues.add(new SQLValueLong(ID));
            try {
                ResultSet rs = sqlConnectionManager.select("Select * from T_Resourcesblocked where id = ?;",
                        SQLValues);

                rs.first();
                this.ID = rs.getLong("id");
                this.refResourceID = rs.getLong("REFRESOURCEID");
                this.resourceName = ResourceNames.valueOf(rs.getString("RESOURCENAME").trim());
                this.startDate = rs.getDate("STARTDATE").toLocalDate();
                this.endDate = rs.getDate("ENDDATE").toLocalDate();
                this.startTimeslot = rs.getInt("STARTTIMESLOT");
                this.endTimeslot = rs.getInt("ENDTIMESLOT");
                this.description = rs.getString("DESCRIPTION").trim();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public ResourcesBlocked(Long resourceID, ResourceNames resourcename, LocalDate startDate, LocalDate endDate,
            Integer startTimeslot, Integer endTimeslot, SQLConnectionManager sqlConnectionManager) {
        this.setSqlConnectionManager(sqlConnectionManager);
        try {

            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();
            SQLValues.add(new SQLValueLong(resourceID));
            SQLValues.add(new SQLValueString(resourcename.toString()));
            SQLValues.add(new SQLValueDate(startDate));
            SQLValues.add(new SQLValueDate(endDate));
            SQLValues.add(new SQLValueInt(startTimeslot));
            SQLValues.add(new SQLValueInt(endTimeslot));

            ResultSet rs = sqlConnectionManager.select(
                    "SELECT * FROM T_RESOURCESBLOCKED where REFRESOURCEID = ? and RESOURCENAME = ? and startdate = ? and enddate = ? and starttimeslot = ? and endtimeslot = ?;",
                    SQLValues);
            rs.first();
            this.setID(rs.getLong("ID"));
            this.setRefResourceID(rs.getLong("REFRESOURCEID"));
            this.setResourceName(ResourceNames.valueOf(rs.getString("RESOURCENAME").trim()));
            this.setStartDate(rs.getDate("startdate").toLocalDate());
            this.setEndDate(rs.getDate("enddate").toLocalDate());
            this.setStartTimeslot(rs.getInt("starttimeslot"));
            this.setEndTimeslot(rs.getInt("endtimeslot"));
            this.setDescription(rs.getString("DESCRIPTION").trim());

        } catch (Exception e) {
            // System.err.println("Couldnt load RessourceBlocked" + resourceID +
            // resourcename + startDate + endDate + startTimeslot + endTimeslot);
            // load dummy object
            this.refResourceID = resourceID;
            this.resourceName = resourcename;
            this.startDate = startDate;
            this.endDate = endDate;
            this.startTimeslot = startTimeslot;
            this.endTimeslot = endTimeslot;
            this.description = "LESSON";
        }
    }

    public void save() {
        try {
            // if this is an dummy lecturer or room we dont want to save them
            if (this.refResourceID == 0) {
                return;
            }
            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

            SQLValues.add(new SQLValueLong(this.refResourceID));
            SQLValues.add(new SQLValueString(this.resourceName.toString()));
            SQLValues.add(new SQLValueDate(this.startDate));
            SQLValues.add(new SQLValueDate(this.endDate));
            SQLValues.add(new SQLValueInt(this.startTimeslot));
            SQLValues.add(new SQLValueInt(this.endTimeslot));
            SQLValues.add(new SQLValueString(this.description));
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
            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();
            SQLValues.add(new SQLValueLong(this.ID));
            sqlConnectionManager.execute(
                    "delete from T_RESOURCESBLOCKED where id = ?;",
                    SQLValues);
            // sqlConnectionManager.close();
        }
    }

    public Long getID() {
        return this.ID;
    }

    public void setID(Long iD) {
        this.ID = iD;
    }

    public Long getRefResourceID() {
        return this.refResourceID;
    }

    public void setRefResourceID(Long rEFRESOURCEID) {
        this.refResourceID = rEFRESOURCEID;
    }

    /**
     * TODO String or Enum?
     *
     * @return
     */
    public String getResourceName() {
        return this.resourceName.toString();
    }

    /**
     * TODO String or Enum?
     *
     * @param rESOURCENAME
     */
    public void setResourceName(ResourceNames rESOURCENAME) {
        this.resourceName = rESOURCENAME;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate sTARTDATE) {
        startDate = sTARTDATE;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate eNDDATE) {
        endDate = eNDDATE;
    }

    public Integer getStartTimeslot() {
        return startTimeslot;
    }

    public void setStartTimeslot(Integer sTARTTIMESLOT) {
        startTimeslot = sTARTTIMESLOT;
    }

    public Integer getEndTimeslot() {
        return endTimeslot;
    }

    public void setEndTimeslot(Integer eNDTIMESLOT) {
        endTimeslot = eNDTIMESLOT;
    }

    public String getDescription() {
        return description.trim();
    }

    public void setDescription(String dESCRIPTION) {
        description = dESCRIPTION;
    }

    public SQLConnectionManager getSqlConnectionManager() {
        return sqlConnectionManager;
    }

    public void setSqlConnectionManager(SQLConnectionManager sqlConnectionManager) {
        this.sqlConnectionManager = sqlConnectionManager;
    }

}
