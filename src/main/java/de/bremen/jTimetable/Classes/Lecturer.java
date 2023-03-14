package de.bremen.jTimetable.Classes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.*;

/**
 * Class represents a lecturer that can teach classes and is saved in database table T_Lecturers.
 * @author Arne Czyborra, Loreen Roose
 */
public class Lecturer {

    /**
     * ID the lecturer has in database, is 0 if the lecturer isn't saved in database yet.
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

    /**
     * Constructor that creates a new empty object if the 0 is passed and loads an existing object from the
     * database if id isn't 0.
     *
     * @param id the lecturer has in database, 0 if not saved in database yet
     */
    public Lecturer(Long id) {
        this.id = id;

        if (this.id == 0) {
            //load dummy object
            this.firstname = "";
            this.lastname = "";
            this.location = new Location(0L);
            this.active = Boolean.TRUE;
        } else {
            //load object from db
            try {
                SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
                ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();
                SQLValues.add(new SQLValueLong(id));

                ResultSet rs = sqlConnectionManager.select("Select * from T_Lecturers where id = ?;", SQLValues);
                rs.first();
                this.id = rs.getLong("id");
                this.firstname = rs.getString("firstname").trim();
                this.lastname = rs.getString("lastname").trim();
                this.location = new Location(rs.getLong("reflocationID"));
                this.active = rs.getBoolean("active");
            } catch (SQLException e) {
                System.err.println("Lecturer with id:  " + this.firstname + " could not be loaded in constructor.");
                e.printStackTrace();
            }
        }

    }

    /**
     * This lecturer instance is saved in the database. If the id is still 0 a new object will be inserted, if the
     * object already has its own id it will be updated.
     *
     * @throws SQLException is thrown if inserting into / updating the database entry doesn't work
     */
    public void save() throws SQLException {
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();

        SQLValues.add(new SQLValueString(this.firstname));
        SQLValues.add(new SQLValueString(this.lastname));
        SQLValues.add(new SQLValueLong(this.location.id));
        SQLValues.add(new SQLValueBoolean(this.active));

        if (this.id == 0) {
            //It's a new object, we have to insert it
            ResultSet rs = sqlConnectionManager.execute("Insert Into `T_Lecturers` (`firstname`, `lastname`," +
                    " `reflocationID`, `ACTIVE`) values (?, ?, ?, ?)", SQLValues);
            rs.first();
            this.id = rs.getLong(1);
        } else {
            //We only have to update an existing entry
            SQLValues.add(new SQLValueLong(this.id));
            sqlConnectionManager.execute("update `T_Lecturers` set `firstname` = ?, `lastname` = ?, " +
                    "`reflocationID` = ?, `ACTIVE` = ? where `id` = ?;", SQLValues);
        }
    }

    /**
     * Checks if a lecturer is available at a certain date and a certain timeslot and a lesson can be placed by
     * checking if the timeslot is not set in the database.
     *
     * @param lecturerID id of the lecturer whose availability is checked
     * @param date       defines the date that is checked for availability
     * @param timeslot   defines the timeslot that is checked for availability
     * @return true if lecturer is available, false if lecturer is blocked
     * @throws SQLException will be thrown if select statement doesn't work or accessing resultSet is invalid
     */
    public static boolean checkLecturerAvailability(long lecturerID, LocalDate date, int timeslot)
            throws SQLException {

        LocalDate startDate;
        LocalDate endDate;
        int startTimeslot;
        int endTimeslot;

        //Database query
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues =
                new ArrayList<>();
        SQLValues.add(new SQLValueLong(lecturerID));
        SQLValues.add(new SQLValueDate(date));
        SQLValues.add(new SQLValueDate(date));
        ResultSet rs = sqlConnectionManager.select(
                "Select * from T_RESOURCESBLOCKED where Resourcename = '" + ResourceNames.LECTURER +
                        "' and refresourceid = ? and STARTDATE <= ? and ENDDATE >= ?;",
                SQLValues);

        while (rs.next()) {
            //Current database entry
            startDate = rs.getDate("startdate").toLocalDate();
            endDate = rs.getDate("enddate").toLocalDate();
            startTimeslot = rs.getInt("starttimeslot");
            endTimeslot = rs.getInt("endtimeslot");

            //If the date we want to check is the same date as the start of the blocked date range,
            // we can check if the blocking starts after the timeslot we want to reserve or ends before it
            if (startDate.compareTo(date) == 0) {
                if (startTimeslot <= timeslot) {
                    if (endDate.compareTo(date) == 0 && endTimeslot <
                            timeslot) {
                        //when a lecturer just can't make it to the 2nd timeslot he should be able
                        // to teach at 3rd timeslot
                        continue;
                    } else {
                        return false;
                    }
                } else {
                    continue;
                }
            }

            //If the date we want to check is the same date as the end of the blocked date range, we can check if the
            // blocking ends before the timestamp we want to reserve
            if (endDate.compareTo(date) == 0) {
                if (timeslot <= endTimeslot) {
                    return false;
                }
//                else {
//                    continue;
//                }
            }

            //TODO why?
            //return false;
        }
        //There is no blocking
        return true;
    }

    /**
     * Method runs select statement on database to get all active/inactive lecturers.
     *
     * @param activeStatus defines whether active or inactive lecturers are selected (true = active; false = inactive)
     * @return ArrayList with all lecturers of the given status
     */
    public static ArrayList<Lecturer> getAllLecturer(Boolean activeStatus) {
        try {
            SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();
            SQLValues.add(new SQLValueBoolean(activeStatus));
            ResultSet rs = sqlConnectionManager.select("Select * from T_Lecturers where active = ?", SQLValues);
            ArrayList<Lecturer> returnList = new ArrayList<Lecturer>();
            while (rs.next()) {
                returnList.add(new Lecturer(rs.getLong("id")));
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
        return ResourcesBlocked.getArrayListofResourcesblocked(this.id, ResourceNames.LECTURER);
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

    //TODO do all setter have to update the lecturer in the database?

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
}
