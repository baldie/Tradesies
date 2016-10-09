package com.mobile.tradesies.registration;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.mobile.tradesies.AnalyticsUtil;
import com.mobile.tradesies.R;
import com.mobile.tradesies.home.LoginDialog;

public class RegistrationSelectionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_start);

        final Activity that = this;
        Button btnManualRegistration = (Button) findViewById(R.id.btn_register_manual);
        btnManualRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ManualRegistration.launch(that, v);
            }
        });

        TextView lnkLogin = (TextView) findViewById(R.id.lnk_log_in_from_registration);
        lnkLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Have the user log in manually.
                Intent loginIntent = new Intent(that, LoginDialog.class);
                loginIntent.putExtra(LoginDialog.REQUEST_CODE, LoginDialog.REQUEST_CODE_FOR_LOGIN);
                startActivityForResult(loginIntent, LoginDialog.REQUEST_CODE_FOR_LOGIN);
            }
        });

        // Facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        CallbackManager callbackManager = CallbackManager.Factory.create();
        LoginButton btnFacebookRegistration = (LoginButton) findViewById(R.id.btn_register_facebook);
        btnFacebookRegistration.setReadPermissions("email");

        // Facebook callback registration
        btnFacebookRegistration.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });
    }

    public static void launch(Activity activity, View transitionView)
    {
        AnalyticsUtil.trackRegistrationStartPageLaunched(activity);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionView, activity.getString(R.string.transition));
        ActivityCompat.startActivity(activity, new Intent(activity, RegistrationSelectionActivity.class), options.toBundle());
    }
}