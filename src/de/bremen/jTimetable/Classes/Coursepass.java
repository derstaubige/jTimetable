package de.bremen.jTimetable.Classes;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

// CREATE TABLE IF NOT EXISTS  `T_Coursepasses` (`id` long not null PRIMARY KEY AUTO_INCREMENT, `refCourseofStudyID` long, `refStudySectionID` long, `start` Date, `end` Date, `active` Boolean,  `description` Char(200), `refRommID` long );
public class Coursepass {
    public Long id;
    CourseofStudy courseofstudy;
    StudySection studysection;
    public LocalDate start;
    public LocalDate end;
    public Boolean active;
    public String description;
    Room room;
    ArrayList<CoursepassLecturerSubject> arraycoursepasslecturersubject = new ArrayList<CoursepassLecturerSubject>();

    public Coursepass(long id) throws SQLException {
        this.id = id;
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        if (this.id == 0){
            //load dummy object
            this.courseofstudy = new CourseofStudy(0L);
            this.studysection = new StudySection(0L);
            this.start = LocalDate.of(1990,1,1);;
            this.end = LocalDate.of(1990,1,1);;
            this.active = Boolean.TRUE;
            this.description = "";
            this.room = new Room(0L);
        }else {
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
            this.room = new Room(rs.getLong("refRommID"));
        }
    }

    public void getCoursepassLecturerSubjects() throws SQLException{
        // load CoursepassLecturerSubjects
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        SQLValues.add(new SQLValueLong(this.id));

        ResultSet rsCoursepassLecturerSubjects = sqlConnectionManager.select("Select id from T_COURSEPASSESLECTURERSUBJECT where REFCOURSEPASSID = ?", SQLValues);

        while (rsCoursepassLecturerSubjects.next()){
            arraycoursepasslecturersubject.add(new CoursepassLecturerSubject(rsCoursepassLecturerSubjects.getLong("id")));
        }
    }


    public void save() throws SQLException{
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
            ResultSet rs = sqlConnectionManager.execute("Insert Into `T_Coursepasses` (`refCourseofStudyID`, `refStudySectionID`, `start`, `end`, `active`,  `description`, `refRommID` ) values (?, ?, ?, ?, ?,? ,?)",SQLValues);
            rs.first();
            this.id = rs.getLong(1);
        }else{
            // we only have to update an existing entry
            SQLValues.add(new SQLValueLong(this.id));
            ResultSet rs = sqlConnectionManager.execute("update `T_Coursepasses` set `refCourseofStudyID` = ?, `refStudySectionID` = ?, `start` = ?, `end` = ?, `active` = ?, `description` = ?, `refRommID` = ? where `id` = ?;",SQLValues);
        }
    }
}
