package de.bremen.jTimetable.Classes;

import javafx.scene.text.Text;

import java.time.LocalDate;

public class JavaFXTimetableHourText extends Text {

    CoursepassLecturerSubject coursepassLecturerSubject;
    LocalDate day;
    int timeslot;
    private TimetableEntry timetableEntry;
    private SQLConnectionManager sqlConnectionManager;

    public JavaFXTimetableHourText(CoursepassLecturerSubject cls, LocalDate day,
            Integer timeslot, SQLConnectionManager sqlConnectionManager) {
        this(cls, new TimetableEntry(cls, day, timeslot, sqlConnectionManager), false, sqlConnectionManager);
    }

    public JavaFXTimetableHourText(CoursepassLecturerSubject pCoursepassLecturerSubject, TimetableEntry timetableEntry,
            SQLConnectionManager sqlConnectionManager) {
        this(pCoursepassLecturerSubject, timetableEntry, false, sqlConnectionManager);
    }

    public void deleteCLS() {
        this.coursepassLecturerSubject.deleteCLS(this.day, this.timeslot);
    }

    public JavaFXTimetableHourText(CoursepassLecturerSubject pCoursepassLecturerSubject, TimetableEntry timetableEntry,
            Boolean showClassname, SQLConnectionManager sqlConnectionManager) {
        super();
        this.coursepassLecturerSubject = pCoursepassLecturerSubject;
        this.day = timetableEntry.getDate();
        this.timeslot = timetableEntry.getTimeslot();
        this.timetableEntry = timetableEntry;
        String string;
        setSqlConnectionManager(sqlConnectionManager);
        if (showClassname == true) {
            if (timetableEntry.isExam()) {
                string = "\uD83D\uDCD3 " + pCoursepassLecturerSubject.getCoursepass().getDescription() + "\r\n"
                        + pCoursepassLecturerSubject.getSubjectCaption()
                        + "\r\n" + pCoursepassLecturerSubject.getRoomCaptionLocatioString();
            } else {
                string = pCoursepassLecturerSubject.getCoursepass().getDescription() + "\r\n"
                        + pCoursepassLecturerSubject.getSubjectCaption()
                        + "\r\n" + pCoursepassLecturerSubject.getRoomCaptionLocatioString();
            }
            super.setText(string);
        } else {
            if (timetableEntry.isExam()) {
                string = "\uD83D\uDCD3 " + pCoursepassLecturerSubject.getSubjectCaption() + "\r\n"
                        + pCoursepassLecturerSubject.getLecturerFullname()
                        + "\r\n" + pCoursepassLecturerSubject.getRoomCaptionLocatioString();
            } else {
                string = pCoursepassLecturerSubject.getSubjectCaption() + "\r\n"
                        + pCoursepassLecturerSubject.getLecturerFullname()
                        + "\r\n" + pCoursepassLecturerSubject.getRoomCaptionLocatioString();
            }
            super.setText(string);
        }

    }

    public LocalDate getDay() {
        return day;
    }

    public int getTimeslot() {
        return timeslot;
    }

    public CoursepassLecturerSubject getCoursepassLecturerSubject() {
        return coursepassLecturerSubject;
    }

    public SQLConnectionManager getSqlConnectionManager() {
        return sqlConnectionManager;
    }

    public void setSqlConnectionManager(SQLConnectionManager sqlConnectionManager) {
        this.sqlConnectionManager = sqlConnectionManager;
    }

    public TimetableEntry getTimetableEntry() {
        return timetableEntry;
    }

    public void setTimetableEntry(TimetableEntry timetableEntry) {
        this.timetableEntry = timetableEntry;
    }

}
