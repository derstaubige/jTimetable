package de.bremen.jTimetable.Classes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.*;

/**
 * Class represents a lecturer that can teach classes and is saved in database
 * table T_Lecturers.
 * 
 * @author Arne Czyborra, Loreen Roose
 */
public class Lecturer {

    /**
     * ID the lecturer has in database, is 0 if the lecturer isn't saved in database
     * yet.
     */
    private Long id;
    /**
     * The lecturers firstname.
     */
    private String firstname;
    /**
     * The lecturers lastname.
     */
    private String lastname;
    /**
     * The lecturers current location. TODO current correct?
     */
    private Location location;
    /**
     * True if lecturer is active, false if not
     */
    private Boolean active;

    // Array list of Blocks for this Lecturer. Does not contain ressources blocked
    private ArrayList<LecturerBlock> lecturerBlocks = new ArrayList<LecturerBlock>();

    // Array list of Ressorces Blocked (Classes and HOlidays and so on)
    private ArrayList<ResourcesBlocked> lecturerResourcesBlocked;
    private SQLConnectionManager sqlConnectionManager;

    public boolean checkifLecturerisBlocked(Integer dayoftheweek, Integer timeslot) {
        for (LecturerBlock lecturerBlock : lecturerBlocks) {
            if (lecturerBlock.getDayNrInt() == dayoftheweek && lecturerBlock.getTimeslot() == timeslot) {
                return true;
            }
        }
        return false;
    }

    public boolean checkifLecturerisBlocked(LocalDate date, Integer timeslot) {
        return checkifLecturerisBlocked(date.getDayOfWeek().getValue(), timeslot);
    }

    /**
     * Constructor that creates a new empty object if the 0 is passed and loads an
     * existing object from the
     * database if id isn't 0.
     *
     * @param id the lecturer has in database, 0 if not saved in database yet
     */
    public Lecturer(Long id, SQLConnectionManager sqlConnectionManager) {
        this.id = id;
        setSqlConnectionManager(sqlConnectionManager);

        if (this.id == 0) {
            // load dummy object
            this.firstname = "";
            this.lastname = "";
            this.location = new Location(0L, getSqlConnectionManager());
            this.active = Boolean.TRUE;
        } else {
            // load object from db
            try {

                ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();
                SQLValues.add(new SQLValueLong(id));

                ResultSet rs = sqlConnectionManager.select("Select * from T_Lecturers where id = ?;", SQLValues);
                rs.first();
                this.id = rs.getLong("id");
                this.firstname = rs.getString("firstname").trim();
                this.lastname = rs.getString("lastname").trim();
                this.location = new Location(rs.getLong("reflocationID"), getSqlConnectionManager());
                this.active = rs.getBoolean("active");

                updateLecturerBlocks();
            } catch (SQLException e) {
                System.err.println("Lecturer with id:  " + this.firstname + " could not be loaded in constructor.");
                e.printStackTrace();
            }
        }

    }

