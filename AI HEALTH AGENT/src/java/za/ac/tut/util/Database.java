package za.ac.tut.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {

    static {
        try {
            Class.forName(DatabaseConfig.JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        if (!DatabaseConfig.hasSeparateCredentials()) {
            return DriverManager.getConnection(DatabaseConfig.JDBC_URL);
        }

        return DriverManager.getConnection(
                DatabaseConfig.JDBC_URL,
                DatabaseConfig.dbUserOrEmpty(),
                DatabaseConfig.dbPassOrEmpty()
        );
    }
}
