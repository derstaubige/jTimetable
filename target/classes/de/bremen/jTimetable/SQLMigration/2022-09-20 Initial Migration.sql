--Course of Study'
CREATE TABLE IF NOT EXISTS  `T_CoursesofStudy` (`id` long not null PRIMARY KEY AUTO_INCREMENT, `caption` char(60), `begin` Date, `end` Date, `active` Boolean );

--Coursepass'
CREATE TABLE IF NOT EXISTS  `T_Coursepasses` (`id` long not null PRIMARY KEY AUTO_INCREMENT, `refCourseofStudyID` long, `refStudySectionID` long, `start` Date, `end` Date, `active` Boolean,  `description` Char(200), `refRoomID` long );

--CoursepassLecturerSubject'
CREATE TABLE IF NOT EXISTS  `T_CoursepassesLecturerSubject` (`id` long not null PRIMARY KEY AUTO_INCREMENT, `refcoursepassID` long, `reflecturerID` long,`refSubjectID` long, `shouldhours` long, `active` Boolean);

--Lecturer'
CREATE TABLE IF NOT EXISTS  `T_Lecturers` (`id` long not null PRIMARY KEY AUTO_INCREMENT, `firstname` char(200), `lastname` char(200),`reflocationID` long, `active` Boolean);

--Locations'
CREATE TABLE IF NOT EXISTS  `T_Locations` (`id` long not null PRIMARY KEY AUTO_INCREMENT, `caption` char(200) , `active` Boolean );

--Room'
CREATE TABLE IF NOT EXISTS  `T_Rooms` (`id` long not null PRIMARY KEY AUTO_INCREMENT, `roomcaption`char(200) , `refLocationID` long,  `active` Boolean );

--Study Sections'
CREATE TABLE IF NOT EXISTS  `T_StudySections` (`id` long not null PRIMARY KEY AUTO_INCREMENT, `description` Char(200), `active` Boolean );

--Subjects'
CREATE TABLE IF NOT EXISTS  `T_Subjects` (`id` long not null PRIMARY KEY AUTO_INCREMENT, `caption` char(200), `active` Boolean);

--saves the actual Timetable
CREATE TABLE IF NOT EXISTS  `T_Timetables` (`id` long not null PRIMARY KEY AUTO_INCREMENT, `timetableday` Date,  `refCoursePass` long, `refCoursepassLecturerSubject` long,  `refRoomID` long, `refLecturer`long, `refSubject`long, timeslot int);

--saves dates when a specific resource isnt avaiable
CREATE TABLE IF NOT EXISTS  `T_RESOURCESBLOCKED` (`id` long not null PRIMARY KEY AUTO_INCREMENT,  `refResourceID` long, `Resourcename` Char(200), `STARTDATE` Date, `ENDDATE` DATE, `STARTTIMESLOT` INTEGER, `ENDTIMESLOT` INTEGER,  `description` char(200));

--migration table'
create table if not exists `T_Migration` (`id` long not null PRIMARY KEY AUTO_INCREMENT, `MigrationName` char(200), `MigrationDate`  Date)