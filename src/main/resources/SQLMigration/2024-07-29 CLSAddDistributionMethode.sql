--add distributionMethode Column
Alter Table T_COURSEPASSESLECTURERSUBJECT  add column distributionMethode Char(50);

--set all Values to NORMAL
Update T_COURSEPASSESLECTURERSUBJECT set distributionMethode = 'NORMAL';