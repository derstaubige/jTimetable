package de.bremen.jTimetable.Classes.SQLConnectionManagerValues;

public class SQLValueBoolean extends SQLConnectionManagerValues {
    private final String type;
    private final Boolean value;

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    public SQLValueBoolean( Boolean value1) {
        this.value = value1;
        this.type = "Boolean";
    }
}
