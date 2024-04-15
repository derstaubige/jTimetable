--add rommid
Alter Table T_TIMETABLES add column isExam int;

--set roomid to 0
Update T_TIMETABLES set isExam = 0 where isExam is null;