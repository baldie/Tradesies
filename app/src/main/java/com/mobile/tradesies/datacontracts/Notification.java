package com.mobile.tradesies.datacontracts;

import java.util.Date;

/**
 * Created by David on 4/18/2015.
 */
public class Notification {

    final public static int Unknown = 0;
    final public static int NewTradeProposed = 1;
    final public static int TradeAccepted = 2;
    final public static int NewTradeChat = 3;
    final public static int NewRating = 4;
    final public static int PleaseRate = 5;

    public int Type;
    public int Id;
    public String Title;
    public String Body;
    public String PhotoUrl;
    public String Extra;
    public Date Date;
}
