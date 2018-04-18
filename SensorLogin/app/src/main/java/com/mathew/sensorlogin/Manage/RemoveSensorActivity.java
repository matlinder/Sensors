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


public class RemoveSensorActivity extends AppCompatActivity {
    // base url for json calls
    private static final String base_url = "https://www.imonnit.com/json/";
    private String authToken, userID, sensorName, sensorID, networkID;
    private HashMap<String, String> sensorPair = new HashMap<String, String>();
    private HashMap<String, String> networkPair = new HashMap<String, String>();
    // list to store the names of the network
    private ArrayList<String> networkNames = new ArrayList<String>();
    private ArrayList<String> sensorNames = new ArrayList<String>();
    // flag to know when spinner is selected
    private boolean spinnerFlagSensor = false;
    private boolean spinnerFlagNetwork = false;
    ArrayAdapter<String> dataAdapter; // adapter for the spinner
    NiceSpinner spinner, spinnerNetwork; // the spinner
    private ProgressDialog prgDialog; //dialog
    private HashMap<String, String> sensorDigitPair = new HashMap<String, String>();
    private Button cancel;
    private CheckBox multiCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_sensor);

        getSupportActionBar().setTitle("REMOVE SENSOR");
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
        }
        spinner = findViewById(R.id.spinner);
        sensorNames.add("Select a Sensor to Remove");
        networkNames.add("Select a Network");
        cancel = findViewById(R.id.cancel);
        multiCheck = findViewById(R.id.multiCheckRemove);
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
        spinnerNetwork = findViewById(R.id.spinner1);
        spinnerNetwork.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                // enter this if it is the first time selecting a spinner
                // change the prompt to be Clear All
                if(!spinnerFlagNetwork)
                {
                    networkNames.remove("Select a Network");
                    networkNames.add(0, "Cancel Edit");
                    spinnerFlagNetwork = true;
                }
                //position--; // snafu to reduce the position because the prompt messed it up
                String networkName = parent.getItemAtPosition((int)id).toString();
                if(!networkName.equals("Select a Network") && !networkName.equals("Cancel Edit")) {
                    // display the associated sensors from the network
                    networkID = networkPair.get(networkName);
                    displaySensorData(networkID);
                }else
                {
                    cancel(null);
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        displayNetworkData();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                // enter this if it is the first time selecting a spinner
                // change the prompt to be Clear All
                if(!spinnerFlagSensor)
                {
                    sensorNames.remove("Select a Sensor to Remove");
                    sensorNames.add(0, "Cancel Remove");
                    spinnerFlagSensor = true;
                }
                //position--; // snafu to reduce the position because the prompt messed it up
                sensorName = parent.getItemAtPosition((int)id).toString();
                if(!sensorName.equals("Select a Sensor to Remove") && !sensorName.equals("Cancel Remove")) {
                    // display the associated sensors from the network
                    sensorID = sensorPair.get(sensorName);
                }else
                {
                    cancel(null);
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

    private void displaySensorData(String _networkID) {
        prgDialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        //params
        RequestParams params = new RequestParams();
        params.put("networkID", _networkID);
        client.get(base_url + "SensorList/" + authToken, params, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                try {
                    prgDialog.hide();
                    JSONObject obj = new JSONObject(response);
                    JSONArray objArray = obj.getJSONArray("Result");
                    sensorPair.clear();
                    sensorDigitPair.clear();
                    sensorNames.clear();
                    sensorNames.add("Select a Sensor to Remove");
                    for (int i = 0; i < objArray.length(); i++) {
                        //grabs the object
                        JSONObject tempSensor = objArray.getJSONObject(i);
                        String name = tempSensor.getString("SensorName");
                        String ID = tempSensor.getString("SensorID");
                        String code = tempSensor.getString("CheckDigit");
                        sensorPair.put(name, ID);
                        sensorDigitPair.put(ID, code);
                        sensorNames.add(name);

                    }
                    dataAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item,sensorNames){};
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    //networkSpinner.setAdapter(dataAdapter);

                    spinner.setAdapter(dataAdapter);

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

    public void cancel(View view) {
        super.finish();
        prgDialog.dismiss();}
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

    public void confirmRemove(View view) {
        if(sensorID == null || sensorID.equals("Select a Sensor to Remove"))
        {
            Toast.makeText(getApplicationContext(), "You must select a Sensor", Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(RemoveSensorActivity.this);
        alert.setTitle("Delete");
        alert.setMessage("Are you sure you want to delete " + sensorName + "?");

        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                removeSensor();
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

    private void removeSensor() {
        //add the params to a RequestParams object
        //these will be used in the request
        RequestParams params = new RequestParams();
        params.put("sensorID", sensorID);
        prgDialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(base_url + "RemoveSensor/" + authToken, params, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                prgDialog.hide();
                try
                {
                    JSONObject obj = new JSONObject(response);
                    String temp = obj.getString("Result");
                    if(temp.equals("Success"))
                    {
                        Toast.makeText(getApplicationContext(), sensorName + " has been removed", Toast.LENGTH_LONG).show();
                        prgDialog.dismiss();
                        if(!multiCheck.isChecked())
                        {
                            finish();
                        }else
                        {
                            if(networkID != null)
                            {
                                displaySensorData(networkID);
                            }
                        }

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
}

