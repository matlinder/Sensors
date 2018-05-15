package com.mathew.sensorlogin.Manage;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import java.util.ArrayList;

/**
 * Activity to create a network in which we can add gateways and sensors to
 */
public class CreateNetworkActivity extends AppCompatActivity {

    // base url for json calls
    private static final String base_url = "https://www.imonnit.com/json/";

    private ProgressDialog prgDialog; //dialog
    private String authToken;
    private EditText networkName;
    private String userID;

    // collections to hold the various IDs
    private ArrayList<String> userIDs = new ArrayList<String>();
    private ArrayList<String> userNames = new ArrayList<String>();
    private ArrayList<String> viewIDs = new ArrayList<String>();


    @Override
    /**
     * Creates the activity and sets up the field for the network name
     */
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
        populateUserList();

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
     * Method that creates the network in the monnit system
     * Called the method "CreateNetwork"
     * All that is needed is a name of the network
     * @param view
     */
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
     * Build an alert that shows all the users with checkboxes beside them
     * User checks off which account is able to see the current created network
     * Pressing Done will add the network to all users
     * @param networkParam the ID of the newly created network
     * @param name the name of the newly created network
     */
    private void addNetworkToMultipleUsers(final String networkParam, final String name) {

        final String[] names = userNames.toArray(new String[userNames.size()]);

        AlertDialog.Builder alert = new AlertDialog.Builder(CreateNetworkActivity.this);
        alert.setTitle("Select Users to View " + name);

        alert.setPositiveButton("Done", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                addNetworkToAllUsers(networkParam);

            }
        });
        alert.setMultiChoiceItems(names,null, new DialogInterface.OnMultiChoiceClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if(isChecked) {
                    viewIDs.add(userIDs.get(which));
                }else
                {
                    viewIDs.remove(userIDs.get(which));
                }
            }
        });

        alert.show();
    }

    /**
     * Loop through all of the users that were checked off in the alert
     * Add the newly created network to these users.
     * Each one is a separate called to "EditCustomerPermissions"
     * @param networkParam the ID of the newly created network
     */
    private void addNetworkToAllUsers(String networkParam) {
        prgDialog.show();
        for(String id : viewIDs)
        {
            //String networkParam = "Network_View_Net_" + networkID;

            AsyncHttpClient client = new AsyncHttpClient();

            //params
            RequestParams params = new RequestParams();
            params.put("custID", id);
            params.put(networkParam, "on");
            client.get(base_url + "EditCustomerPermissions/" + authToken, params, new AsyncHttpResponseHandler() {

                public void onSuccess(String response) {
                    JSONObject obj = null;
                    try {
                        obj = new JSONObject(response);
                        String temp = obj.getString("Result");
                        if(temp.equalsIgnoreCase("Save successful."))
                        {
                            //Toast.makeText(getApplicationContext(), networkID + " was added to user", Toast.LENGTH_LONG).show();
                            //prgDialog.dismiss();



                        }else
                        {
                            Toast.makeText(getApplicationContext(), temp, Toast.LENGTH_LONG).show();
                            prgDialog.dismiss();

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
        prgDialog.dismiss();
        finish();
    }

    /**
     * When creating a network, it is not shown to anyone unless you are an admin.
     * If you are creating it, you need to see it.
     * This method adds the current network to your list, so the logged in user can see it.
     * Kind of useless since only admins can create it, but there are times when you can have create
     * network ability, and not be an admin.
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
                            addNetwork(ID, name);
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

    /**
     * Method that adds the entered network ID to the entered user
     * Calls "EditCustomerPermissions" to add the network to the user's list
     *
     * @param networkID the network that the passes in user needs to view
     * @param name the name of the user that will get access to view the network
     */
    private void addNetwork(final String networkID, final String name)
    {
        final String networkParam = "Network_View_Net_" + networkID;

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
                    if(temp.equalsIgnoreCase("Save successful."))
                    {
                        Toast.makeText(getApplicationContext(), networkID + " was added to user", Toast.LENGTH_LONG).show();
                        prgDialog.dismiss();
                        addNetworkToMultipleUsers(networkParam, name);


                    }else
                    {
                        Toast.makeText(getApplicationContext(), temp, Toast.LENGTH_LONG).show();
                        prgDialog.dismiss();

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

    /**
     * Populates the list of all users on the account and will display them as check boxes
     * for the user to check if they want that user to be able to view the created network.
     * This streamlines the process by removing the need to edit each user individually to
     * access each network.
     * Calls "AccountUserList"
     */
    public void populateUserList()
    {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(base_url + "AccountUserList/" + authToken, new AsyncHttpResponseHandler() {

            public void onSuccess(String response) {
                try {

                    JSONObject obj = new JSONObject(response);
                    JSONArray objArray = obj.getJSONArray("Result");

                    for (int i = 0; i < objArray.length(); i++) {
                        //grabs the object
                        JSONObject tempUser = objArray.getJSONObject(i);
                        String ID = tempUser.getString("UserID");
                        String first = tempUser.getString("FirstName");
                        String last = tempUser.getString("LastName");
                        String name = first + " " + last;


                        userIDs.add(ID);
                        userNames.add(name);
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

    /**
     * Cancels the activity when the button is pressed
     * @param view
     */
    public void cancel(View view) {
        super.finish();
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
    
}
