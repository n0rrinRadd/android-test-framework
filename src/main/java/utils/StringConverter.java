package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * Created by axroberts on 12/4/15.
 */
public class StringConverter {

    public static String toHtml(String string) {
        if (string == null) {
            return "<html><body></body></html>";
        }
        BufferedReader reader = new BufferedReader(new StringReader(string));
        StringBuffer stringBuffer = new StringBuffer("<html><body>");
        try {
            String stringContext = reader.readLine();
            while (stringContext != null) {
                if (stringContext.equalsIgnoreCase("<br/>")) {
                    stringContext = "<br>";
                }
                stringBuffer.append(stringContext);
                if (!stringContext.equalsIgnoreCase("<br>")) {
                    stringBuffer.append("<br>");
                }
                stringContext = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        stringBuffer.append("</body></html>");
        string = stringBuffer.toString();
        return string;
    }

}
