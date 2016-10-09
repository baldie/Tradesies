package com.mobile.tradesies.home;

public class DrawerMenuItem {

    public final static int BROWSE_ID = 0;
    public final static int MY_ITEMS_ID = 1;
    public final static int MY_ACCOUNT_ID = 2;
    public final static int HELP_ID = 3;
    public final static int LOG_IN_ID = 4;
    public final static int LOG_OUT_ID = 5;
    public final static int REGISTER_ID = 6;

    public int Id;
    public int IconId;
    public String Text;
    public int Badge;

    public DrawerMenuItem(int id, int iconId, String text, int badge){
        Id = id;
        IconId = iconId;
        Text = text;
        Badge = badge;
    }
}
