package de.bremen.jTimetable ;

import de.bremen.jTimetable.Classes.SQLConnectionManager;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueInt;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueString;

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
            SQLValueString name = new SQLValueString();
            name.value = "hi";

            SQLValueInt zahl = new SQLValueInt();
            zahl.value = 123;
            SQLValues.add(name);
            SQLValues.add(zahl);
            sqlmanager.insert("Insert into tmp_testtabelle (name, zahl) values (?, ?)", SQLValues);
        } catch ( SQLException e){
            System.out.println(e);
        }


    }
}