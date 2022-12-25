package de.bremen.jTimetable.Classes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLConnectionManagerValues;
import de.bremen.jTimetable.Classes.SQLConnectionManagerValues.SQLValueString;

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
//        System.out.println(pstmt);
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
//        System.out.println(pstmt);
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

    /* The Migrate Function checks if there are new SQL Migrations available and will execute them. It also
    * checks if the Database is new and fills an empty DB */
    public void Migrate() throws SQLException{
        try{
            this.execute("CREATE TABLE IF NOT EXISTS `T_MIGRATION` (`id` long not null PRIMARY KEY AUTO_INCREMENT, MigrationName char(200), MigrationDate Date)", new ArrayList<>());
        }catch(Exception e){
            e.printStackTrace();
        }

        
        try {
            List<Path> listOfFiles = FileResourcesUtils.start();
            FileResourcesUtils fileResourcesUtils = new FileResourcesUtils();
            String tmpFilepath = "";
            for(Path file : listOfFiles){
                ArrayList<SQLConnectionManagerValues> SQLValues = new ArrayList<SQLConnectionManagerValues>();
                SQLValues.add(new SQLValueString(file.getFileName().toString()));
                // System.out.println("Migrationname " + file.getFileName().toString());
                tmpFilepath = "/SQLMigration/" + file.getFileName().toString();
                              
                try {

                    ResultSet rs = this.select("Select count(*) from T_MIGRATION where MIGRATIONNAME = ?",SQLValues);
                    rs.next();
                    if(rs.getLong(1) == 0){
                        // This Migration didnt run yet
                        System.out.println("Running Migration " + file.getFileName().toString());
                        try {
                        
                            //contains the whole migration file
                            String tmpSQLStatement =  new BufferedReader(new InputStreamReader(fileResourcesUtils.getFileFromResourceAsStream(tmpFilepath)))
                            .lines().collect(Collectors.joining("\n"));

                            //execute the migration
                            this.execute(tmpSQLStatement, new ArrayList<SQLConnectionManagerValues>());

                            //mark the migration as done
                            this.execute("INSERT INTO T_MIGRATION (MigrationName, MigrationDate) values (?, CURRENT_TIMESTAMP() )", SQLValues);

                        } catch (Exception e2){
                            e2.printStackTrace();
                        }
                    }
                } catch (SQLException e){
                    e.printStackTrace();
                }
            }
            
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        

    }
}
