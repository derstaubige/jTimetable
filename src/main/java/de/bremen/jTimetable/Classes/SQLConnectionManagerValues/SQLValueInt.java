package de.bremen.jTimetable.Classes.SQLConnectionManagerValues;

public class SQLValueInt extends SQLConnectionManagerValues{
    private final String type;
    private final Integer value;

    public SQLValueInt( Integer value1) {
        this.value = value1;
        this.type = "Int";
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
