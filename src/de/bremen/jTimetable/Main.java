package de.bremen.jTimetable ;

import de.bremen.jTimetable.Classes.SQLConnectionManager;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;

import javax.lang.model.type.NullType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args){
        System.out.println("Ausgabe aus der main()-Methode");
        SQLConnectionManager sqlmanager = new SQLConnectionManager();
        try{
            sqlmanager.connect("jdbc:sqlite:jTimetable.sqlite");
            ArrayList<SQLConnectionManagerValues>  SQLValues = new ArrayList<SQLConnectionManagerValues>();
            ResultSet rs = sqlmanager.select("Select * from tmp_testtabelle", SQLValues);
            System.out.println(rs);
        } catch ( SQLException e){
            System.out.println(e);
        }


    }
}