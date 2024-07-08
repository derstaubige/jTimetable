--add placeAfterDay Column
Alter Table T_COURSEPASSESLECTURERSUBJECT  add column placeAfterCLS Integer;

--set all Values to 1970-01-01
Update T_COURSEPASSESLECTURERSUBJECT set placeAfterCLS = 0;