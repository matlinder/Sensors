package com.mathew.sensorlogin.Manage;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
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
 * Edit any sensor that has been added to the account
 */
public class EditAnySensorActivity extends AppCompatActivity {

    // base url for json calls
    private static final String base_url = "https://www.imonnit.com/json/";
    private String authToken, userID, sensorName, sensorID, networkName, networkID;
    private HashMap<String, String> sensorPair = new HashMap<String, String>();
    // list to store the names of the network
    private ArrayList<String> sensorNames = new ArrayList<String>();
    // flag to know when spinner is selected
    private boolean spinnerFlagSensor = false;
    private boolean spinnerFlagNetwork = false;
    ArrayAdapter<String> dataAdapter; // adapter for the spinner
    NiceSpinner spinner, spinnerNetwork; // the spinner
    private ProgressDialog prgDialog; //dialog
    private HashMap<String, String> sensorDigitPair = new HashMap<String, String>();
    // maps to store sensor and network pairs
    private HashMap<String, String> networkPair = new HashMap<String, String>();
    // list to store the names of the network
    private ArrayList<String> networkNames = new ArrayList<String>();
    private EditText sensorEditName, sensorHeartBeat;
    // variables used to track if changes have been made
    private boolean nameChanged = false;
    private boolean heartbeatChanged = false;
    private boolean temperatureChanged = false;
    private int nameChangeCount = 0;
    private int heartBeatChangeCount = 0;
    private int temperatureChangedCount = 0;
    // checkbox to edit muliple sensors, does not close application
    private CheckBox multiCheck;
    // cancel button
    private Button cancel;
    // temperature switcher button
    private Switch temperature;

