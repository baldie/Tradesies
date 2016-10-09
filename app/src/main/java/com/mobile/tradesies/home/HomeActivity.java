package com.mobile.tradesies.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.tradesies.AnalyticsUtil;
import com.mobile.tradesies.Config;
import com.mobile.tradesies.ImageUtil;import com.mobile.tradesies.R;
import com.mobile.tradesies.Session;
import com.mobile.tradesies.account.UserAccountFragment;
import com.mobile.tradesies.datacontracts.GetNotificationsRequest;
import com.mobile.tradesies.datacontracts.GetNotificationsResponse;
import com.mobile.tradesies.datacontracts.LogOutRequest;
import com.mobile.tradesies.datacontracts.LogOutResponse;
import com.mobile.tradesies.datacontracts.Notification;
import com.mobile.tradesies.registration.RegistrationSelectionActivity;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class HomeActivity extends AppCompatActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    final public static String NAVIGATION_FRAGMENT_REQUESTED = "destination_fragment";
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private TradesiesFragment mFragment;
    private static final int MY_ITEMS = 1;
    private static final int MARKETPLACE = 0;
    private static final int MY_ACCOUNT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        String destination = getIntent().getStringExtra(HomeActivity.NAVIGATION_FRAGMENT_REQUESTED);
        if (destination != null)
        {
            if (destination.equals(MyItemsFragment.class.toString()))
                onNavigationDrawerItemSelected(MY_ITEMS);
        }
    }

    public void refreshBackToMarketplace(){
        mNavigationDrawerFragment.refreshDrawer();
        mNavigationDrawerFragment.selectItem(MARKETPLACE);
    }

    public void refreshNotifications(){
        final Context that = this;
        if (Config.getUserId(that) == 0){
            Session.getInstance().clearNotifications();
            return;
        }

        GetNotificationsRequest request = new GetNotificationsRequest();
        request.UserId = Config.getUserId(this);
        request.AuthToken = Config.getAuthToken(this);

        // Get the user's notifications and store in session. Fail silently.
        Config.getService().getNotifications(request, new Callback<GetNotificationsResponse>() {
            @Override
            public void success(GetNotificationsResponse response, Response http) {
                if (http.getStatus() != 200) {
                    Log.e(Config.TAG, "Error when getting notifications: HTTP code: " + http.getStatus());
                } else {
                    if (response.Error != null && response.Error.length() > 0) {
                        if (Config.TESTING)
                            Toast.makeText(that, response.Error, Toast.LENGTH_LONG).show();

                        Log.e(Config.TAG, response.Error);
                    } else {

                        // Add badges to the drawer as necessary
                        if (mNavigationDrawerFragment != null)
                            mNavigationDrawerFragment.refreshDrawer();

                        Session.getInstance().setNotifications(response.Notifications);

                        //todo: show
                        ArrayList<Notification> notifications = Session.getInstance().getNotifications();
                        if (notifications != null)
                            for (Notification n : notifications)
                                showNotification(n);
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (Config.TESTING)
                    Toast.makeText(that, error.getMessage(), Toast.LENGTH_LONG).show();

                Log.e(Config.TAG, error.getMessage() != null ? error.getMessage() : getString(R.string.generic_error));
            }
        });
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        switch (position) {
            case MARKETPLACE:
                mFragment = new MarketPlaceFragment();
                break;
            case MY_ITEMS:
                mFragment = new MyItemsFragment();
                break;
            case MY_ACCOUNT:
                mFragment = new UserAccountFragment();
                break;
            case 3:
                if (Config.getUserId(this) == 0) {
                    requestManualLogin();
                    return;
                }else {
                    attemptLogOutWithPrompt();
                }
                break;
            case 4:
                if (Config.getUserId(this) == 0) {
                    RegistrationSelectionActivity.launch(this, findViewById(R.id.drawer_layout));
                    return;
                }else {
                    mFragment = new HelpMenuFragment();
                }
                break;
            case 5:
                mFragment = new HelpMenuFragment();
                break;
        }

        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, (Fragment) mFragment)
                .commit();

        if (mNavigationDrawerFragment != null)
            mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    public void requestManualLogin(){
        // Have the user log in manually.
        Intent loginIntent = new Intent(this, LoginDialog.class);
        loginIntent.putExtra(LoginDialog.REQUEST_CODE, LoginDialog.REQUEST_CODE_FOR_LOGIN);
        startActivityForResult(loginIntent, LoginDialog.REQUEST_CODE_FOR_LOGIN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != LoginDialog.REQUEST_CODE_FOR_LOGIN)
            return;

        if (resultCode != RESULT_OK)
            return;

        refreshBackToMarketplace();

        AnalyticsUtil.trackLogIn(this);
    }

    public void attemptLogOutWithPrompt() {
        final HomeActivity that = this;
        new AlertDialog.Builder(this)
            .setTitle("Log out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    logUserOut(that);
                }
            })
            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // do nothing
                }
            })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    public void logUserOut(final HomeActivity that){
        Log.d(Config.TAG, "Logging out...");

        LogOutRequest request = new LogOutRequest();
        request.AuthToken = Config.getAuthToken(that);
        request.UserId = Config.getUserId(that);

        Config.getService().logOut(request, new Callback<LogOutResponse>() {
            @Override
            public void success(LogOutResponse response, Response r) {
                Session.getInstance().reset(that);

                Toast.makeText(that, R.string.you_have_been_logged_out, Toast.LENGTH_SHORT).show();

                // Refresh screen
                that.refreshBackToMarketplace();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(that, R.string.generic_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mFragment == null)
            return false;

        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            if (mFragment.getMenuId() != 0)
                getMenuInflater().inflate(mFragment.getMenuId(), menu);
            restoreActionBar();

            MenuItem searchMenuItem = menu.findItem(R.id.menu_search_items);
            if (searchMenuItem != null) {
                SearchView searchView = (SearchView) searchMenuItem.getActionView();
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        if (mFragment instanceof MarketPlaceFragment) {
                            ((MarketPlaceFragment) mFragment).doSearch(query);
                        }
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String query) {
                        // If the user removes all search terms, lets research for all items automatically
                        if (query == null || query.trim().length() == 0)
                            ((MarketPlaceFragment) mFragment).doSearch("");
                        return false;
                    }
                });
            }

            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_categories) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showNotification(Notification notification){
        LayoutInflater inflater = getLayoutInflater();
        View notificationView = inflater.inflate(R.layout.notification_toast, null);
        notificationView.setTag(notification);

        // Create a custom toast
        final Crouton toast = Crouton.make(this, notificationView);

        // Load the image into the toast view
        ImageView imageView = (ImageView) notificationView.findViewById(R.id.notification_image);
        if (notification.PhotoUrl != null && notification.PhotoUrl.length() > 0){
            Picasso
                .with(this)
                .load(ImageUtil.mapServerPath(notification.PhotoUrl))
                .noPlaceholder()
                .resize(Config.THUMBNAIL_SIZE, Config.THUMBNAIL_SIZE)
                .into(imageView);
        }

        // Set the text
        TextView text = (TextView) notificationView.findViewById(R.id.notification_text);
        text.setText(notification.Title);

        final Activity that = this;
        notificationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast.hide();

                // The user clicked on the notification, therefore it has been acknowledged
                Session.acknowledgeNotification(that, (Notification) v.getTag());

                // TODO: for MVP, clicking on any notification takes you to my items page
                onNavigationDrawerItemSelected(MY_ITEMS);
            }
        });
        toast.show();
    }

    @Override
    public void onResume(){
        super.onResume();

        // The app was sitting around open on the user's phone
        // Android could have garbage collected the data in memory
        if (Session.hasDied()){
            Session.getInstance().refreshMyTrades(this);
            refreshNotifications();
        }
    }

    @Override
    public void onDestroy(){
        Crouton.cancelAllCroutons();
        super.onDestroy();
    }
}