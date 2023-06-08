package base;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import core.AppConfig;
import core.Driver;
import core.DriverFactory;
import data.enums.saucelabs.BrowserInfo;
import pages.web.restaurant.onlineorderingV1.MenuPage;
import utils.Assert;
import utils.Log;

public abstract class OnlineOrderingOnSauceLabsBrowser {

	private BrowserInfo browser;
	public Assert mAssert;

	public OnlineOrderingOnSauceLabsBrowser(){
		this.browser = BrowserInfo.CHROME_45;
	}

	public OnlineOrderingOnSauceLabsBrowser(BrowserInfo browser) {
		this.browser = browser;
	}

	@BeforeClass(alwaysRun = true)
	public void setUp () throws Exception
	{
		AppConfig.setTestName(this.getClass().getName());
		AppConfig.setTestNumber(AppConfig.getTestNumber() + 1);

		mAssert = new Assert();

		Driver.web = DriverFactory.getSauceLabsDriver(browser);
		try
		{
			MenuPage menuPage = new MenuPage();
			menuPage.goTo();
		}
		catch (Exception ex) {
			Log.error("Setup was unable to navigate to online ordering page");
			throw ex;
		}
	}

	@AfterClass(alwaysRun = true)
	public void baseTearDown() throws Exception {
		if (Driver.web != null) {
			Driver.web.quit();
			Driver.web = null;
		}
	}

}
