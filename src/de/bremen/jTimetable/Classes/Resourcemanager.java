package de.bremen.jTimetable.Classes;

import java.time.LocalDate;

public class Resourcemanager {
    public void generateInitialTimetable( Coursepass coursepass){
        LocalDate startdate = coursepass.start;
        LocalDate enddate = coursepass.end;

        // order subjects by should hours descending, count total hours, build stack of hours
        // check how many hours are in the coursepass object and if we need to add more hours (more than 3 per day)
        // iterate over every day between startdate and enddate / hour
        // try to place subject, check if the lecturer and the room is free for this date / hour. if not try next subject
        // if placing is possible, remove hour from stack of hours

    }
}
