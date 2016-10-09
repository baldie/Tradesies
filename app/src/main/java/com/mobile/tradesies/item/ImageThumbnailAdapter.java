package com.mobile.tradesies.item;

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import com.mobile.tradesies.ItemImageWrapper;
import com.mobile.tradesies.R;

public class ImageThumbnailAdapter extends ArrayAdapter {

    private Context _context;
    private List<ItemImageWrapper> _thumbnails;

    public ImageThumbnailAdapter(Context context, int resource, List<ItemImageWrapper> thumbs) {
        super(context, resource, thumbs);
        _context = context;
        _thumbnails = thumbs;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(_context).inflate(R.layout.thumbnail_photo_view, null);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.thumbnail);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        _thumbnails.get(position).populateWithThumbnail(holder.imageView);

        return convertView;
    }

    static class ViewHolder {
        ImageView imageView;
    }
}