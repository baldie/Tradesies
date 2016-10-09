package com.mobile.tradesies.account;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.mobile.tradesies.Config;
import com.mobile.tradesies.ImageUtil;
import com.mobile.tradesies.R;
import com.mobile.tradesies.datacontracts.Rating;
import com.squareup.picasso.Picasso;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RatingsAdapter extends ArrayAdapter {
    private Context _context;
    private List<Rating> _ratings;

    public RatingsAdapter(Context context, int resource, List<Rating> thumbs) {
        super(context, resource, thumbs);
        _context = context;
        _ratings = thumbs;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(_context).inflate(R.layout.ratings_view, null);
            holder = new ViewHolder();
            holder.imageRater = (ImageView) convertView.findViewById(R.id.img_rater);
            holder.ratingBar = (RatingBar) convertView.findViewById(R.id.stars);
            holder.txtComment = (TextView) convertView.findViewById(R.id.txt_rating_comment);
            holder.txtRateDate = (TextView) convertView.findViewById(R.id.txt_rate_date);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Picasso.with(_context)
                .load(ImageUtil.mapServerPath(_ratings.get(position).UserPhoto))
                .resize(Config.THUMBNAIL_SIZE,Config.THUMBNAIL_SIZE)
                .into(holder.imageRater);

        holder.ratingBar.setRating(_ratings.get(position).Stars);

        holder.txtComment.setText(_ratings.get(position).Comment);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        holder.txtRateDate.setText(dateFormat.format(_ratings.get(position).RateDate));

        return convertView;
    }

    static class ViewHolder {
        ImageView imageRater;
        RatingBar ratingBar;
        TextView txtComment;
        TextView txtRateDate;
    }
}