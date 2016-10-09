package com.mobile.tradesies.item;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.graphics.SurfaceTexture;
import android.widget.ImageView;
import com.mobile.tradesies.Config;
import com.mobile.tradesies.ImageUtil;
import com.mobile.tradesies.R;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class AddItemCameraActivity extends ActionBarActivity {

    private ImageButton mBtnToggleFlash = null;
    private ImageButton mBtnSnapPhoto = null;
    private ImageButton mBtnGallery = null;
    private ImageButton mBtnDeletePhoto = null;
    private Button mBtnNext = null;
    private TextureView mTextureView;
    private boolean mFlash = false;
    public Camera mCamera = null;
    private boolean mStartCameraPreviewOnRemoveCurrentImage = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item_camera);

        mTextureView = (TextureView)findViewById(R.id.camera_preview);
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);

        mBtnToggleFlash = (ImageButton)findViewById(R.id.btn_flash_toggle);
        mBtnToggleFlash.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mFlash = !mFlash;
                mBtnToggleFlash.setImageResource(mFlash ? R.drawable.ic_flash : R.drawable.ic_flash_off);
            }
        });

        mBtnSnapPhoto = (ImageButton)findViewById(R.id.btn_snap_photo);
        mBtnSnapPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Camera.Parameters params = mCamera.getParameters();
                params.set("flash-mode", mFlash ? Camera.Parameters.FLASH_MODE_ON : Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(params);
                mCamera.takePicture(null, null, capturedIt);
            }
        });

        mBtnGallery = (ImageButton) findViewById(R.id.btn_gallery);
        mBtnGallery.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 1);
            }
        });

        final Activity that = this;
        final ImageView imageView = (ImageView) findViewById(R.id.captured_photo);
        final CheckBox mChkPrimary = (CheckBox) findViewById(R.id.chkPrimaryPhoto);

        mBtnNext = (Button)findViewById(R.id.btn_next);
        mBtnNext.setEnabled(false);
        mBtnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStartCameraPreviewOnRemoveCurrentImage = false;
                Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                EditItemActivity.launch(that, bitmap, mChkPrimary.isChecked());
            }
        });

        mBtnDeletePhoto = (ImageButton)findViewById(R.id.btn_delete_photo);
        mBtnDeletePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show camera
                View cameraPreview = findViewById(R.id.camera_preview);
                cameraPreview.setVisibility(View.VISIBLE);

                if (mStartCameraPreviewOnRemoveCurrentImage)
                    mCamera.startPreview();

                // Hide image
                ImageView imageView = (ImageView) findViewById(R.id.captured_photo);
                imageView.setVisibility(View.GONE);

                // Hide self
                v.setVisibility(View.GONE);

                // Show camera buttons
                mBtnToggleFlash.setVisibility(View.VISIBLE);
                mBtnSnapPhoto.setVisibility(View.VISIBLE);
                mBtnGallery.setVisibility(View.VISIBLE);

                mBtnNext.setEnabled(false);
            }
        });
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

    public static void launch(Activity activity, View transitionView)
    {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionView, activity.getString(R.string.transition));
        ActivityCompat.startActivity(activity, new Intent(activity, AddItemCameraActivity.class), options.toBundle());
    }

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener(){

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mCamera = Camera.open();
            //Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
            //mTextureView.setLayoutParams(new LinearLayout.LayoutParams(previewSize.width, previewSize.height, Gravity.CENTER));
            mCamera.setDisplayOrientation(90);

            try {
                mCamera.setPreviewTexture(surface);
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
            mCamera.startPreview();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            if (mCamera == null)
                return true;

            mCamera.stopPreview();
            try {
                mCamera.setPreviewTexture(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.release();
            mCamera = null;

            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    private Camera.PictureCallback capturedIt = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            Bitmap bitmap = BitmapFactory.decodeByteArray(data , 0, data .length);
            if (bitmap!= null) {
                mStartCameraPreviewOnRemoveCurrentImage = true;
                bitmap = ImageUtil.rotate90right(ImageUtil.reduceSize(bitmap, Config.IMAGE_SIZE));
                switchToImage(bitmap);
            }
        }
    };

    private final int SELECT_PHOTO = 1;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    try {
                        final Uri imageUri = imageReturnedIntent.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        selectedImage = ImageUtil.reduceSize(selectedImage, Config.IMAGE_SIZE);
                        mStartCameraPreviewOnRemoveCurrentImage = false;
                        switchToImage(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    private void switchToImage(Bitmap bitmap){
        // Hide camera
        View cameraPreview = findViewById(R.id.camera_preview);
        cameraPreview.setVisibility(View.GONE);

        // Show image
        ImageView imageView = (ImageView) findViewById(R.id.captured_photo);
        imageView.setVisibility(View.VISIBLE);

        // Hide camera buttons
        mBtnToggleFlash.setVisibility(View.INVISIBLE);
        mBtnSnapPhoto.setVisibility(View.GONE);
        mBtnDeletePhoto.setVisibility(View.VISIBLE);
        mBtnGallery.setVisibility(View.INVISIBLE);

        imageView.setImageBitmap(bitmap);
        mBtnNext.setEnabled(true);
    }
}