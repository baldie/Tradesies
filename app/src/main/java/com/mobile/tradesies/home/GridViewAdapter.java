package com.mobile.tradesies.home;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobile.tradesies.Config;
import com.mobile.tradesies.ImageUtil;
import com.mobile.tradesies.R;
import com.mobile.tradesies.ScaleImageView;
import com.mobile.tradesies.Session;
import com.mobile.tradesies.datacontracts.Item;
import com.mobile.tradesies.datacontracts.Notification;
import com.mobile.tradesies.datacontracts.Trade;
import com.squareup.picasso.Picasso;
import com.readystatesoftware.viewbadger.BadgeView;
import java.util.ArrayList;
import java.util.Arrays;

public class GridViewAdapter extends RecyclerView.Adapter<GridViewAdapter.ViewHolder> {

    private ArrayList<Item> _items;
    private static ArrayList<BadgeView> _badges = new ArrayList<>();
    private View.OnClickListener mOnClickListener = null;

    public GridViewAdapter(Item[] items, View.OnClickListener listener){
        mOnClickListener = listener;

        _items = new ArrayList<>(Arrays.asList(items));
    }

    public void clear(){
        // This prevents badges from appearing on the wrong items
        for(BadgeView badge : _badges) {
            badge.hide();
        }
        _badges.clear();

        this._items.clear();
        super.notifyDataSetChanged();
    }

    public void addItems(Item[] itemsToAdd){
        if (itemsToAdd == null) {
            Log.d(Config.TAG, "attempting to add null items");
            return;
        }

        for(Item i : itemsToAdd) {
            if (!_items.contains(i))
                _items.add(i);
        }

        super.notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.grid_item, viewGroup, false);
        v.setOnClickListener(mOnClickListener);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Item item = _items.get(i);
        viewHolder.Id = item.Id;

        // Item image
        Picasso.with(viewHolder.itemView.getContext())
                .load(ImageUtil.mapServerPath(item.getPrimaryPhoto().Url))
                .into(viewHolder.mItemImageView);

        try {
            // User image
            Picasso.with(viewHolder.itemView.getContext())
                    .load(ImageUtil.mapServerPath(item.OwnerPhotoUrl))
                    .noPlaceholder()
                    .resize(64, 64)
                    .into(viewHolder.mItemOwnerImageView);
        }
        catch(NullPointerException nex){
            nex.printStackTrace();
        }

        // Item title
        viewHolder.mItemTitle.setText(item.Title);
        viewHolder.mItemImageView.removeOverlay();

        // Badge
        ArrayList<Trade> trades = Session.getInstance().getTrades();
        if (trades != null) {
            int itemTradeCount = 0;
            for (Trade t : trades)
                if (t.ItemOne.Id == item.Id || t.ItemTwo.Id == item.Id) {
                    if (t.Accepted) {
                        // If the trade has been accepted, then that is all we need to know
                        Bitmap tradeStamp = BitmapFactory.decodeResource(viewHolder.mItemImageView.getContext().getResources(), R.drawable.traded_stamp);
                        viewHolder.mItemImageView.addOverlay(tradeStamp);
                        break;
                    }

                    itemTradeCount++;
                }

            if (itemTradeCount > 0) {
                BadgeView badge = new BadgeView(viewHolder.itemView.getContext(), viewHolder.mItemImageView);
                badge.setText(itemTradeCount + "");
                badge.setTextSize(Config.BADGE_FONT_SIZE);
                badge.setBackgroundResource(R.drawable.badge_ifaux);
                badge.show();

                _badges.add(badge);
            }
        }
    }

    public Item getItem(int position){
        return _items.get(position);
    }

    @Override
    public int getItemCount() {
        if (_items != null)
            return _items.size();
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ScaleImageView mItemImageView;
        public ImageView mItemOwnerImageView;
        public TextView mItemTitle;
        public int Id;

        public ViewHolder(View view) {
            super(view);
            mItemImageView = (ScaleImageView) view.findViewById(R.id.image);
            mItemOwnerImageView = (ImageView) view.findViewById(R.id.small_user_photo);
            mItemTitle = (TextView) view.findViewById(R.id.text);
        }
    }
}