import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static java.lang.Class.forName;

public class DatabaseHelper {

    static String url = "jdbc:mysql://localhost:3306/mydb?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=GMT";
    static String user = "root";
    static String password = "password";
    static Connection myConn;
    static Statement st;

    public void connect() {

        try {
            String driverName = "com.mysql.cj.jdbc.Driver";
            forName(driverName);
            myConn = DriverManager.getConnection(url, user, password);
            st = myConn.createStatement();
            System.out.println("Connected to database!");
        } catch (Exception throwable) {
            throwable.printStackTrace();
        }

    }

    public void disconnect() throws SQLException {
        st.close();
        myConn.close();
        System.out.println("Disconnected from database!");

    }

}
