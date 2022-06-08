package de.bremen.jTimetable.Classes;
// CREATE TABLE IF NOT EXISTS  `T_Lecturers` (`id` long not null PRIMARY KEY AUTO_INCREMENT, `firstname` char(200), `lastname` char(200),`reflocationID` long, `active` Boolean);

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueLong;
import jdk.vm.ci.meta.Local;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Lecturer {
    Long id;
    String firstname;
    String lastname;
    Location location;
    Boolean active;

    public Lecturer(Long id) throws SQLException {
        this.id = id;
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();

        if (this.id == 0){
            // create new coursepass object
            ResultSet rs = sqlConnectionManager.insert("Insert Into `T_Lecturers` (`firstname`, `lastname`, `reflocationID`, `ACTIVE`) values ('', '', 0, True)",SQLValues);
            //ResultSet rs = sqlConnectionManager.select("select max(id) as id from T_CoursepassesLecturerSubject",SQLValues);
            rs.first();
            this.id = rs.getLong(1);
        }
        SQLValues.add(new SQLValueLong(id));

        ResultSet rs = sqlConnectionManager.select("Select * from T_CoursepassesLecturerSubject where id = ?;",SQLValues);
        while(rs.next()){
            this.id = rs.getLong("id");
            this.firstname = rs.getString("firstname");
            this.lastname = rs.getString("lastname");
            this.location = new Location(rs.getLong("reflocationID"));
            this.active = rs.getBoolean("active");
        }
    }
}
