package com.afaa.tanktrouble;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;

import com.afaa.tanktrouble.datahouse.GameData;

import org.jetbrains.annotations.Contract;

import java.util.Random;

public class UserUtils {
    private static SharedPreferences sharedPrefs;
    private static String[] adjectiveList, nounList;
    private static String username, userId;
    private static int screenWidth, screenHeight;
    private static float screenScale;

    private static final String USER_ID_KEY = Constants.USER_ID_KEY;
    private static final String USERNAME_KEY = Constants.USERNAME_KEY;

    public static void initialize(Activity activity) {
        adjectiveList = activity.getResources().getStringArray(R.array.adjective_list);
        nounList = activity.getResources().getStringArray(R.array.noun_list);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
        userId = sharedPrefs.getString(USER_ID_KEY, "");
        username = sharedPrefs.getString(USERNAME_KEY, "");
        setScreenSize(activity);
        setUsername(username);
    }


    private static void setScreenSize(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int statusBarHeight = 0;
        int resourceId = activity.getResources().getIdentifier
                ("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = activity.getResources().getDimensionPixelSize(resourceId);
        }

        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels - statusBarHeight;
        screenScale = screenWidth / 1080f;
    }


    public static float scaleGraphicsFloat(float scale) {
        return scale* screenWidth;
    }


    public static int scaleGraphicsInt(float scale) {
        return Math.round(scale* screenWidth);
    }



    public static void setUsername(String newUsername){
        if (userId.length() == 0 || username.length() == 0) {
            String randomUsername = generateRandomUsername();
            setFirstUsername(randomUsername);
        } else {
            updateUsername(newUsername);
        }
    }


    private static void setFirstUsername(final String firstUsername) {
        userId = Integer.toString(GameData.getInstance().getUserId());
        username = firstUsername;
        putStringInPrefs(USER_ID_KEY, userId);
        putStringInPrefs(USERNAME_KEY, firstUsername);
    }


    private static void updateUsername(final String newUsername) {
        username = newUsername;
        putStringInPrefs(USERNAME_KEY, newUsername);
    }


    @NonNull
    public static String generateRandomUsername() {

        Random random = new Random();


        String adjective1 = adjectiveList[random.nextInt(adjectiveList.length)];
        String adjective2 = adjectiveList[random.nextInt(adjectiveList.length)];
        String noun = nounList[random.nextInt(nounList.length)];


        adjective1 = adjective1.substring(0, 1).toUpperCase()
                + adjective1.substring(1).toLowerCase();
        adjective2 = adjective2.substring(0, 1).toUpperCase()
                + adjective2.substring(1).toLowerCase();
        noun = noun.substring(0, 1).toUpperCase()
                + noun.substring(1).toLowerCase();

        return adjective1 + adjective2 + noun;
    }


    private static void putStringInPrefs (String key, String value) {
        SharedPreferences.Editor sSharedPrefEditor = sharedPrefs.edit();
        sSharedPrefEditor.putString(key, value);
        sSharedPrefEditor.apply();
    }


    public static int randomInt (int min, int max){
        Random random = new Random();
        return random.nextInt(max-min+1) + min;
    }

    @Contract(pure = true)
    public static String getUsername() { return username; }

    @Contract(pure = true)
    public static String getUserId() { return userId; }

    @Contract(pure = true)
    public static int getScreenWidth() { return screenWidth; }

    @Contract(pure = true)
    public static int getScreenHeight() { return screenHeight; }

    @Contract(pure = true)
    public static float getScreenScale() { return screenScale; }



}
