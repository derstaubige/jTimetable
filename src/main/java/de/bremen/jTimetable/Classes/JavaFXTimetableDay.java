package de.bremen.jTimetable.Classes;

import javafx.scene.text.Text;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;


public class JavaFXTimetableDay extends Text {

    LocalDate day;
    String weekday;
    SQLConnectionManager sqlConnectionManager;

    public JavaFXTimetableDay(LocalDate pday, SQLConnectionManager sqlConnectionManager){
        super();
        this.day = pday;
        setSqlConnectionManager(sqlConnectionManager);
        this.weekday = this.day.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault());

        super.setText(this.weekday + "\r\n" + day.toString());
    }

    public SQLConnectionManager getSqlConnectionManager() {
        return sqlConnectionManager;
    }

    public void setSqlConnectionManager(SQLConnectionManager sqlConnectionManager) {
        this.sqlConnectionManager = sqlConnectionManager;
    }

}
