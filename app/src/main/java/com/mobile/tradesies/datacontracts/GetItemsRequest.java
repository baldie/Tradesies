package com.mobile.tradesies.datacontracts;

import java.util.Date;

public class GetItemsRequest {
	public int UserId;
	public Category[] Categories;
	public String Latitude;
	public String Longitude;
	public int Radius;
	public String[] SearchTerms;
	public int Limit;
	public int Offset;
}
