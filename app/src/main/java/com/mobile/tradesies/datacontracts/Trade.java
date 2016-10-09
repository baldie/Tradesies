package com.mobile.tradesies.datacontracts;

import java.util.Date;

/**
 * Created by David on 6/7/2015.
 */
public class Trade
{
    public int TradeProposerId;
    public int TradeId;
    public Item ItemOne;
    public Item ItemTwo;
    public Date ProposedDate;
    public Boolean Accepted;
    public Boolean Declined;
    public Boolean CanRate;
}
