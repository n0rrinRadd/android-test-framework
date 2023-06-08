package utils.core;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import core.AppConfig;
import utils.Log;

public final class DriverUtils {

    private DriverUtils() {}

    public static void getScreenshot(WebDriver driver, String message) {

        String testName = AppConfig.getTestName();
        String testNameString = (testName.contains("\\.")) ? testName.split("\\.")[1] : testName;

        String time = new SimpleDateFormat("HH.mm.ss.SSS").format(new Date());

        String fileName = time + "-" + testNameString + "-" + message + ".jpg";

        try {
            File file = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            FileUtils.copyFile(file, new File("../screenshots/" + fileName));
            Log.info("Screenshot Saved  -  " + fileName);

        } catch (Exception ex) {
            Log.error("Unable to take screenshot");
            ex.printStackTrace();
        }
    }

    public static void setScriptTimeout(WebDriver driver, int timeout) {
        driver.manage().timeouts().setScriptTimeout(timeout, TimeUnit.SECONDS);
    }

}
