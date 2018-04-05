package com.mathew.sensorlogin.Manage;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mathew.sensorlogin.R;

public class EditUserPermissionActivity extends AppCompatActivity {
    // base url for json calls
    private static final String base_url = "https://www.imonnit.com/json/";
    private String authToken;
    private ProgressDialog prgDialog; //dialog
    private Button details, permissions;
    private TextView firstLabel, lastLabel, emailLabel;
    private EditText first, last, email;
    private ViewGroup transitionsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_permission);

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

        details = findViewById(R.id.buttonOne);
        permissions = findViewById(R.id.buttonTwo);
        firstLabel = findViewById(R.id.firstText);
        lastLabel = findViewById(R.id.lastText);
        emailLabel = findViewById(R.id.emailText);
        first = findViewById(R.id.editFirstName);
        last = findViewById(R.id.editLastName);
        email = findViewById(R.id.editEmail);
        transitionsContainer = findViewById(R.id.linearTrans);

    }

    public void userDetailsDisplay(View view) {
        TransitionManager.beginDelayedTransition(transitionsContainer);

        firstLabel.setVisibility(View.VISIBLE);
        lastLabel.setVisibility(View.VISIBLE);
        emailLabel.setVisibility(View.VISIBLE);
        first.setVisibility(View.VISIBLE);
        last.setVisibility(View.VISIBLE);
        email.setVisibility(View.VISIBLE);

    }

    public void userPermissionsDisplay(View view) {

        TransitionManager.beginDelayedTransition(transitionsContainer);

        firstLabel.setVisibility(View.GONE);
        lastLabel.setVisibility(View.GONE);
        emailLabel.setVisibility(View.GONE);
        first.setVisibility(View.GONE);
        last.setVisibility(View.GONE);
        email.setVisibility(View.GONE);
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
