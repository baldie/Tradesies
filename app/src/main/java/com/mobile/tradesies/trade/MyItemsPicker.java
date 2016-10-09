package com.mobile.tradesies.trade;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import com.mobile.tradesies.Config;
import com.mobile.tradesies.R;
import com.mobile.tradesies.datacontracts.GetMyItemsRequest;
import com.mobile.tradesies.datacontracts.GetMyItemsResponse;
import com.mobile.tradesies.datacontracts.Item;
import com.mobile.tradesies.home.GridViewAdapter;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MyItemsPicker extends Activity implements View.OnClickListener {

    public static final int REQUEST_CODE = 35;
    public static final String SELECTED_ITEM = "36";

    private RecyclerView mGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_items_picker);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setDimAmount(0.5f);

        mGridView = (RecyclerView)findViewById(R.id.gridView);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        mGridView.setLayoutManager(layoutManager);

        loadMyItems();
    }

    private void loadMyItems(){
        GetMyItemsRequest getMyItemsRequest = new GetMyItemsRequest();
        getMyItemsRequest.UserId = Config.getUserId(this);
        getMyItemsRequest.AuthToken = Config.getAuthToken(this);

        final MyItemsPicker that = this;
        Config.getService().getMyItems(getMyItemsRequest, new Callback<GetMyItemsResponse>() {
            @Override
            public void success(GetMyItemsResponse response, Response lel) {
                View progress = that.findViewById(R.id.bkg_progress);
                progress.setVisibility(View.INVISIBLE);

                if (lel.getStatus() != 200) {
                    Toast.makeText(that, that.getString(R.string.generic_error), Toast.LENGTH_LONG).show();
                } else {
                    if (response.Error != null && response.Error.length() > 0)
                        Toast.makeText(that, response.Error, Toast.LENGTH_LONG).show();
                    else {
                        // Only the items which are tradable should show in the list.
                        ArrayList<Item> tradableItems = new ArrayList<>();
                        for (Item i : response.Items) {
                            if (i.IsActive)
                                tradableItems.add(i);
                        }

                        if (tradableItems.size() == 0) {
                            showEmptySet();
                        } else {
                            mGridView.setVisibility(View.VISIBLE);
                            mGridView.setAdapter(new GridViewAdapter(tradableItems.toArray(new Item[tradableItems.size()]), that));
                        }
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(that, "ERROR: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void clearMyItems(){
        if (mGridView == null)
            return;

        GridViewAdapter currentAdapter = (GridViewAdapter)mGridView.getAdapter();
        if (currentAdapter == null)
            return;

        currentAdapter.clear();
    }

    private void showEmptySet(){
        // TODO: better empty set UX needed
        Toast.makeText(this, R.string.no_items_to_show, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onClick(View v) {
        if (mGridView == null || v == null)
            return;

        // Get the item
        int index = mGridView.getChildPosition(v);
        GridViewAdapter adapter = (GridViewAdapter)mGridView.getAdapter();
        Item item = adapter.getItem(index);

        // Wrap it up
        Bundle data = new Bundle();
        data.putParcelable(MyItemsPicker.SELECTED_ITEM, item);

        // Add it to the return value
        Intent intent = new Intent();
        intent.putExtras(data);

        // Return it
        setResult(RESULT_OK, intent);
        finish();
    }
}