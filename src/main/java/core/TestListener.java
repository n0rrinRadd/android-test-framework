package core;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

import utils.Log;
import utils.core.MobileDriverUtils;
import utils.core.WebDriverUtils;
import utils.mail.testFailMail;

/**
 * Created by axroberts on 10/31/15.
 */
public class TestListener extends TestListenerAdapter {

    /**
     * Controls output for test configuration code blocks (@beforeClass, @beforeMethod, @afterMethod and @afterClass).
     * @param result Results of the test currently being executed
     */
    @Override
    public void beforeConfiguration(ITestResult result) {
        String testClassName = result.getTestClass().getName();

        if (result.getMethod().isBeforeClassConfiguration()) {
            AppConfig.setTestName(testClassName + ".baseSetUp");
            AppConfig.setTestNumber(AppConfig.getTestNumber() + 1);
            Log.info("*** SETTING UP TEST # " + AppConfig.getTestNumber() + " - " +  AppConfig.getTestName() + "***\n\n");

        } else if (result.getMethod().isBeforeMethodConfiguration()) {
            AppConfig.setTestName(testClassName + ".testSetUp");
            Log.info("*** SETTING UP TEST # " + AppConfig.getTestNumber() + " - " + AppConfig.getTestName() + "***\n\n");

        } else if (result.getMethod().isAfterMethodConfiguration()) {
            AppConfig.setTestName(testClassName + ".testTearDown");
            Log.info("*** TEARING DOWN TEST # " + AppConfig.getTestNumber() + " - " + AppConfig.getTestName() + "***\n\n");

        } else if (result.getMethod().isAfterClassConfiguration()) {
            AppConfig.setTestName(testClassName + ".baseTearDown");
            Log.info("*** DISMISSING DRIVERS " + AppConfig.getTestName() + "***\n\n");

        } else {
            AppConfig.setTestName(testClassName + " - Configuration Type Unknown");
            Log.info("*** EXECUTING CONFIGURATION - " + AppConfig.getTestName() + "***\n\n");
        }
    }

    @Override
    public void onTestStart(ITestResult result){
        AppConfig.setTestName(result.getTestClass().getName() + ".test");
        Log.info("*** TEST # " + AppConfig.getTestNumber() + " RUNNING " + AppConfig.getTestName() + "***\n\n");
    }


    @Override
    public void onConfigurationFailure(ITestResult result){
        executeFailure(AppConfig.getTestName()+ " - Configuration Failed", result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        executeFailure(AppConfig.getTestName() + " - Test Failed", result);
    }

    private void executeFailure(String message, ITestResult result) {

        if (!AppConfig.getBuildJobName().isEmpty()) {
            testFailMail failureEmail = new testFailMail(message, Log.getConfigurationSettings(), Log.stackTraceExtractor(result));
            failureEmail.send();

        } else {

            if (Driver.mobile != null) {
                MobileDriverUtils.getScreenshot("Failed");
            }

            if (Driver.web != null) {
                WebDriverUtils.getScreenshot("Failed");
            }
        }
    }

}
