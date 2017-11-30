package link.omny.supportservices.internal;

public class CsvUtils {

    public static String quoteIfNeeded(String text) {
        if (text.indexOf(',') == -1 && text.indexOf('\n') == -1) {
            return text;
        } else {
            return "\"" + text + "\"";
        }
    }

}
