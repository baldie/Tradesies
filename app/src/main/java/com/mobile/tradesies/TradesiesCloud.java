package com.mobile.tradesies;

import com.mobile.tradesies.datacontracts.AcceptTradeRequest;
import com.mobile.tradesies.datacontracts.AcceptTradeResponse;
import com.mobile.tradesies.datacontracts.AcknowledgeChatRequest;
import com.mobile.tradesies.datacontracts.AcknowledgeChatResponse;
import com.mobile.tradesies.datacontracts.AcknowledgeNotificationsRequest;
import com.mobile.tradesies.datacontracts.AcknowledgeNotificationsResponse;
import com.mobile.tradesies.datacontracts.AddItemPhotoRequest;
import com.mobile.tradesies.datacontracts.AddItemPhotoResponse;
import com.mobile.tradesies.datacontracts.AddItemRequest;
import com.mobile.tradesies.datacontracts.AddItemResponse;
import com.mobile.tradesies.datacontracts.AuthenticateUserRequest;
import com.mobile.tradesies.datacontracts.AuthenticateUserResponse;
import com.mobile.tradesies.datacontracts.ChangePrimaryImageRequest;
import com.mobile.tradesies.datacontracts.ChangePrimaryImageResponse;
import com.mobile.tradesies.datacontracts.ChangeUserPhotoRequest;
import com.mobile.tradesies.datacontracts.ChangeUserPhotoResponse;
import com.mobile.tradesies.datacontracts.ChatRequest;
import com.mobile.tradesies.datacontracts.ChatResponse;
import com.mobile.tradesies.datacontracts.DeclineTradeRequest;
import com.mobile.tradesies.datacontracts.DeclineTradeResponse;
import com.mobile.tradesies.datacontracts.DeleteItemPhotoRequest;
import com.mobile.tradesies.datacontracts.DeleteItemPhotoResponse;
import com.mobile.tradesies.datacontracts.DeleteItemRequest;
import com.mobile.tradesies.datacontracts.DeleteItemResponse;
import com.mobile.tradesies.datacontracts.DeleteUserRequest;
import com.mobile.tradesies.datacontracts.DeleteUserResponse;
import com.mobile.tradesies.datacontracts.GetItemRequest;
import com.mobile.tradesies.datacontracts.GetItemResponse;
import com.mobile.tradesies.datacontracts.GetItemsRequest;
import com.mobile.tradesies.datacontracts.GetItemsResponse;
import com.mobile.tradesies.datacontracts.GetMyItemsRequest;
import com.mobile.tradesies.datacontracts.GetMyItemsResponse;
import com.mobile.tradesies.datacontracts.GetMyProfileRequest;
import com.mobile.tradesies.datacontracts.GetMyProfileResponse;
import com.mobile.tradesies.datacontracts.GetMyTradesRequest;
import com.mobile.tradesies.datacontracts.GetMyTradesResponse;
import com.mobile.tradesies.datacontracts.GetNotificationsRequest;
import com.mobile.tradesies.datacontracts.GetNotificationsResponse;
import com.mobile.tradesies.datacontracts.GetTradeChatRequest;
import com.mobile.tradesies.datacontracts.GetTradeChatResponse;
import com.mobile.tradesies.datacontracts.GetUserProfileRequest;
import com.mobile.tradesies.datacontracts.GetUserProfileResponse;
import com.mobile.tradesies.datacontracts.LogOutRequest;
import com.mobile.tradesies.datacontracts.LogOutResponse;
import com.mobile.tradesies.datacontracts.OAuthenticateUserRequest;
import com.mobile.tradesies.datacontracts.OAuthenticateUserResponse;
import com.mobile.tradesies.datacontracts.ProposeTradeRequest;
import com.mobile.tradesies.datacontracts.ProposeTradeResponse;
import com.mobile.tradesies.datacontracts.RateTradeRequest;
import com.mobile.tradesies.datacontracts.RateTradeResponse;
import com.mobile.tradesies.datacontracts.RegisterUserRequest;
import com.mobile.tradesies.datacontracts.RegisterUserResponse;
import com.mobile.tradesies.datacontracts.ReportItemRequest;
import com.mobile.tradesies.datacontracts.ReportItemResponse;
import com.mobile.tradesies.datacontracts.UpdateItemRequest;
import com.mobile.tradesies.datacontracts.UpdateItemResponse;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

