package de.bremen.jTimetable.Classes;

// CREATE TABLE IF NOT EXISTS  `T_CoursepassesLecturerSubject` (`id` long not null PRIMARY KEY AUTO_INCREMENT, `refcoursepassID` long, `reflecturerID` long,`refSubjectID` long, `shouldhours` long, `active` Boolean);

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueBoolean;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueLong;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueString;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class CoursepassLecturerSubject {
    Long id;
    Coursepass coursepass;
    Lecturer lecturer;
    Subject subject;
    public Long shouldhours;
    Boolean active;

    public CoursepassLecturerSubject(Long id) throws SQLException {
        this.id = id;
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        if (this.id == 0){
            //load dummy object
            this.coursepass = new Coursepass(0L);
            this.lecturer = new Lecturer(0L);
            this.subject = new Subject(0L);
            this.shouldhours = 0L;
            this.active = Boolean.TRUE;
        }else{
            //load object from db
            SQLValues.add(new SQLValueLong(id));

            ResultSet rs = sqlConnectionManager.select("Select * from T_CoursepassesLecturerSubject where id = ?;",SQLValues);
            rs.first();
            this.id = rs.getLong("id");
            this.coursepass = new Coursepass(rs.getLong("refCoursePassID"));
            this.lecturer = new Lecturer(rs.getLong("refLecturerID"));
            this.subject = new Subject(rs.getLong("refSubjectID"));
            this.shouldhours = rs.getLong("shouldhours");
            this.active = rs.getBoolean("active");
        }
        SQLValues.add(new SQLValueLong(id));


    }
    public void save() throws SQLException{
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        SQLValues.add(new SQLValueLong(this.coursepass.id));
        SQLValues.add(new SQLValueLong(this.lecturer.id));
        SQLValues.add(new SQLValueLong(this.subject.id));
        SQLValues.add(new SQLValueLong(this.shouldhours));
        SQLValues.add(new SQLValueBoolean(this.active));

        if (this.id == 0) {
            // its a new object, we have to insert it
            ResultSet rs = sqlConnectionManager.execute("Insert Into `T_CoursepassesLecturerSubject` (`refCoursePassID`, `refLecturerID`, `refSubjectID`, `shouldhours`, `ACTIVE`) values (?, ?, ?, ?, ?)",SQLValues);
            rs.first();
            this.id = rs.getLong(1);
        }else{
            // we only have to update an existing entry
            SQLValues.add(new SQLValueLong(this.id));
            ResultSet rs = sqlConnectionManager.execute("update `T_CoursepassesLecturerSubject` set `refCoursePassID` = ?, `refLecturerID` = ?, `refSubjectID` = ?, shouldhours` = ?, `ACTIVE` = ? where `id` = ?;",SQLValues);
        }
    }
}
