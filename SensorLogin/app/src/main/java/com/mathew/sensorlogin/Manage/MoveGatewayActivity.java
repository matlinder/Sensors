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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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

public class MoveGatewayActivity extends AppCompatActivity {
    // base url for json calls
    private static final String base_url = "https://www.imonnit.com/json/";
    private String networkID, gatewayID, gatewayName, networkName;
    private String authToken;
    private String userID;
    private NiceSpinner spinnerNetworkFrom, spinnerGateway, spinnerNetworkTo; // the spinner
    // maps to store sensor and network pairs
    private HashMap<String, String> networkPair = new HashMap<String, String>();
    // list to store the names of the network
    private ArrayList<String> networkNames = new ArrayList<String>();
    private HashMap<String, String> networkPairFrom = new HashMap<String, String>();
    // list to store the names of the network
    private ArrayList<String> networkNamesFrom = new ArrayList<String>();
    // maps to store sensor and network pairs
    private HashMap<String, String> gatewayPair = new HashMap<String, String>();
    private HashMap<String, String> gatewayCheckDigits = new HashMap<String, String>();
    // list to store the names of the network
    private ArrayList<String> gatewayNames = new ArrayList<String>();
    private ProgressDialog prgDialog; //dialog
    private boolean spinnerFlagNetwork = false; // flag to know when spinner is selected
    private boolean spinnerFlagGateway = false; // flag to know when spinner is selected
    private boolean displayNetworkData = false;
    private boolean repeat = false;
    ArrayAdapter<String> dataAdapter; // adapter for the spinner

