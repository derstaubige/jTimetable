package de.bremen.jTimetable.Classes.SQLConnectionManagerValues;

import java.util.Date;

public class SQLValueDate extends SQLConnectionManagerValues{
    private final String type;
    private final Date value;

    public SQLValueDate(Date value1) {
        this.value = value1;
        this.type = "Date";
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Date getValue() {
        return value;
    }
}
