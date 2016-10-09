package com.mobile.tradesies.account;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.mobile.tradesies.Config;
import com.mobile.tradesies.ImageUtil;
import com.mobile.tradesies.R;
import com.mobile.tradesies.datacontracts.ChangeUserPhotoRequest;
import com.mobile.tradesies.datacontracts.ChangeUserPhotoResponse;
import com.mobile.tradesies.datacontracts.GetMyProfileRequest;
import com.mobile.tradesies.datacontracts.GetMyProfileResponse;
import com.mobile.tradesies.datacontracts.GetUserProfileRequest;
import com.mobile.tradesies.datacontracts.GetUserProfileResponse;
import com.mobile.tradesies.datacontracts.Rating;
import com.mobile.tradesies.home.TradesiesFragment;
import com.mobile.tradesies.registration.ManualRegistration;
import com.squareup.picasso.Picasso;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by David on 5/25/2015.
 */
public class UserAccountFragment extends Fragment implements TradesiesFragment {

    private int mUserId = 0;
    public UserAccountFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mUserId = getActivity().getIntent().getIntExtra(UserAccountActivity.USER_ID, Config.getUserId(getActivity()));

        View rootView = inflater.inflate(R.layout.fragment_my_account, container, false);

        Toolbar toolbar = (Toolbar)rootView.findViewById(R.id.toolbar);
        if (toolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            if (getThisIsMyAccount())
                actionBar.setTitle(getActivity().getString(R.string.menu_my_account));
            else {
                actionBar.setTitle(getActivity().getString(R.string.user_profile));
            }
        }

        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        if (drawer != null)
            drawer.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

