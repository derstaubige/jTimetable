package de.bremen.jTimetable.Classes;

import javafx.scene.text.Text;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;


public class JavaFXTimetableDay extends Text {

    LocalDate day;
    String weekday;

    public JavaFXTimetableDay(LocalDate pday){
        super();
        this.day = pday;
        this.weekday = this.day.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());

        super.setText(this.weekday + "\r\n" + day.toString());
    }}
