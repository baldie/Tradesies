package com.mobile.tradesies.home;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.mobile.tradesies.Config;
import com.mobile.tradesies.R;
import com.mobile.tradesies.datacontracts.AuthenticateUserRequest;
import com.mobile.tradesies.datacontracts.AuthenticateUserResponse;
import com.mobile.tradesies.registration.ManualRegistration;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginDialog extends Activity{

    final public static String REQUEST_CODE = "REQ";
    final public static int REQUEST_CODE_FOR_LOGIN = 612;
    final public static int REQUEST_CODE_FOR_ACCOUNT_DELETION = 613;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_login);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setDimAmount(0.5f);

        final EditText txtEmail = (EditText) findViewById(R.id.txt_email);
        final EditText txtPassword = (EditText) findViewById(R.id.txt_password);
        final int auth_reason = getIntent().getIntExtra(REQUEST_CODE, 0);

        Button btnLogin = (Button)findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (auth_reason == REQUEST_CODE_FOR_LOGIN)
                    login(txtEmail.getText().toString(), txtPassword.getText().toString());
                else if (auth_reason == REQUEST_CODE_FOR_ACCOUNT_DELETION){
                    // pass the login info back for usage during account deletion
                    Intent data = new Intent();
                    data.putExtra("u", txtEmail.getText().toString());
                    data.putExtra("p", txtPassword.getText().toString());
                    setResult(RESULT_OK, data);
                    finish();
                }
            }
        });

        TextView lnkForgotPassword = (TextView) findViewById(R.id.lnk_forgot_my_password);
        lnkForgotPassword.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ManualRegistration.validEmail(txtEmail.getText().toString())){
                    Toast.makeText(v.getContext(), R.string.invalid_email, Toast.LENGTH_SHORT).show();
                }else{
                    resetPassword(txtEmail.getText().toString());
                }
            }
        });

        if (Config.TESTING){
            txtEmail.setText("david.1138@gmail.com");
            txtPassword.setText("Gratis123");
        }
    }

    private void login(String email, String password) {
        Log.d(Config.TAG, "Logging in...");

        final LoginDialog that = this;
        if (!ManualRegistration.validEmail(email)){
            Toast.makeText(that, R.string.invalid_email, Toast.LENGTH_SHORT).show();
            return;
        }

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Logging in...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();

        AuthenticateUserRequest request = new AuthenticateUserRequest();
        request.Email = email;
        request.Password = password;

        // Log in
        Config.getService().authenticate(request, new Callback<AuthenticateUserResponse>() {
            @Override
            public void success(AuthenticateUserResponse response, Response r) {
                progress.dismiss();

                if (response.Error != null && response.Error.length() > 0) {
                    Toast.makeText(that, response.Error, Toast.LENGTH_SHORT).show();
                } else {
                    Config.setAuthToken(response.AuthToken, that);
                    Config.setUserId(response.UserId, that);

                    Toast.makeText(that, R.string.welcome_back, Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                progress.dismiss();

                Toast.makeText(that, R.string.generic_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetPassword(String email)
    {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(Config.API_ENDPOINT + "/Web/ResetPassword?email=" + email));
        startActivity(i);
    }
}