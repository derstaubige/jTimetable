package de.bremen.jTimetable.Classes.SQLConnectionManagerValues;

public class SQLValueString extends SQLConnectionManagerValues{
    private final String type;
    private final String value;

    public SQLValueString( String value1) {
        this.value = value1;
        this.type = "String";
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getValue() {
        return value;
    }
}
