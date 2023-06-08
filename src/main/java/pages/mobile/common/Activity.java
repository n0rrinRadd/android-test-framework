package pages.mobile.common;

import core.Elements;
import pages.mobile.common.fragments.Alert;
import pages.mobile.fragments.ActionBar;
import pages.mobile.fragments.ActionBar.ActionBarButton;


public abstract class Activity extends Alert {

    private static ActionBar actionBar = new ActionBar();

    public ActionBar getActionBar() {
        return actionBar;
    }


    public Elements backButton() {
        return getActionBar().backButton();
    }

    public void clickSwitchUser() {
        clickActionBarItem(new ActionBarButton("Switch User"));
    }

    public void clickActionBarItem(ActionBarButton actionBarButton) {

        Elements button = getActionBar().getButtonItem(actionBarButton);

        if (button.waitForVisible(5)) {
            button.click();

        } else {
            getActionBar().moreOptionsDropdown().click();
            getActionBar().getOverflowItem(actionBarButton).click();
        }
    }

}
