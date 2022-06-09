package de.bremen.jTimetable.Classes.SQLConnectionManagerValues;

import java.sql.Date;
import java.time.LocalDate;

public class SQLValueDate extends SQLConnectionManagerValues{
    private final String type;
    private final Date value;

    public SQLValueDate(LocalDate value1) {
        this.value = Date.valueOf(value1);
        this.type = "Date";
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public LocalDate getValue() {
        return value.toLocalDate();
    }
}
