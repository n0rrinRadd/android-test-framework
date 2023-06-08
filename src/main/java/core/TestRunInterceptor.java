package core;

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import utils.Log;
import utils.TestGroup;


/**
 * Created by axroberts on 1/20/16.
 */
public class TestRunInterceptor implements IMethodInterceptor {

    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        List<IMethodInstance> testSuite;

        if (!AppConfig.getPackageName().isEmpty()){
            testSuite = filterTestSuiteByTestPackage(methods);
        }
        else {
            testSuite = filterTestSuiteByTaggedGroups(methods, context);
        }

        testSuite.sort((IMethodInstance m1, IMethodInstance m2) -> m1.getInstance().getClass().getPackage().getName()
                .compareTo(m2.getInstance().getClass().getPackage().getName()));

        Log.info("The following tests will now be run: ");
        testSuite.forEach((IMethodInstance) -> Log.info(IMethodInstance.getInstance().getClass().getName()));

        return testSuite;
    }


    public List<IMethodInstance> filterTestSuiteByTestPackage(List<IMethodInstance> methods) {
        List<IMethodInstance> testSuite = new ArrayList<>();

        Log.info("Filtering tests based on provided package name: " + AppConfig.getPackageName());

        methods.forEach((IMethodInstance) -> {
            if (IMethodInstance.getInstance().getClass().getPackage().toString().contains(AppConfig.getPackageName())) {
                testSuite.add(IMethodInstance);
            }
        });

        return testSuite;
    }

    public  List<IMethodInstance> filterTestSuiteByTaggedGroups(List<IMethodInstance> methods, ITestContext context) {
        List<IMethodInstance> testSuite = new ArrayList<>();
        Set<String> testSuiteGroups = new HashSet(Arrays.asList(context.getIncludedGroups()));
        Set<String> configurationGroupFilter = new HashSet<>(Arrays.asList(TestGroup.SETUP, TestGroup.TEARDOWN));

        Log.info("Filtering tests based on provided groups: " + testSuiteGroups.toString());

        for(IMethodInstance testCase : methods){
            Set<String> testCaseGroups = new HashSet(Arrays
                    .asList(testCase.getMethod().getConstructorOrMethod().getMethod().getAnnotation(Test.class).groups()));

            testSuiteGroups.removeAll(configurationGroupFilter);

            if(testCaseGroups.containsAll(testSuiteGroups)) {
             testSuite.add(testCase);
            }

        }

        return testSuite;
    }
}
