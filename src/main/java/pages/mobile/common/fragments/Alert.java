package pages.mobile.common.fragments;

import core.Driver;
import core.Elements;
import org.openqa.selenium.By;
import utils.core.MobileDriverUtils;

/**
 * Created by karansingh on 12/21/15.
 */
public class Alert {

    // elements
    public Elements alertButtonWithId(String id){
        By locator = By.id(MobileDriverUtils.androidId + id);
        return new Elements(Driver.mobile, locator);
    }

    public Elements alertButtonWithText(String buttonText){
        By locator = By.xpath("//android.widget.Button[@text='" + buttonText + "']");
        return new Elements(Driver.mobile, locator);
    }

    public Elements alertTitle(){
        By locator = By.id(MobileDriverUtils.androidId + "alertTitle");
        return new Elements(Driver.mobile, locator);
    }
}
