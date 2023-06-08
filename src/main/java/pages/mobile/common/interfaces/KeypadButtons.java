package pages.mobile.common.interfaces;


import org.openqa.selenium.By;

import core.Driver;
import core.Elements;
import utils.Log;
import utils.core.MobileDriverUtils;

public interface KeypadButtons {

	default By digitsLocator(String n) {
		return By.id(MobileDriverUtils.baseId + "KBDigit" + n);
	}
	
	// WebElements
	default Elements getDigit(String n){
		return new Elements(Driver.mobile, digitsLocator(n));
	}

	default Elements deleteButton(){
		By locator = By.id(MobileDriverUtils.baseId + "KBDelete");
		return new Elements(Driver.mobile, locator);
	}

	default Elements clearButton(){
		By locator = By.id(MobileDriverUtils.baseId + "KBClear");
		return new Elements(Driver.mobile, locator);
	}
	
	default Elements doneButton(){
		By locator = By.id(MobileDriverUtils.baseId + "KBDone");
		return new Elements(Driver.mobile, locator);
	}
	
	default Elements saveButton(){
		By locator = By.xpath("//android.widget.Button[@text = 'Save']");
		return new Elements(Driver.mobile, locator);
	}
	
	default Elements cancelButton(){
		By locator = By.xpath("//android.widget.Button[@text = 'Cancel']");
		return new Elements(Driver.mobile, locator);
	}

    default Elements cashDueDoneButton() {
        By locator = By.xpath("//android.widget.Button[@text = 'Done']");
        return new Elements(Driver.mobile, locator);

    }

	default Elements customerLookupButton(){
		By locator = By.id(MobileDriverUtils.baseId + "LookupAccount");
		return new Elements(Driver.mobile, locator);
	}
	
	default void sendCode(String code) {
		Log.info("Sending Code: " + code);
		getDigit("1").waitForVisible();
		for(int i = 0; i < code.length(); i++){
			getDigit(code.substring(i, i+1)).click();
		}
	}
}