package de.bremen.jTimetable.Classes;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;

import java.sql.*;
import java.util.ArrayList;

import static java.sql.Types.NULL;

public class SQLConnectionManager {
    Connection conn;

    public SQLConnectionManager() throws SQLException {
        this("jdbc:h2:./h2","sa","");
    }

    public SQLConnectionManager(String jdbcString, String username, String password) throws SQLException {
        this.connect(jdbcString,username,password);
    }

    public ResultSet select(String SQLString, ArrayList<SQLConnectionManagerValues> SQLValues) throws SQLException{
        // select from database
        PreparedStatement pstmt = prepareStatement(SQLString, SQLValues);
        // System.out.println(pstmt);
        ResultSet rs = pstmt.executeQuery();

        return rs;
    }

    public ResultSet execute(String SQLString, ArrayList<SQLConnectionManagerValues> SQLValues) throws  SQLException{
        //used for inserting into the database
        PreparedStatement pstmt = prepareStatement(SQLString, SQLValues);
        pstmt.execute();
        System.out.println(pstmt);
        return pstmt.getGeneratedKeys();
        //if (generatedKeys.next()) {
        //    System.out.println((generatedKeys.getLong(1)));
        //}
        //System.out.println();
    }

    private PreparedStatement prepareStatement(String SQLString, ArrayList<SQLConnectionManagerValues> SQLValues) throws SQLException{
        PreparedStatement pstmt = this.conn.prepareStatement(SQLString, Statement.RETURN_GENERATED_KEYS);
        //use prepared statements, iterate over the TypeValues object boolean, Date, int, long, null, string
        int parameterindex = 1;
        for(SQLConnectionManagerValues typevalue : SQLValues){

            switch (typevalue.getType()){
                case "Boolean":
                    pstmt.setBoolean(parameterindex, (Boolean) typevalue.getValue());
                    break;
                case "Date":
                    //pstmt.setDate(parameterindex, (Date) (typevalue.getValue());
                    pstmt.setObject(parameterindex, typevalue.getValue());
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
        // System.out.println(pstmt);
        return pstmt;
    }

    public void connect(String jdbcstring, String username, String password) throws SQLException{
        Connection conn = null;
        conn = DriverManager.getConnection(jdbcstring, username, password);
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
