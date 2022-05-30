package de.bremen.jTimetable.Classes;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;

import java.sql.*;
import java.util.ArrayList;

import static java.sql.Types.NULL;

public class SQLConnectionManager {
    Connection conn;

    public ResultSet select(String SQLString, ArrayList<SQLConnectionManagerValues> SQLValues) throws SQLException{
        // select from database
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
        int i = 1;
        for(final SQLConnectionManagerValues typevalue : SQLValues){
            switch (typevalue.type){
                case "Boolean":
                    pstmt.setBoolean(i, (Boolean) typevalue.value);
                    break;
                case "Date":
                    pstmt.setDate(i, (Date) typevalue.value);
                    break;
                case "Int":
                    pstmt.setInt(i, (Integer) typevalue.value);
                    break;
                case "Long":
                    pstmt.setLong(i, (Long) typevalue.value);
                    break;
                case "Null":
                    pstmt.setNull(i, NULL);
                    break;
                case "String":
                    pstmt.setString(i, (String) typevalue.value);
                    break;
            }
            i++;
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
