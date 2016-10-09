package com.mobile.tradesies.trade;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.mobile.tradesies.Config;
import com.mobile.tradesies.ImageUtil;
import com.mobile.tradesies.R;
import com.mobile.tradesies.datacontracts.Item;
import com.mobile.tradesies.datacontracts.Trade;
import com.squareup.picasso.Picasso;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TradeAdapter extends BaseAdapter {

    private final List<Trade> Trades;
    private Activity context;
    private Item mOriginalItem;

    public TradeAdapter(Activity context, Item originalItem, List<Trade> Trades) {
        this.context = context;
        this.Trades = Trades;
        this.mOriginalItem = originalItem;
    }

    @Override
    public int getCount() {
        if (Trades != null) {
            return Trades.size();
        } else {
            return 0;
        }
    }

    @Override
    public Trade getItem(int position) {
        if (Trades != null) {
            return Trades.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        final Trade trade = getItem(position);
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = vi.inflate(R.layout.list_item_trade, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Log.d(Config.TAG, "TradeAdapter - showing trade with TradeId " + trade.TradeId);

        // Show the other item in the thumbnail
        Item itemToShow = this.mOriginalItem.Id != trade.ItemOne.Id
                ? trade.ItemOne
                : trade.ItemTwo;

        Picasso.with(holder.image.getContext())
                .load(ImageUtil.mapServerPath(itemToShow.getPrimaryPhoto().Url))
                .noPlaceholder()
                .resize(Config.THUMBNAIL_SIZE, Config.THUMBNAIL_SIZE)
                .into(holder.image);

        int unacceptedText = this.mOriginalItem.Id != trade.ItemOne.Id
                ? R.string.someone_proposed_a_trade
                : R.string.you_proposed_a_trade;

        holder.txtTrade.setText(trade.Accepted ? R.string.completed_trade : unacceptedText);
        holder.txtDate.setText(getTimeText(trade));

        // Launch the details of that trade when clicked
        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TradeActivity.launch(context, v, trade.TradeId);
            }
        });

        return convertView;
    }

    public void add(Trade trade) {
        Trades.add(trade);
    }

    public void add(List<Trade> trades) {
        Trades.addAll(trades);
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.txtTrade = (TextView) v.findViewById(R.id.trade_text);
        holder.txtDate = (TextView) v.findViewById(R.id.trade_date);
        holder.image = (ImageView) v.findViewById(R.id.trade_image);
        holder.rootView = (RelativeLayout)v.findViewById(R.id.root_view);
        return holder;
    }

    private String getTimeText(Trade trade) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("M-d-yy, h:mm a", Locale.US);
        return dateFormat.format(trade.ProposedDate);
    }

    private static class ViewHolder {
        public TextView txtTrade;
        public TextView txtDate;
        public ImageView image;
        public RelativeLayout rootView;
    }
}