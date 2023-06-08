package core;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import data.users.Customer;
import pages.web.home.LogInPage;
import utils.Log;
import utils.core.WebDriverUtils;

public class Database {
	
	Connection connection = null;


	public Database() {

		try {
			Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection("jdbc:postgresql://" + AppConfig.getMachine().getAddress() + ":5432/test_dev", "postgres", "admin");

		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cannot locate PostgreSQL JDBC Driver !  Include in your library path!", e);

		} catch (SQLException e) {
            throw new RuntimeException("Connection Failed! Check output console", e);

        }

		if (connection == null) {
            throw new RuntimeException("Connection Failed!");
		}
	}


    //region SQL Execute Wrapper
    public ResultSet executeQuery(String sqlStatement) {
        try {
            return connection.createStatement().executeQuery(sqlStatement);

        } catch (SQLException e) {
            Log.error("Failed : " + sqlStatement);
            throw new RuntimeException("Failed to executeQuery() !", e);
        }
    }

    public int executeUpdate(String sqlStatement) {
        try {
            return connection.createStatement().executeUpdate(sqlStatement);

        } catch (SQLException e) {
            Log.error("Failed : " + sqlStatement);
            throw new RuntimeException("Failed to executeUpdate() !", e);
        }
    }

    public boolean execute(String sqlStatement) {
        try {
            return connection.createStatement().execute(sqlStatement);

        } catch (SQLException e) {
            Log.error("Failed : " + sqlStatement);
            throw new RuntimeException("Failed to execute() !", e);
        }
    }
    //endregion

	
	public List<String> select(String column, String sqlStatement) {

        try {
            ResultSet rs = executeQuery(sqlStatement);
            List<String> result = new ArrayList<>();

            Log.info(column + "  FROM  " + sqlStatement);

            while (rs.next()) {
                String value = rs.getString(column);
                result.add(value);

                Log.info(column + " = " + value);
            }

            return result;

        } catch (SQLException e) {
            Log.error("Failed : " + sqlStatement);
            throw new RuntimeException("Failed to select column from query !", e);
        }
	}

}
