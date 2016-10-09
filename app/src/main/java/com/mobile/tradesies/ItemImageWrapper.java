package com.mobile.tradesies;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import com.mobile.tradesies.datacontracts.ItemPhoto;
import com.squareup.picasso.Picasso;

import java.net.URI;

// Helper image class.
// Will load image data into an imageview whether you have an image locally or remotely
public class ItemImageWrapper {

    public ItemImageWrapper(Bitmap bmp)
    {
        _uri = ImageUtil.saveToFileSystem(bmp);
        _thumbnail = ImageUtil.reduceSize(bmp, Config.THUMBNAIL_SIZE);
    }

    public ItemImageWrapper(ItemPhoto photo){
        this._url = ImageUtil.mapServerPath(photo.Url);
        this.isPrimary = photo.IsPrimary;
        _id = photo.PhotoId;
    }

    public boolean isPrimary = false;

    // This will only exist if provided by an item image in the constructor
    private int _id;
    public int getPhotoId(){
        return _id;
    }

    // Use URL if you only have a remote image
    private String _url = null;
    public String getUrl(){
        return _url;
    }

    private Bitmap _thumbnail = null;
    private URI _uri = null;
    public URI getUri(){
        return _uri;
    }

    public void populate(ImageView imageView){
        if (_uri != null) {
            Bitmap bmp = ImageUtil.getBitmapFromUri(imageView.getContext().getContentResolver(), _uri);
            imageView.setImageDrawable(new BitmapDrawable(imageView.getContext().getResources(), bmp));
        }
        else if (_url != null) {
            Picasso.with(imageView.getContext())
                    .load(getUrl())
                    .into(imageView);
        }
    }

    public void populateWithThumbnail(ImageView imageView){
        if (_thumbnail != null) {
            imageView.setImageDrawable(new BitmapDrawable(imageView.getContext().getResources(), _thumbnail));
        }
        else if (_url != null) {
            Picasso.with(imageView.getContext())
                    .load(getUrl())
                    .resize(Config.THUMBNAIL_SIZE, Config.THUMBNAIL_SIZE)
                    .into(imageView);
        }
    }
}