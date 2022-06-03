package de.bremen.jTimetable.Classes.SQLConnectionManagerValues;

public abstract class SQLConnectionManagerValues{
    private String type;
    private Object value;

    public String getType(){
        return this.type;
    }

    public Object getValue(){
        return this.value;
    }

}
