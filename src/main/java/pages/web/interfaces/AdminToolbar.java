package pages.web.interfaces;

import org.openqa.selenium.By;

import core.Driver;
import core.Elements;
import pages.web.restaurant.admin.utils.Breadcrumb;

public interface AdminToolbar extends Breadcrumb
{
	
	default Elements getSaveButton(){
		By locator = By.id("save-btn");
		return new Elements(Driver.web, locator);
	}
	
	default Elements getPublishLink(){
		By locator = By.id("publish-link");
		return new Elements(Driver.web, locator);
	}
	
	default Elements alertSuccess(){
		By locator = By.cssSelector(".alert.alert-success");
		return new Elements(Driver.web, locator);
	}

	default Elements alertError(){
		By locator = By.cssSelector(".alert.alert-error");
		return new Elements(Driver.web, locator);
	}

	default Elements alertDismiss() {
		By locator = By.cssSelector(".close");
		return new Elements(Driver.web, locator);
	}

	default Elements actionsButton(){
		By locator = By.xpath("//button[@title='Actions']");
		return new Elements(Driver.web, locator);
	}

	default Elements archiveActionOption(){
		By locator = By.xpath("//a[@class='delete-entity' and contains(.,'Archive')]");
		return new Elements(Driver.web, locator);
	}

	default Elements archivedText(){
		By locator = By.xpath("//h3[text()='Archived']");
		return new Elements(Driver.web, locator);
	}

	default void waitForLoading(int timeout) {
		new Elements(Driver.web, By.cssSelector(".loadmask")).waitForNotVisible(timeout);
	}

	default void clickSaveAndPublish(){
		getSaveButton().click();
		getPublishLink().click();
		alertSuccess().waitForTextVisible("Configuration published successfully");
	}

	default void clickSave(){
		getSaveButton().click();
		alertSuccess().waitForVisible();
	}
}
