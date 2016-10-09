package com.mobile.tradesies;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.common.primitives.Ints;
import com.mobile.tradesies.datacontracts.AcknowledgeNotificationsRequest;
import com.mobile.tradesies.datacontracts.AcknowledgeNotificationsResponse;
import com.mobile.tradesies.datacontracts.GetMyTradesRequest;
import com.mobile.tradesies.datacontracts.GetMyTradesResponse;
import com.mobile.tradesies.datacontracts.GetNotificationsRequest;
import com.mobile.tradesies.datacontracts.GetNotificationsResponse;
import com.mobile.tradesies.datacontracts.Notification;
import com.mobile.tradesies.datacontracts.Trade;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by David on 6/7/2015.
 */
public class Session {

    private static Session mInstance;
    private Session(){}

    public static Session getInstance(){
        if (mInstance == null)
            mInstance = new Session();
        return mInstance;
    }

    public static boolean hasDied(){
        return mInstance == null;
    }

    public void reset(Context context){
        Config.setAuthToken(null, context);
        Config.setUserId(0, context);
        Session.getInstance().clearNotifications();
        Session.getInstance().clearTrades();
    }

    private ArrayList<Trade> mTrades = null;

    private void setTrades(Trade[] trades){
        if (trades != null) {
            mTrades = new ArrayList<>(Arrays.asList(trades));
        }else {
            mTrades = null;
        }
    }
    private void clearTrades(){
        if (mTrades != null)
            mTrades.clear();
    }
    public ArrayList<Trade> getTrades(){
        return mTrades;
    }

    public void refreshMyTrades(final Context context){
        if (context == null)
            return;

        if (Config.getUserId(context) == 0)
        {
            if (mTrades != null)
                mTrades.clear();
            return;
        }

        Log.d(Config.TAG, "Refreshing trades...");

        GetMyTradesRequest request = new GetMyTradesRequest();
        request.UserId = Config.getUserId(context);
        request.AuthToken = Config.getAuthToken(context);

        // Get the user's trades and store in session. Fail silently.
        Config.getService().getMyTrades(request, new Callback<GetMyTradesResponse>() {
            @Override
            public void success(GetMyTradesResponse response, Response lel) {
                if (lel.getStatus() != 200) {
                    Log.e(Config.TAG, "Error when getting my trades: http code: " + lel.getStatus());
                } else {
                    if (response.Error != null && response.Error.length() > 0) {
                        if (Config.TESTING)
                            Toast.makeText(context, response.Error, Toast.LENGTH_LONG).show();

                        Log.e(Config.TAG, response.Error);
                    } else {

                        setTrades(response.Trades);

                        Log.d(Config.TAG, "Trades refreshed");
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (Config.TESTING)
                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();

                Log.e(Config.TAG, error.getMessage());
            }
        });
    }

    private ArrayList<Notification> mNotifications;
    public ArrayList<Notification> getNotifications(){
        return mNotifications;
    }
    public void setNotifications(Notification[] notifications){
        if (notifications != null) {
            mNotifications = new ArrayList<>(Arrays.asList(notifications));
        }else {
            mNotifications = null;
        }
    }
    public void setNotifications(ArrayList<Notification> notifications){
        mNotifications = notifications;
    }
    public void clearNotifications(){
        if (mNotifications != null)
            mNotifications.clear();
    }

    public static void acknowledgeNotification(final Activity that, Notification notification){
        final ArrayList<Integer> notificationsToAcknowledge = new ArrayList<>();
        notificationsToAcknowledge.add(notification.Id);
        Session.acknowledgeNotifications(that, notificationsToAcknowledge);
    }
    public static void acknowledgeNotifications(final Activity that, final ArrayList<Integer> notificationsToAcknowledge){
        if (notificationsToAcknowledge.size() == 0)
            return;

        Log.d(Config.TAG, "Acknowledging notifications...");
        AcknowledgeNotificationsRequest request = new AcknowledgeNotificationsRequest();
        request.UserId = Config.getUserId(that);
        request.AuthToken = Config.getAuthToken(that);
        request.NotificationIds = Ints.toArray(notificationsToAcknowledge);

        Config.getService().acknowledgeNotifications(request, new Callback<AcknowledgeNotificationsResponse>() {
            @Override
            public void success(AcknowledgeNotificationsResponse response, Response http) {

                boolean errorOccurred = response.Error != null && response.Error.length() > 0;
                if (!errorOccurred) {

                    // Remove the notifications that we have acknowledged from memory
                    final ArrayList<Notification> notificationsToKeep = new ArrayList<>();
                    ArrayList<Notification> notifications = Session.getInstance().getNotifications();
                    for(Notification n : notifications)
                        if (!notificationsToAcknowledge.contains(n.Id))
                            notificationsToKeep.add(n);

                    Session.getInstance().setNotifications(notificationsToKeep);

                    Log.d(Config.TAG, "Acknowledged notifications");
                } else {
                    if (Config.TESTING)
                        Toast.makeText(that, response.Error, Toast.LENGTH_SHORT).show();
                    Log.e(Config.TAG, response.Error);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (Config.TESTING)
                    Toast.makeText(that, error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(Config.TAG, error.getMessage());
            }
        });
    }
}