public interface TradesiesCloud
{
    @POST("/User/Register")
    void register(@Body RegisterUserRequest request, Callback<RegisterUserResponse> callBack);
    @POST("/User/Authenticate")
    void authenticate(@Body AuthenticateUserRequest request, Callback<AuthenticateUserResponse> callBack);
    @POST("/User/OAuthenticate")
    void oAuthenticate(@Body OAuthenticateUserRequest request, Callback<OAuthenticateUserResponse> callBack);
    @POST("/Item/Browse")
    void getItems(@Body GetItemsRequest request, Callback<GetItemsResponse> callBack);
    @POST("/Item/GetMine")
    void getMyItems(@Body GetMyItemsRequest request, Callback<GetMyItemsResponse> callBack);
    @POST("/Item/Add")
    void addItem(@Body AddItemRequest request, Callback<AddItemResponse> callBack);
    @POST("/Item/Report")
    void reportItem(@Body ReportItemRequest request, Callback<ReportItemResponse> callBack);
    @POST("/Item/AddPhoto")
    void addItemPhoto(@Body AddItemPhotoRequest request, Callback<AddItemPhotoResponse> callBack);
    @POST("/Item/DeletePhoto")
    void deleteItemPhoto(@Body DeleteItemPhotoRequest request, Callback<DeleteItemPhotoResponse> callBack);
    @POST("/Item/Update")
    void updateItem(@Body UpdateItemRequest request, Callback<UpdateItemResponse> callBack);
    @POST("/Item/GetDetail")
    void getItem(@Body GetItemRequest request, Callback<GetItemResponse> callBack);
    @POST("/User/LogOut")
    void logOut(@Body LogOutRequest request, Callback<LogOutResponse> callBack);
    @POST("/Item/Delete")
    void deleteItem(@Body DeleteItemRequest request, Callback<DeleteItemResponse> callBack);
    @POST("/User/GetMyProfile")
    void getMyProfile(@Body GetMyProfileRequest request, Callback<GetMyProfileResponse> callback);
    @POST("/User/ChangePhoto")
    void changeUserPhoto(@Body ChangeUserPhotoRequest request, Callback<ChangeUserPhotoResponse> callback);
    @POST("/User/Delete")
    void deleteUser(@Body DeleteUserRequest request, Callback<DeleteUserResponse> callback);
    @POST("/Item/ChangePrimaryImage")
    void changePrimaryImage(@Body ChangePrimaryImageRequest request, Callback<ChangePrimaryImageResponse> callback);
    @POST("/User/GetProfile")
    void getUserProfile(@Body GetUserProfileRequest request, Callback<GetUserProfileResponse> callback);
    @POST("/Trade/Propose")
    void proposeTrade(@Body ProposeTradeRequest request, Callback<ProposeTradeResponse> callback);
    @POST("/Trade/GetMyTrades")
    void getMyTrades(@Body GetMyTradesRequest request, Callback<GetMyTradesResponse> callback);
    @POST("/Trade/GetChat")
    void getTradeChat(@Body GetTradeChatRequest request, Callback<GetTradeChatResponse> callback);
    @POST("/Trade/Chat")
    void chat(@Body ChatRequest request, Callback<ChatResponse> callback);
    @POST("/Notifications/Get")
    void getNotifications(@Body GetNotificationsRequest request, Callback<GetNotificationsResponse> callback);
    @POST("/Notifications/Acknowledge")
    void acknowledgeNotifications(@Body AcknowledgeNotificationsRequest request, Callback<AcknowledgeNotificationsResponse> callback);
    @POST("/Trade/Accept")
    void acceptTrade(@Body AcceptTradeRequest request, Callback<AcceptTradeResponse> callback);
    @POST("/Trade/Decline")
    void declineTrade(@Body DeclineTradeRequest request, Callback<DeclineTradeResponse> callback);
    @POST("/Trade/Rate")
    void rateTrade(@Body RateTradeRequest request, Callback<RateTradeResponse> callback);
    @POST("/Trade/AcknowledgeChat")
    void acknowledgeChat(@Body AcknowledgeChatRequest request, Callback<AcknowledgeChatResponse> callback);
}

