import javax.swing.*;
import java.sql.*;

public class DBUtils {

    private static final String URL = "jdbc:mysql://localhost:3306/stadium_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    public static void loadDriver() throws ClassNotFoundException {
        Class.forName(DRIVER);
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static int getSelectedId(JComboBox<String> combo) {
        String sel = (String) combo.getSelectedItem();
        if (sel == null) return 0;
        return Integer.parseInt(sel.split(" - ")[0]);
    }

    public static void setOptionalString(PreparedStatement pst, int index, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            pst.setNull(index, Types.VARCHAR);
        } else {
            pst.setString(index, value);
        }
    }

    public static void setOptionalInt(PreparedStatement pst, int index, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            pst.setNull(index, Types.INTEGER);
        } else {
            pst.setInt(index, Integer.parseInt(value));
        }
    }
}
