package com.mathew.sensorlogin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity {

    String authToken;
    String userName;
    String userID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); //displays the home screen




        //grab the token from the previous intent
        Bundle extras = this.getIntent().getExtras();
        if(extras != null)
        {
            authToken = extras.getString("token");
            userName = extras.getString("userName");
        }
        grabUserID();


    }

    /**
     * starts the AccountActivity details activity
     * passes the token via extra
     */
    public void displayAccountDetails(View view)
    {
        Intent intent = new Intent(getApplicationContext(), AccountActivity.class);
        intent.putExtra("token", authToken);
        intent.putExtra("userName", userName);
        intent.putExtra("userID", userID);
        startActivity(intent);
    }
    public void displaySensorData(View view) {
        Intent intent = new Intent(getApplicationContext(), SensorActivity.class);
        intent.putExtra("token", authToken);
        intent.putExtra("userID", userID);
        startActivity(intent);
    }
    /**
     * method to grab the user ID as it is used to identify what sensors/gateways the user can see
     */
    public void grabUserID()
    {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("https://www.imonnit.com/json/AccountUserList/" +authToken, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'

            public void onSuccess(String response) {

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
                        if(temp.getString("UserName").equals(userName))
                        {
                            userID = temp.getString("UserID");
                        }
                    }





                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error Occured could not display data!", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


}
