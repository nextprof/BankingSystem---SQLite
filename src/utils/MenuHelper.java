package utils;

public class MenuHelper {

    public static int menuChoice(String value, int defaultVal) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e)
        {
            return defaultVal;
        }
    }
}
