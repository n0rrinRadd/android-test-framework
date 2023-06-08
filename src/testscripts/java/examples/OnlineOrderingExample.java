package examples;

import org.testng.annotations.Test;

import base.OnlineOrderingOnSauceLabsBrowser;
import data.enums.saucelabs.BrowserInfo;

public class OnlineOrderingExample extends OnlineOrderingOnSauceLabsBrowser {

	public OnlineOrderingExample(){
		super(BrowserInfo.CHROME_45);
	}

	@Test (groups={"onlineOrderingBrowserTests"})
	public void testOnlineOrdering() throws InterruptedException {

	}
}
