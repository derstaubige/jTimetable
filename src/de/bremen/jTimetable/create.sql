'#// saves the actual Timetable
CREATE TABLE IF NOT EXISTS  `T_Timetables` (`id` long not null PRIMARY KEY AUTO_INCREMENT, `day` Date,  `refCoursePass` long, `refCoursepassLecturerSubject` long,  `refRoomID` long, `refLecturer`long, `refSubject`long);
'#//saves dates when a specific resource isnt avaiable
CREATE TABLE IF NOT EXISTS  `T_RESOURCESBLOCKED` (`id` long not null PRIMARY KEY AUTO_INCREMENT,  `refResourceID` long, `Resourcename` Char(200), `start` Date, `end` Date,  `description` char(200));