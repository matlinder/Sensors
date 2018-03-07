package com.mathew.sensorlogin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AccountActivity extends AppCompatActivity {
    // base url for json calls
    private static final String base_url = "https://www.imonnit.com/json/";

    private String authToken; //authorization token from json
    private String userName; //the users username for json calls
    private String userID; //the user id linked to the username, for json calls
    private TextView name; //temp placeholder to display the account json data
    private TextView email;


    /**
     * Create the account acitivity to display the user account details
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("ACCOUNT DETAILS");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_account);

        name = findViewById(R.id.nameTag);
        email = findViewById(R.id.email);

        //grab the token from the previous intent
        Bundle extras = this.getIntent().getExtras();
        if(extras != null)
        {
            authToken = extras.getString("token");
            userName = extras.getString("userName");
            userID = extras.getString("userID");
            //Toast.makeText(getApplicationContext(), "user id = " + userID, Toast.LENGTH_LONG).show();
        }
        displayData();
    }

    /**
     * menu options back button
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
     * menu options buttons
     * @param menu
     * @return
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    /**
     * method to display the user data after a successful login
     */
    public void displayData()
    {
        AsyncHttpClient client = new AsyncHttpClient(); //create the client
        RequestParams params = new RequestParams(); //create a place to store the json params
        params.put("userID", userID);

        //json request
        client.get(base_url + "AccountUserGet/" + authToken, params, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            public void onSuccess(String response) {

                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // Get the data of the user
                    JSONObject result = obj.getJSONObject("Result");
                    // assign the data to the fields so that it is displayed
                    name.setText(String.format("Name: \t\t%s %s", result.getString("FirstName"), result.getString("LastName")));
                    email.setText(String.format("Email: \t\t%s", result.getString("EmailAddress")));

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error occurred could not display data!", Toast.LENGTH_LONG).show();
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
}
