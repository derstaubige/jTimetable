package de.bremen.jTimetable.Classes;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
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

    /**
     * Returns all lessons that are already planned and executes generateInitialTimetable so there will always be
     * a timetable even if non was generated yet.
     *
     * @return ResultSet with dates and lessons
     */
    public ArrayList<TimetableDay> getTimetable() throws SQLException {
        //Generate a Timetable if non exists yet
        Resourcemanager resourcemanager = new Resourcemanager();
        //resourcemanager.generateInitialTimetable(this);
        //Create new SQLValues that are used for the following select statement
        ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<>();
        SQLValues.add(new SQLValueLong(this.id));
        //Create new Connection to database
        SQLConnectionManager sqlConnectionManager = new SQLConnectionManager();
        ResultSet rs = sqlConnectionManager.select("Select * From T_TIMETABLES where REFCOURSEPASS=? ORDER BY TIMETABLEDAY, TIMESLOT ASC;", SQLValues);
        ArrayList<TimetableDay> result = new ArrayList<TimetableDay>();

        while (rs.next()) {
            //Check if the day of this recordset is allready an timetableday object. if so select the existing object

            TimetableDay tmpDayObject = null;
            Long tmpTimeslot = rs.getLong("Timeslot");
            LocalDate tmpDate = rs.getDate("TIMETABLEDAY").toLocalDate();
            for (TimetableDay day : result) {
                if (day.getDate().isEqual(tmpDate)){
                    tmpDayObject = day;
                    break;
                }
            }
            // if not create a new object
            if (tmpDayObject == null){
                tmpDayObject = new TimetableDay(rs.getDate("TIMETABLEDAY").toLocalDate());
                result.add(tmpDayObject);
            }

            //ToDo: Check if timeslot is already filled?

            //Check if we have to add to the max timeslots per day
            //ToDo: i guess we will crash here if we dont fill up our array of empty TimetableHours, aka index out of bounds
            if (tmpDayObject.getTimeslots() <= tmpTimeslot){
                tmpDayObject.setTimeslots(tmpTimeslot.intValue());
            }
            //System.out.println(rs.getLong("REFCOURSEPASSLECTURERSUBJECT"));
            //add this timeslot/TimetableHour to our tmpDayObject
            tmpDayObject.getArrayTimetableDay().set(tmpTimeslot.intValue(), new TimetableHour(tmpTimeslot.intValue(), new CoursepassLecturerSubject(rs.getLong("REFCOURSEPASSLECTURERSUBJECT"))));

        }

        //if we havent add CoursepassLecturerSubjects for this Coursepass yet, we should return an empty array to display
        if(result.size() == 0){
            result = resourcemanager.getWorkingDaysBetweenTwoDates(start, end);
            for(TimetableDay tmpTimetableDay : result){

                ArrayList<TimetableHour> tmpArrayList = new ArrayList<>();
                tmpArrayList.add(new TimetableHour(0, new CoursepassLecturerSubject(0L)));
                tmpArrayList.add(new TimetableHour(1, new CoursepassLecturerSubject(0L)));
                tmpArrayList.add(new TimetableHour(2, new CoursepassLecturerSubject(0L)));
                tmpTimetableDay.setArrayTimetableDay(tmpArrayList);
            }

        }

        return result;
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
