package com.mathew.sensorlogin;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

public class EditSensorActivity extends AppCompatActivity {
    // base url for json calls
    private static final String base_url = "https://www.imonnit.com/json/";

    private String authToken;   // the user authorization token, passed on through activity
    private String sensorID;    // the sensor ID that we want to edit
    private boolean textChanged = false;    // flag - true makes a json call
    private int changeCount = 0;    // count so we can set the text box to display current data,
                                    // but not count as on changed
    private ProgressDialog prgDialog; // progress box when json takes too long
    private EditText sensorName; // field for the new sensor name

    /**
     * Create the Edit activity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_edit_sensor);

        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            authToken = extras.getString("token");
            sensorID = extras.getString("sensorID");
            getSupportActionBar().setTitle("EDIT SENSOR " + sensorID);

        }
        sensorName = findViewById(R.id.editName);
        sensorName.addTextChangedListener(filterTextWatcher);

        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        fillInData();



    }

    /*
     * Text change listener
     * Only change value if the text has been changed, otherwise finish the activity
     */
    private TextWatcher filterTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            textChanged = false;
        }

        @Override
        public void afterTextChanged(Editable s) {
            // setting the text causes it to be a text change
            // setting the flag only happens after the initial text change
            if(changeCount > 0)
            {
                textChanged = true;
            }
            changeCount++;
        }
    };
    /*
     * Fill in the current data so the user can see what they are going to change
     */
    private void fillInData() {

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
                    // set the text to the current name so the user has an easy
                    // time to adjust it, rather than writing it completely
                    sensorName.setText(name);


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

    /**
     * Set the new name of the sensor and finish the activity
     * @param view
     */
    public void updateSensor(View view) {

        //client
        AsyncHttpClient client = new AsyncHttpClient();

        //params
        String newSensorName = sensorName.getText().toString();
        if (!textChanged) {
            Toast.makeText(getApplicationContext(), "No Changes Found", Toast.LENGTH_LONG).show();

            return;
        }
        RequestParams params = new RequestParams();
        params.put("sensorID", sensorID);
        params.put("sensorName", newSensorName);
        client.get(base_url + "SensorSetName/" + authToken, params, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                //startSensorActivity();
                finish();
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

    /**
     * Finish the activity when the user selects the cancel button
     * @param view
     */
    public void cancelEdit(View view) {
        super.finish();
    }

    /**
     * Item listener for the menu on the title bar
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
     * Standard method for the back button
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
