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

public class MoveSensorActivity extends AppCompatActivity {
    // base url for json calls
    private static final String base_url = "https://www.imonnit.com/json/";
    private String authToken, networkID, userID, networkName, sensorName, sensorID, networkNameFrom, networkIDFrom;
    private HashMap<String, String> networkPair = new HashMap<String, String>();
    // list to store the names of the network
    private ArrayList<String> networkNames = new ArrayList<String>();
    private HashMap<String, String> networkPairFrom = new HashMap<String, String>();
    // list to store the names of the network
    private ArrayList<String> networkNamesFrom = new ArrayList<String>();
    private HashMap<String, String> sensorPair = new HashMap<String, String>();
    // list to store the names of the network
    private ArrayList<String> sensorNames = new ArrayList<String>();

    private boolean spinnerFlag = false; // flag to know when spinner is selected
    private boolean spinnerFlagSensor = false;
    private boolean spinnerFlagFrom = false;
    private boolean displayNetworkData = false;
    ArrayAdapter<String> dataAdapter; // adapter for the spinner
    NiceSpinner spinner, spinnerSensor, spinnerNetworkFrom; // the spinner
    private ProgressDialog prgDialog; //dialog
    private HashMap<String, String> sensorDigitPair = new HashMap<String, String>();
    private CheckBox multiCheck;
    private Button cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move_sensor);
        getSupportActionBar().setTitle("MOVE SENSOR");
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

        spinner = findViewById(R.id.spinner2);
        spinnerNetworkFrom = findViewById(R.id.spinner1);
        spinnerSensor = findViewById(R.id.spinnerGateway);
        networkNamesFrom.add("Select a Network");
        networkNamesFrom.add("All Networks");
        networkNames.add("Select a new Network");
        sensorNames.add("Select a Sensor to Move");
        cancel = findViewById(R.id.cancel);
        multiCheck = findViewById(R.id.multiCheck);
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
        displayNetworkDataSpinnerFrom();

        spinnerNetworkFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                // enter this if it is the first time selecting a spinner
                // change the prompt to be Clear All
                if(!spinnerFlagFrom)
                {
                    networkNamesFrom.remove("Select a Network");
                    networkNamesFrom.add(0, "Cancel");
                    spinnerFlagFrom = true;

                }

                //id--; // snafu to reduce the position because the prompt messed it up
                networkNameFrom = parent.getItemAtPosition((int)id).toString();
                if(!networkNameFrom.equals("Select a new Network") && !networkNameFrom.equals("Cancel")) {
                    // display the associated sensors from the network
                    networkIDFrom = networkPairFrom.get(networkNameFrom);
                    displaySensorData(networkIDFrom);

                }else
                {
                    // "Cancel All" was selected so just clear the table
                    cancelMove(null);
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
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


        spinnerSensor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                // enter this if it is the first time selecting a spinner
                // change the prompt to be Clear All
                if(!spinnerFlagSensor)
                {
                    sensorNames.remove("Select a Sensor to Move");
                    sensorNames.add(0, "Cancel");
                    spinnerFlagSensor = true;
                }
                if(sensorNames.get(0).equalsIgnoreCase("Select a Sensor to Move"))
                {
                    sensorNames.remove("Select a Sensor to Move");
                    sensorNames.add(0, "Cancel");
                }

                sensorName = parent.getItemAtPosition((int)id).toString();
                if(!sensorName.equals("Select a Sensor to Move") && !sensorName.equals("Cancel")) {
                    // display the associated sensors from the network
                    sensorID = sensorPair.get(sensorName);
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
                    sensorNames.add("Select a Sensor to Move");
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

                    spinnerSensor.setAdapter(dataAdapter);

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




    public void confirmMove(View view) {
        if(sensorID == null || sensorID.length() == 0 )
        {
            Toast.makeText(getApplicationContext(), "You must select a sensor to move", Toast.LENGTH_LONG).show();
            return;
        }else if( networkID == null || networkID.length() == 0)
        {
            Toast.makeText(getApplicationContext(), "You must select a network", Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(MoveSensorActivity.this);
        alert.setTitle("Move Sensor");
        alert.setMessage("Are you sure you want to move sensor " + sensorName + " to " + networkName + "?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                moveSensor();
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

    public void moveSensor()
    {
        prgDialog.show();
        //params
        String code = sensorDigitPair.get(sensorID);
        RequestParams params = new RequestParams();
        params.put("networkID", networkID);
        params.put("sensorID", sensorID);
        params.put("checkDigit", code);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(base_url + "AssignSensor/" + authToken, params, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                prgDialog.hide();
                try {
                    JSONObject obj = new JSONObject(response);
                    String temp = obj.getString("Result");
                    if(temp.equals("Success"))
                    {
                        Toast.makeText(getApplicationContext(), "Moved the sensor to " + networkName, Toast.LENGTH_LONG).show();
                        prgDialog.dismiss();
                        if(!multiCheck.isChecked()) {
                            finish();
                        }else
                        {
                            displaySensorData(networkIDFrom);
                        }
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Sensor was not moved", Toast.LENGTH_LONG).show();
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


    public void cancelMove(View view) {
        super.finish();
        prgDialog.dismiss();
    }
}
