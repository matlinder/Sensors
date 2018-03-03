package com.mathew.sensorlogin;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class ManageActivity extends AppCompatActivity {

    private String authToken;
    private String userID;
    Button createNetwork, removeNetwork;
    boolean shown = false;


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

        createNetwork = findViewById(R.id.createNetwork);
        removeNetwork = findViewById(R.id.removeNetwork);
    }

    public void showNetworkButtons(View view) {
        if(!shown) {
            createNetwork.setVisibility(View.VISIBLE);
            removeNetwork.setVisibility(View.VISIBLE);
            shown = !shown;
        }
        else
        {
            createNetwork.setVisibility(View.GONE);
            removeNetwork.setVisibility(View.GONE);
            shown = !shown;
        }
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


    public void startAddGatewayActivity(View view) {
    }

    public void startAddSensorActivity(View view) {
    }

    public void startMoveSensorActivity(View view) {
    }

    public void startUserActivity(View view) {
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