        Button btnChangeProfileImage = (Button) rootView.findViewById(R.id.btn_change_profile_image);
        btnChangeProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, ManualRegistration.SELECT_PHOTO);
            }
        });
        if (!getThisIsMyAccount()) {
            btnChangeProfileImage.setVisibility(View.GONE);
        }

        ToggleButton chkNotifications = (ToggleButton)rootView.findViewById(R.id.btn_notify_me_of_new_trades);
        if (!getThisIsMyAccount()) {
            chkNotifications.setVisibility(View.GONE);

            View lblNotifyMeOfNewTrades = rootView.findViewById(R.id.lbl_notify_me_of_new_trades);
            lblNotifyMeOfNewTrades.setVisibility(View.GONE);
        }

        return rootView;
    }

    private boolean getThisIsMyAccount(){
        return mUserId == Config.getUserId(getActivity());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case ManualRegistration.SELECT_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        Bitmap selectedImage = ImageUtil.reduceSize(getActivity().getContentResolver(), Config.THUMBNAIL_SIZE, imageReturnedIntent.getData());
                        uploadNewProfilePhoto(selectedImage);
                    } catch (IOException ioex) {
                        ioex.printStackTrace();
                    }
                }
        }
    }

    private void uploadNewProfilePhoto(Bitmap bmp){
        final Activity that = getActivity();

        // Show progress indicator
        final ProgressDialog progress = new ProgressDialog(that);
        progress.setMessage("Updating photo...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();

        final ChangeUserPhotoRequest request = new ChangeUserPhotoRequest();
        request.UserId = Config.getUserId(getActivity());
        request.AuthToken = Config.getAuthToken(getActivity());
        request.Photo = ImageUtil.toBase64(bmp);

        Config.getService().changeUserPhoto(request, new Callback<ChangeUserPhotoResponse>() {
            @Override
            public void success(ChangeUserPhotoResponse response, Response http) {
                progress.dismiss();

                if (http.getStatus() != 200) {
                    Toast.makeText(that, that.getString(R.string.generic_error), Toast.LENGTH_LONG).show();
                } else {
                    if (response.Error != null && response.Error.length() > 0)
                        Toast.makeText(that, response.Error, Toast.LENGTH_LONG).show();
                    else {
                        refreshAccountDetails();
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
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onResume(){
        super.onResume();
        refreshAccountDetails();
    }

    private void refreshAccountDetails() {
        final Activity that = getActivity();

        // Show progress indicator
        final ProgressDialog progress = new ProgressDialog(that);
        progress.setMessage("Getting profile...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();

        if (getThisIsMyAccount()) {
            final GetMyProfileRequest request = new GetMyProfileRequest();
            request.UserId = Config.getUserId(getActivity());
            request.AuthToken = Config.getAuthToken(getActivity());

            Config.getService().getMyProfile(request, new Callback<GetMyProfileResponse>() {
                @Override
                public void success(GetMyProfileResponse response, Response http) {
                    progress.dismiss();

                    if (http.getStatus() != 200) {
                        Toast.makeText(that, that.getString(R.string.generic_error), Toast.LENGTH_LONG).show();
                    } else {
                        if (response.Error != null && response.Error.length() > 0)
                            Toast.makeText(that, response.Error, Toast.LENGTH_LONG).show();
                        else {
                            ToggleButton chkNotifications = (ToggleButton)that.findViewById(R.id.btn_notify_me_of_new_trades);
                            chkNotifications.setChecked(response.Preferences != null); //todo: preferences

                            populateControls(response.DisplayName, response.LastSeen, response.MemberSince, response.PhotoUrl, response.Ratings, that);
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
        else
        {
            final GetUserProfileRequest request = new GetUserProfileRequest();
            request.UserId = mUserId;

            Config.getService().getUserProfile(request, new Callback<GetUserProfileResponse>() {
                @Override
                public void success(GetUserProfileResponse response, Response http) {
                    progress.dismiss();

                    if (http.getStatus() != 200) {
                        Toast.makeText(that, that.getString(R.string.generic_error), Toast.LENGTH_LONG).show();
                    } else {
                        if (response.Error != null && response.Error.length() > 0)
                            Toast.makeText(that, response.Error, Toast.LENGTH_LONG).show();
                        else {
                            populateControls(response.DisplayName, response.LastSeen, response.MemberSince, response.PhotoUrl, response.Ratings, that);
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
    }

    private void populateControls(String displayName, Date lastSeen, Date memberSince, String photoUrl, Rating[] ratings, Activity that) {
        ImageView imageView = (ImageView)that.findViewById(R.id.img_profile);
        Picasso.with(imageView.getContext())
                .load(ImageUtil.mapServerPath(photoUrl))
                .into(imageView);

        TextView txtUserName = (TextView)that.findViewById(R.id.lbl_user_name);
        txtUserName.setText(displayName);

        TextView txtMemberSince = (TextView)that.findViewById(R.id.lbl_member_since);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        txtMemberSince.setText(dateFormat.format(memberSince));

        if (!getThisIsMyAccount() && lastSeen != null) {
            TextView txtLastSeen = (TextView) that.findViewById(R.id.lbl_last_seen);
            CharSequence last_seen = "last seen " + DateUtils.getRelativeTimeSpanString(getActivity(), lastSeen.getTime(), true);
            txtLastSeen.setText(last_seen);
        }

        RatingBar avgRatings = (RatingBar) that.findViewById(R.id.stars_averaged);
        if (ratings != null) {
            int totalStars = 0;
            for (int i = 0; i < ratings.length; i++) {
                totalStars += ratings[i].Stars;
            }
            avgRatings.setRating(totalStars / Math.max(1, ratings.length));

            String reviewCountText = ratings.length == 1 ? "1 review" : ratings.length + " reviews";
            TextView lblReviewCount = (TextView)that.findViewById(R.id.lbl_review_count);
            lblReviewCount.setText(reviewCountText);
        }

        RatingsAdapter ratingsAdapter = new RatingsAdapter(that, R.layout.ratings_view, Arrays.asList(ratings));
        ListView lvRatings = (ListView)that.findViewById(R.id.lv_ratings);
        lvRatings.setAdapter(ratingsAdapter);
    }

    @Override
    public int getMenuId() {
        return R.menu.menu_my_account;
    }
}