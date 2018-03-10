package com.mathew.sensorlogin.Manage;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mathew.sensorlogin.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CreateNetworkActivity extends AppCompatActivity {

    // base url for json calls
    private static final String base_url = "https://www.imonnit.com/json/";

    private ProgressDialog prgDialog; //dialog
    private String authToken;
    private EditText networkName;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_network);
        getSupportActionBar().setTitle("CREATE NETWORK");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        //grab the tokens from the previous intent
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            authToken = extras.getString("token");
            userID = extras.getString("userID");
        }
        networkName = findViewById(R.id.editName);

    }

    /**
     * Clicking the back button on the title bar returns to the previous activity on the stack
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                prgDialog.dismiss();
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Method for back button on title bar
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }


    public void createNetwork(View view) {
        prgDialog.show();
        if(networkName == null || networkName.getText().toString().length() == 0)
        {
            prgDialog.hide();
            Toast.makeText(getApplicationContext(), "You must enter a name!", Toast.LENGTH_LONG).show();
            return;
        }
        final String name = networkName.getText().toString();
        //client
        AsyncHttpClient client = new AsyncHttpClient();
        prgDialog.show();
        //params
        RequestParams params = new RequestParams();
        params.put("name", name);
        client.get(base_url + "CreateNetwork/" + authToken, params, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                prgDialog.hide();
                Toast.makeText(getApplicationContext(), name + " was created!", Toast.LENGTH_LONG).show();
                addNetworkToCurrentUser(name);

            }

            public void onFailure(int statusCode, Throwable error, String content) {
                prgDialog.hide();
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occurred! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    /**
     * display the network data of the associated account
     * user to select which network to display gateways from
     */
    public void addNetworkToCurrentUser(final String name) {

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(base_url + "NetworkList/" + authToken, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                try {

                    JSONObject obj = new JSONObject(response);
                    JSONArray objArray = obj.getJSONArray("Result");

                    for (int i = 0; i < objArray.length(); i++) {
                        //grabs the object
                        JSONObject tempNetwork = objArray.getJSONObject(i);
                        if(name.equals(tempNetwork.getString("NetworkName")))
                        {
                            String ID = tempNetwork.getString("NetworkID");
                            addNetwork(ID);
                        }


                    }


                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "message: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            public void onFailure(int statusCode, Throwable error, String content) {

                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occurred! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });


    }
    private void addNetwork(final String networkID)
    {
        String networkParam = "Network_View_Net_" + networkID;

        AsyncHttpClient client = new AsyncHttpClient();

        //params
        RequestParams params = new RequestParams();
        params.put("custID", userID);
        params.put(networkParam, "on");
        client.get(base_url + "EditCustomerPermissions/" + authToken, params, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                JSONObject obj = null;
                try {
                    obj = new JSONObject(response);
                    String temp = obj.getString("Result");
                    if(temp.equals("Success"))
                    {
                        Toast.makeText(getApplicationContext(), networkID + " was added to user", Toast.LENGTH_LONG).show();
                        prgDialog.dismiss();
                        finish();

                    }else
                    {
                        Toast.makeText(getApplicationContext(), temp, Toast.LENGTH_LONG).show();
                        prgDialog.dismiss();
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }



            }

            public void onFailure(int statusCode, Throwable error, String content) {

                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occurred! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void cancel(View view) { super.finish();    }
    /**
     * What to do when the acitivty is destroyed
     */
    public void onDestroy() {

        super.onDestroy();
        prgDialog.dismiss();
        finish();
    }

    /**
     * what to do when the activity is paused
     */
    public void onPause()
    {
        super.onPause();
        prgDialog.dismiss();

    }
    
}
