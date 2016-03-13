package com.example.emcleod.cs407_android_hw2;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.calendar.CalendarScopes;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.model.*;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import java.util.Calendar;

public class MainActivity extends Activity {
    GoogleAccountCredential mCredential;
    private TextView mOutputText;
    ProgressDialog mProgress;
    private TextView errorCheckerText;
    private TextView currentDateView;

    ArrayAdapter<String> myAdapter;
    private ListView eventListView;
    private String[] eventStringArray = {"", "", "", "", "", "", "", "", "", ""};

    private Button prevDayButton;
    private Button nextDayButton;
    private Button addEventButton;

    private int offset = 0;
    private String dateDisplaying = "";

    private String[] eventInfoArray;
    private List<Event> currentEventList;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR };

    public boolean deleteDone = false;

    /**
     * Create the main activity.
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();


        LinearLayout activityLayout = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        activityLayout.setLayoutParams(lp);
        activityLayout.setOrientation(LinearLayout.VERTICAL);
        activityLayout.setPadding(16, 16, 16, 16);

        ViewGroup.LayoutParams tlp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        ViewGroup.LayoutParams dlp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        LinearLayout dateSelectLayout = new LinearLayout(this);
        dateSelectLayout.setLayoutParams(dlp);
        dateSelectLayout.setOrientation(LinearLayout.HORIZONTAL);
        activityLayout.addView(dateSelectLayout);

        TableRow.LayoutParams params = new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

        currentDateView = new TextView(this);
        currentDateView.setLayoutParams(tlp);
        currentDateView.setPadding(16, 16, 16, 16);
        currentDateView.setText("Viewing events for " + dateDisplaying);
        currentDateView.setTextAppearance(this, android.R.style.TextAppearance_Large);
        activityLayout.addView(currentDateView);

        prevDayButton = new Button(this);
        prevDayButton.setText("Previous Day");
        prevDayButton.setPadding(16, 16, 16, 16);
        prevDayButton.setLayoutParams(params);
        prevDayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                offset--;
                intent.putExtra("offset", offset);
                startActivity(intent);
            }
        });
        dateSelectLayout.addView(prevDayButton);

        nextDayButton = new Button(this);
        nextDayButton.setText("Next Day");
        nextDayButton.setPadding(16, 16, 16, 16);
        nextDayButton.setLayoutParams(params);
        nextDayButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                offset++;
                intent.putExtra("offset", offset);
                startActivity(intent);
            }
        });
        dateSelectLayout.addView(nextDayButton);


        addEventButton = new Button(this);
        addEventButton.setText("Add an Event");
        addEventButton.setPadding(16, 16, 16, 16);
        addEventButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddEventActivity.class);
                startActivity(intent);
            }
        });
        activityLayout.addView(addEventButton);

        errorCheckerText = new TextView(this);
        errorCheckerText.setLayoutParams(tlp);
        errorCheckerText.setPadding(16, 16, 16, 16);
        //activityLayout.addView(errorCheckerText);

        mOutputText = new TextView(this);
        mOutputText.setLayoutParams(tlp);
        mOutputText.setPadding(16, 16, 16, 16);
        mOutputText.setVerticalScrollBarEnabled(true);
        mOutputText.setMovementMethod(new ScrollingMovementMethod());
        //activityLayout.addView(mOutputText);


        myAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, eventStringArray);

        eventListView = new ListView(this);
        eventListView.setLayoutParams(tlp);
        eventListView.setPadding(16, 16, 16, 16);
        eventListView.setVerticalScrollBarEnabled(true);
        eventListView.setAdapter(myAdapter);
        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openDeleteDialog(parent, view, position);
                //while (!DeleteEventTask.deleteDone) {
                //new MakeRequestTask(mCredential).execute();
                /*Log.d("before wait", "about to wait");
                try {
                    TimeUnit.MILLISECONDS.sleep(3000);
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                Log.d("after wait", "finished waiting");

                myAdapter.notifyDataSetChanged();
                Log.d("after notify", "after notify");
                myAdapter = new ArrayAdapter<String>(MainActivity.this,
                        android.R.layout.simple_list_item_1, eventStringArray);
                Log.d("after reassigning", "after reassignment");
                eventListView.invalidate();
                Log.d("after invalidate", "after invalidation");*/
                //myAdapter.notifyDataSetChanged();
                //eventListView.invalidate();
                /*boolean hasNulls = false;
                for (int i = 0; i < eventStringArray.length; i++) {
                    if (eventStringArray[i] == null) {
                        hasNulls = true;
                    }
                }
                if (hasNulls) {
                    Toast.makeText(getApplicationContext(), "msg msg", Toast.LENGTH_LONG).show();
                    Log.d("nulls", "yep nulls");
                }
                if (!hasNulls) {
                    Log.d("no nulls","no nulls");
                }*/
            }
        });
        activityLayout.addView(eventListView);


        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Calendar API ...");


        setContentView(activityLayout);

        // Initialize credentials and service object.
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));

        if (extras !=null) {
            if (extras.containsKey("eventArray")) {
                String[] eventArray = extras.getStringArray("eventArray");
                eventInfoArray = eventArray;
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AddEventTask(mCredential).execute();
                    }
                });
            }
            if (extras.containsKey("offset")) {
                offset = extras.getInt("offset");
            }
        }
    }


    /**
     * Called whenever this activity is pushed to the foreground, such as after
     * a call to onCreate().
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            refreshResults();
        } else {
            mOutputText.setText("Google Play Services required: " +
                    "after installing, close and relaunch this app.");
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mCredential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    mOutputText.setText("Account unspecified.");
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode != RESULT_OK) {
                    chooseAccount();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Attempt to get a set of data from the Google Calendar API to display. If the
     * email address isn't known yet, then call chooseAccount() method so the
     * user can pick an account.
     */
    private void refreshResults() {
        if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                new MakeRequestTask(mCredential).execute();
            } else {
                mOutputText.setText("No network connection available.");
            }
        }
    }

    /**
     * Starts an activity in Google Play Services so the user can pick an
     * account.
     */
    private void chooseAccount() {
        startActivityForResult(
                mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date. Will
     * launch an error dialog for the user to update Google Play Services if
     * possible.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                connectionStatusCode,
                MainActivity.this,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        public MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of the next 10 events from the primary calendar.
         * @return List of Strings describing returned events.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            // List the next 10 events from the primary calendar.
            Date currentDate = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(currentDate);

            Calendar c = new GregorianCalendar();
            c.set(Calendar.HOUR_OF_DAY, 0); //anything 0 - 23
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            Date d1 = c.getTime();
            //add in the offset here
            d1.setTime(d1.getTime() + offset*86400000);
            Calendar tempCal = Calendar.getInstance();
            tempCal.setTime(d1);
            int month = tempCal.get(Calendar.MONTH);
            int day = tempCal.get(Calendar.DAY_OF_MONTH);
            dateDisplaying = String.valueOf(month+1) +"/"+ String.valueOf(day);
            currentDateView.setText("Viewing events for " + dateDisplaying);
            DateTime dayStart = new DateTime(d1);

            Date d2 = d1;
            d2.setTime(d2.getTime() + 86400000);
            DateTime dayEnd = new DateTime(d2);

            //DateTime now = new DateTime(System.currentTimeMillis());
            List<String> eventStrings = new ArrayList<String>();
            Events events = mService.events().list("primary")
                    .setMaxResults(10)
                    .setTimeMin(dayStart)
                    .setTimeMax(dayEnd)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            List<Event> items = events.getItems();
            currentEventList = events.getItems();

            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    // All-day events don't have start times, so just use
                    // the start date.
                    start = event.getStart().getDate();
                }
                DateTime end = event.getEnd().getDateTime();
                if (end == null) {
                    end = event.getEnd().getDate();
                }
                eventStrings.add(
                        String.format("%s from (%s) to (%s)", event.getSummary(), start.toString().substring(11,16), end.toString().substring(11,16)));
            }
            return eventStrings;
        }


        @Override
        protected void onPreExecute() {
            mOutputText.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                mOutputText.setText("No results returned.");
            } else {
                //output.add(0, "Data retrieved using the Google Calendar API:");
                mOutputText.setText(TextUtils.join("\n", output));

                String[] tempArray = new String[output.size()];
                for (int i = 0; i < output.size(); i++) {
                    eventStringArray[i] = output.get(i);
                }
                eventStringArray = tempArray;
                //eventListView.setAdapter(myAdapter);
                ((BaseAdapter) eventListView.getAdapter()).notifyDataSetChanged();

            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }
    }

    private class AddEventTask extends AsyncTask<String, Void, String> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;


        public AddEventTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                sendEvent();
            } catch (Exception e) {
                //oh no
                mLastError = e;
                cancel(true);
                return null;
            }
            return "success";
        }

        private void sendEvent() throws IOException {
            Event event = new Event()
                    .setSummary(eventInfoArray[0]);

            DateTime startDateTime = new DateTime(eventInfoArray[1]);
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone("America/Chicago");
            event.setStart(start);

            DateTime endDateTime = new DateTime(eventInfoArray[2]);
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone("America/Chicago");
            event.setEnd(end);

            com.google.api.services.calendar.Calendar mService;
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, mCredential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();
            String calendarId = "primary";
            try{
                event = mService.events().insert(calendarId, event).execute();
            } catch (Exception e) {
                errorCheckerText.setText(e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String s) {
            //new MakeRequestTask(mCredential).execute();
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                    mOutputText.setText("first error");
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                    mOutputText.setText("second error");
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
        }

    }

    private class DeleteEventTask extends AsyncTask<String, Void, String> {
        private com.google.api.services.calendar.Calendar mService = null;
        private String eventID = null;
        private Exception mLastError = null;


        public DeleteEventTask(GoogleAccountCredential credential, String eventTBDid) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();
            eventID = eventTBDid;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                deleteEvent();
            } catch (Exception e) {
                //oh no
                mLastError = e;
                cancel(true);
                deleteDone = true;
                return null;
            }
            deleteDone = true;
            return "success";
        }

        private void deleteEvent() throws IOException {

            try{
                Log.d("start time", String.valueOf(new Date().getTime()));
                mService.events().delete("primary", eventID).execute();
            } catch (Exception e) {
                errorCheckerText.setText(e.getMessage());
                deleteDone = true;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            /*((BaseAdapter) eventListView.getAdapter()).notifyDataSetChanged();
            myAdapter.notifyDataSetChanged();
            new MakeRequestTask(mCredential).execute();*/
            myAdapter.notifyDataSetChanged();
            Log.d("end time", String.valueOf(new Date().getTime()));
            deleteDone = true;
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                    mOutputText.setText("first error");
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            MainActivity.REQUEST_AUTHORIZATION);
                    mOutputText.setText("second error");
                } else {
                    mOutputText.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                mOutputText.setText("Request cancelled.");
            }
            deleteDone = true;
        }

    }

    private void openDeleteDialog(final AdapterView<?> adapter, View view, final int pos) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        dialogBuilder.setTitle("Delete Event");
        dialogBuilder.setMessage("Delete selected Event?");
        dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                String selectedEventData = (String) adapter.getItemAtPosition(pos);
                String[] eventArray = selectedEventData.split("@");
                String selectedEventName = eventArray[0].trim();
                String eventTBDid = "";
                Iterator<Event> itr = currentEventList.iterator();
                while (itr.hasNext()) {
                    Event tempEvent = itr.next();
                    if (tempEvent.getSummary().equals(selectedEventName)) {
                        eventTBDid = tempEvent.getId();
                    }
                }

                new DeleteEventTask(mCredential, eventTBDid).execute();

                String[] tempEventArray = new String[eventStringArray.length - 1];
                for (int i = 0; i < pos; i++) {
                    tempEventArray[i] = eventStringArray[i];
                }
                for (int i = pos + 1; i < eventStringArray.length; i++) {
                    tempEventArray[i - 1] = eventStringArray[i];
                }
                eventStringArray = tempEventArray;

                //while (!deleteDone) {}
                deleteDone = false;
                //updateAdapter();

                /*myAdapter.notifyDataSetChanged();
                myAdapter = new ArrayAdapter<String>(MainActivity,
                        android.R.layout.simple_list_item_1, eventStringArray);
                eventListView.setAdapter(myAdapter);*/
                //eventListView.invalidate();
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);

            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

/*
    public void updateAdapter() {
        myAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, eventStringArray);
        eventListView.setAdapter(myAdapter);
        eventListView.invalidate();
    }
*/

}