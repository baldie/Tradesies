package com.mobile.tradesies.trade;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import com.mobile.tradesies.Config;
import com.mobile.tradesies.R;
import com.mobile.tradesies.datacontracts.RateTradeRequest;
import com.mobile.tradesies.datacontracts.RateTradeResponse;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RateDialog extends Activity{

    final public static String TRADE_ID_BUNDLE_KEY = "TRADE_ID_BUNDLE_KEY";
    private int mTradeId = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_rate_trade);

        mTradeId = getIntent().getIntExtra(TRADE_ID_BUNDLE_KEY, 0);

        final EditText txtComment = (EditText)findViewById(R.id.txt_comment);

        final RatingBar starSelection = (RatingBar) findViewById(R.id.stars);

        Button btnSubmit = (Button)findViewById(R.id.btn_submit_rating);
        btnSubmit.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                rateTrade(Math.round(starSelection.getRating()), txtComment.getText().toString());
            }
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setDimAmount(0.5f);
    }

    private void rateTrade(int stars, String comment) {
        Log.d(Config.TAG, "Rating trade");

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Rating trade...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();

        RateTradeRequest request = new RateTradeRequest();
        request.AuthToken = Config.getAuthToken(this);
        request.UserId = Config.getUserId(this);
        request.TradeId = mTradeId;
        request.StarCount = stars;
        request.Comment = comment;

        // Rating the trade
        final Activity that = this;
        Config.getService().rateTrade(request, new Callback<RateTradeResponse>() {
            @Override
            public void success(RateTradeResponse response, Response http) {
                progress.dismiss();

                if (http.getStatus() == 200) {
                    boolean errorOccurred = response.Error != null && response.Error.length() > 0;
                    if (!errorOccurred) {
                        Log.d(Config.TAG, "Rated trade successfully.");

                        Toast.makeText(that, R.string.thanks, Toast.LENGTH_SHORT).show();

                        finish();
                    } else {
                        Toast.makeText(that, response.Error, Toast.LENGTH_SHORT).show();
                        Log.e(Config.TAG, "Error: " + response.Error);
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
}