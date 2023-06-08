package base;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;

import core.AppConfig;
import core.Database;
import core.Driver;
import core.DriverFactory;
import core.TestListener;
import core.TestRunInterceptor;
import data.enums.Employees;
import data.login.SetUp;
import data.users.User;
import pages.web.home.LogInPage;
import utils.Assert;
import utils.Log;

@Listeners({TestListener.class, TestRunInterceptor.class})
public abstract class WebAndMobileTest {

    public Assert mAssert;

    @BeforeClass(alwaysRun = true)
	public void setUp() throws Exception {
        setUp(new User(Employees.MANAGER), new User(Employees.MANAGER));
	}

    public void setUp(User loginUser) throws Exception {
        setUp(loginUser, loginUser);
    }
	
    public void setUp (User webLoginUser, User mobileLoginUser) throws Exception {
        Log.info("Setting Up Test...");
        mAssert = new Assert();

        Database database = new Database();
        if (AppConfig.getTestNumber() == 1) database.setEmployeePasscodes();
        database.resetDatabaseAndSync();
        Driver.mobile = DriverFactory.getMobileDriver();
        Driver.web = DriverFactory.getWebDriver();
        try{
            SetUp.mobileLogin(mobileLoginUser);
            LogInPage logInPage = new LogInPage();
			logInPage.logIn(webLoginUser);
            Log.success("Success: Test Set Up Complete\n\n");
        }
		catch (Exception ex) {
            Log.error("Set Up was unable to finish\n\n");
            throw ex;
        }
	}

    @AfterClass(alwaysRun = true)
    public void baseTearDown() throws Exception {
        if (Driver.mobile != null) {
            Driver.mobile.quit();
            Driver.mobile = null;
        }
        if (Driver.web != null) {
            Driver.web.quit();
            Driver.web = null;
        }
    }
}
