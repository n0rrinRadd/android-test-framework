package utils.mail;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.InternetAddress;

import core.AppConfig;
import core.Driver;
import utils.Log;

public class testFailMail extends testMail {

    public testFailMail(String failureSummary, String configuration, String stackTrace) {
        try {
            InternetAddress jiraEmail = new InternetAddress("jira@test.atlassian.net", "JIRA");
            List<File> screenshots = new ArrayList<>();

            if (Driver.mobile != null) {
                File mobileScreenshot = ((TakesScreenshot) Driver.mobile).getScreenshotAs(OutputType.FILE);
                screenshots.add(mobileScreenshot);
            }

            if (Driver.web != null) {
                File webScreenshot = ((TakesScreenshot) Driver.web).getScreenshotAs(OutputType.FILE);
                screenshots.add(webScreenshot);
            }

            if (AppConfig.getBuildJobName().contains("integration")) {
                mCC = new InternetAddress("automation+integration@test.com", "JIRA");
            }
            else if (AppConfig.getBuildJobName().contains("retro")) {
                mCC = new InternetAddress("automation+retro@test.com", "JIRA");
            }
            else if (AppConfig.getBuildJobName().contains("menusphere")) {
                mCC = new InternetAddress("automation+menusphere@test.com", "JIRA");
            }
            else if (AppConfig.getBuildJobName().contains("release-candidate-ga")) {
                mCC = new InternetAddress("automation+release-ga@test.com", "JIRA");
            }
            else if (AppConfig.getBuildJobName().contains("release-candidate-preview")) {
                mCC = new InternetAddress("automation+release-preview@test.com", "JIRA");
            }
            else if (AppConfig.getBuildJobName().contains("generally-available")) {
                mCC = new InternetAddress("automation+generally-available@test.com", "JIRA");
            }
            else if (AppConfig.getBuildJobName().contains("preview-pool")) {
                mCC = new InternetAddress("automation+preview-pool@test.com", "JIRA");
            }

            mSession = sessionConstructor("testqa@gmail.com", "");
            mRecipient = jiraEmail;
            mTitle = failureSummary;
            mMessage = emailMessageConstructor(configuration + stackTrace);
            screenshotAttachments = imageAttachmentConstructor(screenshots);

            prepareEmail();

        } catch (Exception exception) {
            Log.info("Failure: Unable to Generate Failure Email!");
        }
    }

}
