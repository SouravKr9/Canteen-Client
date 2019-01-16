package cvrce.cvrce.com.cvrcecanteen;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

public class Paytm {

    @SerializedName("MID")
    String mId;

    @SerializedName("ORDER_ID")
    String orderId;

    @SerializedName("CUST_ID")
    String custId;

    @SerializedName("CHANNEL_ID")
    String channelId;

    @SerializedName("TXN_AMOUNT")
    String txnAmount;

    @SerializedName("WEBSITE")
    String website;

    @SerializedName("CALLBACK_URL")
    String callBackUrl;

    @SerializedName("INDUSTRY_TYPE_ID")
    String industryTypeId;

    public Paytm(String mId, String orderId, String custId, String channelId, String txnAmount, String website, String callBackUrl, String industryTypeId) {
        this.mId = mId;
        this.orderId = orderId;
        this.custId = custId;
        this.channelId = channelId;
        this.txnAmount = txnAmount;
        this.website = website;
        this.callBackUrl = callBackUrl;
        this.industryTypeId = industryTypeId;

        website.concat(orderId);

        Log.d("paytmdebug", mId);
        Log.d("paytmdebug", orderId);
        Log.d("paytmdebug", custId);
        Log.d("paytmdebug", channelId);
        Log.d("paytmdebug", txnAmount);
        Log.d("paytmdebug", website);
        Log.d("paytmdebug", callBackUrl);
        Log.d("paytmdebug", industryTypeId);


    }

    public String getmId() {
        return mId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustId() {
        return custId;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getTxnAmount() {
        return txnAmount;
    }

    public String getWebsite() {
        return website;
    }

    public String getCallBackUrl() {
        return callBackUrl;
    }

    public String getIndustryTypeId() {
        return industryTypeId;
    }

    /*
     * The following method we are using to generate a random string everytime
     * As we need a unique customer id and order id everytime
     * For real scenario you can implement it with your own application logic
     * */
    private String generateString() {
        String uuid = UUID.randomUUID().toString();
        //Log.d("paytmdebug",uuid);
        return uuid.replaceAll("-", "");
    }
}