    private Button cancel, moveGateway;
    private CheckBox multiCheck;
    private boolean spinnerFlag = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move_gateway);
        getSupportActionBar().setTitle("MOVE GATEWAY");
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

        moveGateway = findViewById(R.id.moveGateway);
        cancel = findViewById(R.id.cancelMoveGateway);
        multiCheck = findViewById(R.id.multiCheckGateway);
        multiCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if(isChecked)
                {
                    cancel.setText("DONE");
                }else
                {
                    cancel.setText("CANCEL");
                }
            }
        });

        spinnerNetworkFrom = findViewById(R.id.spinnerNetworkStart);
        spinnerGateway = findViewById(R.id.spinnerGateway);
        spinnerNetworkTo = findViewById(R.id.spinnerNetworkEnd);
        networkNamesFrom.add("Select a Network");
        networkNamesFrom.add("All Networks");
        gatewayNames.add("Select a Gateway");
        networkNames.add("Select a new Network");
        displayNetworkDataSpinnerFrom();

        spinnerNetworkFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                // enter this if it is the first time selecting a spinner
                // change the prompt to be Clear All
                if(!spinnerFlagNetwork)
                {
                    networkNamesFrom.remove("Select a Network");
                    networkNamesFrom.add(0, "Cancel");
                    spinnerFlagNetwork = true;
                }
                //position--; // snafu to reduce the position because the prompt messed it up

                String networkName = parent.getItemAtPosition((int)id).toString();
                if(!networkName.equals("Select a Network") && !networkName.equals("Cancel")) {
                    // display the associated sensors from the network
//                    networkID = networkPairFrom.get(networkName);
                    displayGatewayData(networkPairFrom.get(networkName));
                }else
                {
                    cancelMove(null);
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerGateway.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                // enter this if it is the first time selecting a spinner
                // change the prompt to be Clear All
                if(!spinnerFlagGateway)
                {
                    gatewayNames.remove("Select a Gateway");
                    gatewayNames.add(0, "Cancel");
                    spinnerFlagGateway = true;
                }
                if(gatewayNames.get(0).equalsIgnoreCase("Select a Gateway"))
                {
                    gatewayNames.remove("Select a Gateway");
                    gatewayNames.add(0, "Cancel");
                }
                //position--; // snafu to reduce the position because the prompt messed it up
                gatewayName = parent.getItemAtPosition((int)id).toString();
                if(!gatewayName.equals("Select a Gateway") && !gatewayName.equals("Cancel")) {
                    // display the associated sensors from the network
                    gatewayID = gatewayPair.get(gatewayName);

                    if(!displayNetworkData)
                    {
                        displayNetworkData = !displayNetworkData;
                        displayNetworkData();
                    }

                }else
                {
                    cancelMove(null);
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinnerNetworkTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                // enter this if it is the first time selecting a spinner
                // change the prompt to be Clear All
                if(!spinnerFlag)
                {
                    networkNames.remove("Select a new Network");
                    networkNames.add(0, "Cancel");
                    spinnerFlag = true;
                }

                //position--; // snafu to reduce the position because the prompt messed it up
                networkName = parent.getItemAtPosition((int)id).toString();
                if(!networkName.equals("Select a new Network") && !networkName.equals("Cancel")) {
                    // display the associated sensors from the network
                    networkID = networkPair.get(networkName);
                }else {

                    // "Cancel All" was selected so just clear the table
                    cancelMove(null);
                }

            }
            public void onNothingSelected(AdapterView<?> parent) {
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

                    spinnerNetworkTo.setAdapter(dataAdapter);

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

                    gatewayNames.clear();
                    gatewayPair.clear();
                    //networkPair.clear();
                    //networkNames.clear();
                    gatewayNames.add("Select a Gateway");
                    //networkNames.add("Select a new Network");
                    //how to clear a spinner or reset it ???

                    JSONObject obj = new JSONObject(response);
                    JSONArray objArray = obj.getJSONArray("Result");

                    for (int i = 0; i < objArray.length(); i++) {
                        //grabs the object
                        JSONObject tempGateway = objArray.getJSONObject(i);
                        String name = tempGateway.getString("Name");
                        String ID = tempGateway.getString("GatewayID");
                        String checkDigit = tempGateway.getString("CheckDigit");
                        gatewayCheckDigits.put(ID, checkDigit);
                        gatewayPair.put(name, ID);
                        gatewayNames.add(name);

                    }
                    dataAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item,gatewayNames){};
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //networkSpinner.setAdapter(dataAdapter);

                    spinnerGateway.setAdapter(dataAdapter);

//                    if(spinnerClearFlag) {
//                        dataAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, networkNames) {
//                        };
//                        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                        //networkSpinner.setAdapter(dataAdapter);
//
//                        spinnerNetworkTo.setAdapter(dataAdapter);
//                    }
//                    spinnerClearFlag = true;
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
    public void displayNetworkDataSpinnerFrom() {
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
                        networkPairFrom.put(name, ID);
                        networkNamesFrom.add(name);

                    }
                    dataAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item,networkNamesFrom){};
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //networkSpinner.setAdapter(dataAdapter);

                    spinnerNetworkFrom.setAdapter(dataAdapter);

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
    public void confirmMove(View view) {

        if(gatewayID == null || networkID == null)
        {
            Toast.makeText(getApplicationContext(), "Something went wrong, try to restart", Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(MoveGatewayActivity.this);
        alert.setTitle("Move");
        alert.setMessage("Are you sure you want to move " + gatewayName + "to " + networkName + "?");

        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                moveGateway();
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

    private void moveGateway() {

        final String errorMsg = "GatewayID: " + gatewayID + " could not be transfered to new network.";

        //params
        RequestParams params = new RequestParams();
        params.put("networkID", networkID);
        params.put("gatewayID", gatewayID);
        params.put("checkDigit", gatewayCheckDigits.get(gatewayID));

        prgDialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(base_url + "AssignGateway/" + authToken, params, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                prgDialog.hide();
                try {
                    JSONObject obj = new JSONObject(response);
                    String temp = obj.getString("Result");
                    if(temp.equals(errorMsg) && !repeat)
                    {
                        Toast.makeText(getApplicationContext(), "Running again ", Toast.LENGTH_LONG).show();
                        repeat = true;
                        moveGateway(); //snafu need to call the method again since first time could be false negative
                    }else if(temp.equals("Success"))
                    {
                        Toast.makeText(getApplicationContext(), "Move the gateway to " + networkName, Toast.LENGTH_LONG).show();
                        repeat = false;
                        prgDialog.dismiss();
                        if(!multiCheck.isChecked()) {
                            finish();
                        }
                        else
                        {
                            displayGatewayData(networkPairFrom.get(networkName));
                        }
                    }
                    else
                    {
                        repeat = false;
                        Toast.makeText(getApplicationContext(), "ID or Code is incorrect", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
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

    public void cancelMove(View view) {
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
