package de.bremen.jTimetable.Classes;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;

import java.sql.*;
import java.util.ArrayList;

import static java.sql.Types.NULL;

public class SQLConnectionManager {
    Connection conn;

    public ResultSet select(String SQLString, ArrayList<SQLConnectionManagerValues> SQLValues) throws SQLException{
        // select from database
        // select * from kunde where name = loreen; select passwort from kunde where name = loreen;
        PreparedStatement pstmt = prepareStatement(SQLString, SQLValues);
        return pstmt.executeQuery();
    }

    public  void insert(String SQLString, ArrayList<SQLConnectionManagerValues> SQLValues) throws  SQLException{
        //used for inserting into the database
        PreparedStatement pstmt = prepareStatement(SQLString, SQLValues);
        pstmt.execute();
        System.out.println(pstmt);
    }

    private PreparedStatement prepareStatement(String SQLString, ArrayList<SQLConnectionManagerValues> SQLValues) throws SQLException{
        PreparedStatement pstmt = this.conn.prepareStatement(SQLString);
        //use prepared statements, iterate over the TypeValues object boolean, Date, int, long, null, string
        int parameterindex = 1;
        for(SQLConnectionManagerValues typevalue : SQLValues){
            System.out.println(typevalue.getType());
            System.out.println(typevalue.getClass());

            switch (typevalue.getType()){
                case "Boolean":
                    pstmt.setBoolean(parameterindex, (Boolean) typevalue.getValue());
                    break;
                case "Date":
                    pstmt.setDate(parameterindex, (Date) typevalue.getValue());
                    break;
                case "Int":
                    pstmt.setInt(parameterindex, (Integer) typevalue.getValue());
                    break;
                case "Long":
                    pstmt.setLong(parameterindex, (Long) typevalue.getValue());
                    break;
                case "Null":
                    pstmt.setNull(parameterindex, NULL);
                    break;
                case "String":
                    pstmt.setString(parameterindex, (String) typevalue.getValue());
                    break;
            }
            parameterindex++;
        }
        return pstmt;
    }

    public void connect(String jdbcstring) throws SQLException{
        Connection conn = null;
        conn = DriverManager.getConnection(jdbcstring);
        this.conn = conn;
    }

    public void close(Connection conn) throws  SQLException{
        conn.close();
    }

    @Override protected void finalize() throws Throwable{
        //close database when SQLConnectionManager is deleted
        super.finalize();
    }
}
