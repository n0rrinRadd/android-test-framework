package examples;

import org.testng.annotations.Test;

import base.MobileTest;
import pages.mobile.activities.home.HomeScreenActivity;
import pages.mobile.activities.login.LoginActivity;
import pages.mobile.activities.login.SwitchUserActivity;
import pages.mobile.activities.login.SwitchUserActivity.ActionBarOptions;

public class MobileExample extends MobileTest {
	
	@Test (groups={"mobileExample"})
	public void testLogout() throws InterruptedException {
		HomeScreenActivity homeScreen = new HomeScreenActivity();
		LoginActivity loginScreen = new LoginActivity();
		SwitchUserActivity switchUserActivity = new SwitchUserActivity();


		homeScreen.backButton().click();
		homeScreen.clickSwitchUser();

		switchUserActivity.clickActionBarItem(ActionBarOptions.LOGOUT);

		mAssert.assertTrue(loginScreen.getEmailInput().waitForVisible());
	}
}
