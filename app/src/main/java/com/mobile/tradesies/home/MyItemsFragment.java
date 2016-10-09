package com.mobile.tradesies.home;

import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import com.mobile.tradesies.R;
import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.widget.Toast;
import com.mobile.tradesies.Config;
import com.mobile.tradesies.Session;
import com.mobile.tradesies.datacontracts.GetMyItemsRequest;
import com.mobile.tradesies.datacontracts.GetMyItemsResponse;
import com.mobile.tradesies.datacontracts.Item;
import com.mobile.tradesies.datacontracts.OAuthenticateUserRequest;
import com.mobile.tradesies.datacontracts.OAuthenticateUserResponse;
import com.mobile.tradesies.item.ItemDetailActivity;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MyItemsFragment extends Fragment implements TradesiesFragment, View.OnClickListener {

    private RecyclerView mGridView;
    private DrawerLayout drawer;

    public MyItemsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my_items, container, false);

        Toolbar toolbar = (Toolbar)rootView.findViewById(R.id.toolbar);
        if (toolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle(getActivity().getString(R.string.my_items));
        }

        drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        drawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mGridView = (RecyclerView)rootView.findViewById(R.id.gridView);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(Config.getNumberOfColumnsForScreen(getActivity()), StaggeredGridLayoutManager.VERTICAL);
        mGridView.setLayoutManager(layoutManager);

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

    // do we really need to do oAuth again?
    private void doOAuth(){
        if (Config.getUserId(getActivity()) != 0 && Config.getAuthToken(getActivity()) != null) {
            final Activity that = getActivity();

            // Show progress indicator
            View progress = getActivity().findViewById(R.id.bkg_progress);
            progress.setVisibility(View.VISIBLE);

            final OAuthenticateUserRequest request = new OAuthenticateUserRequest();
            request.UserId = Config.getUserId(getActivity());
            request.AuthToken = Config.getAuthToken(getActivity());

            Config.getService().oAuthenticate(request, new Callback<OAuthenticateUserResponse>() {
                @Override
                public void success(OAuthenticateUserResponse response, Response lel) {
                    if (lel.getStatus() != 200) {
                        Toast.makeText(that, that.getString(R.string.generic_error), Toast.LENGTH_LONG).show();
                    } else {
                        if (response.Error != null && response.Error.length() > 0)
                            Toast.makeText(that, response.Error, Toast.LENGTH_LONG).show();
                        else {
                            // Set the data for the currently logged in user.
                            Config.getUserProfile().UserId = response.UserId;
                            Config.getUserProfile().Name = response.Name;
                            Config.getUserProfile().PhotoUrl = response.PhotoUrl;
                            Config.getUserProfile().IsEmailVerified = response.IsEmailVerified;

                            Log.d(Config.TAG, "Finished oAuth");
                            startReloadMyItems();
                        }
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(that, "ERROR: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        else
        {
            showEmptySet();
        }
    }

    private void showEmptySet(){
        // Empty set
        View progress = getActivity().findViewById(R.id.bkg_progress);
        progress.setVisibility(View.GONE);

        Toast.makeText(getActivity(), R.string.no_items_to_show, Toast.LENGTH_LONG).show();
        Log.d(Config.TAG, "Empty set");
    }

    private void startReloadMyItems(){
        Log.d(Config.TAG, "Requesting my items from cloud");
        GetMyItemsRequest getMyItemsRequest = new GetMyItemsRequest();
        getMyItemsRequest.UserId = Config.getUserId(getActivity());
        getMyItemsRequest.AuthToken = Config.getAuthToken(getActivity());

        final Activity that = getActivity();
        final MyItemsFragment fragment = this;
        Config.getService().getMyItems(getMyItemsRequest, new Callback<GetMyItemsResponse>() {
            @Override
            public void success(GetMyItemsResponse response, Response lel) {
                View progress = that.findViewById(R.id.bkg_progress);
                progress.setVisibility(View.INVISIBLE);

                if (lel.getStatus() != 200) {
                    Toast.makeText(that, that.getString(R.string.generic_error), Toast.LENGTH_LONG).show();
                    Log.e(Config.TAG, "HTTP error: " + lel.getStatus());
                } else {
                    if (response.Error != null && response.Error.length() > 0) {
                        Toast.makeText(that, response.Error, Toast.LENGTH_LONG).show();
                        Log.e(Config.TAG, response.Error);
                    }
                    else {
                        if (response.Items == null || response.Items.length == 0) {
                            showEmptySet();
                        } else {
                            Log.d(Config.TAG, "Loading items into memory");
                            if (mGridView.getAdapter() == null) {
                                mGridView.setAdapter(new GridViewAdapter(response.Items, fragment));
                            }
                            else {
                                // onResume
                                GridViewAdapter adapter = (GridViewAdapter) mGridView.getAdapter();
                                adapter.clear();
                                adapter.addItems(response.Items);
                            }
                        }
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(Config.TAG, "Retrofit error: " + error.getMessage());
                Toast.makeText(that, "ERROR: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getMenuId() {
        return R.menu.menu_my_items;
    }
}