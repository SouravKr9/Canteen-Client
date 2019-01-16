package cvrce.cvrce.com.cvrcecanteen;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.paytm.pgsdk.PaytmMerchant;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static java.security.AccessController.getContext;

public class CartActivity extends AppCompatActivity implements PaytmPaymentTransactionCallback {


    HashMap<String, Integer> quantity;
    HashMap<String, Integer> price;
    HashSet<String> product;
    ArrayList<Integer> amount;
    int cost=0;
    //Button payBtn;
    ListView listCart;
    //View view = findViewById(R.id.activity_cart);
    CartAdapter cartAdapter;
    Object[] prod_name = new Object[]{};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        //Bundle cart = getIntent().getExtras();

        Log.d("cartdebug", "Inside Cart Activity");
        //Log.d("cartdebug", cart.toString());

        quantity = new HashMap<>();
        price = new HashMap<>();
        amount = new ArrayList<>();
        listCart = findViewById(R.id.cart_list);


        product = (HashSet<String>) getIntent().getSerializableExtra("product");
        quantity = (HashMap<String, Integer>) getIntent().getSerializableExtra("quantity");
        price = (HashMap<String, Integer>) getIntent().getSerializableExtra("price");

        Log.d("cartdebug", quantity.toString());
        Log.d("cartdebug", price.toString());
        Log.d("cartdebug", product.toString());

        cartAdapter = new CartAdapter(product, quantity, price, amount, cost, getApplicationContext());
        listCart.setAdapter(cartAdapter);

        //payBtn = (Button)view.findViewById(R.id.pay_button);
        //payBtn.setText("Pay "+cost);

        findViewById(R.id.pay_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prod_name = product.toArray();
                int len = prod_name.length;
                for(int i=0;i<len;i++){
                    cost+=((quantity.get(prod_name[i]))*(price.get(prod_name[i])));
                }
                generateChecksum();
            }
        });

    }

    private void generateChecksum(){

        String tempCost = cost+" ";

        String txnAmount = tempCost.trim();
        Log.d("paytmdebug",tempCost);

        //creating a retrofit object.
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Api.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //creating the retrofit api service
        Api apiService = retrofit.create(Api.class);

        //creating paytm object
        //containing all the values required
        String custId = generateString();
        String orderId = generateString();
       // String callbackURL = Constants.CALLBACK_URL.concat(orderId);
        String callbackURL = Constants.CALLBACK_URL;
        final Paytm paytm = new Paytm(
                Constants.M_ID,
                orderId,
                custId,
                Constants.CHANNEL_ID,
                txnAmount,
                Constants.WEBSITE,
                callbackURL,
                Constants.INDUSTRY_TYPE_ID
        );

        //creating a call object from the apiService
        Call<Checksum> call = apiService.getChecksum(
                paytm.getmId(),
                paytm.getOrderId(),
                paytm.getCustId(),
                paytm.getChannelId(),
                paytm.getTxnAmount(),
                paytm.getWebsite(),
                paytm.getCallBackUrl(),
                paytm.getIndustryTypeId()
        );

        //Log.d("paytmdebug",pay);

        //making the call to generate checksum
        call.enqueue(new Callback<Checksum>() {
            @Override
            public void onResponse(Call<Checksum> call, Response<Checksum> response) {

                //once we get the checksum we will initiailize the payment.
                //the method is taking the checksum we got and the paytm object as the parameter
                Log.d("paytmdebug",response.toString());

                //String checksumHash = "GhAJV057opOCD3KJuVWesQ9pUxMtyUGLPAiIRtkEQXBeSws2hYvxaj7jRn33rTYGRLx2TosFkgReyCslu4OUj/A85AvNC6E4wUP+CZnrBGM=";

                initializePaytmPayment(response.body().getChecksumHash(), paytm);
                //initializePaytmPayment(checksumHash, paytm);
            }

            @Override
            public void onFailure(Call<Checksum> call, Throwable t) {

            }
        });

//        PaytmMerchant Merchant = new PaytmMerchant(
//                "https://pguat.paytm.com/paytmchecksum/paytmCheckSumGenerator.jsp",
//                "https://pguat.paytm.com/paytmchecksum/paytmCheckSumVerify.jsp");
//
//        initializePaytmPayment(Merchant, paytm);
    }

    private String generateString() {
        String uuid = UUID.randomUUID().toString();
        //Log.d("paytmdebug",uuid);
        return uuid.replaceAll("-", "");
    }

    private void initializePaytmPayment(String checksumHash, Paytm paytm) {

        //getting paytm service
        PaytmPGService Service = PaytmPGService.getStagingService();

        //use this when using for production
        //PaytmPGService Service = PaytmPGService.getProductionService();

        //creating a hashmap and adding all the values required
        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("MID", Constants.M_ID);
        paramMap.put("ORDER_ID", "asgasfgasfsdfhl7");
        paramMap.put("CUST_ID", paytm.getCustId());
        paramMap.put("CHANNEL_ID", paytm.getChannelId());
        paramMap.put("TXN_AMOUNT", paytm.getTxnAmount());
        paramMap.put("WEBSITE", paytm.getWebsite());
        paramMap.put("CALLBACK_URL", paytm.getCallBackUrl());
        paramMap.put("CHECKSUMHASH", checksumHash);
        paramMap.put("INDUSTRY_TYPE_ID", paytm.getIndustryTypeId());


        //creating a paytm order object using the hashmap
        PaytmOrder order = new PaytmOrder(paramMap);

        //intializing the paytm service
        Service.initialize(order,  null);

        //finally starting the payment transaction
        Service.startPaymentTransaction(this, true, true, this);

    }

    //all these overriden method is to detect the payment result accordingly
    @Override
    public void onTransactionResponse(Bundle bundle) {

        Toast.makeText(this, bundle.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void networkNotAvailable() {
        Toast.makeText(this, "Network error", Toast.LENGTH_LONG).show();
    }

    @Override
    public void clientAuthenticationFailed(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void someUIErrorOccurred(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onErrorLoadingWebPage(int i, String s, String s1) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressedCancelTransaction() {
        Toast.makeText(this, "Back Pressed", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTransactionCancel(String s, Bundle bundle) {
        Toast.makeText(this, s + bundle.toString(), Toast.LENGTH_LONG).show();
    }

    private class CartAdapter extends BaseAdapter {

        HashSet<String> product = new HashSet<>();
        HashMap<String, Integer> price;
        HashMap<String, Integer> quantity;
        ArrayList<Integer> amount;
        private Context context;
        int cost;
        Object [] p = new Object[]{};


        public CartAdapter(HashSet<String> product,HashMap<String, Integer> price, HashMap<String, Integer> quantity, ArrayList<Integer> amount, int cost, Context context) {
            this.product = product;
            this.price = price;
            this.quantity = quantity;
            this.amount = amount;
            this.cost = cost;
            this.context = context;

            this.p = product.toArray();

            //Log.d("cartdebug", p.toString());


        }

        //Object[] p = product.toArray();

        @Override
        public int getCount() {
            return price.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.cart_item_list_layout, null);

            //Log.d("cartdebug", p.toString());

            TextView product_name = (TextView) convertView.findViewById(R.id.cart_item_name);
            product_name.setText(p[position].toString());

            TextView product_quantity = (TextView) convertView.findViewById(R.id.cart_item_quantity);
            product_quantity.setText(quantity.get(p[position]).toString());

            TextView product_amount = (TextView) convertView.findViewById(R.id.cart_item_amount);
            product_amount.setText(((quantity.get(p[position]))*(price.get(p[position])))+" ");

            return convertView;
        }
    }
}