    public void updateLecturerResourcesBlocked() {
        ArrayList<ResourcesBlocked> lecturerResourcesBlocks = new ArrayList<ResourcesBlocked>();
        try {

            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();
            SQLValues.add(new SQLValueLong(getId()));
            SQLValues.add(new SQLValueDate(LocalDate.now()));

            ResultSet rs = sqlConnectionManager
                    .select("Select * from T_RESOURCESBLOCKED  where REFRESOURCEID = ? and RESOURCENAME  = 'LECTURER' and enddate >= ?;",
                            SQLValues);

            while (rs.next()) {
                lecturerResourcesBlocks.add(new ResourcesBlocked(rs.getLong("id"), getSqlConnectionManager()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        setLecturerResourcesBlocked(lecturerResourcesBlocks);
    }

    public void addLecturerBlocks(DayOfWeek dow, Integer timeslot) {
        LecturerBlock lecturerBlock = new LecturerBlock(0L, getSqlConnectionManager());
        lecturerBlock.setDayNr(dow);
        lecturerBlock.setTimeslot(timeslot);
        lecturerBlock.setRefLecturerID(id);

        this.lecturerBlocks.add(lecturerBlock);
    }

    /**
     * Reads the saved LecturerBlocks from the Database
     */
    private void updateLecturerBlocks() {
        ArrayList<LecturerBlock> lecturerBlocks = new ArrayList<LecturerBlock>();
        try {

            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();
            SQLValues.add(new SQLValueLong(getId()));
            SQLValues.add(new SQLValueDate(LocalDate.now()));

            ResultSet rs = sqlConnectionManager
                    .select("Select * from T_LECTURERBLOCKS where refLecturerID = ? and BlockEnd >= ?;", SQLValues);

            while (rs.next()) {
                lecturerBlocks.add(new LecturerBlock(rs.getLong("id"), getSqlConnectionManager()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        setLecturerBlocks(lecturerBlocks);
    }

    /**
     * This lecturer instance is saved in the database. If the id is still 0 a new
     * object will be inserted, if the
     * object already has its own id it will be updated.
     *
     * @throws SQLException is thrown if inserting into / updating the database
     *                      entry doesn't work
     */
    public void save() throws SQLException {
        try {
            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();

            SQLValues.add(new SQLValueString(this.firstname));
            SQLValues.add(new SQLValueString(this.lastname));
            SQLValues.add(new SQLValueLong(this.location.id));
            SQLValues.add(new SQLValueBoolean(this.active));

            if (this.id == 0) {
                // It's a new object, we have to insert it
                ResultSet rs = sqlConnectionManager.execute("Insert Into `T_Lecturers` (`firstname`, `lastname`," +
                        " `reflocationID`, `ACTIVE`) values (?, ?, ?, ?)", SQLValues);
                rs.first();
                this.id = rs.getLong(1);
            } else {
                // We only have to update an existing entry
                SQLValues.add(new SQLValueLong(this.id));
                sqlConnectionManager.execute("update `T_Lecturers` set `firstname` = ?, `lastname` = ?, " +
                        "`reflocationID` = ?, `ACTIVE` = ? where `id` = ?;", SQLValues);
            }

            deleteLecturerBlocks();

            if (lecturerBlocks != null) {
                for (LecturerBlock lecturerBlock : lecturerBlocks) {
                    lecturerBlock.save();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Naiv LecturerBlocks handeling
     */
    private void deleteLecturerBlocks() {
        try {

            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();

            SQLValues.add(new SQLValueLong(getId()));
            sqlConnectionManager.execute("Delete From `T_LECTURERBLOCKS` where `RefLecturerID` = ?",
                    SQLValues);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Checks if a lecturer is available at a certain date and a certain timeslot
     * and a lesson can be placed by
     * checking if the timeslot is not set in the database.
     *
     * @param lecturerID id of the lecturer whose availability is checked
     * @param date       defines the date that is checked for availability
     * @param timeslot   defines the timeslot that is checked for availability
     * @return true if lecturer is available, false if lecturer is blocked
     * @throws SQLException will be thrown if select statement doesn't work or
     *                      accessing resultSet is invalid
     */
    public static boolean checkLecturerAvailability(long lecturerID, LocalDate date, int timeslot,
            SQLConnectionManager sqlConnectionManager)
            throws SQLException {

        LocalDate startDate;
        LocalDate endDate;
        int startTimeslot;
        int endTimeslot;

        // if lecturerID == 0 (= FREETIME) we should return true
        if (lecturerID == 0) {
            return true;
        }

        // Check if the Lecturer is generall not available at this day and timestamp
        if (new Lecturer(lecturerID, sqlConnectionManager).checkifLecturerisBlocked(date, timeslot) == true) {
            return false;
        }

        // Database query
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();
        SQLValues.add(new SQLValueLong(lecturerID));
        SQLValues.add(new SQLValueDate(date));
        SQLValues.add(new SQLValueDate(date));
        ResultSet rs = sqlConnectionManager.select(
                "Select * from T_RESOURCESBLOCKED where Resourcename = '" + ResourceNames.LECTURER +
                        "' and refresourceid = ? and STARTDATE <= ? and ENDDATE >= ?;",
                SQLValues);

        while (rs.next()) {
            // Current database entry
            startDate = rs.getDate("startdate").toLocalDate();
            endDate = rs.getDate("enddate").toLocalDate();
            startTimeslot = rs.getInt("starttimeslot");
            endTimeslot = rs.getInt("endtimeslot");

            // If the date we want to check is the same date as the start of the blocked
            // date range,
            // we can check if the blocking starts after the timeslot we want to reserve or
            // ends before it
            if (startDate.compareTo(date) == 0) {
                if (startTimeslot <= timeslot) {
                    if (endDate.compareTo(date) == 0 && endTimeslot < timeslot) {
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
            if (endDate.compareTo(date) == 0) {
                if (timeslot <= endTimeslot) {
                    // sqlConnectionManager.close();
                    return false;
                } else {
                    continue;
                }
            }
            // sqlConnectionManager.close();
            return false;
        }
        // There is no blocking
        // sqlConnectionManager.close();
        return true;
    }

    /**
     * Method runs select statement on database to get all active/inactive
     * lecturers.
     *
     * @param activeStatus defines whether active or inactive lecturers are selected
     *                     (true = active; false = inactive)
     * @return ArrayList with all lecturers of the given status
     */
    public static ArrayList<Lecturer> getAllLecturer(Boolean activeStatus, SQLConnectionManager sqlConnectionManager) {
        try {

            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();
            SQLValues.add(new SQLValueBoolean(activeStatus));
            ResultSet rs = sqlConnectionManager.select("Select * from T_Lecturers where active = ?", SQLValues);
            ArrayList<Lecturer> returnList = new ArrayList<Lecturer>();
            while (rs.next()) {
                returnList.add(new Lecturer(rs.getLong("id"), sqlConnectionManager));
            }
            return returnList;
        } catch (SQLException e) {
            System.err.println("Not all lecturers could be selected in Lecturer.getAllLecturer().");
            throw new RuntimeException(e);
        }
    }

    /**
     * Getter for all blocked resources by this lecturer
     *
     * @return arrayList of blocked resources by this lecturer
     */
    public ArrayList<ResourcesBlocked> getArrayListOfResourcesBlocked() {
        return ResourcesBlocked.getArrayListofResourcesblocked(this.id, ResourceNames.LECTURER,
                getSqlConnectionManager());
    }

    /**
     * Getter for this.id
     *
     * @return this.id
     */
    public Long getId() {
        return id;
    }

    /**
     * Getter for this.firstname
     *
     * @return this.firstname
     */
    public String getFirstname() {
        return firstname.trim();
    }

    /**
     * Getter for this.lastname
     *
     * @return this.lastname
     */
    public String getLastname() {
        return lastname.trim();
    }

    /**
     * Getter for full name of lecturer
     *
     * @return this.lastname + ", " + this.firstname
     */
    public String getLecturerFullName() {
        return this.lastname + ", " + this.firstname;
    }

    /**
     * Getter for this.location
     *
     * @return this.location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Getter for the name of this.location
     *
     * @return this.location.getCaption()
     */
    public String getLocationCaption() {
        return location.getCaption();
    }

    /**
     * Getter for this.active
     *
     * @return this.active
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * Setter for this.id
     *
     * @param id new value for this.id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Setter for this.firstname
     *
     * @param firstname new value for this.firstname
     */
    public void setFirstname(String firstname) {
        this.firstname = firstname.trim();
    }

    /**
     * Setter for this.lastname
     *
     * @param lastname new value for this.lastname
     */
    public void setLastname(String lastname) {
        this.lastname = lastname.trim();
    }

    /**
     * Setter for this.location
     *
     * @param location new value for this.location
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Setter for this.active
     *
     * @param active new value for this.active
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean isActive() {
        return this.active;
    }

    public ArrayList<LecturerBlock> getLecturerBlocks() {
        return this.lecturerBlocks;
    }

    public void setLecturerBlocks(ArrayList<LecturerBlock> lecturerBlocks) {
        this.lecturerBlocks = lecturerBlocks;
    }

    public ArrayList<ResourcesBlocked> getLecturerResourcesBlocked() {
        return lecturerResourcesBlocked;
    }

    public void setLecturerResourcesBlocked(ArrayList<ResourcesBlocked> lecturerResourcesBlocked) {
        this.lecturerResourcesBlocked = lecturerResourcesBlocked;
    }

    public SQLConnectionManager getSqlConnectionManager() {
        return sqlConnectionManager;
    }

    public void setSqlConnectionManager(SQLConnectionManager sqlConnectionManager) {
        this.sqlConnectionManager = sqlConnectionManager;
    }

}
