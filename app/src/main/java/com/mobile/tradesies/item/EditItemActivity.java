package com.mobile.tradesies.item;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.tradesies.AnalyticsUtil;
import com.mobile.tradesies.Config;
import com.mobile.tradesies.ImageUtil;
import com.mobile.tradesies.ItemImageWrapper;
import com.mobile.tradesies.R;
import com.mobile.tradesies.TradesiesException;
import com.mobile.tradesies.datacontracts.AddItemPhotoRequest;
import com.mobile.tradesies.datacontracts.AddItemPhotoResponse;
import com.mobile.tradesies.datacontracts.AddItemRequest;
import com.mobile.tradesies.datacontracts.AddItemResponse;
import com.mobile.tradesies.datacontracts.ChangePrimaryImageRequest;
import com.mobile.tradesies.datacontracts.ChangePrimaryImageResponse;
import com.mobile.tradesies.datacontracts.DeleteItemPhotoRequest;
import com.mobile.tradesies.datacontracts.DeleteItemPhotoResponse;
import com.mobile.tradesies.datacontracts.DeleteItemRequest;
import com.mobile.tradesies.datacontracts.DeleteItemResponse;
import com.mobile.tradesies.datacontracts.Item;
import com.mobile.tradesies.datacontracts.ItemPhoto;
import com.mobile.tradesies.datacontracts.UpdateItemRequest;
import com.mobile.tradesies.datacontracts.UpdateItemResponse;
import com.mobile.tradesies.home.HomeActivity;
import com.mobile.tradesies.home.MyItemsFragment;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import it.sephiroth.android.library.widget.AbsHListView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class EditItemActivity extends AppCompatActivity {
    private it.sephiroth.android.library.widget.HListView _lvItemPhotos = null;
    private List<ItemImageWrapper> _thumbs = new ArrayList<>();
    private ArrayAdapter<ItemImageWrapper> mThumbnailsAdapter = null;
    final public static String ITEM_BUNDLE_KEY = "ITEM_BUNDLE_KEY";

    private Item mExistingItem = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        // If there is a bundle, then we're editing an existing item, so lets set up the initial image.
        mExistingItem = getIntent().getParcelableExtra(ITEM_BUNDLE_KEY);
        if (mExistingItem != null) {
            mInitialImage = new ItemImageWrapper(mExistingItem.getPrimaryPhoto());
        }

        final ImageView selectedImageView = (ImageView) findViewById(R.id.currentImage);
        if (mInitialImage != null) {
            mInitialImage.populate(selectedImageView);
            selectedImageView.setTag(mInitialImage);
        }

        // This may occur if Android kills our memory
        if (mExistingItem == null && mInitialImage == null) {
            finish();
            return;
        }

        // If there is no item yet, then the initial image is all we have to add to the array.
        if (mExistingItem == null)
            // This occurs when a user is adding a brand new item
            _thumbs.add(mInitialImage);
        else
        {
            for(ItemPhoto photo : mExistingItem.Photos)
                _thumbs.add(new ItemImageWrapper(photo));
        }

        // Create the add new photo button programmatically
        ImageButton btnAddPhoto = new ImageButton(this);
        btnAddPhoto.setImageResource(R.drawable.ic_plus_circle);
        btnAddPhoto.setLayoutParams(new AbsHListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));
        btnAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPhoto();
            }
        });

        final ImageButton btnDeleteImage = (ImageButton)findViewById(R.id.btn_delete_photo);
        btnDeleteImage.setEnabled(_thumbs.size() > 1);
        btnDeleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePhoto(selectedImageView, btnDeleteImage);
            }
        });

        final CheckBox chkPrimary = (CheckBox) findViewById(R.id.chkPrimaryPhoto);
        chkPrimary.setChecked(mInitialImage.isPrimary);
        chkPrimary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePrimaryImage((CheckBox) v, selectedImageView);
            }
        });

        mThumbnailsAdapter = new ImageThumbnailAdapter(this, R.layout.thumbnail_photo_view, _thumbs);
        _lvItemPhotos = (it.sephiroth.android.library.widget.HListView)findViewById(R.id.lv_item_photos);
        _lvItemPhotos.addFooterView(btnAddPhoto);
        _lvItemPhotos.setAdapter(mThumbnailsAdapter);

        _lvItemPhotos.setOnItemClickListener(new it.sephiroth.android.library.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(it.sephiroth.android.library.widget.AdapterView<?> adapterView, View view, int i, long l) {
                selectThumbnailAtPosition(i);
            }
        });

        final Button btnSave = (Button) findViewById(R.id.btn_save_item);
        btnSave.setText(mExistingItem != null ? R.string.update_item : R.string.save_item);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mExistingItem != null) {
                    Item itemToSave = getExistingItem();
                    if (validateExistingItem(itemToSave))
                        saveExistingItem(itemToSave);
                } else {
                    Item itemToSave = getNewItem();
                    if (validateNewItem(itemToSave))
                        saveNewItem(itemToSave);
                }
            }
        });

        final Button btnDeleteItem = (Button) findViewById(R.id.btn_delete_item);
        btnDeleteItem.setVisibility(mExistingItem != null ? View.VISIBLE : View.GONE);
        btnDeleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
