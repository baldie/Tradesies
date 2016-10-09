package com.mobile.tradesies.item;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.tradesies.Config;
import com.mobile.tradesies.R;
import com.mobile.tradesies.datacontracts.ReportItemRequest;
import com.mobile.tradesies.datacontracts.ReportItemResponse;
import com.mobile.tradesies.datacontracts.ReportReason;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ReportDialog extends Activity{

    final public static String ITEM_ID_BUNDLE_KEY = "TRADE_ID_BUNDLE_KEY";
    private int mItemId = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_report_item);

        mItemId = getIntent().getIntExtra(ITEM_ID_BUNDLE_KEY, 0);

        final EditText txtComment = (EditText)findViewById(R.id.txt_comment);

        final Spinner spnReasons = (Spinner)findViewById(R.id.spn_report_reasons);
        spnReasons.setAdapter(new ListAdapter(getReportReasons(), this, android.R.layout.simple_spinner_dropdown_item));

        Button btnSubmit = (Button)findViewById(R.id.btn_submit_report);
        btnSubmit.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                ReportReason reason = (ReportReason) spnReasons.getSelectedItem();
                if (reason == null){
                    Toast.makeText(v.getContext(), R.string.please_select_reason, Toast.LENGTH_SHORT).show();
                } else {
                    reportItem(reason, txtComment.getText().toString());
                }
            }
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setDimAmount(0.5f);
    }

    private ReportReason[] getReportReasons(){
        ReportReason[] reasons = new ReportReason[5];

        reasons[0] = new ReportReason();
        reasons[0].ReasonID = 1;
        reasons[0].Reason = "Inaccurate posting";

        reasons[1] = new ReportReason();
        reasons[1].ReasonID = 2;
        reasons[1].Reason = "Inappropriate photo";

        reasons[2] = new ReportReason();
        reasons[2].ReasonID = 3;
        reasons[2].Reason = "Offensive Language";

        reasons[3] = new ReportReason();
        reasons[3].ReasonID = 4;
        reasons[3].Reason = "Spam";

        reasons[4] = new ReportReason();
        reasons[4].ReasonID = 5;
        reasons[4].Reason = "Scammer";

        return reasons;
    }

    private void reportItem(ReportReason reason, String comment) {
        Log.d(Config.TAG, "Reporting item");
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Reporting...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();

        ReportItemRequest request = new ReportItemRequest();
        request.AuthToken = Config.getAuthToken(this);
        request.UserId = Config.getUserId(this);
        request.ItemId = mItemId;
        request.ReasonId = reason.ReasonID;
        request.Comment = comment;

        // Report the item
        Config.getService().reportItem(request, new Callback<ReportItemResponse>() {
            @Override
            public void success(ReportItemResponse reportItemResponse, Response response) {
                Toast.makeText(progress.getContext(), R.string.reported, Toast.LENGTH_SHORT).show();
                finish();
                progress.dismiss();
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(progress.getContext(), R.string.generic_error, Toast.LENGTH_SHORT).show();
                progress.dismiss();
            }
        });

        finish();
    }

    static class ListAdapter extends ArrayAdapter {
        ReportReason [] mReasons;

        public ListAdapter(ReportReason[] reasons, Context context, int layoutResourceId){
            super(context, layoutResourceId);

            mReasons = reasons;
        }
        @Override
        public int getCount() {
            return mReasons.length;
        }

        @Override
        public Object getItem(int position) {
            return mReasons[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            TextView text = new TextView(parent.getContext());
            text.setText(mReasons[position].Reason);
            text.setTag(mReasons[position].ReasonID);
            return text;
        }
    }
}