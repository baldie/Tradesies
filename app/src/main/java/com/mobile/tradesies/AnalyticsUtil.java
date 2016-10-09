package com.mobile.tradesies;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.mobile.tradesies.datacontracts.Item;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by David on 10/2/2015.
 */
public class AnalyticsUtil {

    public static void trackLogIn(Context context){
        Bundle bundle = new Bundle();
        bundle.putBoolean("EmailVerified", Config.getUserProfile().IsEmailVerified);
        FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
    }

    public static void trackRegistrationStartPageLaunched(Context context){
        FirebaseAnalytics.getInstance(context).logEvent("Registration start", null);
    }

    public static void trackRegistrationDetailsPageLaunched(Context context){
        FirebaseAnalytics.getInstance(context).logEvent("Registration details", null);
    }

    public static void trackRegistrationCompleted(Context context){
        FirebaseAnalytics.getInstance(context).logEvent("Registration completed", null);
    }

    public static void trackItemDetailsLaunched(Context context){
        FirebaseAnalytics.getInstance(context).logEvent("Item detail launched", null);
    }

    public static void trackTradeProposalStarted(Context context){
        FirebaseAnalytics.getInstance(context).logEvent("Trade proposal started", null);
    }

    public static void trackTradeProposalCompleted(Context context){
        FirebaseAnalytics.getInstance(context).logEvent("Trade proposal completed", null);
    }

    public static void trackTradeAccepted(Context context){
        FirebaseAnalytics.getInstance(context).logEvent("Trade accepted", null);
    }

    public static void trackTradeDeclined(Context context){
        FirebaseAnalytics.getInstance(context).logEvent("Trade declined", null);
    }

    public static void trackItemAdded(Context context, Item newItem){
        Bundle props = new Bundle();
        props.putInt("Number of photos", newItem.Photos.length);
        props.putInt("Description length", newItem.Description.length());
        props.putString("Latitude", newItem.Latitude);
        props.putString("Longitude", newItem.Longitude);
        FirebaseAnalytics.getInstance(context).logEvent("New item added", props);
    }

    public static void trackSearchConducted(Context context, String searchTerm){
        if (searchTerm == null)
            return;

        if (searchTerm.trim().length() == 0)
            return;

        Bundle props = new Bundle();
        props.putString("Search term", searchTerm);
        FirebaseAnalytics.getInstance(context).logEvent("Item search performed", props);
    }

    public static void trackHelpVisit(Context context){
        FirebaseAnalytics.getInstance(context).logEvent("Help launched", null);
    }
}