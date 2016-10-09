package com.mobile.tradesies.trade;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.primitives.Ints;
import com.mobile.tradesies.AnalyticsUtil;
import com.mobile.tradesies.Config;
import com.mobile.tradesies.ImageUtil;
import com.mobile.tradesies.R;
import com.mobile.tradesies.Session;
import com.mobile.tradesies.datacontracts.AcceptTradeRequest;
import com.mobile.tradesies.datacontracts.AcceptTradeResponse;
import com.mobile.tradesies.datacontracts.AcknowledgeNotificationsRequest;
import com.mobile.tradesies.datacontracts.AcknowledgeNotificationsResponse;
import com.mobile.tradesies.datacontracts.DeclineTradeRequest;
import com.mobile.tradesies.datacontracts.DeclineTradeResponse;
import com.mobile.tradesies.datacontracts.Item;
import com.mobile.tradesies.datacontracts.Notification;
import com.mobile.tradesies.datacontracts.ProposeTradeRequest;
import com.mobile.tradesies.datacontracts.ProposeTradeResponse;
import com.mobile.tradesies.datacontracts.Trade;
import com.mobile.tradesies.item.ItemDetailActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class TradeActivity extends ChatActivity {

    public static final String ITEM_ONE = "item1";
    public static final String TRADE_ID = "tradeId";

    private Item mItemOne;
    private Item mItemTwo;

    private Button btnChangeItem;
    private Button btnStartTrade;
    private Button btnAcceptTrade;
    private Button btnDeclineTrade;
    private Button btnRateUser;
    private View mPnlTradeChat;
    private EditText mTxtIntroChat;

    private enum TradeModes
    {
        PreProposal,
        IProposedThisTrade,
        OtherUserProposedThisTrade,
        CompletedTrade
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_trade);
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        if (toolbar != null) {
            this.setSupportActionBar(toolbar);

            ActionBar actionBar = this.getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        btnChangeItem = (Button) findViewById(R.id.btn_change_my_item);
        btnChangeItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMyItem();
            }
        });

        btnStartTrade = (Button) findViewById(R.id.btn_start_trade);
        btnStartTrade.setEnabled(false); // User needs to choose their item first.
        btnStartTrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proposeTrade();
            }
        });

        btnAcceptTrade = (Button) findViewById(R.id.btn_accept_proposed_trade);
        btnAcceptTrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptTrade();
            }
        });
        btnDeclineTrade = (Button) findViewById(R.id.btn_decline_trade);
        btnDeclineTrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                declineTrade();
            }
        });
        btnRateUser = (Button) findViewById(R.id.btn_rate_user);
        btnRateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateUser();
            }
        });

        // Hide the keyboard that immediately appears.
        mTxtIntroChat = (EditText)findViewById(R.id.txt_first_message);
        mTxtIntroChat.clearFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mTxtIntroChat.getWindowToken(), 0);

        mPnlTradeChat = findViewById(R.id.pnl_trade_chat);
    }

    @Override
    protected void onResume(){
        super.onResume();

        updateControlsForTradeStatus();

        acknowledgeMyNotifications();
    }

    private void updateControlsForTradeStatus(){
        Trade trade = getTrade();
        if (trade != null){

            // Change mode
            if (trade.Accepted || trade.Declined)
                changeTradeMode(TradeModes.CompletedTrade);
            else {
                if (trade.TradeProposerId == Config.getUserId(this))
                    changeTradeMode(TradeModes.IProposedThisTrade);
                else
                    changeTradeMode(TradeModes.OtherUserProposedThisTrade);
            }

            // Load item one
            mItemOne = trade.ItemTwo;
            ImageView imgItemOne = (ImageView) findViewById(R.id.img_item_one);
            imgItemOne.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showItem(v, mItemOne);
                }
            });
            Picasso
                    .with(this)
                    .load(ImageUtil.mapServerPath(mItemOne.getPrimaryPhoto().Url))
                    .noPlaceholder()
                    .into(imgItemOne);
            TextView lblItemOne = (TextView) findViewById(R.id.lbl_item_one_title);
            lblItemOne.setText(mItemOne.Title);

            // Load item two
            mItemTwo = trade.ItemOne;
            ImageView imgItemTwo = (ImageView) findViewById(R.id.img_item_two);
            imgItemTwo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showItem(v, mItemTwo);
                }
            });
            Picasso
                    .with(this)
                    .load(ImageUtil.mapServerPath(mItemTwo.getPrimaryPhoto().Url))
                    .noPlaceholder()
                    .into(imgItemTwo);
            TextView lblItemTwo = (TextView) findViewById(R.id.lbl_item_two_title);
            lblItemTwo.setText(mItemTwo.Title);

            // Allow/Disallow rating of the trade
            if (trade.Accepted) {
                if (!trade.CanRate) {
                    btnRateUser.setBackgroundColor(getResources().getColor(R.color.tradesiesColorSecondary));
                    btnRateUser.setEnabled(false);
                }else {
                    btnRateUser.setBackgroundColor(getResources().getColor(R.color.tradesiesColorPrimary));
                    btnRateUser.setEnabled(true);
                }
            }

        } else {

            // Brand new trade being proposed
            mItemOne = getIntent().getParcelableExtra(ITEM_ONE);
            ImageView imgItemOne = (ImageView) findViewById(R.id.img_item_one);
            Picasso
                    .with(this)
                    .load(ImageUtil.mapServerPath(mItemOne.getPrimaryPhoto().Url))
                    .noPlaceholder()
                    .into(imgItemOne);
            TextView lblItemOne = (TextView) findViewById(R.id.lbl_item_one_title);
            lblItemOne.setText(mItemOne.Title);

            changeTradeMode(TradeModes.PreProposal);
        }
    }

    private Trade getTrade(){
        int tradeId = getIntent().getIntExtra(TRADE_ID, 0);

        ArrayList<Trade> trades = Session.getInstance().getTrades();
        if (trades == null)
            return null;

        Trade trade = null;
        for(Trade t : trades)
        {
            if (t.TradeId == tradeId){
                trade = t;
                break;
            }
        }
        return trade;
    }

    private void changeTradeMode(TradeModes newMode){
        switch(newMode){
            case PreProposal:
                this.getSupportActionBar().setTitle(R.string.trade_these_items);
                btnChangeItem.setVisibility(View.VISIBLE);
                mTxtIntroChat.setVisibility(View.VISIBLE);
                btnStartTrade.setVisibility(View.VISIBLE);
                btnAcceptTrade.setVisibility(View.GONE);
                btnDeclineTrade.setVisibility(View.GONE);
                btnRateUser.setVisibility(View.GONE);
                mPnlTradeChat.setVisibility(View.GONE);
                break;

            case IProposedThisTrade:
                this.getSupportActionBar().setTitle(R.string.proposed_trade);
                btnChangeItem.setVisibility(View.GONE);
                mTxtIntroChat.setVisibility(View.GONE);
                btnStartTrade.setVisibility(View.GONE);
                btnAcceptTrade.setVisibility(View.GONE);
                btnDeclineTrade.setVisibility(View.GONE);
                btnRateUser.setVisibility(View.GONE);
                mPnlTradeChat.setVisibility(View.VISIBLE);
                break;

            case OtherUserProposedThisTrade:
                this.getSupportActionBar().setTitle(R.string.trade_these_items);
                btnChangeItem.setVisibility(View.GONE);
                mTxtIntroChat.setVisibility(View.GONE);
                btnStartTrade.setVisibility(View.GONE);
                btnAcceptTrade.setVisibility(View.VISIBLE);
                btnDeclineTrade.setVisibility(View.VISIBLE);
                btnRateUser.setVisibility(View.GONE);
                mPnlTradeChat.setVisibility(View.VISIBLE);
                break;

            case CompletedTrade:
                this.getSupportActionBar().setTitle(R.string.trade_accepted);
                btnChangeItem.setVisibility(View.GONE);
                mTxtIntroChat.setVisibility(View.GONE);
                btnStartTrade.setVisibility(View.GONE);
                btnAcceptTrade.setVisibility(View.GONE);
                btnDeclineTrade.setVisibility(View.GONE);
                btnRateUser.setVisibility(View.VISIBLE);
                mPnlTradeChat.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // the only menu item on this screen is the back button
        finish();
        return super.onOptionsItemSelected(item);
    }

    public static void launch(Activity activity, View transitionView, Item item)
    {
        AnalyticsUtil.trackTradeProposalStarted(activity);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionView, activity.getString(R.string.transition));
        Intent launchIntent = new Intent(activity, TradeActivity.class);
        launchIntent.putExtra(TradeActivity.ITEM_ONE, item);
        ActivityCompat.startActivity(activity, launchIntent, options.toBundle());
    }

    public static void launch(Activity activity, View transitionView, int tradeId)
    {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionView, activity.getString(R.string.transition));
        Intent launchIntent = new Intent(activity, TradeActivity.class);
        launchIntent.putExtra(TradeActivity.TRADE_ID, tradeId);
        ActivityCompat.startActivity(activity, launchIntent, options.toBundle());
    }

    private void showItem(View v, Item i){
        if (i == null)
            return;

        ItemDetailActivity.launch(this, v, i);
    }

    private void changeMyItem(){
        Intent chooseFromMyItemsIntent = new Intent(this, MyItemsPicker.class);
        startActivityForResult(chooseFromMyItemsIntent, MyItemsPicker.REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode != RESULT_OK)
            return;

        if (requestCode == MyItemsPicker.REQUEST_CODE) {
            mItemTwo = data.getParcelableExtra(MyItemsPicker.SELECTED_ITEM);
            ImageView imgItemTwo = (ImageView) findViewById(R.id.img_item_two);
            Picasso
                .with(this)
                .load(ImageUtil.mapServerPath(mItemTwo.getPrimaryPhoto().Url))
                .noPlaceholder()
                .into(imgItemTwo);
            TextView lblItemTwo = (TextView) findViewById(R.id.lbl_item_two_title);
            lblItemTwo.setText(mItemTwo.Title);

            btnStartTrade.setEnabled(true);
        }
    }

    private void proposeTrade(){
        // Proposing a trade
        Log.d(Config.TAG, "Proposing a trade...");

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Proposing trade...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();

        ProposeTradeRequest request = new ProposeTradeRequest();
        request.AuthToken = Config.getAuthToken(this);
        request.ProposerUserId = Config.getUserId(this);
        request.ProposerItemId = mItemTwo.Id;
        request.OtherItemId = mItemOne.Id;
        request.InitialMessage = mTxtIntroChat.getText().toString();

        final Activity that = this;
        Config.getService().proposeTrade(request, new Callback<ProposeTradeResponse>() {
            @Override
            public void success(ProposeTradeResponse response, Response http) {
                progress.dismiss();

                boolean errorOccurred = response.Error != null && response.Error.length() > 0;
                if (!errorOccurred) {

                    // Reload my trades
                    Session.getInstance().refreshMyTrades(that);

                    // Show that we're done.
                    AlertDialog myDialogBox = new AlertDialog.Builder(that)
                            .setTitle(that.getString(R.string.app_name))
                            .setMessage(that.getString(R.string.user_will_be_contacted))
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setPositiveButton("Finished", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    finish();
                                }
                            }).create();
                    myDialogBox.show();

                    updateControlsForTradeStatus();

                    AnalyticsUtil.trackTradeProposalCompleted(that);
                    Log.d(Config.TAG, "Successfully proposed a new trade!");
                } else {
                    Toast.makeText(that, response.Error, Toast.LENGTH_SHORT).show();
                    Log.e(Config.TAG, response.Error);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(that, error.getMessage(), Toast.LENGTH_SHORT).show();
                progress.dismiss();
            }
        });
    }

    //If there are any notifications associated with this trade, acknowledge them
    private void acknowledgeMyNotifications() {
        final ArrayList<Notification> notifications = Session.getInstance().getNotifications();
        if (notifications == null)
            return;

        final ArrayList<Integer> notificationsToAcknowledge = new ArrayList<>();
        for (Notification n : notifications) {
            if (n.Type == Notification.TradeAccepted || n.Type == Notification.NewTradeProposed || n.Type == Notification.NewTradeChat) {
                int notificationTradeId = Integer.parseInt(n.Extra);
                if (getTrade() != null && getTrade().TradeId == notificationTradeId)
                    notificationsToAcknowledge.add(n.Id);
            }
        }

        Session.acknowledgeNotifications(this, notificationsToAcknowledge);
    }

    private void acceptTrade(){
        Log.d(Config.TAG, "Accepting a trade...");
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Accepting trade...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();

        // This is what this is all about!
        AcceptTradeRequest request = new AcceptTradeRequest();
        request.AuthToken = Config.getAuthToken(this);
        request.UserId = Config.getUserId(this);
        request.TradeId = getTrade().TradeId;

        final Activity that = this;
        Config.getService().acceptTrade(request, new Callback<AcceptTradeResponse>() {
            @Override
            public void success(AcceptTradeResponse response, Response http) {
                progress.dismiss();

                boolean errorOccurred = response.Error != null && response.Error.length() > 0;
                if (!errorOccurred) {

                    // Reload my trades
                    Session.getInstance().refreshMyTrades(that);

                    // Show that we're done.
                    final AlertDialog myDialogBox = new AlertDialog.Builder(that)
                            .setTitle(that.getString(R.string.awesome))
                            .setMessage(that.getString(R.string.trade_completed_message))
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                }
                            }).create();
                    myDialogBox.show();

                    updateControlsForTradeStatus();

                    AnalyticsUtil.trackTradeAccepted(that);
                    Log.d(Config.TAG, "Successfully accepted trade!");
                } else {
                    Toast.makeText(that, response.Error, Toast.LENGTH_SHORT).show();
                    Log.e(Config.TAG, response.Error);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(that, error.getMessage(), Toast.LENGTH_SHORT).show();
                progress.dismiss();
            }
        });
    }

    private void declineTrade() {
        new AlertDialog.Builder(this)
            .setTitle("Decline this trade?")
            .setMessage("Are you sure you want to decline this trade?")
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    actuallyDeclineTrade();
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

    private void actuallyDeclineTrade(){
        Log.d(Config.TAG, "declining a trade...");

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Declining...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();

        DeclineTradeRequest request = new DeclineTradeRequest();
        request.AuthToken = Config.getAuthToken(this);
        request.UserId = Config.getUserId(this);
        request.TradeId = getTrade().TradeId;

        final Activity that = this;
        Config.getService().declineTrade(request, new Callback<DeclineTradeResponse>() {
            @Override
            public void success(DeclineTradeResponse response, Response http) {
                progress.dismiss();

                boolean errorOccurred = response.Error != null && response.Error.length() > 0;
                if (!errorOccurred) {

                    // reload my trades
                    Session.getInstance().refreshMyTrades(that);

                    // Show that we're done.
                    AlertDialog myDialogBox = new AlertDialog.Builder(that)
                            .setTitle(that.getString(R.string.app_name))
                            .setMessage(that.getString(R.string.trade_declined_message))
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                    Log.d(Config.TAG, "Successfully declined trade.");
                                    finish();
                                }
                            }).create();
                    myDialogBox.show();

                    AnalyticsUtil.trackTradeDeclined(that);
                    Log.d(Config.TAG, "Trade declined.");
                } else {
                    Toast.makeText(that, response.Error, Toast.LENGTH_SHORT).show();
                    Log.e(Config.TAG, response.Error);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(that, error.getMessage(), Toast.LENGTH_SHORT).show();
                progress.dismiss();
            }
        });
    }

    private void rateUser(){
        Intent intent = new Intent(this, RateDialog.class);
        Trade trade = getTrade();
        if (trade == null)
            return;
        intent.putExtra(RateDialog.TRADE_ID_BUNDLE_KEY, trade.TradeId);
        startActivity(intent);
    }
}