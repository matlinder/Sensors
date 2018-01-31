package com.mathew.sensorlogin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AccountActivity extends AppCompatActivity {
    String authToken;
    String userName;
    String userID;
    TextView accountDetails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        accountDetails = findViewById(R.id.AccountDetails);

        //grab the token from the previous intent
        Bundle extras = this.getIntent().getExtras();
        if(extras != null)
        {
            authToken = extras.getString("token");
            userName = extras.getString("userName");
            userID = extras.getString("userID");
            Toast.makeText(getApplicationContext(), "user id = " + userID, Toast.LENGTH_LONG).show();
        }
        displayData();
    }
    /**
     * method to display the user data after a successful login
     */
    public void displayData()
    {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("userID", userID);

        client.get("https://www.imonnit.com/json/AccountUserGet/" + authToken, params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'

            public void onSuccess(String response) {

                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // Get the data of the user
                    accountDetails.setText(obj.getString("Result"));
//                    JSONArray users = obj.getJSONArray("Result");
//                    // loop through the array for the specific user and save their details
//                    for(int i = 0; i < users.length(); i++)
//                    {
//                        // grab each object and store in a temp variable
//                        JSONObject temp = users.getJSONObject(i);
//                        // check if the object in the array matches the username entered to login
//                        if(temp.getString("UserName").equals(userName))
//                        {
//                            // set the details for viewing purpose to see if the correct user was selected
//                            accountDetails.setText(temp.toString());
//                        }
//                    }





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
