package de.bremen.jTimetable.Classes;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

public class Lecturer {


    Long id;
    public String firstname;
    public String lastname;
    Location location;
    public Boolean active;

    public Lecturer(Long id) throws SQLException {
        this.id = id;
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        if (this.id == 0){
            //load dummy object
            this.firstname = "";
            this.lastname = "";
            this.location = new Location(0L);
            this.active = Boolean.TRUE;
        }else{
            //load object from db
            SQLValues.add(new SQLValueLong(id));

            ResultSet rs = sqlConnectionManager.select("Select * from T_Lecturers where id = ?;",SQLValues);
            rs.first();
            this.id = rs.getLong("id");
            this.firstname = rs.getString("firstname").trim();
            this.lastname = rs.getString("lastname").trim();
            this.location = new Location(rs.getLong("reflocationID"));
            this.active = rs.getBoolean("active");
        }

    }
    public void save() throws SQLException{
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        SQLValues.add(new SQLValueString(this.firstname));
        SQLValues.add(new SQLValueString(this.lastname));
        SQLValues.add(new SQLValueLong(this.location.id));
        SQLValues.add(new SQLValueBoolean(this.active));

        if (this.id == 0) {
            // its a new object, we have to insert it
            ResultSet rs = sqlConnectionManager.execute("Insert Into `T_Lecturers` (`firstname`, `lastname`, `reflocationID`, `ACTIVE`) values (?, ?, ?, ?)",SQLValues);
            rs.first();
            this.id = rs.getLong(1);
        }else{
            // we only have to update an existing entry
            SQLValues.add(new SQLValueLong(this.id));
            ResultSet rs = sqlConnectionManager.execute("update `T_Lecturers` set `firstname` = ?, `lastname` = ?, `reflocationID` = ?, `ACTIVE` = ? where `id` = ?;",SQLValues);
        }
    }

    public static boolean checkLecturerAvailability(long lecturerID, LocalDate date, int timeslot)
            throws SQLException {
        LocalDate startdate;
        LocalDate enddate;
        Integer starttimeslot;
        Integer endtimeslot;

        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues =
                new ArrayList<SQLConnectionManagerValues>();
        SQLValues.add(new SQLValueLong(lecturerID));
        SQLValues.add(new SQLValueDate(date));
        SQLValues.add(new SQLValueDate(date));

        ResultSet rs = sqlConnectionManager.select(
                "Select * from T_RESOURCESBLOCKED where Resourcename = 'Lecturer' and refresourceid = ? and STARTDATE <= ? and ENDDATE >= ?;",
                SQLValues);
        while (rs.next()) {
            startdate = rs.getDate("startdate").toLocalDate();
            enddate = rs.getDate("enddate").toLocalDate();
            starttimeslot = rs.getInt("starttimeslot");
            endtimeslot = rs.getInt("endtimeslot");

            // if the date we want to check is the same date as the start of the blocked date range, we can check if the blocking starts after the timestamp we want to reserve
            if (startdate.compareTo(date) == 0) {
                if (starttimeslot <= timeslot) {
                    if (enddate.compareTo(date) == 0 && endtimeslot <
                            timeslot) { //when a lecturer just cant make it to the 2nd timeslot he should be able to teach at 3rd timeslot
                        continue;
                    } else {
                        return false;
                    }
                } else {
                    continue;
                }
            }

            // if the date we want to check is the same date as the end of the blocked date range, we can check if the blocking ends before the timestamp we want to reserve
            if (enddate.compareTo(date) == 0) {
                if (timeslot <= endtimeslot) {
                    return false;
                } else {
                    continue;
                }

            }

            return false;
        }
        // there is no blocking
        return true;
    }
    public Long getId() {
        return id;
    }
    public String getLecturerFullName(){
        return this.lastname + ", " + this.firstname;
    }
}
