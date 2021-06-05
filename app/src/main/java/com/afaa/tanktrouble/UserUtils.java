package com.afaa.tanktrouble;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;

import com.afaa.tanktrouble.datahouse.GameData;

import org.jetbrains.annotations.Contract;

import java.util.Random;

public class UserUtils {
    private static SharedPreferences sSharedPref;
    private static String[] sAdjectiveList, sNounList;
    private static String sUsername, sUserId;
    private static int sScreenWidth, sScreenHeight;
    private static float sScreenScale;

    private static final String USERS_KEY = Constants.USERS_KEY;
    private static final String USER_ID_KEY = Constants.USER_ID_KEY;
    private static final String USERNAME_KEY = Constants.USERNAME_KEY;
    private static final String TAG = "WL/UserUtils";

    public static void initialize(Activity activity) {
//        Log.d(TAG, "initialize UserUtils");


        sAdjectiveList = activity.getResources().getStringArray(R.array.adjective_list);
        sNounList = activity.getResources().getStringArray(R.array.noun_list);


        sSharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        sUserId = sSharedPref.getString(USER_ID_KEY, "");
        sUsername = sSharedPref.getString(USERNAME_KEY, "");


        setScreenSize(activity);
        setUsername(sUsername);
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

        sScreenWidth = metrics.widthPixels;
        sScreenHeight = metrics.heightPixels - statusBarHeight;
        sScreenScale = sScreenWidth / 1080f;
    }


    public static float scaleGraphicsFloat(float scale) {
        return scale*sScreenWidth;
    }


    public static int scaleGraphicsInt(float scale) {
        return Math.round(scale*sScreenWidth);
    }



    public static void setUsername(String newUsername){
        if (sUserId.length() == 0 || sUsername.length() == 0) {
            String randomUsername = generateRandomUsername();
            setFirstUsername(randomUsername);
        } else {
            updateUsername(newUsername);
        }
    }


    private static void setFirstUsername(final String firstUsername) {
        sUserId = Integer.toString(GameData.getInstance().getThisPlayer());
        sUsername = firstUsername;
        putStringInPrefs(USER_ID_KEY, sUserId);
        putStringInPrefs(USERNAME_KEY, firstUsername);
//        Log.d(TAG, "added new user with sUserId=" + sUserId
//                + " and sUsername=" + sUsername);
    }


    private static void updateUsername(final String newUsername) {
        sUsername = newUsername;
        putStringInPrefs(USERNAME_KEY, newUsername);
//        Log.d(TAG, "updated new username for sUserId=" + sUserId
//                + " with sUsername=" + sUsername);
    }


    @NonNull
    public static String generateRandomUsername() {

        Random random = new Random();


        String adjective1 = sAdjectiveList[random.nextInt(sAdjectiveList.length)];
        String adjective2 = sAdjectiveList[random.nextInt(sAdjectiveList.length)];
        String noun = sNounList[random.nextInt(sNounList.length)];


        adjective1 = adjective1.substring(0, 1).toUpperCase()
                + adjective1.substring(1).toLowerCase();
        adjective2 = adjective2.substring(0, 1).toUpperCase()
                + adjective2.substring(1).toLowerCase();
        noun = noun.substring(0, 1).toUpperCase()
                + noun.substring(1).toLowerCase();

        return adjective1 + adjective2 + noun;
    }


    private static void putStringInPrefs (String key, String value) {
        SharedPreferences.Editor sSharedPrefEditor = sSharedPref.edit();
        sSharedPrefEditor.putString(key, value);
        sSharedPrefEditor.apply();
    }


    public static int randomInt (int min, int max){
        Random random = new Random();
        return random.nextInt(max-min+1) + min;
    }

    @Contract(pure = true)
    public static String getUsername() { return sUsername; }

    @Contract(pure = true)
    public static String getUserId() { return sUserId; }

    @Contract(pure = true)
    public static int getScreenWidth() { return sScreenWidth; }

    @Contract(pure = true)
    public static int getScreenHeight() { return sScreenHeight; }

    @Contract(pure = true)
    public static float getScreenScale() { return sScreenScale; }



}
