package utils;

import org.testng.asserts.Assertion;
import org.testng.asserts.IAssert;

//
//  Created by Marko Zhen on 2015-10-23.
//

public class Assert extends Assertion {

    @Override
    public void onAssertSuccess(IAssert<?> assertCommand) {
        String command = getAssertCommandFromStackTrace();
        Log.success(command + " -" + ( (command.contains("Equals")) ? "  Expected: " + assertCommand.getExpected() + " ," : "" ) + "  Actual: " + assertCommand.getActual());
    }

    private String getAssertCommandFromStackTrace() {

        StackTraceElement[] trace = Thread.currentThread().getStackTrace();

        try {
            for (int i = 0; i < trace.length; i++) {

                if (trace[i].toString().contains("org.testng.asserts.Assertion.assert")) {
                     return trace[i].getMethodName() + " in " + trace[i + 1].getClassName() + "." + trace[i + 1].getMethodName() + ":" + trace[i + 1].getLineNumber();
                }
            }
        } catch (Exception e) {
            // Fail silently
        }

        return "Assert Success";
    }

}