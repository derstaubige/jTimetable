package de.bremen.jTimetable.Classes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.GregorianCalendar;

public class TimetableWeek {
    private ArrayList<TimetableDay> arrayTimetableDays = new ArrayList<TimetableDay>();
    private LocalDate startDate;
    private LocalDate endDate;
    private GregorianCalendar calendar = new GregorianCalendar();
}
