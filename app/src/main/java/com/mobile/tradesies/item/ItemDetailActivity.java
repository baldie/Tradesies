package com.mobile.tradesies.item;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.mobile.tradesies.AnalyticsUtil;
import com.mobile.tradesies.Config;
import com.mobile.tradesies.ImageUtil;
import com.mobile.tradesies.R;
import com.mobile.tradesies.Session;
import com.mobile.tradesies.account.UserAccountActivity;
import com.mobile.tradesies.datacontracts.GetItemRequest;
import com.mobile.tradesies.datacontracts.GetItemResponse;
import com.mobile.tradesies.datacontracts.Item;
import com.mobile.tradesies.datacontracts.ItemPhoto;
import com.mobile.tradesies.datacontracts.Trade;
import com.mobile.tradesies.registration.RegistrationSelectionActivity;
import com.mobile.tradesies.trade.TradeActivity;
import com.mobile.tradesies.trade.TradeAdapter;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ItemDetailActivity extends AppCompatActivity implements BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener {

    private Item mItem;
    private boolean mUserEditedItem = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        mItem = getIntent().getParcelableExtra("item");

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        if (toolbar != null) {
            this.setSupportActionBar(toolbar);

            ActionBar actionBar = this.getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle(mItem.Title);
        }

        final Activity that = this;
        final int itemOwnerId = mItem.OwnerUserId;
        CircularImageView itemOwnerImage = (CircularImageView) findViewById(R.id.item_owner_photo);
        itemOwnerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent showProfile = new Intent(that, UserAccountActivity.class);
                showProfile.putExtra(UserAccountActivity.USER_ID, itemOwnerId);
                startActivity(showProfile);
            }
        });

        TextView lnkReport = (TextView)findViewById(R.id.lnk_report_item);
        lnkReport.setPaintFlags(lnkReport.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        lnkReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ReportDialog.class);
                intent.putExtra(ReportDialog.ITEM_ID_BUNDLE_KEY, mItem.Id);
                startActivity(intent);
            }
        });
    }

    private void updateControlsWithItem() {
        SliderLayout imagePager = (SliderLayout)findViewById(R.id.slider);

        imagePager.removeAllSliders();
        for(final ItemPhoto photo : mItem.Photos){
            DefaultSliderView imageSliderView = new DefaultSliderView(this);
            imageSliderView
                    .image(ImageUtil.mapServerPath(photo.Url))
                    .setScaleType(BaseSliderView.ScaleType.CenterInside)
                    .setOnSliderClickListener(this);

            imageSliderView.setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                @Override
                public void onSliderClick(BaseSliderView baseSliderView) {
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    String path = ImageUtil.mapServerPath(photo.Url);
                    intent.setDataAndType(Uri.parse(path),"image/*");
                    startActivity(intent);
                }
            });

            imagePager.addSlider(imageSliderView);
        }
        imagePager.setMinimumHeight(imagePager.getWidth());
        imagePager.setCustomIndicator((PagerIndicator) findViewById(R.id.custom_indicator));
        imagePager.stopAutoCycle();
        //imagePager.setPresetTransformer(SliderLayout.Transformer.Default);

        CircularImageView img = (CircularImageView)findViewById(R.id.item_owner_photo);
        Picasso.with(this)
                .load(ImageUtil.mapServerPath(mItem.OwnerPhotoUrl))
                .noPlaceholder()
                .resize(Config.THUMBNAIL_SIZE, Config.THUMBNAIL_SIZE)
                .into(img);

        TextView lblTitle = (TextView)findViewById(R.id.lbl_item_title);
        lblTitle.setText(mItem.Title);

        String distance = mItem.Distance == null ? "find on map" : mItem.Distance + " miles away";
        TextView lnkDistance = (TextView)findViewById(R.id.lnk_item_distance);
        lnkDistance.setText(distance);
        lnkDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("geo:0,0?q=" + mItem.Latitude + "," + mItem.Longitude + "(" + mItem.Title + ")");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                v.getContext().startActivity(intent);
            }
        });

        TextView lblDescription = (TextView)findViewById(R.id.lbl_item_description);
        lblDescription.setText(mItem.Description);
    }

    private boolean getThisIsMyItem(){
        return mItem.OwnerUserId == Config.getUserId(this);
    }

    private int getAssociatedTradeId(){
        int tradeId = 0;
        ArrayList<Trade> myTrades = Session.getInstance().getTrades();
        if (myTrades != null){
            for(Trade t : myTrades){
                // And this item is involved in the trade
                if (t.ItemOne.Id == mItem.Id || t.ItemTwo.Id == mItem.Id) {
                    // And the trade has not been declined already
                    if (!t.Declined)
                        tradeId = t.TradeId;
                    break;
                }
            }
        }
        return tradeId;
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(Config.TAG, "ItemDetailActivity - onResume called");

        if (mItem != null){
            // Get a fresh copy of the item
             if (mUserEditedItem)
                 refreshItem();

            updateControlsWithItem();
        }
        else
        {
            Log.e(Config.TAG, "mItem in memory is null - closing activity");
            finish();
        }

        final Button btnProposeTrade = (Button)findViewById(R.id.btn_propose_trade);
        if (getThisIsMyItem()){
            btnProposeTrade.setVisibility(View.GONE);

            TextView lnkReport = (TextView)findViewById(R.id.lnk_report_item);
            lnkReport.setVisibility(View.GONE);

            // Show the indications that others wanted to trade on this item.
            showProposedTrades();
        }else {

            // Show the proposed trade that the current user has with this item
            Log.d(Config.TAG, "Showing current user's proposed trades on this item");
            final Activity that = this;
            final SliderLayout imagePager = (SliderLayout)findViewById(R.id.slider);
            boolean currentlyTradingOnThisItem = getAssociatedTradeId() != 0;
            if (currentlyTradingOnThisItem) {
                btnProposeTrade.setBackgroundColor(getResources().getColor(R.color.tradesiesColorSecondary));
                btnProposeTrade.setText(R.string.view_proposed_trade);

                btnProposeTrade.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TradeActivity.launch(that, imagePager, getAssociatedTradeId());
                    }
                });
            }else{
                btnProposeTrade.setBackgroundColor(getResources().getColor(R.color.tradesiesColorPrimary));
                btnProposeTrade.setText(R.string.propose_trade);

                btnProposeTrade.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Config.getUserId(that) == 0){
                            // User needs to register
                            Toast.makeText(that, R.string.please_register_first, Toast.LENGTH_LONG).show();
                            RegistrationSelectionActivity.launch(that, v);
                        }else {
                            TradeActivity.launch(that, imagePager, mItem);
                        }
                    }
                });
            }
        }
    }

    private void refreshItem(){
        Log.d(Config.TAG, "Refreshing item...");

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Refreshing...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();

        GetItemRequest request = new GetItemRequest();
        request.UserId = Config.getUserId(this);
        request.ItemId = this.mItem.Id;

        final Activity that = this;
        Config.getService().getItem(request, new Callback<GetItemResponse>() {
            @Override
            public void success(GetItemResponse response, Response http) {
                progress.dismiss();

                if (http.getStatus() == 200) {

                    boolean errorOccurred = response.Error != null && response.Error.length() > 0;
                    if (!errorOccurred) {
                        mItem = response.Item;
                        updateControlsWithItem();

                        Log.d(Config.TAG, "Deleted image successfully from server.");
                    } else {
                        Log.e(Config.TAG, "Error: " + response.Error);
                        Toast.makeText(that, response.Error, Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Log.e(Config.TAG, "Error: HTTP " + http.getStatus() + ", " + http.getReason());
                    Toast.makeText(that, http.getReason(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                progress.dismiss();
                Toast.makeText(that, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onSliderClick(BaseSliderView slider) {
        //Toast.makeText(this, slider.getBundle().get("extra") + "", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageSelected(int position) { }

    @Override
    public void onPageScrollStateChanged(int state) {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getThisIsMyItem())
            getMenuInflater().inflate(R.menu.menu_my_item, menu);
        else
            getMenuInflater().inflate(R.menu.menu_item_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_edit_item) {

            // Determine if this item is being traded on
            boolean thisItemIsBeingTraded = false;
            boolean thisItemHasBeenTraded = false;
            ArrayList<Trade> trades = Session.getInstance().getTrades();
            if (trades != null){
                for(Trade t : trades){
                    if (t.ItemOne.Id == mItem.Id || t.ItemTwo.Id == mItem.Id) {
                        if (t.Accepted)
                            thisItemHasBeenTraded = true;
                        else
                            thisItemIsBeingTraded = true;
                        break;
                    }
                }
            }

            // Provide a warning that they are editing an item with a trade on it.
            if (thisItemIsBeingTraded) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.modify_this_item))
                        .setMessage(getString(R.string.edit_item_that_is_under_trade))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                editItem();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            } else if (thisItemHasBeenTraded) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.modify_this_item))
                        .setMessage(getString(R.string.edit_item_that_has_been_traded))
                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
            else {
                // no trades on the item, proceed
                editItem();
            }
        } else if (id == R.id.action_categories) {
        } else {
            // the only thing left is back
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void editItem(){
        mUserEditedItem = true;
        Intent editIntent = new Intent(this, EditItemActivity.class);
        editIntent.putExtra(EditItemActivity.ITEM_BUNDLE_KEY, mItem);
        this.startActivity(editIntent);
    }

    public static void launch(Activity activity, View transitionView, Item item)
    {
        Log.d(Config.TAG, "Launching item detail activity");
        AnalyticsUtil.trackItemDetailsLaunched(activity);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionView, activity.getString(R.string.transition));
        Intent launchIntent = new Intent(activity, ItemDetailActivity.class);
        launchIntent.putExtra("item", item);
        ActivityCompat.startActivity(activity, launchIntent, options.toBundle());
    }

    private void showProposedTrades(){
        ArrayList<Trade> myTrades = Session.getInstance().getTrades();
        if (myTrades == null)
            return;

        // Filter out all trades which have been accepted or declined.
        ArrayList<Trade> newTrades = new ArrayList<>();
        for(Trade t : myTrades){
            if (t.ItemOne.Id == mItem.Id || t.ItemTwo.Id == mItem.Id)
                    newTrades.add(t);
        }

        Log.d(Config.TAG, "Showing (" + newTrades.size() + ") trade(s) on this item");
        ListView listView = (ListView)findViewById(R.id.lv_item_trades);
        listView.setAdapter(new TradeAdapter(this, mItem, newTrades));
    }
}