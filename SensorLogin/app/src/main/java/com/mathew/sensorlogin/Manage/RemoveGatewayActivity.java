package com.mathew.sensorlogin.Manage;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mathew.sensorlogin.R;

import org.angmarch.views.NiceSpinner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Activity to remove a gateway from a specified network
 */
public class RemoveGatewayActivity extends AppCompatActivity {
    // base url for json calls
    private static final String base_url = "https://www.imonnit.com/json/";
    private String networkID, gatewayID, gatewayName;
    private String authToken;
    private String userID;
    private NiceSpinner spinnerNetwork, spinnerGateway; // the spinner
    // maps to store sensor and network pairs
    private HashMap<String, String> networkPair = new HashMap<String, String>();
    // list to store the names of the network
    private ArrayList<String> networkNames = new ArrayList<String>();
    // maps to store sensor and network pairs
    private HashMap<String, String> gatewayPair = new HashMap<String, String>();
    // list to store the names of the network
    private ArrayList<String> gatewayNames = new ArrayList<String>();
    private ProgressDialog prgDialog; //dialog
    private boolean spinnerFlagNetwork = false; // flag to know when spinner is selected
    private boolean spinnerFlagGateway = false; // flag to know when spinner is selected
    ArrayAdapter<String> dataAdapter; // adapter for the spinner

    @Override
    /**
     * OnCreate method to set up the layout and populate the spinners
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_gateway);
        getSupportActionBar().setTitle("REMOVE GATEWAY");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //grab the token from the previous intent
        Bundle extras = this.getIntent().getExtras();
        if(extras != null)
        {
            authToken = extras.getString("token");
            userID = extras.getString("userID");
        }
        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        spinnerNetwork = findViewById(R.id.spinnerGateway);
        spinnerGateway = findViewById(R.id.spinner2);
        networkNames.add("Select a Network");
        gatewayNames.add("Select a Gateway");
        spinnerNetwork.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                // enter this if it is the first time selecting a spinner
                // change the prompt to be Clear All
                if(!spinnerFlagNetwork)
                {
                    networkNames.remove("Select a Network");
                    networkNames.add(0, "Cancel Remove");
                    spinnerFlagNetwork = true;
                }
                //position--; // snafu to reduce the position because the prompt messed it up
                String networkName = parent.getItemAtPosition((int)id).toString();
                if(!networkName.equals("Select a Network") && !networkName.equals("Cancel Remove")) {
                    // display the associated sensors from the network
                    networkID = networkPair.get(networkName);
                    displayGatewayData(networkID);
                }else
                {
                    cancelRemove(null);
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        displayNetworkData();
        spinnerGateway.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                // enter this if it is the first time selecting a spinner
                // change the prompt to be Clear All
                if(!spinnerFlagGateway)
                {
                    gatewayNames.remove("Select a Gateway");
                    gatewayNames.add(0, "Cancel Remove");
                    spinnerFlagGateway = true;
                }

                //position--; // snafu to reduce the position because the prompt messed it up
                gatewayName = parent.getItemAtPosition((int)id).toString();
                if(!gatewayName.equals("Select a Gateway") && !gatewayName.equals("Cancel Remove")) {
                    // display the associated sensors from the network
                    gatewayID = gatewayPair.get(gatewayName);

                }else
                {
                    cancelRemove(null);
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * Populates the spinner with gateways that are associated with the selected network
     * @param networkID - gateways that belong to this network ID will be displayed in the spinner
     */
    private void displayGatewayData(String networkID) {
        prgDialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        //grab a specific gateway to display
        RequestParams params = new RequestParams();
        params.put("networkID", networkID);
        client.get(base_url + "GatewayList/" + authToken, params, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                try {
                    prgDialog.hide();
                    JSONObject obj = new JSONObject(response);
                    JSONArray objArray = obj.getJSONArray("Result");

                    for (int i = 0; i < objArray.length(); i++) {
                        //grabs the object
                        JSONObject tempGateway = objArray.getJSONObject(i);
                        String name = tempGateway.getString("Name");
                        String ID = tempGateway.getString("GatewayID");
                        gatewayPair.put(name, ID);
                        gatewayNames.add(name);

                    }
                    dataAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item,gatewayNames){};
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //networkSpinner.setAdapter(dataAdapter);

                    spinnerGateway.setAdapter(dataAdapter);

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error occurred could not process data!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
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
    public void displayNetworkData() {
        prgDialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(base_url + "NetworkList/" + authToken, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                try {
                    prgDialog.hide();
                    JSONObject obj = new JSONObject(response);
                    JSONArray objArray = obj.getJSONArray("Result");

                    for (int i = 0; i < objArray.length(); i++) {
                        //grabs the object
                        JSONObject tempNetwork = objArray.getJSONObject(i);
                        String name = tempNetwork.getString("NetworkName");
                        String ID = tempNetwork.getString("NetworkID");
                        networkPair.put(name, ID);
                        networkNames.add(name);

                    }
                    dataAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item,networkNames){};
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //networkSpinner.setAdapter(dataAdapter);

                    spinnerNetwork.setAdapter(dataAdapter);

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "message: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
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
     * A gateway may be active and reading data, so the user is prompted before the final removal
     * to confirm if they want to remove the selected gateway
     * @param view
     */
    public void confirmRemove(View view) {
        if(networkID == null || networkID.equals("Select a Network"))
        {
            Toast.makeText(getApplicationContext(), "You must select a network", Toast.LENGTH_LONG).show();
            return;
        }
        if(gatewayID == null || gatewayID.length() == 0)
        {
            Toast.makeText(getApplicationContext(), "You must select a gateway", Toast.LENGTH_LONG).show();
            return;
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(RemoveGatewayActivity.this);
        alert.setTitle("Delete");
        alert.setMessage("Are you sure you want to delete " + gatewayName + "?");

        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                removeGateway();
                dialog.dismiss();
            }
        });

        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    /**
     * Method to remove the specified gateway from the current network that owns it
     * Calls RemoveGateway
     */
    public void removeGateway()
    {
        if(networkID == null || networkID.equals("Select a Network"))
        {
            Toast.makeText(getApplicationContext(), "You must select a network", Toast.LENGTH_LONG).show();
            return;
        }
        if(gatewayID == null || gatewayID.length() == 0)
        {
            Toast.makeText(getApplicationContext(), "You must select a gateway", Toast.LENGTH_LONG).show();
            return;
        }

        //add the params to a RequestParams object
        //these will be used in the request
        RequestParams params = new RequestParams();
        params.put("gatewayID", gatewayID);
        prgDialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(base_url + "RemoveGateway/" + authToken, params, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                prgDialog.hide();
                try
                {
                    JSONObject obj = new JSONObject(response);
                    String temp = obj.getString("Result");
                    if(temp.equals("Success"))
                    {
                        Toast.makeText(getApplicationContext(), gatewayName + " has been removed", Toast.LENGTH_LONG).show();
                        prgDialog.dismiss();
                        finish();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Something went wrong, no changes were made", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
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

    /**
     * OnClick method for when the cancel button is pressed, finish the activity
     * @param view
     */
    public void cancelRemove(View view) {
        prgDialog.dismiss();
        finish();
    }

    /**
     * What to do when the acitivty is destroyed
     */
    public void onDestroy() {

        super.onDestroy();
        prgDialog.dismiss();
        finish();
    }

}