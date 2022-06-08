package de.bremen.jTimetable.Classes;
import de.bremen.jTimetable.Classes.SQLConnectionManager;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueLong;

import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

// CREATE TABLE IF NOT EXISTS  `T_Coursepasses` (`id` long not null PRIMARY KEY AUTO_INCREMENT, `refCourseofStudyID` long, `refStudySectionID` long, `start` Date, `end` Date, `active` Boolean,  `description` Char(200), `refRommID` long );
public class Coursepass {
    Long id;
    CourseofStudy courseofstudy;
    StudySection studysection;
    LocalDate start;
    LocalDate end;
    Boolean active;
    String description;
    Room room;
    CoursepassLecturerSubject[] arraycoursepasslecturersubject;

    public Coursepass(long id) throws SQLException {
        this.id = id;
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        if (this.id == 0){
            // create new coursepass object
            ResultSet rs = sqlConnectionManager.insert("Insert Into `T_Coursepasses` (`refCourseofStudyID`, `refStudySectionID`, `START`, `END`, `ACTIVE`, `DESCRIPTION`, `refRommID`) values (0, 0, '1990-01-01', '1990-01-01', True, '', 0)",SQLValues);
            //ResultSet rs = sqlConnectionManager.select("select max(id) as id from T_Coursepasses",SQLValues);
            rs.first();
            this.id = rs.getLong(1);
        }
        SQLValues.add(new SQLValueLong(id));

        ResultSet rs = sqlConnectionManager.select("Select * from T_Coursepasses where id = ?;",SQLValues);
        while(rs.next()){
            this.id = rs.getLong("id");
            this.courseofstudy = new CourseofStudy(rs.getLong("REFCOURSEOFSTUDYID"));
            this.studysection = new StudySection(rs.getLong("REFSTUDYSECTIONID"));
            this.start = rs.getDate("start").toLocalDate();
            this.end = rs.getDate("end").toLocalDate();
            this.active = rs.getBoolean("active");
            this.description = "";
            this.room = new Room(rs.getLong("refRommID"));
        }
    }
}
