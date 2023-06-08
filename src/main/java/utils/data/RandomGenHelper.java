package utils.data;

import java.util.Random;

public final class RandomGenHelper {

    public final static String randomSample  = "abcdefghijklmnopqrstuvwxyz1234567890";
    public static Random randomGenerator = new Random();

    private RandomGenHelper(){

    }

    public static String generateRandomAlphaNumString(int length){
        char[] sample = randomSample.toCharArray();
        String result = "";
        for (int i = 0; i < length; i++){
            int randomIndex = randomGenerator.nextInt(sample.length);
            result += sample[randomIndex];
        }
        return result;
    }

    public static String generateRandomAlphaString(int length){
        char[] sample = randomSample.substring(0, 26).toCharArray();
        String result = "";
        for (int i = 0; i < length; i++){
            int randomIndex = randomGenerator.nextInt(sample.length);
            result += sample[randomIndex];
        }
        return result;
    }

    public static String generateRandomNumString(int length){
        char[] sample = randomSample.substring(26).toCharArray();
        String result = "";
        for (int i = 0; i < length; i++){
            int randomIndex = randomGenerator.nextInt(sample.length);
            result+= sample[randomIndex];
        }
        return result;
    }


    public static int generateRandomNum(int length){
        int result = randomGenerator.nextInt((int)Math.pow(length, 10));
        return result;
    }
}
