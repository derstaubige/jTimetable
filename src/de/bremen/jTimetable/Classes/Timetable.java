package de.bremen.jTimetable.Classes;

import javafx.stage.FileChooser;

import java.util.ArrayList;

public class Timetable  {
    ArrayList<TimetableDay> arrayTimetableDays;
    Coursepass coursepass;

    public Timetable(Coursepass coursepass){
        this.coursepass = coursepass;
        try{
            this.arrayTimetableDays = this.coursepass.getTimetable();
        }catch (Exception e){
            //ToDO: Errorhandling
            System.out.println(e);
        }
    }

    public ArrayList<TimetableDay> getArrayTimetableDays() {
        return arrayTimetableDays;
    }

    public void exportTimetableToFile(){
        //https://docs.oracle.com/javafx/2/ui_controls/file-chooser.htm
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
//        fileChooser.showOpenDialog();
    }

}
