package com.mobile.tradesies.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.tradesies.AnalyticsUtil;
import com.mobile.tradesies.Config;
import com.mobile.tradesies.R;
import com.mobile.tradesies.datacontracts.DeleteUserRequest;
import com.mobile.tradesies.datacontracts.DeleteUserResponse;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class HelpMenuFragment extends Fragment implements TradesiesFragment {

    private View mRootView = null;

    public HelpMenuFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_help, container, false);

        Toolbar toolbar = (Toolbar)mRootView.findViewById(R.id.toolbar);
        if (toolbar != null) {
            ((ActionBarActivity) getActivity()).setSupportActionBar(toolbar);

            ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle(getActivity().getString(R.string.marketplace));
        }

        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        drawer.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

        refreshMenu();

        AnalyticsUtil.trackHelpVisit(getActivity());

        return mRootView;
    }

    private void refreshMenu(){
        final ListView lv_menu = (ListView)mRootView.findViewById(R.id.lv_menu);
        lv_menu.setAdapter(new MyHelpMenuItemAdapter(getActivity(), getHelpMenu()));
        lv_menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onListItemClick(lv_menu, view, position, id);
            }
        });
    }

    private HelpMenuItem [] getHelpMenu(){
        int menuItemCount = Config.getUserId(getActivity()) != 0 ? 6 : 5;
        HelpMenuItem[] menu = new HelpMenuItem[menuItemCount];

        menu[0] = new HelpMenuItem(1, "FAQ", Config.API_ENDPOINT + "/Web/FAQ");
        menu[1] = new HelpMenuItem(2, "About", Config.API_ENDPOINT + "/Web/About");
        menu[2] = new HelpMenuItem(3, "Contact Us", Config.API_ENDPOINT + "/Web/Contact");
        menu[3] = new HelpMenuItem(4, "Terms of Service", Config.API_ENDPOINT + "/Web/Terms");
        menu[4] = new HelpMenuItem(5, "Privacy Policy", Config.API_ENDPOINT + "/Web/Privacy");

        if (Config.getUserId(getActivity()) != 0)
            menu[5] = new HelpMenuItem(6, "Deactivate account", null);

        return menu;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void onListItemClick(ListView l, View v, int position, long id) {

        switch (position) {
            case 5: //deactivate
                final Activity that = getActivity();
                new AlertDialog.Builder(that)
                        .setTitle("Deactivate account?")
                        .setMessage("Are you sure you want to deactivate your account?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                authenticateUserBeforeAccountDeletion(that);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                break;

            default:
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(getHelpMenu()[position].Url));
                startActivity(i);
                break;
        }
    }

    private void authenticateUserBeforeAccountDeletion(Context that){
        Toast.makeText(that, that.getString(R.string.login_required), Toast.LENGTH_SHORT).show();
        Intent authIntent = new Intent(that, LoginDialog.class);
        authIntent.putExtra(LoginDialog.REQUEST_CODE, LoginDialog.REQUEST_CODE_FOR_ACCOUNT_DELETION);
        startActivityForResult(authIntent, LoginDialog.REQUEST_CODE_FOR_ACCOUNT_DELETION);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode != LoginDialog.REQUEST_CODE_FOR_ACCOUNT_DELETION)
            return;

        if (resultCode != Activity.RESULT_OK)
            return;

        deactivateAccount(data.getStringExtra("u"), data.getStringExtra("p"));
    }

    //todo: add survey
    private String mSurvey = "";

    private void deactivateAccount(String email, String password){
        final Activity that = getActivity();

        // Show progress indicator
        final ProgressDialog progress = new ProgressDialog(that);
        progress.setMessage("Deleting account...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();

        final DeleteUserRequest request = new DeleteUserRequest();
        request.UserId = Config.getUserId(getActivity());
        request.AuthToken = Config.getAuthToken(getActivity());
        request.Email = email;
        request.Password = password;
        request.SurveyData = mSurvey;

        Config.getService().deleteUser(request, new Callback<DeleteUserResponse>() {
            @Override
            public void success(DeleteUserResponse response, Response http) {
                progress.dismiss();

                if (http.getStatus() != 200) {
                    Toast.makeText(that, that.getString(R.string.generic_error), Toast.LENGTH_LONG).show();
                } else {
                    if (response.Error != null && response.Error.length() > 0)
                        Toast.makeText(that, response.Error, Toast.LENGTH_LONG).show();
                    else {
                        // Empty out cached data
                        Config.setAuthToken(null, that);
                        Config.setUserId(0, that);

                        refreshMenu();

                        Toast.makeText(that, "Your account has been deleted", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                progress.dismiss();

                Toast.makeText(that, "ERROR: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getMenuId() {
        return 0;
    }

    static class MyHelpMenuItemAdapter extends ArrayAdapter<HelpMenuItem> {
        private final Context context;
        private final HelpMenuItem[] values;

        public MyHelpMenuItemAdapter(Context context, HelpMenuItem[] values) {
            super(context, R.layout.help_row_layout, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.help_row_layout, parent, false);
            TextView textView = (TextView) rowView.findViewById(R.id.label);

            textView.setText(values[position].Title);

            return rowView;
        }
    }

    static class HelpMenuItem
    {
        public HelpMenuItem(int id, String text, String url)
        {
            ID = id;
            Title = text;
            Url = url;
        }
        int ID;
        String Title;
        String Url;
    }

}
