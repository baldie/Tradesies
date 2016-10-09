package com.mobile.tradesies.trade;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.mobile.tradesies.Config;
import com.mobile.tradesies.R;
import com.mobile.tradesies.datacontracts.AcknowledgeChatRequest;
import com.mobile.tradesies.datacontracts.AcknowledgeChatResponse;
import com.mobile.tradesies.datacontracts.ChatMessage;
import com.mobile.tradesies.datacontracts.ChatRequest;
import com.mobile.tradesies.datacontracts.ChatResponse;
import com.mobile.tradesies.datacontracts.GetTradeChatRequest;
import com.mobile.tradesies.datacontracts.GetTradeChatResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ChatActivity extends ActionBarActivity {

    private ListView mChatList;
    private ProgressBar mProgressBar;
    private EditText mTxtNewChat;
    private ImageButton mBtnSendChat;
    private Timer mPoller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mChatList = (ListView) findViewById(R.id.lv_chat);
        mProgressBar = (ProgressBar) findViewById(R.id.chat_progress_indicator);
        mTxtNewChat = (EditText) findViewById(R.id.txt_chat);
        mBtnSendChat = (ImageButton) findViewById(R.id.btn_submit_chat);
        mBtnSendChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTxtNewChat.getText().toString().trim().length() > 0) {
                    addChatEntry(mTxtNewChat.getText().toString());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        TimerTask pollForChats = new TimerTask() {
            @Override
            public void run() {
                loadChatFromCloud();
            }
        };

        mPoller = new Timer();
        mPoller.scheduleAtFixedRate(pollForChats, 0, Config.CHAT_POLL_INTERVAL);
    }

    @Override
    protected void onPause(){
        if (mPoller != null)
            mPoller.cancel();

        super.onPause();
    }

    @Override
    protected void onDestroy(){
        if (mPoller != null)
            mPoller.cancel();

        super.onDestroy();
    }

    private void loadChatFromCloud(){
        Log.d(Config.TAG, "Refreshing chat...");

        int tradeId = getIntent().getIntExtra(TradeActivity.TRADE_ID, 0);
        if (tradeId == 0)
            return;

        GetTradeChatRequest request = new GetTradeChatRequest();
        request.UserId = Config.getUserId(this);
        request.AuthToken = Config.getAuthToken(this);
        request.TradeId = tradeId;

        final Activity that = this;
        Config.getService().getTradeChat(request, new Callback<GetTradeChatResponse>() {
            @Override
            public void success(GetTradeChatResponse response, Response http) {
                if (http.getStatus() == 200) {
                    mProgressBar.setVisibility(View.GONE);
                    mChatList.setVisibility(View.VISIBLE);
                    mTxtNewChat.setVisibility(View.VISIBLE);
                    mBtnSendChat.setVisibility(View.VISIBLE);

                    boolean errorOccurred = response.Error != null && response.Error.length() > 0;
                    if (!errorOccurred) {
                        populateChat(response.Chat);
                        acknowledgeChat(response.Chat);
                        Log.d(Config.TAG, "Chat box updated.");
                    } else {
                        Log.e(Config.TAG, "Error: " + response.Error);
                        Toast.makeText(that, response.Error, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(Config.TAG, "Error in http response: " + http.getStatus() + " " + http.getReason());
                    Toast.makeText(that, R.string.generic_error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(that, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateChat(ChatMessage[] chat){
        ChatAdapter adapter = new ChatAdapter(ChatActivity.this, new ArrayList<>(Arrays.asList(chat)));
        mChatList.setAdapter(adapter);
        scrollDown();
    }

    private void acknowledgeChat(ChatMessage[] chat){
        // TODO: Bundle acknowledgements?
        for(ChatMessage message : chat) {
            if (!message.Seen && message.AuthorUserId != Config.getUserId(this)){
                acknowledgeSingleMessage(message.Id);
            }
        }
    }

    private void acknowledgeSingleMessage(int chatId){
        Log.d(Config.TAG, "Refreshing chat...");

        int tradeId = getIntent().getIntExtra(TradeActivity.TRADE_ID, 0);
        if (tradeId == 0)
            return;

        AcknowledgeChatRequest request = new AcknowledgeChatRequest();
        request.UserId = Config.getUserId(this);
        request.AuthToken = Config.getAuthToken(this);
        request.TradeId = tradeId;
        request.ChatId = chatId;

        Config.getService().acknowledgeChat(request, new Callback<AcknowledgeChatResponse>() {
            @Override
            public void success(AcknowledgeChatResponse response, Response http) {
                if (http.getStatus() == 200) {
                   Log.d(Config.TAG, "chat message acknowledged");
                } else {
                    Log.e(Config.TAG, "Error in http response: " + http.getStatus() + " " + http.getReason());
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(Config.TAG, error.getMessage());
            }
        });
    }

    private void scrollDown() {
        mChatList.setSelection(mChatList.getCount() - 1);
    }

    private void addChatEntry(String newChatText){
        Log.d(Config.TAG, "Refreshing chat...");

        if (newChatText == null)
            return;

        if (newChatText.trim().length() == 0)
            return;

        int tradeId = getIntent().getIntExtra(TradeActivity.TRADE_ID, 0);
        if (tradeId == 0)
            return;

        // Clear the text box
        mTxtNewChat.setText("");

        ChatRequest request = new ChatRequest();
        request.UserId = Config.getUserId(this);
        request.AuthToken = Config.getAuthToken(this);
        request.TradeId = tradeId;
        request.Message = newChatText.trim();

        final Activity that = this;
        Config.getService().chat(request, new Callback<ChatResponse>() {
            @Override
            public void success(ChatResponse response, Response http) {
                if (http.getStatus() == 200) {
                    boolean errorOccurred = response.Error != null && response.Error.length() > 0;
                    if (!errorOccurred) {
                        loadChatFromCloud();

                        Log.d(Config.TAG, "Added chat comment.");
                    } else {
                        Log.e(Config.TAG, "Error: " + response.Error);
                        Toast.makeText(that, response.Error, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(Config.TAG, "Error in http response: " + http.getStatus() + " " + http.getReason());
                    Toast.makeText(that, R.string.generic_error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(that, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}