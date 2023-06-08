package examples;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import base.WebTest;
import data.enums.Employees;
import data.users.User;
import pages.web.restaurant.admin.AdminPage;
import utils.TestGroup;

public class WebExample extends WebTest {

	@Override
	@BeforeClass(alwaysRun = true)
	public void setUp() throws Exception {
		super.setUp(new User(Employees.ADMIN));
	}

    @Test (groups={"webExample", TestGroup.WEBSMOKE})
	public void testRestaurantAdminLogin() {
		AdminPage adminPage = new AdminPage();
		mAssert.assertTrue(adminPage.accountDropdown().waitForVisible());
	}	

}
