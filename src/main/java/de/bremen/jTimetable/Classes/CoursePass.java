package de.bremen.jTimetable.Classes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.*;

/**
 * One CourseOfStudy in a specific study section can attend classes in a timeframe = CoursePass
 *
 * @author Arne Czyborra, Loreen Roose
 */
public class CoursePass {
    /**
     * ID of this CoursePass that is unique when saved in the database.
     */
    private Long id;
    /**
     * CourseOfStudy associated with this CoursePass.
     */
    private CourseofStudy courseOfStudy;
    /**
     * StudySection with this CoursePass.
     */
    private StudySection studySection;
    /**
     * StartDate of this CoursePass.
     */
    private LocalDate start;
    /**
     * EndDate of this CoursePass.
     */
    private LocalDate end;
    /**
     * Boolean that defines if the CoursePass is active or not.
     */
    private Boolean active;
    /**
     * Description of this CoursePass.
     */
    private String description;
    /**
     * Room associated with this CoursePass.
     */
    private Room room;
    /**
     * List with all CoursePassLecturerSubjects associated with this CoursePass.
     */
    public ArrayList<CoursepassLecturerSubject> arrayCoursePassLecturerSubject = new ArrayList<>();
    private SQLConnectionManager sqlConnectionManager;

    /**
     * Constructor that creates a dummy object if the id is 0 or loads an already existing object from the
     * database with the given id
     *
     * @param id if id = 0 dummy object is created otherwise object with this id is loaded
     */
    public CoursePass(long id, SQLConnectionManager sqlConnectionManager) {
        this.id = id;
        setSqlConnectionManager(sqlConnectionManager);
        try {
            
            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();

            if (this.id == 0) {
                //load dummy object
                this.courseOfStudy = new CourseofStudy(0L, getSqlConnectionManager());
                this.studySection = new StudySection(0L, getSqlConnectionManager());
                this.start = LocalDate.of(1990, 1, 1);
                this.end = LocalDate.of(1990, 1, 1);
                this.active = Boolean.TRUE;
                this.description = "";
                this.room = new Room(0L, getSqlConnectionManager());
            } else {
                //load object from db
                SQLValues.add(new SQLValueLong(id));

                ResultSet rs = sqlConnectionManager.select("Select * from T_Coursepasses where id = ?;", SQLValues);
                rs.first();
                this.id = rs.getLong("id");
                this.courseOfStudy = new CourseofStudy(rs.getLong("REFCOURSEOFSTUDYID"), getSqlConnectionManager());
                this.studySection = new StudySection(rs.getLong("REFSTUDYSECTIONID"), getSqlConnectionManager());
                this.start = rs.getDate("start").toLocalDate();
                this.end = rs.getDate("end").toLocalDate();
                this.active = rs.getBoolean("active");
                this.description = rs.getString("description");
                this.room = new Room(rs.getLong("refRoomID"), getSqlConnectionManager());
            }
        } catch (SQLException e) {
            System.err.println("The CoursePass with the id: " + this.id +
                    " could not be created/loaded in the constructor correct.");
            e.printStackTrace();
        }
        
    }

    /**
     * Method empties out this.arrayCoursePassLecturerSubject and reloads the values from the database for this
     * CoursePass id.
     */
    public void updateCoursePassLecturerSubjects() {
        // load CoursePassLecturerSubjects

        //Empty arrayList
        this.arrayCoursePassLecturerSubject = new ArrayList<>();

        try {
            
            ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();

            SQLValues.add(new SQLValueLong(this.id));

            ResultSet rsCoursePassLecturerSubjects = sqlConnectionManager.select
                    ("Select id from T_COURSEPASSESLECTURERSUBJECT where REFCOURSEPASSID = ?", SQLValues);

            while (rsCoursePassLecturerSubjects.next()) {
                arrayCoursePassLecturerSubject.add
                        (new CoursepassLecturerSubject(rsCoursePassLecturerSubjects.getLong("id"), getSqlConnectionManager()));
            }
        } catch (SQLException e) {
            System.err.println("Updating the CoursePassLecturerSubjects of CoursePass with the id: "
                    + this.id + " wasn't successful.");
            e.printStackTrace();
        }

        //Sort the array after shouldHours descending
        Collections.sort(this.arrayCoursePassLecturerSubject);
        Collections.reverse(this.arrayCoursePassLecturerSubject);
    }

    /**
     * Method saves this object in the database by updating an existing entry (id != 0) or inserting
     * a new entry (id = 0)
     */
    public void save() {
        //Set up SQLValues
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();

        SQLValues.add(new SQLValueLong(this.courseOfStudy.id));
        SQLValues.add(new SQLValueLong(this.studySection.id));
        SQLValues.add(new SQLValueDate(this.start));
        SQLValues.add(new SQLValueDate(this.end));
        SQLValues.add(new SQLValueBoolean(this.active));
        SQLValues.add(new SQLValueString(this.description));
        SQLValues.add(new SQLValueLong(this.room.id));

        try {
            
            if (this.id == 0) {
                //It's a new object, we have to insert it
                ResultSet rs = sqlConnectionManager.execute("Insert Into `T_Coursepasses` " +
                        "(`refCourseofStudyID`, `refStudySectionID`, `start`, `end`, `active`,  `description`," +
                        " `refRoomID` ) values (?, ?, ?, ?, ?,? ,?)", SQLValues);
                rs.first();
                //Get the correct (auto incremented) id
                this.id = rs.getLong(1);
            } else {
                //We only have to update an existing entry
                SQLValues.add(new SQLValueLong(this.id));
                sqlConnectionManager.execute("update `T_Coursepasses` set `refCourseofStudyID` = ?, " +
                        "`refStudySectionID` = ?, `start` = ?, `end` = ?, `active` = ?, `description` = ?," +
                        " `refRoomID` = ? where `id` = ?;", SQLValues);
            }
        } catch (SQLException e) {
            System.err.println("Saving CoursePass with the  id:" + this.id + " wasn't successful.");
            e.printStackTrace();
        }
    }

