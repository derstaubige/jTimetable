package de.bremen.jTimetable.Classes.SQLConnectionManagerValues;

public class SQLValueLong extends SQLConnectionManagerValues{
    private final String type;
    private final Long value;

    public SQLValueLong( Long value1) {
        this.value = value1;
        this.type ="Long";
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Long getValue() {
        return value;
    }
}
