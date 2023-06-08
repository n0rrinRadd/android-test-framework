package pages.web.home;

import data.users.User;
import org.openqa.selenium.By;

import core.AppConfig;
import core.Driver;
import core.Elements;
import data.enums.Employees;
import pages.web.interfaces.AdminToolbar;
import pages.web.restaurant.admin.AdminPage;

public class LogInPage extends HomeBasePage implements AdminToolbar {

	public void goTo(){
		Driver.web.navigate().to(AppConfig.getBaseUrl() + "/login");
	}

	//region Elements
	public Elements getEmailInput(){
		By locator = By.id("email");
		return new Elements(Driver.web, locator);
	}
	
	public Elements getPasswordInput(){
		By locator = By.id("password");
		return new Elements(Driver.web, locator);
	}
	
	public Elements getLogInButton(){
		By locator = By.cssSelector(".btn.btn-primary");
		return new Elements(Driver.web, locator);
	}

	public Elements getForgotPasswordLink(){
		By locator = By.xpath(".//a[@href='/account/passwordreset']");
		return new Elements(Driver.web, locator);
	}
	//endregion
	
	public void setEmailInput(String email){
		getEmailInput().clear();
        getEmailInput().sendKeys(email);
	}
	
	public void setPasswordInput(String password){
		getPasswordInput().clear();
		getPasswordInput().sendKeys(password);
	}

	public void logIn(){
		logIn(new User(Employees.ADMIN));
	}

	public void logIn(User user){
		goTo();

		getEmailInput().clear();
		setEmailInput(user.getEmail());
		getPasswordInput().clear();
		setPasswordInput(user.getPassword());

		getLogInButton().click();
		AdminPage adminPage = new AdminPage();
		adminPage.accountDropdown().waitForVisible();
	}
}
