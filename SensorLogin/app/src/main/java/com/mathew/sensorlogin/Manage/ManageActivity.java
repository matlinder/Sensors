package com.mathew.sensorlogin.Manage;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.TransitionManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mathew.sensorlogin.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ManageActivity extends AppCompatActivity {
    // base url for json calls
    private static final String base_url = "https://www.imonnit.com/json/";
    private String authToken;
    private String userID;
    private Button createNetwork, removeNetwork, addGateway, removeGateway, addSensor, moveSensor;
    private Button editSensor, removeSensor, addUser, removeUser, editUser, userButton;
    private boolean networkVisible = false;
    private boolean gatewayVisible = false;
    private ViewGroup transitionsContainer;
    private boolean sensorVisible = false;
    private boolean userVisible = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);
        getSupportActionBar().setTitle("MANAGE");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //grab the token from the previous intent
        Bundle extras = this.getIntent().getExtras();
        if(extras != null)
        {
            authToken = extras.getString("token");
            userID = extras.getString("userID");
        }
        userButton = findViewById(R.id.users);

        userVisibilityButton();
        transitionsContainer = findViewById(R.id.linearLayout2);
        createNetwork = findViewById(R.id.createNetwork);
        removeNetwork = findViewById(R.id.removeNetwork);
        addGateway = findViewById(R.id.addGateway);
        removeGateway = findViewById(R.id.removeGateway);
        addSensor = findViewById(R.id.addSensor);
        editSensor = findViewById(R.id.editSensor);
        moveSensor = findViewById(R.id.moveSensor);
        removeSensor = findViewById(R.id.removeSensor);
        addUser = findViewById(R.id.addUser);
        editUser = findViewById(R.id.editUser);
        removeUser = findViewById(R.id.removeUser);



    }

    public void showNetworkButtons(View view) {


        TransitionManager.beginDelayedTransition(transitionsContainer);

        if(!networkVisible)
        {
            if(gatewayVisible) {
                showGatewayButtons(null);
            }
            if(sensorVisible)
            {
                showSensorButtons(null);
            }
            if(userVisible)
            {
                showUserButtons(null);
            }
        }
        networkVisible = !networkVisible;
        createNetwork.setVisibility(networkVisible ? View.VISIBLE : View.GONE);
        removeNetwork.setVisibility(networkVisible ? View.VISIBLE : View.GONE);

    }
    public void startCreateNetworkActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), CreateNetworkActivity.class);
        intent.putExtra("token", authToken);
        intent.putExtra("userID", userID);
        startActivity(intent);
    }

    public void startRemoveNetworkActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), RemoveNetworkActivity.class);
        intent.putExtra("token", authToken);
        intent.putExtra("userID", userID);
        startActivity(intent);
    }
    public void showGatewayButtons(View view) {


            TransitionManager.beginDelayedTransition(transitionsContainer);

        if(!gatewayVisible)
        {
            if(networkVisible) {
                showNetworkButtons(null);
            }
            if(sensorVisible)
            {
                showSensorButtons(null);
            }
            if(userVisible)
            {
                showUserButtons(null);
            }
        }
        gatewayVisible = !gatewayVisible;
        addGateway.setVisibility(gatewayVisible ? View.VISIBLE : View.GONE);
        removeGateway.setVisibility(gatewayVisible ? View.VISIBLE : View.GONE);
    }

    public void startAddGatewayActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), AddGatewayActivity.class);
        intent.putExtra("token", authToken);
        intent.putExtra("userID", userID);
        startActivity(intent);
    }
    public void startRemoveGatewayActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), RemoveGatewayActivity.class);
        intent.putExtra("token", authToken);
        intent.putExtra("userID", userID);
        startActivity(intent);
    }

    public void showSensorButtons(View view) {

            TransitionManager.beginDelayedTransition(transitionsContainer);

        if(!sensorVisible)
        {
            if(networkVisible) {
                showNetworkButtons(null);
            }
            if(gatewayVisible)
            {
                showGatewayButtons(null);
            }
            if(userVisible)
            {
                showUserButtons(null);
            }
        }
        sensorVisible = !sensorVisible;
        addSensor.setVisibility(sensorVisible ? View.VISIBLE : View.GONE);
        editSensor.setVisibility(sensorVisible? View.VISIBLE : View.GONE);
        moveSensor.setVisibility(sensorVisible ? View.VISIBLE : View.GONE);
        removeSensor.setVisibility(sensorVisible ? View.VISIBLE : View.GONE);
    }
    public void startAddSensorActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), AddSensorActivity.class);
        intent.putExtra("token", authToken);
        intent.putExtra("userID", userID);
        startActivity(intent);
    }
    public void startMoveSensorActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), MoveSensorActivity.class);
        intent.putExtra("token", authToken);
        intent.putExtra("userID", userID);
        startActivity(intent);
    }
    public void startRemoveSensorActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), RemoveSensorActivity.class);
        intent.putExtra("token", authToken);
        intent.putExtra("userID", userID);
        startActivity(intent);
    }
    public void startEditSensorActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), EditAnySensorActivity.class);
        intent.putExtra("token", authToken);
        intent.putExtra("userID", userID);
        startActivity(intent);
    }

    public void showUserButtons(View view) {

            TransitionManager.beginDelayedTransition(transitionsContainer);

        if(!userVisible)
        {
            if(networkVisible) {
                showNetworkButtons(null);
            }
            if(gatewayVisible)
            {
                showGatewayButtons(null);
            }
            if(sensorVisible)
            {
                showSensorButtons(null);
            }
        }
        userVisible = !userVisible;
        addUser.setVisibility(userVisible ? View.VISIBLE : View.GONE);
        editUser.setVisibility(userVisible ? View.VISIBLE : View.GONE);
        removeUser.setVisibility(userVisible ? View.VISIBLE : View.GONE);
    }
    public void startAddUserActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), AddUserActivity.class);
        //Intent intent = new Intent(getApplicationContext(), AddUserPermissionActivity.class);
        intent.putExtra("token", authToken);
        intent.putExtra("userID", userID);
        startActivity(intent);
    }
    public void startEditUserActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), EditUserActivity.class);
        //Intent intent = new Intent(getApplicationContext(), AddUserPermissionActivity.class);
        intent.putExtra("token", authToken);
        startActivity(intent);
    }
    public void startRemoveUserActivity(View view) {
    }

    /**
     * Checks the visibility if a user is allowed to add/edit/remove users
     */
    public void userVisibilityButton() {

        AsyncHttpClient client = new AsyncHttpClient();
        //params
        RequestParams params = new RequestParams();
        params.put("custID", userID);
        client.get(base_url + "GetCustomerPermissions/" +authToken,params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'

            public void onSuccess(String response) {
                boolean access = false;
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // Get the array of users
                    JSONArray users = obj.getJSONArray("Result");
                    // loop through the array for the specific user and save their details
                    for(int i = 0; i < users.length(); i++)
                    {
                        // grab each object and store in a temp variable
                        JSONObject temp = users.getJSONObject(i);
                        // check if the object in the array matches the username entered to login
                        if(temp.getString("Name").equals("Customer_Create"))
                        {
                            access = temp.getBoolean("Can");
                        }
                    }
                    if(access)
                    {
                        userButton.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error Occurred could not display data!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }
            }

            // When the response returned by REST has Http response code other than '200'

            public void onFailure(int statusCode, Throwable error,
                                  String content) {

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
        finish();
    }



}
