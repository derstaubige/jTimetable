package de.bremen.jTimetable.Classes.SQLConnectionManagerValues;

public class SQLValueNull extends SQLConnectionManagerValues{

    private final String type ;
    private final String value;

    public SQLValueNull(String value1) {
        this.value = value1;
        this.type = "Null";
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
