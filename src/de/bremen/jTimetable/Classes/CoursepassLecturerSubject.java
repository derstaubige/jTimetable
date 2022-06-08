package de.bremen.jTimetable.Classes;

// CREATE TABLE IF NOT EXISTS  `T_CoursepassesLecturerSubject` (`id` long not null PRIMARY KEY AUTO_INCREMENT, `refcoursepassID` long, `reflecturerID` long,`refSubjectID` long, `shouldhours` long, `active` Boolean);

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueLong;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class CoursepassLecturerSubject {
    Long id;
    Coursepass coursepass;
    Lecturer lecturer;
    Subject subject;
    Long shouldhours;
    Boolean active;

    public CoursepassLecturerSubject(Long id) throws SQLException {
        this.id = id;
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        if (this.id == 0){
            // create new coursepass object
            ResultSet rs = sqlConnectionManager.insert("Insert Into `T_CoursepassesLecturerSubject` (`refcoursepassID`, `reflecturerID`, `refSubjectID`, `shouldhours`, `ACTIVE`) values (0, 0, 0, 0, True)",SQLValues);
            //ResultSet rs = sqlConnectionManager.select("select max(id) as id from T_CoursepassesLecturerSubject",SQLValues);
            rs.first();
            this.id = rs.getLong(1);
        }
        SQLValues.add(new SQLValueLong(id));

        ResultSet rs = sqlConnectionManager.select("Select * from T_CoursepassesLecturerSubject where id = ?;",SQLValues);
        while(rs.next()){
            this.id = rs.getLong("id");
            this.coursepass = new Coursepass(rs.getLong("refcoursepassID"));
            this.lecturer = new Lecturer(rs.getLong("reflecturerID"));
            this.subject = new Subject(rs.getLong("refSubjectID"));
            this.shouldhours = rs.getLong("shouldhours");
            this.active = rs.getBoolean("active");
        }
    }
}
