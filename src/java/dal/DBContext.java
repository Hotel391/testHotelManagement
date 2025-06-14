package dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBContext {
    protected Connection connect;
    
    public DBContext(){
         try{
             String url = "jdbc:sqlserver://"+serverName+":"+portNumber +
                ";databaseName="+dbName;
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); 
            connect = DriverManager.getConnection(url, userID, password);
         }catch (ClassNotFoundException | SQLException ex){
             ex.printStackTrace();
         }
     }

    private static final String serverName = "localhost";
    private static final String dbName = "HotelManagementDB";
    private static final String portNumber = "1433";
    private static final String userID = "sa";
    private static final String password = "123";
}
