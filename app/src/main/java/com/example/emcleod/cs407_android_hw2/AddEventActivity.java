package com.example.emcleod.cs407_android_hw2;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.api.services.calendar.model.Event;

public class AddEventActivity extends AppCompatActivity {

    private EditText eventName_editText;
    private EditText eventDate_editText;
    private EditText eventStart_editText;
    private EditText eventEnd_editText;
    private Button create_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

       /* LinearLayout activityLayout = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        activityLayout.setLayoutParams(lp);
        activityLayout.setOrientation(LinearLayout.VERTICAL);
        activityLayout.setPadding(16, 16, 16, 16);

        eventNameTV = new TextView(this);
        eventNameTV.setHint("Event Name");
        activityLayout.addView(eventNameTV);


        setContentView(activityLayout);*/

        eventName_editText = (EditText) findViewById(R.id.eventName_editText);
        eventDate_editText = (EditText) findViewById(R.id.eventDate_editText);
        eventStart_editText = (EditText) findViewById(R.id.eventStart_editText);
        eventEnd_editText = (EditText) findViewById(R.id.eventEnd_editText);
        create_button = (Button) findViewById(R.id.create_button);

        create_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Event event = new Event()
                        .setSummary(eventName_editText.getText().toString());

                String rawEventDate = eventDate_editText.getText().toString();
                String eventYear = rawEventDate.substring(0, 4);
                String rawMonth = rawEventDate.substring(5, 7);
                int tempMonth = Integer.parseInt(rawMonth) -1;
                String eventMonth = "" + tempMonth;
                String eventDay = rawEventDate.substring(8,10);
                String eventDate = eventYear+"-"+eventMonth+"-"+eventDay+"T";
            }
        });
    }

}
