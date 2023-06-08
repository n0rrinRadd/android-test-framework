package utils;

import org.apache.commons.exec.util.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestResult;

import core.AppConfig;

//
// Created by Marko Zhen on 2015-10-08.
//

public final class Log {

    private static String NEW_LINE = System.getProperty("line.separator");
    private static final Level PASS = Level.forName("PASS", 400);
    private static Logger log = LogManager.getLogger();

    private Log () {}

    public static void debug(String s) {
        log.debug(s);
    }

    public static void success(String s) {
        log.log(PASS, s);
    }

    public static void info(String s) {
        log.info(s);
    }

    public static void warn(String s) {
        log.warn(s);
    }

    public static void error(String s) {
        log.error(s);
    }



    private static String stackTraceFormatter(Throwable throwable) {

        StringBuilder result = new StringBuilder();
        result.append(throwable.toString()).append(NEW_LINE);

        for (StackTraceElement element : throwable.getStackTrace()) {
            result.append("&#9;").append(element).append(NEW_LINE);
        }

        return result.toString();
    }

    public static String stackTraceExtractor(ITestResult testResult) {

        StringBuilder stackTraceAsString = new StringBuilder();
        stackTraceAsString.append("\\\\ *Stack Trace from test Failure :* ");
        stackTraceAsString.append(NEW_LINE);

        stackTraceAsString.append("{code:java}").append(NEW_LINE);
        stackTraceAsString.append(stackTraceFormatter(testResult.getThrowable()));

        if (testResult.getThrowable().getCause() != null) {
            stackTraceAsString.append(NEW_LINE).append("Caused by : ");
            stackTraceAsString.append(stackTraceFormatter(testResult.getThrowable().getCause()));
        }

        stackTraceAsString.append("{code}").append(NEW_LINE);
        return stackTraceAsString.toString();
    }


    public static String getConfigurationSettings() {

        String NEW_LINE = System.getProperty("line.separator");
        String[] testNameString = StringUtils.split(AppConfig.getTestName(), ".");

        return  "*TEST # :* "       + AppConfig.getTestNumber()     + NEW_LINE +
                "*TEST PACKAGE :* "   + testNameString[0]             + NEW_LINE +
                "*TEST CLASS :* "    + testNameString[1]             + NEW_LINE +
                "*MACHINE :* "      + AppConfig.getMachine()        + NEW_LINE +
                "*ENVIRONMENT :* "  + AppConfig.gettestEnv()       + NEW_LINE +
                "*BROWSER :* "      + AppConfig.getBrowser()        + NEW_LINE +

                "*BUILD_URL :* "    + "http://"+ AppConfig.getJenkinsIp() + ":" + AppConfig.getJenkinsPort() + "/job/" + AppConfig.getBuildJobName() + "/"
                                    + AppConfig.getBuildJobNumber() + NEW_LINE + NEW_LINE;
    }

}