    @Override
    /**
     * Creates the Edit Sensor Activity
     * Sets up the network spinner and the entry fields to change the name, heartbeat and the
     * temperature unit (F/C)
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_any_sensor);

        getSupportActionBar().setTitle("EDIT SENSOR");
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

        spinner = findViewById(R.id.spinnerGateway);
        sensorEditName = findViewById(R.id.editName);
        sensorEditName.addTextChangedListener(nameTextWatcher);
        sensorHeartBeat = findViewById(R.id.editHeartBeat);
        sensorHeartBeat.addTextChangedListener(heartbeatTextWatcher);
        sensorNames.add("Select a Sensor to Edit");
        networkNames.add("Select a Network");
        networkNames.add("All Networks");
        spinnerNetwork = findViewById(R.id.spinner2);
        multiCheck = findViewById(R.id.editCheckBox);
        cancel = findViewById(R.id.cancel);
        temperature = findViewById(R.id.tempSwitch);

        setupListeners();
        setupSpinners();

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

    /*
     * Text change listener
     * Only change value if the text has been changed, otherwise finish the activity
     */
    private TextWatcher nameTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            nameChanged = false;
        }

        @Override
        public void afterTextChanged(Editable s) {
            // setting the text causes it to be a text change
            // setting the flag only happens after the initial text change
            if(nameChangeCount > 0)
            {
                nameChanged = true;
            }
            nameChangeCount++;
        }
    };

    /*
     * Text change listener
     * Only change value if the text has been changed, otherwise finish the activity
     */
    private TextWatcher heartbeatTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            heartbeatChanged = false;
        }

        @Override
        public void afterTextChanged(Editable s) {
            // setting the text causes it to be a text change
            // setting the flag only happens after the initial text change
            if(heartBeatChangeCount > 0)
            {
                heartbeatChanged = true;
            }
            heartBeatChangeCount++;
        }
    };

    /*
     * When a sensor is selected, read the current information and put it into the text fields
     * so the user sees what is there currently and can make adjustments if needed.
     * Nothing will save unless information has been changed. That is what the watchers/listeners
     * are for.
     */
    private void populateEditFields(String sensorID) {
        //client
        AsyncHttpClient client = new AsyncHttpClient();

        //params
        RequestParams params = new RequestParams();
        params.put("sensorID", sensorID);
        client.get(base_url + "SensorGetExtended/" + authToken, params, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                try {
                    //grab the JSON object from the response
                    JSONObject obj = new JSONObject(response);
                    //grab the result array
                    JSONObject result = obj.getJSONObject("Result");
                    String name = result.getString("SensorName");
                    String heartbeat = result.getString("ReportInterval");
                    // set the text to the current name so the user has an easy
                    // time to adjust it, rather than writing it completely
                    sensorEditName.setText(name);
                    sensorHeartBeat.setText(heartbeat);


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
        client.get(base_url + "SensorAttributes/" + authToken, params, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                try {
                    //grab the JSON object from the response
                    JSONObject obj = new JSONObject(response);
                    //grab the result array
                    JSONArray objArray = obj.getJSONArray("Result");
                    for (int i = 0; i < objArray.length(); i++) {
                        //grabs the object
                        JSONObject tempObject = objArray.getJSONObject(i);

                        String name = tempObject.getString("Value");
                        if (name.equalsIgnoreCase("C")) {
                            temperature.setChecked(true);
                        } else {
                            temperature.setChecked(false);
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

    /*
     * Pull all the sensors from the chosen network and display them in a spinner
     * Populate the collections to link the names with the IDs and checkdigits, so all the info
     * needed to call methods to edit is already stored.
     */
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
                    sensorPair.clear();
                    sensorDigitPair.clear();
                    sensorNames.clear();
                    sensorNames.add("Select a Sensor to Edit");
                    JSONObject obj = new JSONObject(response);
                    JSONArray objArray = obj.getJSONArray("Result");

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
     * OnClick method to update the details of the selected sensor
     * Validates the information first, returns if no changes were made to the fields
     * Calls "SenorNameSet" "SensorHeartBeatSet" "SensorAttributeSet" if the conditions are met
     * Finishes the activity if the Multiple Entry checkbox is not selected
     *
     * @param view autogenerated param, not needed
     */
    public void updateSensor(View view) {
        if(!nameChanged && !heartbeatChanged && !temperatureChanged)
        {
            Toast.makeText(getApplicationContext(), "No changes made", Toast.LENGTH_LONG).show();
            return;
        }else if(sensorEditName == null || sensorEditName.length() == 0 || sensorHeartBeat == null || sensorHeartBeat.length() == 0)
        {
            Toast.makeText(getApplicationContext(), "Cannot have empty fields", Toast.LENGTH_LONG).show();
            return;
        }
        if(heartbeatChanged && sensorHeartBeat != null && sensorHeartBeat.length() != 0)
        {
            int heartbeat = Integer.parseInt(sensorHeartBeat.getText().toString());
            if(heartbeat >= 10)
            {
                //client
                AsyncHttpClient client = new AsyncHttpClient();
                //params
                RequestParams params = new RequestParams();
                params.put("sensorID", sensorID);
                params.put("reportInterval", sensorHeartBeat.getText().toString());
                params.put("activeStateInterval", "30");
                client.get(base_url + "SensorSetHeartbeat/" + authToken, params, new AsyncHttpResponseHandler() {

                    public void onSuccess(String response) {
                        prgDialog.hide();
                        try
                        {
                            JSONObject obj = new JSONObject(response);
                            String temp = obj.getString("Result");
                            if(temp.equals("Success"))
                            {
                                Toast.makeText(getApplicationContext(), "Changes saved", Toast.LENGTH_LONG).show();
                                prgDialog.dismiss();
                                if(!multiCheck.isChecked()) {
                                    finish();
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

                        // When Http response code is '404'
                        if (statusCode == 404) {
                            Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                        }
                        // When Http response code is '500'
                        else if (statusCode == 500) {
                            Toast.makeText(getApplicationContext(), "Something went wrong at server end ", Toast.LENGTH_LONG).show();
                        }
                        // When Http response code other than 404, 500
                        else {
                            Toast.makeText(getApplicationContext(), "Unexpected Error occurred! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }else
            {
                Toast.makeText(getApplicationContext(), "Hearbeat must be a minimum of 10 minutes", Toast.LENGTH_LONG).show();
            }
        }else if(sensorHeartBeat == null || sensorHeartBeat.length() == 0)
        {
            Toast.makeText(getApplicationContext(), "Cannot have empty heartbeat", Toast.LENGTH_LONG).show();

        }
        if(nameChanged && sensorEditName != null && sensorEditName.length() != 0)
        {
            //client
            AsyncHttpClient client = new AsyncHttpClient();

            //params
            String newSensorName = sensorEditName.getText().toString();

            RequestParams params = new RequestParams();
            params.put("sensorID", sensorID);
            params.put("sensorName", newSensorName);
            client.get(base_url + "SensorSetName/" + authToken, params, new AsyncHttpResponseHandler() {

                public void onSuccess(String response) {
                    prgDialog.hide();
                    try {
                        JSONObject obj = new JSONObject(response);
                        String temp = obj.getString("Result");
                        if(temp.equals("Success"))
                        {
                            Toast.makeText(getApplicationContext(), "Changes saved", Toast.LENGTH_LONG).show();
                            prgDialog.dismiss();
                            if(!multiCheck.isChecked()) {
                                finish();
                            }
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Something went wrong, no changes were made", Toast.LENGTH_LONG).show();
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
        }else if(sensorEditName == null || sensorEditName.length() == 0)
        {
            Toast.makeText(getApplicationContext(), "Cannot have an empty name", Toast.LENGTH_LONG).show();

        }

        //client
        AsyncHttpClient client = new AsyncHttpClient();

        //params


        RequestParams params2 = new RequestParams();
        params2.put("sensorID", sensorID);
        params2.put("name", "CorF");
        params2.put("value", temperature.isChecked() ? "C" : "F");
        client.get(base_url + "SensorAttributeSet/" + authToken, params2, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                prgDialog.hide();
                displaySensorData(networkID);
                if(!multiCheck.isChecked()) {
                    prgDialog.dismiss();
                    finish();
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

        if(networkID != null) {
            displaySensorData(networkID);
            sensorEditName.setText("");
            sensorHeartBeat.setText("");
            sensorEditName.requestFocus();
        }

    }

    /*
     * method to set up the listeners for the fields
     * if the field is changed, then the flag is changed and the button is allowed to be
     * pressed to follow through with the edit
     */
    private void setupListeners() {
        temperature.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                if(temperatureChangedCount > 0) {
                    temperatureChanged = !temperatureChanged;
                }
                temperatureChangedCount++;
            }
        });


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
    }

    /*
     * method to set up the spinners for the network and for the sensors
     * calls the appropriate method when a network and a sensor is clicked
     */
    private void setupSpinners() {
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
                    cancelEdit(null);
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
                    sensorNames.remove("Select a Sensor to Edit");
                    sensorNames.add(0, "Cancel Edit");
                    spinnerFlagSensor = true;
                }
                //position--; // snafu to reduce the position because the prompt messed it up
                sensorName = parent.getItemAtPosition((int)id).toString();
                if(!sensorName.equals("Select a Sensor to Edit") && !sensorName.equals("Cancel Edit")) {
                    // display the associated sensors from the network
                    sensorID = sensorPair.get(sensorName);
                    populateEditFields(sensorID);
                }else
                {
                    cancelEdit(null);
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * OnClick for the cancel button
     * Finishes the activity
     * @param view
     */
    public void cancelEdit(View view) {
        prgDialog.dismiss();
        finish();
    }

    @Override
    /**
     * Menu selection method to determine what happens when a specific menu item is selected
     * This activity only has a home button, meaning the top left arrow to go back to the previous
     * activity on the stack
     */
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
     * Standard method for the menu to create it
     * @param menu
     * @return
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    /**
     * finish the activity and close the dialog
     * needed this because of leaked activity
     */
    public void onDestroy() {

        super.onDestroy();
        prgDialog.dismiss();
        finish();
    }

    /**
     * dismiss the dialog so that we do not have a leaked activity
     */
    public void onPause()
    {
        super.onPause();
        prgDialog.dismiss();
        finish();
    }
}