deleteItemWithPrompt();
            }
        });

        Button btnCategories = (Button) findViewById(R.id.btn_categories);
        btnCategories.setVisibility(View.GONE);

        TextView txtTitle = (TextView) findViewById(R.id.txt_title);
        TextView txtDescription = (TextView) findViewById(R.id.txt_description);

        if (Config.TESTING && mExistingItem == null){
            txtTitle.setText(getString(R.string.debug_item_title));
            txtDescription.setText(getString(R.string.debug_item_description));
        }
        if (mExistingItem != null){
            txtTitle.setText(mExistingItem.Title);
            txtDescription.setText(mExistingItem.Description);
            btnSave.setText(R.string.update_item);
        }
    }

    private void changePrimaryImage(CheckBox checkBox, ImageView selectedImageView) {
        CheckBox chkPrimary = checkBox;

        ItemImageWrapper imageWrapper = (ItemImageWrapper) selectedImageView.getTag();
        imageWrapper.isPrimary = chkPrimary.isChecked();

        // Change isPrimary on thumbnails
        for (int i = 0; i < mThumbnailsAdapter.getCount(); i++) {
            ItemImageWrapper img = mThumbnailsAdapter.getItem((i));
            if (img.getPhotoId() == imageWrapper.getPhotoId()) {
                img.isPrimary = chkPrimary.isChecked();
            }
            else {
                img.isPrimary = false;
            }
        }

        // Change isPrimary on item in memory
        for(int i = 0; i < mExistingItem.Photos.length; i++){
            if (mExistingItem.Photos[i].PhotoId == imageWrapper.getPhotoId()){
                mExistingItem.Photos[i].IsPrimary = chkPrimary.isChecked();
            }
            else
            {
                mExistingItem.Photos[i].IsPrimary = false;
            }
        }

        // Change isPrimary on server
        if (chkPrimary.isChecked()) {
            Log.d(Config.TAG, "Changing primary photo for item in the cloud...");

            final ProgressDialog progress = new ProgressDialog(this);
            progress.setMessage("Updating...");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.show();

            ChangePrimaryImageRequest request = new ChangePrimaryImageRequest();
            request.AuthToken = Config.getAuthToken(this);
            request.UserId = Config.getUserId(this);
            request.ItemId = mExistingItem.Id;
            request.PrimaryPhotoId = imageWrapper.getPhotoId();

            final Activity that = this;
            Config.getService().changePrimaryImage(request, new Callback<ChangePrimaryImageResponse>() {
                @Override
                public void success(ChangePrimaryImageResponse response, Response http) {
                    boolean errorOccurred = response.Error != null && response.Error.length() > 0;
                    if (!errorOccurred) {
                        Log.d(Config.TAG, "Updated primary photo on server.");
                    } else {
                        Toast.makeText(that, response.Error, Toast.LENGTH_SHORT).show();
                        Log.e(Config.TAG, response.Error);
                    }
                    progress.dismiss();
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(that, error.getMessage(), Toast.LENGTH_SHORT).show();
                    progress.dismiss();
                }
            });
        }
    }

    private void selectThumbnailAtPosition(int index){
        ItemImageWrapper thumb = (ItemImageWrapper) _lvItemPhotos.getItemAtPosition(index);

        ImageView selectedImageView = (ImageView) findViewById(R.id.currentImage);
        thumb.populate(selectedImageView);
        selectedImageView.setTag(thumb);

        final CheckBox chkPrimary = (CheckBox) findViewById(R.id.chkPrimaryPhoto);
        chkPrimary.setChecked(thumb.isPrimary);
    }

    private void deletePhoto(ImageView mSelectedImageView, final ImageButton btnDelete) {
        final ItemImageWrapper imageToDelete = (ItemImageWrapper) mSelectedImageView.getTag();

        if (imageToDelete.isPrimary) {
            Toast.makeText(this, R.string.cannot_delete_primary_image, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mExistingItem != null){
            Log.d(Config.TAG, "Deleting image...");

            final ProgressDialog progress = new ProgressDialog(this);
            progress.setMessage("Deleting...");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.show();

            DeleteItemPhotoRequest request = new DeleteItemPhotoRequest();
            request.AuthToken = Config.getAuthToken(this);
            request.UserId = Config.getUserId(this);
            request.ItemId = this.mExistingItem.Id;
            request.PhotoId = imageToDelete.getPhotoId();

            final Activity that = this;
            Config.getService().deleteItemPhoto(request, new Callback<DeleteItemPhotoResponse>() {
                @Override
                public void success(DeleteItemPhotoResponse deleteItemPhotoResponse, Response response) {
                    progress.dismiss();

                    boolean errorOccurred = deleteItemPhotoResponse.Error != null && deleteItemPhotoResponse.Error.length() > 0;
                    if (!errorOccurred) {

                        // Remove the image from the list
                        mThumbnailsAdapter.remove(imageToDelete);
                        mThumbnailsAdapter.notifyDataSetChanged();

                        // Select a different image.
                        selectThumbnailAtPosition(0);

                        // Update controls
                        btnDelete.setEnabled(mThumbnailsAdapter.getCount() > 1);

                        Log.d(Config.TAG, "Deleted image successfully from server.");
                    } else {
                        Log.e(Config.TAG, "Error: " + deleteItemPhotoResponse.Error);
                        Toast.makeText(that, deleteItemPhotoResponse.Error, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    progress.dismiss();
                    Toast.makeText(that, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            finish();
        }
    }

    private boolean validateExistingItem(Item itemToValidate){
        boolean valid = validateBaseItem(itemToValidate);
        try
        {
            int photoId = 0;
            for(ItemPhoto photo : itemToValidate.Photos) {
                if (photo.IsPrimary) {
                    photoId = photo.PhotoId;
                    break;
                }
            }
            if (photoId == 0)
                throw new TradesiesException("Please indicate a primary photo");
        }
        catch(TradesiesException tex){
            Toast.makeText(this, tex.getMessage(), Toast.LENGTH_SHORT).show();
            valid = false;
        }
        return valid;
    }

    private boolean validateNewItem(Item itemToValidate){
        return validateBaseItem(itemToValidate);
    }

    private boolean validateBaseItem(Item itemToValidate){
        boolean valid = true;
        try
        {
            if (itemToValidate.Title == null || itemToValidate.Title.trim().length() < 1)
                throw new TradesiesException("Please add a title");

            if (itemToValidate.Title.trim().length() > Config.ITEM_TITLE_MAX_LENGTH)
                throw new TradesiesException("Title is too long.");

            if (itemToValidate.Description == null || itemToValidate.Description.trim().length() < 1)
                throw new TradesiesException("Please add a description");

            if (itemToValidate.Description.trim().length() > Config.ITEM_DESCRIPTION_MAX_LENGTH)
                throw new TradesiesException("Description is too long.");
        }
        catch(TradesiesException tex){
            Toast.makeText(this, tex.getMessage(), Toast.LENGTH_SHORT).show();
            valid = false;
        }
        return valid;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_item, menu);
        return true;
    }

    // This is only used when the user is coming from the create new item screen
    private static ItemImageWrapper mInitialImage = null;
    public static void setInitialImage(ItemImageWrapper image){
        mInitialImage = image;
    }

    public static void launch(Activity activity, Bitmap bmp, boolean isPrimary)
    {
        ItemImageWrapper initialImage = new ItemImageWrapper(bmp);
        initialImage.isPrimary = isPrimary;

        EditItemActivity.setInitialImage(initialImage);

        Intent editIntent = new Intent(activity, EditItemActivity.class);
        ActivityCompat.startActivity(activity, editIntent, new Bundle());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_categories) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int OPEN_IMAGE_GALLERY = 2;
    private void addPhoto() {
        final CharSequence[] items = { "Use Camera", "Choose from gallery", "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(EditItemActivity.this);
        builder.setTitle("Add a photo");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Use Camera")) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    Uri imageFileUri = Uri.parse("file://" + Environment.getExternalStorageDirectory().getPath() + "/tradesies_item.png");
                    takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageFileUri);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                } else if (items[item].equals("Choose from gallery")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Choose from gallery"), OPEN_IMAGE_GALLERY);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK)
            return;

        // Get the image and shrink it
        Uri pathToNewImage =  (requestCode == REQUEST_IMAGE_CAPTURE)
            ? Uri.parse("file://" + Environment.getExternalStorageDirectory().getPath() + "/tradesies_item.png")
            : data.getData();

        Bitmap newBitmap = null;
        try {
            newBitmap = ImageUtil.reduceSize(getContentResolver(), Config.IMAGE_SIZE, pathToNewImage);
        }
        catch(FileNotFoundException fnfe ){
            fnfe.printStackTrace();
        }

        if (mExistingItem == null)
        {
            // Add the thumbnail to the list
            final ItemImageWrapper itemImage = new ItemImageWrapper(newBitmap);
            mThumbnailsAdapter.add(itemImage);
            mThumbnailsAdapter.notifyDataSetChanged();
        }
        else
        {
            // Save the new image to server
            Log.d(Config.TAG, "Uploading item image to cloud...");
            final ProgressDialog progress = new ProgressDialog(this);
            progress.setMessage("Uploading image...");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.show();

            AddItemPhotoRequest request = new AddItemPhotoRequest();
            request.AuthToken = Config.getAuthToken(this);
            request.UserId = Config.getUserId(this);
            request.ItemId = mExistingItem.Id;
            request.Photo = new ItemPhoto();
            request.Photo.ImageData = ImageUtil.toBase64(newBitmap);

            final Activity that = this;
            Config.getService().addItemPhoto(request, new Callback<AddItemPhotoResponse>() {
                @Override
                public void success(AddItemPhotoResponse addItemPhotoResponse, Response response) {
                    boolean errorOccurred = addItemPhotoResponse.Error != null && addItemPhotoResponse.Error.length() > 0;
                    if (!errorOccurred) {

                        // Add the thumbnail to the list
                        final ItemImageWrapper itemImage = new ItemImageWrapper(addItemPhotoResponse.Photo);
                        mThumbnailsAdapter.add(itemImage);
                        mThumbnailsAdapter.notifyDataSetChanged();

                        // Add the photo to the existing item in memory
                        ItemPhoto [] newArray = new ItemPhoto[mExistingItem.Photos.length + 1];
                        for(int i = 0; i < mExistingItem.Photos.length; i++){
                            newArray[i] = mExistingItem.Photos[i];
                        }
                        newArray[newArray.length - 1] = addItemPhotoResponse.Photo;
                        mExistingItem.Photos = newArray;

                        final ImageButton btnDelete = (ImageButton)findViewById(R.id.btn_delete_photo);
                        btnDelete.setEnabled(mThumbnailsAdapter.getCount() > 1);

                        // Select the recently added photo
                        //selectThumbnailAtPosition(mThumbnailsAdapter.getCount() - 1);

                        Log.d(Config.TAG, "Saved item image successfully to cloud.");
                    } else {
                        Toast.makeText(that, addItemPhotoResponse.Error, Toast.LENGTH_SHORT).show();
                        Log.e(Config.TAG, addItemPhotoResponse.Error);
                    }
                    progress.dismiss();
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(that, error.getMessage(), Toast.LENGTH_SHORT).show();
                    progress.dismiss();
                }
            });
        }
    }

    public Item getNewItem(){
        Item item = new Item();

        EditText txtTitle = (EditText)findViewById(R.id.txt_title);
        item.Title = txtTitle.getText().toString();

        EditText txtDescription = (EditText)findViewById(R.id.txt_description);
        item.Description = txtDescription.getText().toString();

        // todo: wire up categories
        item.Categories = new int[1];
        item.Categories[0] = 1;

        // Get the user's last known location.
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        item.Latitude = String.valueOf(lastKnownLocation.getLatitude());
        item.Longitude = String.valueOf(lastKnownLocation.getLongitude());

        // When we save an existing item, we don't send images. Those are handled separately
        if (mExistingItem == null) {
            ItemPhoto[] photos = new ItemPhoto[mThumbnailsAdapter.getCount()];
            for (int i = 0; i < mThumbnailsAdapter.getCount(); i++) {
                ItemImageWrapper img = mThumbnailsAdapter.getItem(i);

                Bitmap actualImage = ImageUtil.getBitmapFromUri(getContentResolver(), img.getUri());
                ItemPhoto newPhoto = new ItemPhoto();
                newPhoto.IsPrimary = img.isPrimary;
                newPhoto.ImageData = ImageUtil.toBase64(actualImage);
                photos[i] = newPhoto;
            }
            item.Photos = photos;
        }
        item.IsActive = true;

        return item;
    }

    private void saveNewItem(final Item newItem){
        Log.d(Config.TAG, "Saving new item...");
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Saving...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();

        // Run this on a separate thread to immediately trigger the progress dialog
        final EditItemActivity that = this;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                AddItemRequest addItemRequest = new AddItemRequest();
                addItemRequest.UserId = Config.getUserId(that);
                addItemRequest.AuthToken = Config.getAuthToken(that);
                addItemRequest.Item = newItem;

                Config.getService().addItem(addItemRequest, new Callback<AddItemResponse>() {
                    @Override
                    public void success(AddItemResponse response, Response lel) {
                        progress.dismiss();

                        if (response.Error != null && response.Error.length() > 0) {
                            Toast.makeText(that, "ERROR: " + response.Error, Toast.LENGTH_LONG).show();
                        } else {
                            Log.d(Config.TAG, "New item added!");
                            AnalyticsUtil.trackItemAdded(that, newItem);

                            Intent returnToMyItems = new Intent(that, HomeActivity.class);
                            returnToMyItems.putExtra(HomeActivity.NAVIGATION_FRAGMENT_REQUESTED, MyItemsFragment.class.toString());
                            returnToMyItems.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            that.startActivity(returnToMyItems);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        progress.dismiss();
                        Toast.makeText(that, "ERROR: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }, 250);
    }

    private Item getExistingItem(){
        Item itemFromScreen = getNewItem();
        mExistingItem.Title = itemFromScreen.Title;
        mExistingItem.Description = itemFromScreen.Description;
        mExistingItem.Latitude = itemFromScreen.Latitude;
        mExistingItem.Longitude = itemFromScreen.Longitude;
        mExistingItem.IsActive = itemFromScreen.IsActive;
        mExistingItem.Categories = itemFromScreen.Categories;
        return mExistingItem;
    }

    private void deleteItemWithPrompt(){
        new AlertDialog.Builder(this)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                       deleteItem();
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

    private void deleteItem(){
        Log.d(Config.TAG, "Deleting new item...");
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Deleting...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();

        // Run this on a separate thread to immediately trigger the progress dialog
        final EditItemActivity that = this;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(Config.TAG, "Deleting item...");
                DeleteItemRequest request = new DeleteItemRequest();
                request.UserId = Config.getUserId(that);
                request.AuthToken = Config.getAuthToken(that);
                request.ItemId = mExistingItem.Id;

                Config.getService().deleteItem(request, new Callback<DeleteItemResponse>() {
                    @Override
                    public void success(DeleteItemResponse response, Response lel) {
                        progress.dismiss();

                        if (response.Error != null && response.Error.length() > 0) {
                            Toast.makeText(that, "ERROR: " + response.Error, Toast.LENGTH_LONG).show();
                        } else {
                            mExistingItem = null;

                            // todo: go to "my items", not marketplace
                            Intent returnToMyItems = new Intent(that, HomeActivity.class);
                            returnToMyItems.putExtra(HomeActivity.NAVIGATION_FRAGMENT_REQUESTED, MyItemsFragment.class);
                            returnToMyItems.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            that.startActivity(returnToMyItems);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        progress.dismiss();
                        Toast.makeText(that, "ERROR: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }, 250);
    }

    private void saveExistingItem(final Item item){
        Log.d(Config.TAG, "Saving existing item...");
        final ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Updating...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();

        // Find the primary image
        int photoId = 0;
        for(ItemPhoto photo : item.Photos) {
            if (photo.IsPrimary) {
                photoId = photo.PhotoId;
                break;
            }
        }
        final int primaryPhotoId = photoId;
        final ItemPhoto[] photosPreserved = item.Photos;

        // Run this on a separate thread to immediately trigger the progress dialog
        final EditItemActivity that = this;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                UpdateItemRequest request = new UpdateItemRequest();
                request.UserId = Config.getUserId(that);
                request.AuthToken = Config.getAuthToken(that);
                request.Item = item;
                request.Item.Photos = null; // this will be ignored on server for all UpdateItem calls, so lets reduce payload size
                request.PrimaryPhotoId = primaryPhotoId;

                // On Update Item, photo content does not get saved.
                Config.getService().updateItem(request, new Callback<UpdateItemResponse>() {
                    @Override
                    public void success(UpdateItemResponse response, Response lel) {
                        // Put the photos back, in case we need to save again.
                        mExistingItem.Photos = photosPreserved;
                        progress.dismiss();

                        if (response.Error != null && response.Error.length() > 0) {
                            Toast.makeText(that, "ERROR: " + response.Error, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(that, "Updated", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        // Put the photos back, in case we need to save again.
                        mExistingItem.Photos = photosPreserved;

                        progress.dismiss();
                        Toast.makeText(that, "ERROR: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }, 250);
    }
}