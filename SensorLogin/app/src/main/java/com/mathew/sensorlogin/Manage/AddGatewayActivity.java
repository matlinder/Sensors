package com.mathew.sensorlogin.Manage;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
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

public class AddGatewayActivity extends AppCompatActivity {
    // base url for json calls
    private static final String base_url = "https://www.imonnit.com/json/";
    // Strings to store the information from the previous intent
    private String authToken, networkID, userID, networkName;
    private EditText gatewayID, gatewayCode;
    private HashMap<String, String> networkPair = new HashMap<String, String>();
    // list to store the names of the network
    private ArrayList<String> networkNames = new ArrayList<String>();
    private ProgressDialog prgDialog; //dialog
    private boolean spinnerFlag = false; // flag to know when spinner is selected
    private boolean repeat = false;
    ArrayAdapter<String> dataAdapter; // adapter for the spinner
    NiceSpinner spinner; // the spinner
    private Button cancel;
    private CheckBox multiCheck;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_gateway);
        getSupportActionBar().setTitle("ADD GATEWAY");
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

        spinner = findViewById(R.id.spinnerGateway);
        gatewayID = findViewById(R.id.gatewayID);
        gatewayCode = findViewById(R.id.gatewayCode);

        cancel = findViewById(R.id.cancelAddGateway);
        multiCheck = findViewById(R.id.multiCheckGateway2);
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

        networkNames.add("Select a Network");
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                // enter this if it is the first time selecting a spinner
                // change the prompt to be Clear All
                if(!spinnerFlag)
                {
                    networkNames.remove("Select a Network");
                    networkNames.add(0, "Cancel");
                    spinnerFlag = true;
                }

                //position--; // snafu to reduce the position because the prompt messed it up
                networkName = parent.getItemAtPosition((int)id).toString();
                if(!networkName.equals("Select a Network") && !networkName.equals("Cancel")) {
                    // display the associated sensors from the network
                    networkID = networkPair.get(networkName);

                }else
                {
                    // "Cancel All" was selected so just clear the table
                    cancelCreate(null);
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        displayNetworkData();

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

                    spinner.setAdapter(dataAdapter);

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

    public void addGateway(View view) {

        if(gatewayCode != null && gatewayID != null)
        {
            final String ID = gatewayID.getText().toString();
            final String code = gatewayCode.getText().toString();
            final String errorMsg = "GatewayID: " + ID + " could not be transfered to new network.";


            if(networkID == null)
            {
                Toast.makeText(getApplicationContext(), "You must select a network", Toast.LENGTH_LONG).show();
                return;
            }else if(ID.length() == 0)
            {
                Toast.makeText(getApplicationContext(), "You must enter a gateway ID", Toast.LENGTH_LONG).show();
                return;
            }else if (code.length() == 0)
            {
                Toast.makeText(getApplicationContext(), "You must enter a gateway code", Toast.LENGTH_LONG).show();
                return;
            }else
            {
                //params
                RequestParams params = new RequestParams();
                params.put("networkID", networkID);
                params.put("gatewayID", ID);
                params.put("checkDigit", code);

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
                                addGateway(null); //snafu need to call the method again since first time could be false negative
                            }else if(temp.equals("Success"))
                            {
                                Toast.makeText(getApplicationContext(), "Added the gateway to " + networkName, Toast.LENGTH_LONG).show();
                                repeat = false;
                                prgDialog.dismiss();
                                if(!multiCheck.isChecked()) {
                                    finish();
                                }
                                else
                                {
                                    gatewayID.setText("");
                                    gatewayCode.setText("");
                                    gatewayID.requestFocus();
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
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Something went wrong, restarting the activity", Toast.LENGTH_LONG).show();
            finish();
        }


    }

    public void cancelCreate(View view) {
        prgDialog.dismiss();
        finish();

    }
}
