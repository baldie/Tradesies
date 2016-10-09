package com.mobile.tradesies.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobile.tradesies.Config;
import com.mobile.tradesies.R;
import com.readystatesoftware.viewbadger.BadgeView;
import java.util.List;

public class DrawerMenuAdapter extends ArrayAdapter {
    private Context _context;
    private List<DrawerMenuItem> _menuItems;

    public DrawerMenuAdapter(Context context, int resource, List<DrawerMenuItem> thumbs) {
        super(context, resource, thumbs);
        _context = context;
        _menuItems = thumbs;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(_context).inflate(R.layout.nav_drawer_item, null);

            holder = new ViewHolder();
            holder.imgIcon = (ImageView) convertView.findViewById(R.id.img_drawer_menu_icon);
            holder.txtMenuText = (TextView) convertView.findViewById(R.id.img_drawer_menu_text);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (_menuItems.get(position).IconId != 0)
            holder.imgIcon.setImageResource(_menuItems.get(position).IconId);

        holder.txtMenuText.setText(_menuItems.get(position).Text);

        // Badge
        if (_menuItems.get(position).Badge > 0) {
            BadgeView badge = new BadgeView(_context, holder.txtMenuText);
            badge.setText(_menuItems.get(position).Badge + "");
            badge.setBackgroundResource(R.drawable.badge_ifaux);
            badge.setTextSize(Config.BADGE_FONT_SIZE);
            badge.show();
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView imgIcon;
        TextView txtMenuText;
    }
}