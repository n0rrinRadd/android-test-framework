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


    public void deleteTable(String tableName) {
        Log.info("deleting table " + tableName + "...");
        executeUpdate("UPDATE \"" + tableName + "\" SET deleted = true, \"deletedDate\" = now() at time zone 'UTC', \"modifiedDate\" = now() at time zone 'UTC', version = version + 1;");
    }

    public void deleteAllRestaurantCustomers() {
        Log.info("deleting all restaurant customers...");
        executeUpdate("update \"RestaurantSetCustomer\" set deleted = true, \"deletedDate\" = now() at time zone 'UTC', \"modifiedDate\" = now() at time zone 'UTC';");
        executeUpdate("update \"RestaurantSetCustomer_CustomerAddress\" set deleted = true, \"deletedDate\" = now() at time zone 'UTC', \"modifiedDate\" = now() at time zone 'UTC';");
        executeUpdate("update \"RestaurantSetCustomer_CustomerEmail\" set deleted = true, \"deletedDate\" = now() at time zone 'UTC';");
        executeUpdate("update \"RestaurantSetCustomer_CustomerPhone\" set deleted = true, \"deletedDate\" = now() at time zone 'UTC';");
    }

    public void deleteAllRestaurantAccounts() {
        Log.info("deleting all restaurant accounts...");
        executeUpdate("update \"testCard\" set deleted = true, \"deletedDate\" = now() at time zone 'UTC', \"modifiedDate\" = now() at time zone 'UTC', version = version + 1;");
        execute("truncate table \"LoyaltyPointsTransaction\";");
        execute("truncate table \"LoyaltyPoints\" cascade;");
    }

    public void updateRestaurantUserPasscode(String passcode, String id) {
        Log.info("updating userID:" + id + " passcode to " + passcode);
        executeUpdate("UPDATE \"RestaurantUser\" set passcode = " + passcode + ", \"modifiedDate\" = now() at time zone 'UTC', version = version + 1 where user_id = (select id from \"USERS\" where \"firstName\" = '" + id + "');");
    }

    public void setEmployeePasscodes() {
        executeUpdate("update \"RestaurantUser\" set \"deletedDate\" = null;");

        updateRestaurantUserPasscode("1", "Britanny");
        updateRestaurantUserPasscode("2", "Chris");
        updateRestaurantUserPasscode("3", "Heather");
        updateRestaurantUserPasscode("4", "Martha");
        updateRestaurantUserPasscode("5", "Steve");
        updateRestaurantUserPasscode("6", "Owen");
        updateRestaurantUserPasscode("7", "Stephanie");
        updateRestaurantUserPasscode("8", "Walter");
        updateRestaurantUserPasscode("0", "test");
    }

    public String loadGiftCard(Customer customer) {
        List<String> result = select("id", "SELECT id FROM \"testCard\" WHERE number = '" + customer.getUniqueNumericIdString() + "';");
        return result.get(result.size() - 1);
    }

    public String getGroupId(String menuItemName, String menuGroupName) {
        List<String> result = select("group_id", "SELECT group_id FROM \"MenuGroup_MenuItems\" WHERE item_id = (SELECT id FROM \"MenuItem\" WHERE name = '" + menuItemName + "' ORDER BY id ASC limit 1) and group_id = (SELECT id from \"MenuItem\" where name = '" + menuGroupName + "' ORDER BY id ASC LIMIT 1) limit 1;");
        return result.get(result.size() - 1);
    }

    public String getMenuItemId(String menuItemName) {
        List<String> result = select("id", "SELECT id FROM \"MenuItem\" WHERE name = '" + menuItemName + "' ORDER BY id ASC limit 1;");
        return result.get(result.size() - 1);
    }

    public void detachCashDrawers() {
        Log.info("detaching cashDrawers ...");
        executeUpdate("UPDATE \"CashDrawer\" SET \"printer_id\" = null, \"printersecondary_id\" = null, deleted = true;");
        executeUpdate("UPDATE \"CashDrawerBalance\" SET \"server_id\" = null, \"shift_id\" = null, deleted = true;");
        executeUpdate("UPDATE \"CashEntry\" SET \"server_id\" = null, \"server2_id\" = null, \"shift_id\" = null, deleted = true;");
        executeUpdate("UPDATE \"Printer\" SET \"cashDrawerCount\" = 'NO_CASHDRAWER';");
    }


    public void resetDatabase() {
        deleteTable("TimeEntry");
        deleteTable("Orders");
        deleteTable("Check");
        deleteTable("ServiceCharge");
        deleteTable("OrderPayment");
        deleteTable("ModifierDecoratorGroup");

        deleteAllRestaurantCustomers();
        deleteAllRestaurantAccounts();
    }

    public void resetDatabaseAndSync() {
        resetDatabase();

        try {
            Driver.web = DriverFactory.getWebDriver();
            LogInPage logInPage = new LogInPage();
            logInPage.logIn();

            WebDriverUtils.pushReloadDataToMobile();
            Driver.web.quit();
            Driver.web = null;

        } catch (Exception e) {
            throw new RuntimeException("Error Publishing Database Reset", e);
        }
    }

}
