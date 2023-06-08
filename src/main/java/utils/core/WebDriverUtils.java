package utils.core;

import org.openqa.selenium.Alert;
import org.openqa.selenium.NoAlertPresentException;

import core.AppConfig;
import core.Driver;
import data.enums.RestaurantName;
import pages.web.restaurant.PublishPage;
import pages.web.restaurant.RestaurantPage;
import utils.Log;

public final class WebDriverUtils {

    private WebDriverUtils (){}


    //region DriverUtils methods
    /**
     * Gets the screenshpt of the current web page
     * @param message String containing the exception that caused the screenshot to be taken
     */
    public static void getScreenshot(String message){
        DriverUtils.getScreenshot(Driver.web, message);
    }
    //endregion

    //region Webdriver specfic methods

    /**
     * waits for javscript alert
     * @throws InterruptedException if Thread.sleep fails
     */
    public static void waitForAlert() throws InterruptedException {
        for(int i = 0; i < 5; i++) {
            try {
                Driver.web.switchTo().alert();
                break;
            } catch(NoAlertPresentException e) {
                Thread.sleep(1000);
                continue;
            }
        }
    }
    
    /**
     * Accepts the alert on screen
     */
    public static void acceptAlert() {
        try {
            waitForAlert();
            Alert alert = Driver.web.switchTo().alert();
            Log.info("Successfully switched to alert");
            alert.accept();
        } catch (Exception e) {
            //exception handling
        }
    }

    /**
     * Publishes all config changes
     */
    public static void publishWeb(){
        Driver.web.navigate().to(AppConfig.getBaseUrl() + "/restaurants/admin/applyconfigchanges?restaurantId=1000000000000");
        PublishPage publishPage = new PublishPage();
        publishPage.publishMessageText().waitForVisible();
        Log.info("Publish Message: " + publishPage.publishMessageText().getText());
    }

    /**
     * This will resync all restaurant users, orders, time entries, etc.
     */
    public static void deltaReload() {
        RestaurantPage restaurantPage = new RestaurantPage();
        restaurantPage.goToAndForceRecheckConfigDeltaByRestaurantName(RestaurantName.test_TAB_AND_GRILL.toString());
    }

    /**
     * Push a reload data to all linked mobile devices
     */
    public static void pushReloadDataToMobile() {
        RestaurantPage restaurantPage = new RestaurantPage();
        restaurantPage.goToAndForceResycnAllDataDropDownItem(RestaurantName.test_TAB_AND_GRILL.toString());
    }
    //endregion
}