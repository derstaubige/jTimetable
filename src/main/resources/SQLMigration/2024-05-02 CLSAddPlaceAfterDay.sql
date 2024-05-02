--add placeAfterDay Column
Alter Table T_COURSEPASSESLECTURERSUBJECT  add column placeAfterDay Date;

--set all Values to 1970-01-01
Update T_COURSEPASSESLECTURERSUBJECT set placeAfterDay = '1970-01-01';