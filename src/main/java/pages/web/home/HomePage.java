package pages.web.home;

import core.AppConfig;
import core.Driver;


public class HomePage extends HomeBasePage{

	public void goTo(){
		Driver.web.navigate().to(AppConfig.getBaseUrl() + "/");
	}

}
