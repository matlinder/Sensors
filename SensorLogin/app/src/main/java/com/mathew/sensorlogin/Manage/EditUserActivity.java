package com.mathew.sensorlogin.Manage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.mathew.sensorlogin.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class EditUserActivity extends AppCompatActivity {
    // base url for json calls
    private static final String base_url = "https://www.imonnit.com/json/";
    private String authToken;
    private ProgressDialog prgDialog; //dialog
    private LinearLayout listLayout;
    private ArrayList<String> userIDs = new ArrayList<String>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);
        getSupportActionBar().setTitle("EDIT USER");
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

        listLayout = findViewById(R.id.userList);
        populateUserList();
    }


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
                        String email = tempUser.getString("EmailAddress");

                        userIDs.add(ID);
                        final TextView item = new TextView(getApplicationContext());
                        item.setTextSize(18);
                        item.setId(i);
                        item.setTextColor(Color.BLACK);
                        item.setPadding(15,8,15,15);
                        item.setClickable(true);
                        if(i % 2 == 0){
                            item.setBackgroundColor(Color.LTGRAY);
                        }
                        item.setText(name + " " + email);
                        item.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int id = item.getId();
                                String tempID = userIDs.get(id);

                                startEditPermissionsActivity(tempID);
                            }
                        });
                        listLayout.addView(item);
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

    private void startEditPermissionsActivity(String tempID) {
        Intent intent = new Intent(getApplicationContext(), EditUserPermissionActivity.class);
        intent.putExtra("token", authToken);
        intent.putExtra("userID", tempID);
        startActivity(intent);

    }


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

    }
}
