package de.bremen.jTimetable.Classes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.*;
public class Coursepass {
    public Long id;
    CourseofStudy courseofstudy;
    StudySection studysection;
    public LocalDate start;
    public LocalDate end;
    public Boolean active;
    public String description;
    Room room;
    public ArrayList<CoursepassLecturerSubject> arraycoursepasslecturersubject = new ArrayList<CoursepassLecturerSubject>();


    public Coursepass(long id) throws SQLException {
        this.id = id;
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        if (this.id == 0) {
            //load dummy object
            this.courseofstudy = new CourseofStudy(0L);
            this.studysection = new StudySection(0L);
            this.start = LocalDate.of(1990, 1, 1);
            ;
            this.end = LocalDate.of(1990, 1, 1);
            ;
            this.active = Boolean.TRUE;
            this.description = "";
            this.room = new Room(0L);
        } else {
            //load object from db
            SQLValues.add(new SQLValueLong(id));

            ResultSet rs = sqlConnectionManager.select("Select * from T_Coursepasses where id = ?;", SQLValues);
            rs.first();
            this.id = rs.getLong("id");
            this.courseofstudy = new CourseofStudy(rs.getLong("REFCOURSEOFSTUDYID"));
            this.studysection = new StudySection(rs.getLong("REFSTUDYSECTIONID"));
            this.start = rs.getDate("start").toLocalDate();
            this.end = rs.getDate("end").toLocalDate();
            this.active = rs.getBoolean("active");
            this.description = rs.getString("description");
            this.room = new Room(rs.getLong("refRoomID"));

        }
    }

    public void updateCoursepassLecturerSubjects() throws SQLException {
        // load CoursepassLecturerSubjects

        // empty arraylist and reload everything
        this.arraycoursepasslecturersubject.removeAll(this.arraycoursepasslecturersubject);

        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        SQLValues.add(new SQLValueLong(this.id));

        ResultSet rsCoursepassLecturerSubjects = sqlConnectionManager.select("Select id from T_COURSEPASSESLECTURERSUBJECT where REFCOURSEPASSID = ?", SQLValues);

        while (rsCoursepassLecturerSubjects.next()) {
            arraycoursepasslecturersubject.add(new CoursepassLecturerSubject(rsCoursepassLecturerSubjects.getLong("id")));
        }

        //check if array is empty, then add dummy object
//        if(arraycoursepasslecturersubject.size() == 0){
//            arraycoursepasslecturersubject.add(new CoursepassLecturerSubject(0L));
//        }

        //sort the array after shouldhours descending
        Collections.sort(this.arraycoursepasslecturersubject);
        Collections.reverse(this.arraycoursepasslecturersubject);
    }


    public void save() throws SQLException {
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        SQLValues.add(new SQLValueLong(this.courseofstudy.id));
        SQLValues.add(new SQLValueLong(this.studysection.id));
        SQLValues.add(new SQLValueDate(this.start));
        SQLValues.add(new SQLValueDate(this.end));
        SQLValues.add(new SQLValueBoolean(this.active));
        SQLValues.add(new SQLValueString(this.description));
        SQLValues.add(new SQLValueLong(this.room.id));

        if (this.id == 0) {
            // its a new object, we have to insert it
            ResultSet rs = sqlConnectionManager.execute("Insert Into `T_Coursepasses` (`refCourseofStudyID`, `refStudySectionID`, `start`, `end`, `active`,  `description`, `refRoomID` ) values (?, ?, ?, ?, ?,? ,?)", SQLValues);
            rs.first();
            this.id = rs.getLong(1);
        } else {
            // we only have to update an existing entry
            SQLValues.add(new SQLValueLong(this.id));
            sqlConnectionManager.execute("update `T_Coursepasses` set `refCourseofStudyID` = ?, `refStudySectionID` = ?, `start` = ?, `end` = ?, `active` = ?, `description` = ?, `refRoomID` = ? where `id` = ?;", SQLValues);
        }
    }

    public static ArrayList<Coursepass> getCoursepasses(Boolean activeStatus) throws SQLException {
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();
        SQLValues.add(new SQLValueBoolean(activeStatus));
        ResultSet rs = sqlConnectionManager.select("Select * from T_Coursepasses where active = ?", SQLValues);
        ArrayList<Coursepass> returnList = new ArrayList<>();

        while (rs.next()) {
            returnList.add(new Coursepass(rs.getLong("id")));
        }
        return returnList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CourseofStudy getCourseofstudy() {
        return courseofstudy;
    }

    public void setCourseofstudy(CourseofStudy courseofstudy) {
        this.courseofstudy = courseofstudy;
    }

    public StudySection getStudysection() {
        return studysection;
    }

    public void setStudysection(StudySection studysection) {
        this.studysection = studysection;
    }

    public LocalDate getStart() {
        return start;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public void setEnd(LocalDate end) {
        this.end = end;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description.trim();
    }

    public void setDescription(String description) {
        this.description = description.trim();
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public String getCourseofstudycaption() {
        return this.courseofstudy.getCaption();
    }

    public void setCourseofstudycaption(String courseofstudyCpation) {
        this.courseofstudy.setCaption(courseofstudyCpation);
    }

    public String getCPstudysection() {
        return this.studysection.getDescription().trim();
    }

    public void setCPstudysection(String CPStudySection) {
        this.studysection.setDescription(CPStudySection);
    }

    public ArrayList<CoursepassLecturerSubject> getArraycoursepasslecturersubject() {
        return arraycoursepasslecturersubject;
    }
    public ArrayList<CoursepassLecturerSubject> getAllCLS(Boolean activeStatus) throws SQLException{
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();
        SQLValues.add(new SQLValueBoolean(activeStatus));
        SQLValues.add(new SQLValueLong(this.id));
        ResultSet rs = sqlConnectionManager.select("Select * from  T_COURSEPASSESLECTURERSUBJECT  where active = ? and REFCOURSEPASSID = ?",SQLValues);
        ArrayList<CoursepassLecturerSubject> returnList = new ArrayList<CoursepassLecturerSubject>();
        while( rs.next() ){
            returnList.add(new CoursepassLecturerSubject(rs.getLong("id")));
        }
        return returnList;
    }
}
