package de.bremen.jTimetable.Classes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.*;

public class LecturerBlock {
    private long id;
    private long refLecturerID;
    private DayOfWeek DayNr;
    private LocalDate BlockStart;
    private LocalDate BlockEnd;
    private int timeslot;
    private boolean active;
    private SQLConnectionManager sqlConnectionManager;

    public LecturerBlock(SQLConnectionManager sqlConnectionManager){
        this(0L, sqlConnectionManager);
    }

    public LecturerBlock(long id, SQLConnectionManager sqlConnectionManager){
        setId(id);
        setSqlConnectionManager(sqlConnectionManager);

        if (getId() == 0L){
            setRefLecturerID(0L);
            setDayNr(DayOfWeek.SUNDAY);
            setBlockStart(LocalDate.now());
            setBlockEnd(LocalDate.of(2999, 12, 31));
            setTimeslot(0);
            setActive(true);
        }else{
            //load object from db
            try {
                
                ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();
                SQLValues.add(new SQLValueLong(getId()));

                ResultSet rs = sqlConnectionManager.select("Select * from T_LECTURERBLOCKS where id = ?;", SQLValues);
                rs.first();
                setRefLecturerID(rs.getLong("refLecturerID"));
                setDayNr(DayOfWeek.of( rs.getInt("DayNr")));
                setBlockStart(rs.getDate("BlockStart").toLocalDate());
                setBlockEnd(rs.getDate("BlockEnd").toLocalDate());
                setTimeslot(rs.getInt("timeslot"));
                setActive(rs.getBoolean("active"));
            } catch (SQLException e) {
                System.err.println("LecturerBlock with id:  " + getId() + " could not be loaded in constructor.");
                e.printStackTrace();
            }
        }
    }

    public void save() throws SQLException {
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();

        SQLValues.add(new SQLValueLong(getRefLecturerID()));
        SQLValues.add(new SQLValueInt(getDayNrInt()));
        SQLValues.add(new SQLValueDate(getBlockStart()));
        SQLValues.add(new SQLValueDate(getBlockEnd()));
        SQLValues.add(new SQLValueInt(getTimeslot()));
        SQLValues.add(new SQLValueBoolean(this.active));

        if (this.id == 0) {
            //It's a new object, we have to insert it
            ResultSet rs = sqlConnectionManager.execute("Insert Into `T_LECTURERBLOCKS` (`RefLecturerID`, `DayNr`," +
                    " `BlockStart`, `BlockEnd`, `timeslot`, `ACTIVE`) values (?, ?, ?, ?, ?, ?)", SQLValues);
            rs.first();
            setId(rs.getLong(1));
        } else {
            //We only have to update an existing entry
            SQLValues.add(new SQLValueLong(this.id));
            sqlConnectionManager.execute("update `T_LECTURERBLOCKS` set `RefLecturerID` = ?, `DayNr` = ?, " +
                    "`BlockStart` = ?, `BlockEnd` = ?,`timeslot` = ?, `ACTIVE` = ? where `id` = ?;", SQLValues);
        }
        // sqlConnectionManager.close();
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRefLecturerID() {
        return this.refLecturerID;
    }

    public void setRefLecturerID(long refLecturerID) {
        this.refLecturerID = refLecturerID;
    }

    public DayOfWeek getDayNr() {
        return this.DayNr;
    }

    public int getDayNrInt(){
        return this.DayNr.getValue();
    }

    public void setDayNr(DayOfWeek DayNr) {
        this.DayNr = DayNr;
    }

    public LocalDate getBlockStart() {
        return this.BlockStart;
    }

    public void setBlockStart(LocalDate BlockStart) {
        this.BlockStart = BlockStart;
    }

    public LocalDate getBlockEnd() {
        return this.BlockEnd;
    }

    public void setBlockEnd(LocalDate BlockEnd) {
        this.BlockEnd = BlockEnd;
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean getActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }


    public int getTimeslot() {
        return this.timeslot;
    }

    public void setTimeslot(int timeslot) {
        this.timeslot = timeslot;
    }

    public SQLConnectionManager getSqlConnectionManager() {
        return sqlConnectionManager;
    }

    public void setSqlConnectionManager(SQLConnectionManager sqlConnectionManager) {
        this.sqlConnectionManager = sqlConnectionManager;
    }

}
