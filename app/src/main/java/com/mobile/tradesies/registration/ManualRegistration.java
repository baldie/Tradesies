package com.mobile.tradesies.registration;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.mobile.tradesies.AnalyticsUtil;
import com.mobile.tradesies.Config;
import com.mobile.tradesies.ImageUtil;
import com.mobile.tradesies.R;
import com.mobile.tradesies.TradesiesException;
import com.mobile.tradesies.datacontracts.AuthenticateUserRequest;
import com.mobile.tradesies.datacontracts.AuthenticateUserResponse;
import com.mobile.tradesies.datacontracts.RegisterUserRequest;
import com.mobile.tradesies.datacontracts.RegisterUserResponse;

import java.io.IOException;
import java.util.regex.Pattern;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ManualRegistration extends AppCompatActivity {

    private ImageView mProfileImageView = null;
    private EditText mTxtName = null;
    private EditText mTxtEmail = null;
    private EditText mTxtPassword1 = null;
    private EditText mTxtPassword2 = null;

    public static final int SELECT_PHOTO = 100;
    public static final int MIN_PASSWORD_LENGTH = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_registration);

        mTxtName = (EditText) findViewById(R.id.txtName);
        mTxtEmail = (EditText) findViewById(R.id.txtEmail);
        mTxtPassword1 = (EditText) findViewById(R.id.txtPassword1);
        mTxtPassword2 = (EditText) findViewById(R.id.txtPassword2);
        mProfileImageView = (ImageView) findViewById(R.id.img_profile);

        if (Config.TESTING){
            mTxtName.setText("David Baldie");
            mTxtEmail.setText("david.1138@gmail.com");
            mTxtPassword1.setText("Gratis123");
            mTxtPassword2.setText("Gratis123");
        }

        Button btnPhoto = (Button) findViewById(R.id.btn_change_profile_image);
        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });

        Button btnRegister = (Button) findViewById(R.id.btn_complete_manual_registration);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateData())
                    register();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        try {
            switch (requestCode) {
                case SELECT_PHOTO:
                    if (resultCode == RESULT_OK) {
                        Bitmap selectedImage = ImageUtil.reduceSize(getContentResolver(), 64, imageReturnedIntent.getData());
                        mProfileImageView.setImageBitmap(selectedImage);
                    }
            }
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }

    private boolean validateData(){
        boolean valid = false;
        try {

            if (mTxtName.getText().length() < 1)
                throw new TradesiesException("Please enter a name");

            if (!validEmail(mTxtEmail.getText().toString()))
                throw new TradesiesException("Please enter a valid email address");

            boolean passwordsMatch = mTxtPassword1.getText().toString().equals(mTxtPassword2.getText().toString());
            if (mTxtPassword1.getText().length() < MIN_PASSWORD_LENGTH)
                throw new TradesiesException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
            if (!passwordsMatch)
                throw new TradesiesException("Passwords do not match");

            valid = true;
        }
        catch(TradesiesException tex){
            Toast.makeText(this, tex.getMessage(), Toast.LENGTH_LONG).show();
        }
        return valid;
    }

    private void register()
    {
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Registering...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();

        try {
            final RegisterUserRequest registerRequest = new RegisterUserRequest();
            registerRequest.Photo = ImageUtil.toBase64(mProfileImageView.getDrawable());
            registerRequest.Name = mTxtName.getText().toString();
            registerRequest.Email = mTxtEmail.getText().toString();
            registerRequest.Password = mTxtPassword1.getText().toString();
            registerRequest.RegistrationEventType = Config.MANUAL_REGISTRATION;

            final Activity that = this;
            Config.getService().register(registerRequest, new Callback<RegisterUserResponse>() {
                @Override
                public void success(RegisterUserResponse response, Response lel) {
                    if (lel.getStatus() != 200){
                        progress.dismiss();
                        Toast.makeText(getApplicationContext(), that.getString(R.string.generic_error), Toast.LENGTH_LONG).show();
                    } else {
                        if (response.Error != null && response.Error.length() > 0) {
                            progress.dismiss();
                            Toast.makeText(getApplicationContext(), response.Error, Toast.LENGTH_LONG).show();
                        }
                        else {
                            AuthenticateUserRequest authRequest = new AuthenticateUserRequest();
                            authRequest.Email = registerRequest.Email;
                            authRequest.Password = registerRequest.Password;
                            autoLogin(authRequest, progress);
                        }
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    progress.dismiss();
                    Toast.makeText(getApplicationContext(), "ERROR: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private void autoLogin(AuthenticateUserRequest request, final ProgressDialog progress){
        final Activity that = this;
        Config.getService().authenticate(request, new Callback<AuthenticateUserResponse>() {
            @Override
            public void success(AuthenticateUserResponse response, Response lel) {
                progress.dismiss();
                if (lel.getStatus() != 200) {
                    Toast.makeText(getApplicationContext(), that.getString(R.string.generic_error), Toast.LENGTH_LONG).show();
                } else {
                    if (response.Error != null && response.Error.length() > 0)
                        Toast.makeText(getApplicationContext(), response.Error, Toast.LENGTH_LONG).show();
                    else {

                        // Save auth info
                        Config.setUserId(response.UserId, that);
                        Config.setAuthToken(response.AuthToken, that);
                        mUfaHasJustRegistered = true;
                        AnalyticsUtil.trackRegistrationCompleted(that);

                        // Show that we're done.
                        AlertDialog myDialogBox = new AlertDialog.Builder(that)
                                .setTitle(that.getString(R.string.verify_email_dialog_title))
                                .setMessage(that.getString(R.string.verify_email_dialog_body))
                                .setIcon(android.R.drawable.ic_dialog_email)
                                .setPositiveButton("Finished", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        finish();
                                    }
                                }).create();
                        myDialogBox.show();
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                progress.dismiss();
                Toast.makeText(getApplicationContext(), "ERROR: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    boolean mUfaHasJustRegistered = false;
    @Override
    public void onResume() {
        super.onResume();

        // If the user just completed registration and they now have an id, then we're all done here.
        if (mUfaHasJustRegistered && Config.getUserId(this) != 0)
            finish();
    }

    public static boolean validEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    public static void launch(Activity activity, View transitionView)
    {
        AnalyticsUtil.trackRegistrationDetailsPageLaunched(activity);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionView, activity.getString(R.string.transition));
        ActivityCompat.startActivity(activity, new Intent(activity, ManualRegistration.class), options.toBundle());
    }
}
