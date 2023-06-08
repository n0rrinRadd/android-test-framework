package core;

import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.net.URL;

import core.AppConfig.Machine;
import core.AppConfig.testEnvironment;
import data.enums.saucelabs.BrowserInfo;
import io.appium.java_client.android.AndroidDriver;
import utils.Log;

public final class DriverFactory {

    private DriverFactory() {}

    public static WebDriver getMobileDriver() {
        return createAndroidDriver(true, false);
    }

    public static WebDriver getMobileDriver(boolean noReset, boolean fullReset) {
        return createAndroidDriver(noReset, fullReset);
    }

    public static WebDriver getWebDriver() {

        if (AppConfig.getMachine() == Machine.local) {
            return createLocalDriver();

        } else if (AppConfig.getMachine() != Machine.sauce) {
            return createRemoteDriver();
        }

        throw new RuntimeException("Invalid Machine Environment !");
    }


    public static WebDriver getSauceLabsDriver(BrowserInfo info) {

        if (AppConfig.getMachine() == Machine.sauce && AppConfig.gettestEnv() == testEnvironment.stage) {
            return createSauceLabsDriver(info);
        }

        throw new RuntimeException("Invalid Configuration. Sauce + Stage  must be used !");
    }



    private static AndroidDriver createAndroidDriver(boolean noReset, boolean fullReset) {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        String urlBase = "http://";

        capabilities.setCapability("platformName", "ANDROID");
        capabilities.setCapability("deviceName", AppConfig.getDeviceName());
        capabilities.setCapability("app-package", AppConfig.getAppPackage());
        capabilities.setCapability("app-activity", AppConfig.getAppActivity());
        capabilities.setCapability("newCommandTimeout", 300);
        capabilities.setCapability("noReset", noReset);
        capabilities.setCapability("fullReset", fullReset);

        if (AppConfig.gettestEnv() == testEnvironment.local) {
            urlBase = (AppConfig.getMachine() == Machine.local) ? SystemUtils.USER_HOME : "http://" + AppConfig.getMachine().getAddress() + ":8080";
        }

        capabilities.setCapability("app", urlBase + AppConfig.gettestEnv().getAPKPath());

        AndroidDriver result = Elements.Wait(30, () -> {
            Log.info("Attempting to create mobile driver...");
            return new AndroidDriver(new URL("http://" + AppConfig.getMachine().getAddressMobile() + "/wd/hub"), capabilities);
        });

        if (result != null) { return result; }
        throw new RuntimeException("Failed to createAndroidDriver() !");
    }


    private static WebDriver createRemoteDriver() {
        try {
            URL remoteUrl = new URL("http://" + AppConfig.getMachine().getAddressWeb() + "/wd/hub");
            DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setCapability(CapabilityType.BROWSER_NAME, AppConfig.getBrowser());
            capabilities.setCapability("idleTimeout", 300);

            return new RemoteWebDriver(remoteUrl, capabilities);

        } catch (Exception e) {
            throw new RuntimeException("Failed to createRemoteDriver() !", e);
        }
    }


    private static WebDriver createLocalDriver() {

        switch (AppConfig.getBrowser()) {
            case "chrome" :
                String chromePath = SystemUtils.USER_DIR + File.separator + "lib" + File.separator + "chromedriver-";

                if (SystemUtils.IS_OS_MAC) {
                    chromePath = chromePath + "mac";

                } else if (SystemUtils.IS_OS_LINUX) {
                    chromePath = chromePath + "linux";

                } else {
                    throw new RuntimeException("OS not supported !");
                }

                System.setProperty("webdriver.chrome.driver", chromePath);
                return new ChromeDriver();

            case "firefox" :
                return new FirefoxDriver();

            default:
                throw new RuntimeException("Invalid browser !");
        }
    }


    private static WebDriver createSauceLabsDriver(BrowserInfo info) {
        try {
            URL remoteUrl = new URL("http://" + AppConfig.getMachine().getAddressWeb() + "/wd/hub");
            DesiredCapabilities capabilities = new DesiredCapabilities(info.getBrowser(), info.getBrowserVersion(), info.getPlatform());
            capabilities.setCapability("name", AppConfig.getTestName());

            return new RemoteWebDriver(remoteUrl , capabilities);

        } catch (Exception e) {
            throw new RuntimeException("Failed to createSauceLabsDriver() !", e);
        }
    }
}
