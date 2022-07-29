'#// saves the actual Timetable
CREATE TABLE IF NOT EXISTS  `T_Timetables` (`id` long not null PRIMARY KEY AUTO_INCREMENT, `timetableday` Date,  `refCoursePass` long, `refCoursepassLecturerSubject` long,  `refRoomID` long, `refLecturer`long, `refSubject`long, timeslot int);
'#//saves dates when a specific resource isnt avaiable
CREATE TABLE IF NOT EXISTS  `T_RESOURCESBLOCKED` (`id` long not null PRIMARY KEY AUTO_INCREMENT,  `refResourceID` long, `Resourcename` Char(200), `STARTDATE` Date, `ENDDATE` DATE, `STARTTIMESLOT` INTEGER, `ENDTIMESLOT` INTEGER,  `description` char(200));