package pages.mobile.fragments;

import org.openqa.selenium.By;

import core.Driver;
import core.Elements;
import utils.core.MobileDriverUtils;


public class ActionBar {

    public static class ActionBarButton {

        private String optionText;

        public ActionBarButton(String text) {
            optionText = text;
        }

        public String getOptionText() {
            return optionText;
        }
    }


    private String actionBarXPath = "//android.view.View[contains(@resource-id, '" + MobileDriverUtils.androidId + "action')]";


    public Elements getTitle(){
        By locator = By.id(MobileDriverUtils.androidId + "action_bar_title");
        return new Elements(Driver.mobile, locator);
    }

    public Elements backButton() {
        By locator = By.xpath(actionBarXPath + "//android.widget.LinearLayout[contains(@content-desc, 'Navigate up')]");
        return new Elements(Driver.mobile, locator);
    }

    public Elements moreOptionsDropdown() {
        By locator = By.xpath(actionBarXPath + "//android.widget.ImageButton[@content-desc='More options']");
        return new Elements(Driver.mobile, locator);
    }

    public Elements getOverflowItem(ActionBarButton button) {
        By locator = By.xpath("//android.widget.TextView[@text='" + button.getOptionText() + "']");
        return new Elements(Driver.mobile, locator);
    }

    public Elements getButtonItem(ActionBarButton button) {
        By locator = By.xpath(actionBarXPath + "//*[@text='" + button.getOptionText() + "']");
        return new Elements(Driver.mobile, locator);
    }

}
