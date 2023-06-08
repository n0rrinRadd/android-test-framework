package utils.core;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.ScreenOrientation;

import core.Driver;
import core.Elements;
import data.enums.SwipeDirection;
import io.appium.java_client.NetworkConnectionSetting;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import utils.Log;

public final class MobileDriverUtils {

    private MobileDriverUtils() {}
    public static final String baseId = "com.test.pos:id/";
    public static final String androidId = "android:id/";

    //region DriverUtils Methods
    public static void getScreenshot(String message) {
        DriverUtils.getScreenshot(Driver.mobile, message);
    }
    //endregion

    //region MobileDriver specific methods
    public static void tapPoint(int x, int y) {
        Log.info("Tapping position (" + x + ", " + y + ") on screen");
        ((AndroidDriver) Driver.mobile).tap(1, x, y, 1);
    }

    public static void setNetwork(boolean enable) {
        Log.info("Setting network connectivity to " + (enable ? "'Enabled'" : "'Disabled'"));
        ((AndroidDriver) Driver.mobile).setNetworkConnection(new NetworkConnectionSetting(!enable, enable, false));
    }

    public static boolean isNetworkEnabled() {
        NetworkConnectionSetting network = ((AndroidDriver) Driver.mobile).getNetworkConnection();
        return (network.wifiEnabled() && !network.airplaneModeEnabled());
    }


    public static boolean scrollTo(String text) {
        return scrollTo(text, 2);
    }

    public static boolean scrollTo(String text, int attempts) {
        return executeScrollTo(false, text, attempts);
    }

    public static boolean scrollToExact(String text) {
        return scrollToExact(text, 2);
    }

    public static boolean scrollToExact(String text, int attempts) {
        return executeScrollTo(true, text, attempts);
    }

    private static boolean executeScrollTo(boolean isExact, String text, int attempts) {

        // Invokes Google's UIAutomator framework through Appium
        // This overrides Appium's AndroidDriver.scrollTo() method in order to resolve intermittent scroll+click failures
        // The original problem seems to be caused by using multiple UiSelector() objects in the search query

        // UIAutomator scrollIntoView Reference:
        // http://android.googlesource.com/platform/frameworks/testing/+/jb-mr0-release%5E1/uiautomator/library/src/com/android/uiautomator/core/UiScrollable.java

        String uiScrollableObject = "new UiScrollable(new UiSelector().scrollable(true).instance(0)).scrollIntoView(new UiSelector()." + ((isExact) ? "text" : "textContains") + "(\""+text+"\").instance(0))";

        for (int i = 0; i < attempts; i++) {
            try {

                Log.info("Scrolling to " + text + "...");
                ((AndroidDriver) Driver.mobile).findElementByAndroidUIAutomator(uiScrollableObject);
                return true;

            } catch (Exception exception) {
                // Silent fail
            }
        }

        return false;
    }


    public static void rotate(ScreenOrientation orientation) {
        Log.info("Rotating screen to " + orientation.toString());

        if (orientation == ScreenOrientation.LANDSCAPE) {
            orientation = ScreenOrientation.PORTRAIT;
        } else {
            orientation = ScreenOrientation.LANDSCAPE;
        }

        try {
            ((AndroidDriver) Driver.mobile).rotate(orientation);
        } catch (Exception e) {
            Log.error("ERROR : Failed to rotate screen  - " + e.getMessage());
        }

    }

    public static ScreenOrientation getOrientation() {
        ScreenOrientation orientation = ((AndroidDriver) Driver.mobile).getOrientation();

        if (orientation == ScreenOrientation.LANDSCAPE) {
            orientation = ScreenOrientation.PORTRAIT;
        } else {
            orientation = ScreenOrientation.LANDSCAPE;
        }

        return orientation;
    }

    public static void openNotificationCenter() {
        Log.info("Opening notification center");
        ((AndroidDriver) Driver.mobile).openNotifications();
    }

    public static void dismissNotificationCenter() {
        Log.info("Dismissing notification center");
        Driver.mobile.navigate().back();
    }

    public static void dragAndDrop(Elements from, Elements to) {
        Log.info("Dragging " + from.getTagName() + " to " + to.getTagName());
        new TouchAction(((AndroidDriver) Driver.mobile)).longPress(from.getRawElement()).moveTo(to.getRawElement()).release().perform();
    }

    public static void swipe(SwipeDirection swipeDirection){
        Dimension dimension = Driver.mobile.manage().window().getSize();
        int startX;
        int startY;
        switch (swipeDirection) {
            case LEFT:
                startX = (int) (dimension.getWidth() * 0.9);
                startY = (int) (dimension.getHeight() * 0.5);
                new TouchAction(((AndroidDriver) Driver.mobile)).longPress(startX,startY).moveTo(dimension.getWidth() - startX, startY).release().perform();
                break;
            case RIGHT:
                startX = (int) (dimension.getWidth() * 0.1);
                startY = (int) (dimension.getHeight() * 0.5);
                new TouchAction(((AndroidDriver) Driver.mobile)).longPress(startX,startY).moveTo(dimension.getWidth(), startY).release().perform();
                break;
            case UP:
                startX = (int) (dimension.getWidth() * 0.5);
                startY = (int) (dimension.getHeight() * 0.9);
                new TouchAction(((AndroidDriver) Driver.mobile)).longPress(startX,startY).moveTo(startX, dimension.getHeight()-startY).release().perform();
                break;
            case DOWN:
                startX = (int) (dimension.getWidth() * 0.5);
                startY = (int) (dimension.getHeight() * 0.1);
                new TouchAction(((AndroidDriver) Driver.mobile)).longPress(startX,startY).moveTo(startX, dimension.getHeight()-startY).release().perform();
                break;
        }
    }

    public static Boolean verifyListItems(String[] listItems) {
        for (String listItem : listItems) {
            if (new Elements(Driver.mobile, By.xpath("//*[@text = '" + listItem + "']")).waitForVisible(2)) {
                Log.info("Found " + listItem + " On Screen");
            } else {
                if(!scrollTo(listItem)){
                    return false;
                }
                Log.info("Found " + listItem + " By Scrolling");
            }
        }
        return true;
    }
    //endegion
}