    /**
     * Method selects all active/inactive CoursePasses from the database
     *
     * @param activeStatus defines whether active CoursePasses (true) or inactive ones (false) are selected
     * @return list of active or inactive CoursePasses
     * @throws SQLException is thrown if database query to get CoursePasses fails
     */
    public static ArrayList<CoursePass> getCoursePasses(Boolean activeStatus, SQLConnectionManager sqlConnectionManager) throws SQLException {
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();
        SQLValues.add(new SQLValueBoolean(activeStatus));
        ResultSet rs = sqlConnectionManager.select("Select * from T_Coursepasses where active = ?", SQLValues);
        ArrayList<CoursePass> returnList = new ArrayList<>();

        while (rs.next()) {
            returnList.add(new CoursePass(rs.getLong("id"), sqlConnectionManager));
        }
        // sqlConnectionManager.close();
        return returnList;
    }

    /**
     * Method selects all active/inactive CoursePassLecturerSubjects from the database
     *
     * @param activeStatus defines whether active CoursePassLecturerSubjects (true) or inactive ones (false) are selected
     * @return list of active or inactive CoursePassLecturerSubjects
     * @throws SQLException is thrown if database query to get coursePassLecturerSubjects fails
     */
    public ArrayList<CoursepassLecturerSubject> getAllCLS(Boolean activeStatus) throws SQLException {
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();
        SQLValues.add(new SQLValueBoolean(activeStatus));
        SQLValues.add(new SQLValueLong(this.id));
        ResultSet rs = sqlConnectionManager.select("Select * from  T_COURSEPASSESLECTURERSUBJECT  where active = ? and REFCOURSEPASSID = ?", SQLValues);
        ArrayList<CoursepassLecturerSubject> returnList = new ArrayList<>();
        while (rs.next()) {
            returnList.add(new CoursepassLecturerSubject(rs.getLong("id"), getSqlConnectionManager()));
        }
        // sqlConnectionManager.close();
        return returnList;
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
     * Getter for this.courseOfStudy
     *
     * @return this.courseOfStudy
     */
    public CourseofStudy getCourseOfStudy() {
        return courseOfStudy;
    }

    /**
     * Getter for this.studySection
     *
     * @return this.studySection
     */
    public StudySection getStudySection() {
        return studySection;
    }

    /**
     * Getter for this.start
     *
     * @return this.start
     */
    public LocalDate getStart() {
        return start;
    }

    /**
     * Getter for this.end
     *
     * @return this.end
     */
    public LocalDate getEnd() {
        return end;
    }

    /**
     * Getter for this.active
     *
     * @return true if coursePass is active, false if not
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * Getter for this.description
     *
     * @return trimmed description
     */
    public String getDescription() {
        return description.trim();
    }

    /**
     * Getter for this.room
     *
     * @return this.room
     */
    public Room getRoom() {
        return room;
    }

    /**
     * Getter for caption of this.courseOfStudy
     *
     * @return description of this.courseOfStudy
     */
    public String getCourseOfStudyCaption() {
        return this.courseOfStudy.getCaption().trim();
    }

    /**
     * Getter for the description of this.studySection
     *
     * @return trimmed description of this.studySection
     */
    public String getCPStudySection() {
        return this.studySection.getDescription().trim();
    }

    /**
     * Getter for this.arrayCoursePassLecturerSubject
     *
     * @return this.arrayCoursePassLecturerSubject
     */
    public ArrayList<CoursepassLecturerSubject> getArrayCoursePassLecturerSubject() {
        return this.arrayCoursePassLecturerSubject;
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
     * Setter for this.courseOfStudy
     *
     * @param courseOfStudy new value for this.courseOfStudy
     */
    public void setCourseOfStudy(CourseofStudy courseOfStudy) {
        this.courseOfStudy = courseOfStudy;
    }

    /**
     * Setter for this.studySection
     *
     * @param studySection new value for this.studySection
     */
    public void setStudySection(StudySection studySection) {
        this.studySection = studySection;
    }

    /**
     * Setter for this.start
     *
     * @param start new value for this.start
     */
    public void setStart(LocalDate start) {
        this.start = start;
    }

    /**
     * Setter for this.end
     *
     * @param end new value for this.end
     */
    public void setEnd(LocalDate end) {
        this.end = end;
    }

    /**
     * Setter for this.active
     *
     * @param active new value for this.active
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * Setter for this.description
     *
     * @param description new value for this.description
     */
    public void setDescription(String description) {
        this.description = description.trim();
    }

    /**
     * Setter for this.room
     *
     * @param room new value for this.room
     */
    public void setRoom(Room room) {
        this.room = room;
    }

    /**
     * Setter for the description of this.courseOfStudy
     *
     * @param courseOfStudyCaption new value for the description of this.courseOfStudy
     */
    public void setCourseOfStudyCaption(String courseOfStudyCaption) {
        this.courseOfStudy.setCaption(courseOfStudyCaption);
    }

    /**
     * Setter for the description of this.studySection
     *
     * @param CPStudySection new value for the description of this.studySection
     */
    public void setCPStudySection(String CPStudySection) {
        this.studySection.setDescription(CPStudySection);
    }

    public SQLConnectionManager getSqlConnectionManager() {
        return sqlConnectionManager;
    }

    public void setSqlConnectionManager(SQLConnectionManager sqlConnectionManager) {
        this.sqlConnectionManager = sqlConnectionManager;
    }
    
}
