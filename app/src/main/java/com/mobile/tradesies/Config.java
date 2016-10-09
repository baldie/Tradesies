package com.mobile.tradesies;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Date;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class Config {
    public static final String TAG = "Tradesies";
    public static final boolean TESTING = false;
    private static final String APP_SHARED_PREFS = "TradesiesPreferences";
    private static final String PREFS_USER_ID_KEY = "TradesiesUserId";
    private static final String PREFS_AUTH_TOKEN = "TradesiesAuthToken";
    public static final String API_KEY_HEADER = "api_key";
    public static final String API_KEY = "bf3ae48b-d884-4925-a412-78c0377f4ae1";
    public static final int MANUAL_REGISTRATION = 22;
    public static final String API_ENDPOINT = "http://thetradesiesapp.com";
    public static final int IMAGE_SIZE = 1024;
    public static final int THUMBNAIL_SIZE = 128;
    public static final int ITEM_TITLE_MAX_LENGTH = 80;
    public static final int INITIAL_RADIUS = 15;
    public static final int ITEM_DESCRIPTION_MAX_LENGTH = 420;
    public static final int BADGE_FONT_SIZE = 16;
    public static final int CHAT_POLL_INTERVAL = 5000;

    public static TradesiesCloud getService() {
        // Creates the json object which will manage the information received
        GsonBuilder builder = new GsonBuilder();

        // Register an adapter to manage the date types as long values
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                String data = json.getAsJsonPrimitive().getAsString().substring(6, 19);
                return new Date(Long.valueOf(data));
            }
        });

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestInterceptor.RequestFacade request) {
                        request.addHeader(Config.API_KEY_HEADER, Config.API_KEY);
                    }
                })
                .setLogLevel(TESTING ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                .setEndpoint(Config.API_ENDPOINT)
                .setConverter(new GsonConverter(builder.create()))
                .build();

        return restAdapter.create(TradesiesCloud.class);
    }

    private static int _userId = 0;
    public static void setUserId(int userId, Context context)
    {
        _userId = userId;
        SharedPreferences sharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
        sharedPrefs.edit().putInt(PREFS_USER_ID_KEY, userId).apply();
    }
    public static int getUserId(Context context){
        if (_userId == 0){
            SharedPreferences sharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
            _userId = sharedPrefs.getInt(PREFS_USER_ID_KEY, 0);
        }
        return _userId;
    }

    private static String _authToken = null;
    public static void setAuthToken(String authToken, Context context)
    {
        _authToken = authToken;
        SharedPreferences sharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
        sharedPrefs.edit().putString(PREFS_AUTH_TOKEN, authToken).apply();
    }
    public static String getAuthToken(Context context){
        if (_authToken == null){
            SharedPreferences sharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
            _authToken = sharedPrefs.getString(PREFS_AUTH_TOKEN, null);
        }
        return _authToken;
    }

    private static UserProfile _userProfile = new UserProfile();
    public static UserProfile getUserProfile(){
        return _userProfile;
    }

    public static int getNumberOfColumnsForScreen(Context context){
        int screenSize = context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        int columns = 2;
        switch(screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                columns = 4;
                break;
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                columns = 3;
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                columns = 2;
                break;
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                columns = 1;
                break;
        }
        return columns;
    }

    public static int getPageSizeForScreen(Context context){
        int screenSize = context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        int items_that_can_fit_on_screen = 2;
        switch(screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                items_that_can_fit_on_screen = 20;
                break;
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                items_that_can_fit_on_screen = 12;
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                items_that_can_fit_on_screen = 6;
                break;
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                items_that_can_fit_on_screen = 3;
                break;
        }
        return items_that_can_fit_on_screen * 2;
    }
}