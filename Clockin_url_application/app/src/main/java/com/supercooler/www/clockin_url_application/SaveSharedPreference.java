package com.supercooler.www.clockin_url_application;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * Created by gio on 1/19/2017
 * for SuperCooler Technologies
 *
 *
 */

/**
 * Class used to establish connection between the preference settings and the whole application.
 * Without repeating code.
 */

public class SaveSharedPreference {
    public static final String PREFS_NAME = "SETTINGS_PREFERENCE";
    public static final String IP_STRING = "ip";
    public static final String PORT_STRING = "port";




    //get the preference that requires true/activate or false/inactive reference such as mute or
    //be able to send emails.
    public static boolean getBooleanPreference(Context context, String name){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        boolean status = settings.getBoolean(name, true);
        return status;
    }

    //Stores the preference as a boolean.
    public static void storeBooleanPreference(Context context, String name, boolean status){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(name, status);

        // Commit the edits!
        editor.commit();
    }

    //Get the string that will be used for the recipient to send email updates.
    public static String getStringPreference(Context context, String name){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        String recipient = settings.getString(name, null);
        if(recipient == null){
            return "";
        }
        return recipient;
    }

    public static void storeStringPreference(Context context, String name, String data){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(name, data);
        // Commit the edits!
        editor.apply();
    }

    public static int getIntPreference(Context context, String name){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getInt(name, 100);
    }

    public static void storeIntPreference(Context context, String name, int data){
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(name, data);
        // Commit the edits!
        editor.apply();
    }

}
