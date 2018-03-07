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
import android.widget.TextView;
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

public class RemoveNetworkActivity extends AppCompatActivity {
    // base url for json calls
    private static final String base_url = "https://www.imonnit.com/json/";
    private String authToken, networkID;
    private String userID;
    private NiceSpinner spinner; // the spinner
    // maps to store sensor and network pairs
    private HashMap<String, String> networkPair = new HashMap<String, String>();
    // list to store the names of the network
    private ArrayList<String> networkNames = new ArrayList<String>();
    private ProgressDialog prgDialog; //dialog
    private TextView networkPrompt; // prompt to tell the user to select a network
    private boolean spinnerFlag = false; // flag to know when spinner is selected
    ArrayAdapter<String> dataAdapter; // adapter for the spinner

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_network);
        getSupportActionBar().setTitle("REMOVE NETWORK");
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

        spinner = findViewById(R.id.spinner);
        networkNames.add("Select a Network");
        networkPrompt = findViewById(R.id.networkPrompt);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                // enter this if it is the first time selecting a spinner
                // change the prompt to be Clear All
                if(!spinnerFlag)
                {
                    networkNames.remove("Select a Network");
                    networkNames.add(0, "Cancel Remove");
                    spinnerFlag = true;
                }
                if(position != 0)
                {
                    position--; // snafu to reduce the position because the prompt messed it up
                    String networkName = parent.getItemAtPosition(position).toString();
                    if(!networkName.equals("Select a Network")) {
                        // display the associated sensors from the network
                        networkID = networkPair.get(networkName);
                        networkPrompt.setVisibility(View.INVISIBLE);
                    }
                }else
                {
                    cancelRemove(null);
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
    public void confirmRemove(View view) {

        AlertDialog.Builder alert = new AlertDialog.Builder(RemoveNetworkActivity.this);
        alert.setTitle("Delete");
        alert.setMessage("Are you sure you want to delete?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeNetwork();

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
    public void removeNetwork() {

        if(networkID == null)
        {
            Toast.makeText(getApplicationContext(), "Please select a network", Toast.LENGTH_LONG).show();
            return;
        }
        //add the params to a RequestParams object
        //these will be used in the request
        RequestParams params = new RequestParams();
        params.put("networkID", networkID);
        prgDialog.show();
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(base_url + "RemoveNetwork/" + authToken, params, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {

                prgDialog.hide();
                Toast.makeText(getApplicationContext(), networkID + " has been removed", Toast.LENGTH_LONG).show();
                prgDialog.dismiss();
                finish();
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

    public void cancelRemove(View view) {
        super.finish();
        prgDialog.dismiss();
    }


}
