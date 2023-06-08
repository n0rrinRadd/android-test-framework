package logout;

import org.testng.annotations.Test;

import base.MobileTest;
import data.enums.Employees;
import data.users.User;
import pages.mobile.activities.home.HomeScreenActivity;
import pages.mobile.activities.login.LoginActivity;
import pages.mobile.activities.tableService.ServiceAreasActivity;
import pages.mobile.activities.login.SwitchUserActivity;
import pages.mobile.activities.login.SwitchUserActivity.ActionBarOptions;
import utils.TestGroup;

public class SmokeTestManagerLoginLogout extends MobileTest {

    @Test(groups={TestGroup.MOBILESMOKE, TestGroup.MOBILEFULL, TestGroup.LOGOUT})
    public void testLoginLogout() throws InterruptedException
    {
        HomeScreenActivity homeScreen = new HomeScreenActivity();
        LoginActivity loginScreen = new LoginActivity();
        ServiceAreasActivity serviceAreasActivity = new ServiceAreasActivity();
        SwitchUserActivity switchUserActivity = new SwitchUserActivity();
        User manager = new User(Employees.MANAGER);

        homeScreen.clickSwitchUser();
        switchUserActivity.clickActionBarItem(ActionBarOptions.LOGOUT);

        mAssert.assertTrue(loginScreen.getEmailInput().waitForVisible());
        loginScreen.loginWithEmailPassword(manager.getEmail(), manager.getPassword());
        serviceAreasActivity.backButton().waitForVisible();
    }

}