package com.mobile.tradesies.home;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mobile.tradesies.AnalyticsUtil;
import com.mobile.tradesies.Config;
import com.mobile.tradesies.R;
import com.mobile.tradesies.Session;
import com.mobile.tradesies.datacontracts.GetItemsRequest;
import com.mobile.tradesies.datacontracts.GetItemsResponse;
import com.mobile.tradesies.datacontracts.GetMyTradesRequest;
import com.mobile.tradesies.datacontracts.GetMyTradesResponse;
import com.mobile.tradesies.datacontracts.Item;
import com.mobile.tradesies.datacontracts.OAuthenticateUserRequest;
import com.mobile.tradesies.datacontracts.OAuthenticateUserResponse;
import com.mobile.tradesies.item.ItemDetailActivity;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MarketPlaceFragment extends Fragment implements TradesiesFragment, View.OnClickListener {

    final static int DOWNWARDS = 1;
    final static int UPWARDS = -1;
    private RecyclerView mGridView;
    final static String WHITESPACE_PATTERN = "\\W+";
    public MarketPlaceFragment() {    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_market_place, container, false);

        Toolbar toolbar = (Toolbar)rootView.findViewById(R.id.toolbar);
        if (toolbar != null) {
            ((ActionBarActivity) getActivity()).setSupportActionBar(toolbar);

            ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle(getActivity().getString(R.string.marketplace));
        }

        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        drawer.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

        mGridView = (RecyclerView)rootView.findViewById(R.id.gridView);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(Config.getNumberOfColumnsForScreen(getActivity()), StaggeredGridLayoutManager.VERTICAL);
        mGridView.setLayoutManager(layoutManager);
        mGridView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!mGridView.canScrollVertically(DOWNWARDS))
                    invokeReachedTheBottom();
            }
        });

        final PullToRefresh swipeView = (PullToRefresh)rootView.findViewById(R.id.swipe);
        swipeView.setColorSchemeColors(R.color.tradesiesColorPrimaryDark, R.color.tradesiesColorPrimary, R.color.tradesiesColorPrimaryWeak);
        swipeView.setScrollResolver(new PullToRefresh.ScrollResolver() {
            @Override
            public boolean canScrollUp() {
                return mGridView.canScrollVertically(UPWARDS);
            }
        });
        swipeView.setOnRefreshListener(new PullToRefresh.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeView.setRefreshing(true);

                ((HomeActivity)getActivity()).refreshNotifications();

                Session.getInstance().refreshMyTrades(getActivity());

                startReloadMarketPlace();
            }
        });

        return rootView;
    }

    @Override
    public void onClick(View v) {
        if (mGridView == null || v == null)
            return;

        // Launch details screen for item
        int index = mGridView.getChildPosition(v);
        GridViewAdapter adapter = (GridViewAdapter)mGridView.getAdapter();
        Item item = adapter.getItem(index);
        ItemDetailActivity.launch(getActivity(), v, item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onResume(){
        super.onResume();
        doOAuth();
    }

    private void doOAuth(){
        if (Config.getUserId(getActivity()) != 0 && Config.getAuthToken(getActivity()) != null) {
            final HomeActivity that = (HomeActivity)getActivity();

            final OAuthenticateUserRequest request = new OAuthenticateUserRequest();
            request.UserId = Config.getUserId(getActivity());
            request.AuthToken = Config.getAuthToken(getActivity());

            Config.getService().oAuthenticate(request, new Callback<OAuthenticateUserResponse>() {
                @Override
                public void success(OAuthenticateUserResponse response, Response lel) {
                    if (lel.getStatus() != 200) {
                        Toast.makeText(that, that.getString(R.string.generic_error), Toast.LENGTH_LONG).show();
                    } else {
                        if (response.Error != null && response.Error.length() > 0) {
                            Toast.makeText(that, response.Error, Toast.LENGTH_LONG).show();
                            that.logUserOut(that);
                            that.requestManualLogin();
                        }
                        else {
                            // Set the data for the currently logged in user.
                            Config.getUserProfile().UserId = response.UserId;
                            Config.getUserProfile().Name = response.Name;
                            Config.getUserProfile().PhotoUrl = response.PhotoUrl;
                            Config.getUserProfile().IsEmailVerified = response.IsEmailVerified;

                            Session.getInstance().refreshMyTrades(getActivity());

                            that.refreshNotifications();

                            startReloadMarketPlace();
                        }
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    String errorMessage = error.getKind() == RetrofitError.Kind.NETWORK ? getString(R.string.unable_to_connect) : error.getMessage();
                    Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        }
        else {
            // Non registered users can browse as well
            startReloadMarketPlace();
        }
    }

    private void startReloadMarketPlace(){
        // We have since moved on from this fragment
        if (getFragmentManager() == null)
            return;

        // Clear current results
        final Activity that = getActivity();
        final MarketPlaceFragment fragment = this;
        final RecyclerView gridView = (RecyclerView) that.findViewById(R.id.gridView);
        final GridViewAdapter adapter = (GridViewAdapter)gridView.getAdapter();
        if (adapter != null && mCurrentSearchOffset == 0)
            adapter.clear();

        // Show progress
        final View progress = that.findViewById(R.id.bkg_progress);
        if (progress == null)
            return;
        progress.setVisibility(View.VISIBLE);

        // Get the user's last known location.
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);

        GetItemsRequest getItemsRequest = new GetItemsRequest();
        getItemsRequest.UserId = Config.getUserId(getActivity());
        getItemsRequest.Limit = Config.getPageSizeForScreen(that);
        getItemsRequest.Radius = Config.INITIAL_RADIUS;
        getItemsRequest.Offset = mCurrentSearchOffset;
        getItemsRequest.Latitude = String.valueOf(lastKnownLocation.getLatitude());
        getItemsRequest.Longitude = String.valueOf(lastKnownLocation.getLongitude());
        getItemsRequest.SearchTerms = mCurrentQuery != null ? mCurrentQuery.split(WHITESPACE_PATTERN) : null;

        final PullToRefresh swipeView = (PullToRefresh)that.findViewById(R.id.swipe);

        Config.getService().getItems(getItemsRequest, new Callback<GetItemsResponse>() {
            @Override
            public void success(GetItemsResponse response, Response lel) {
                progress.setVisibility(View.INVISIBLE);
                swipeView.setRefreshing(false);
                mIsLoading = false;

                if (lel.getStatus() != 200) {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.generic_error), Toast.LENGTH_LONG).show();
                } else {
                    if (response.Error != null && response.Error.length() > 0)
                        Toast.makeText(getActivity(), response.Error, Toast.LENGTH_LONG).show();
                    else {
                        if (response.Items == null || response.Items.length == 0){
                            // We've made it beyond the first page, and no results have been returned.
                            if (mCurrentSearchOffset > 0) {
                                mNothingMoreToLoad = true;
                                // todo: expand radius?
                            }
                            else
                            {
                                // todo: empty set
                            }

                        }else{

                            boolean appendResults = mCurrentSearchOffset > 0;
                            if (appendResults) {
                                adapter.addItems(response.Items);
                            } else {
                                gridView.setAdapter(new GridViewAdapter(response.Items, fragment));
                            }
                        }
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                String errorMessage = error.getKind() == RetrofitError.Kind.NETWORK ? getString(R.string.unable_to_connect) : error.getMessage();
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean mIsLoading = false;
    int mCurrentSearchOffset = 0;
    private String mCurrentQuery = null;
    private boolean mNothingMoreToLoad = false;

    public void doSearch(String query){
        // If the user has changed search terms, then start at the first page again.
        if (mCurrentQuery != null && !mCurrentQuery.equals(query)){
            mCurrentSearchOffset = 0;
        }
        mCurrentQuery = query;

        AnalyticsUtil.trackSearchConducted(getActivity(), query);
        startReloadMarketPlace();
    }

    @Override
    public int getMenuId() {
        return R.menu.menu_marketplace;
    }

    private void invokeReachedTheBottom(){
        if (mNothingMoreToLoad)
            return;

        if (!mIsLoading) {
            mIsLoading = true;
            mCurrentSearchOffset += Config.getPageSizeForScreen(getActivity());
            startReloadMarketPlace();
        }
    }
}