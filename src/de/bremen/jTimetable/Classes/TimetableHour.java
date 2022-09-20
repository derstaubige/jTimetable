package de.bremen.jTimetable.Classes;

public class TimetableHour {
    int timeslot;
    String lecturerName;
    String subjectCaption;
    String roomCaption;
    CoursepassLecturerSubject coursepassLecturerSubject;

    public TimetableHour(int timeslot, CoursepassLecturerSubject coursepassLecturerSubject, Long RoomID ){
        this.timeslot = timeslot;
        this.coursepassLecturerSubject = coursepassLecturerSubject;
        this.lecturerName = this.coursepassLecturerSubject.lecturer.getLecturerFullName();
        this.subjectCaption = this.coursepassLecturerSubject.subject.getCaption();
        try {
            this.roomCaption = new Room(RoomID).getRoomcaption();

        }catch (Exception e) {
            //ToDo: Real Errorhandling please
            System.out.println(e);
        }


    }
    public TimetableHour(int timeslot, CoursepassLecturerSubject coursepassLecturerSubject) {
        this(timeslot, coursepassLecturerSubject, 0L );
    }

    public int getTimeslot() {
        return timeslot;
    }

    public void setTimeslot(int timeslot) {
        this.timeslot = timeslot;
    }

    public String getLecturerName() {
        return lecturerName;
    }

    public void setLecturerName(String lecturerName) {
        this.lecturerName = lecturerName;
    }

    public String getSubjectCaption() {
        return subjectCaption;
    }

    public void setSubjectCaption(String subjectCaption) {
        this.subjectCaption = subjectCaption;
    }

    public String getRoomCaption() {
        return roomCaption;
    }

    public void setRoomCaption(String roomCaption) {
        this.roomCaption = roomCaption;
    }
}
