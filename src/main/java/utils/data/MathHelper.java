package utils.data;

public final class MathHelper {

    private MathHelper() {

    }

    public static double roundToDecimalPlace(double number, int place){
        double multiplier = Math.pow(10, place);
        return Math.round(number * multiplier)/multiplier;
    }

    public static int[] convertStrToIntArray(String val){
        int initialInt = Integer.parseInt(val);
        int[] returnValue = new int[val.length()];
        for (int i = val.length() -1; i >= 0; i--){
            returnValue[i] = initialInt % 10;
            initialInt /= 10;
        }
        return returnValue;
    }
}
