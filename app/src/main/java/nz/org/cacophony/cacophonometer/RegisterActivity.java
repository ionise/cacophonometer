package nz.org.cacophony.cacophonometer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity implements IdlingResourceForEspressoTesting {

    private static final String TAG = RegisterActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //https://developer.android.com/training/appbar/setting-up#java
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_help, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String group = extras.getString("GROUP");
            if (group != null) {
                ((EditText) findViewById(R.id.setupGroupNameInput)).setText(group);
            }
        }

        IntentFilter iff = new IntentFilter("SERVER_REGISTER");
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);
        displayOrHideGUIObjects();
    }

    void displayOrHideGUIObjects() {
        Prefs prefs = new Prefs(getApplicationContext());
        if (prefs.getGroupName() != null && prefs.getDeviceName() != null) {
            // Phone is registered
            // Phone is NOT registered
            //Input fields to be INVISIBLE
            findViewById(R.id.setupGroupNameInput).setVisibility(View.INVISIBLE);
            findViewById(R.id.setupDeviceNameInput).setVisibility(View.INVISIBLE);

            //Set appropriate messages

            ((TextView) findViewById(R.id.tvTitleMessage)).setText(getString(R.string.register_title_registered));
            ((TextView) findViewById(R.id.tvGroupName)).setText(getString(R.string.group_name_registered) + prefs.getGroupName());
            ((TextView) findViewById(R.id.tvDeviceName)).setText(getString(R.string.device_name_registered) + prefs.getDeviceName());

            //Only unregister button is visible
            findViewById(R.id.btnRegister).setVisibility(View.INVISIBLE);
            findViewById(R.id.btnUnRegister).setVisibility(View.VISIBLE);

            //Nudge use to Next Step button
            findViewById(R.id.btnNext).requestFocus();
            findViewById(R.id.btnNext).setBackgroundColor(getResources().getColor(R.color.colorAlert));


        } else {
            // Phone is NOT registered
            //Input fields to be visible
            findViewById(R.id.setupGroupNameInput).setVisibility(View.VISIBLE);
            findViewById(R.id.setupDeviceNameInput).setVisibility(View.VISIBLE);

            //Set appropriate messages

            ((TextView) findViewById(R.id.tvTitleMessage)).setText(getString(R.string.register_title_unregistered));
            ((TextView) findViewById(R.id.tvGroupName)).setText(getString(R.string.group_name_unregistered));
            ((TextView) findViewById(R.id.tvDeviceName)).setText(getString(R.string.device_name_unregistered));

            //Only register button is visible
            findViewById(R.id.btnRegister).setVisibility(View.VISIBLE);
            findViewById(R.id.btnUnRegister).setVisibility(View.INVISIBLE);

            //Nudge user to Enter Group Name box
            //Nudge use to Next Step button
            findViewById(R.id.tvGroupName).requestFocus();
            findViewById(R.id.btnNext).setBackgroundColor(getResources().getColor(R.color.accent));
        }


        if (prefs.getGroupName() != null) {
            ((TextView) findViewById(R.id.tvGroupName)).setText("Group - " + prefs.getGroupName());
        }
        if (prefs.getDeviceName() != null) {
            ((TextView) findViewById(R.id.tvDeviceName)).setText("Device Name - " + prefs.getDeviceName());

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onNotice);
    }

    public void registerButton(View v) {
        //  registerIdlingResource.increment();

        Prefs prefs = new Prefs(getApplicationContext());

        // if (prefs.getOffLineMode()){
        if (prefs.getInternetConnectionMode().equalsIgnoreCase("offline")) {
            Util.getToast(getApplicationContext(), "The internet connection (in Advanced) has been set 'offline' - so this device can not be registered", true).show();
            return;
        }

        if (!Util.isNetworkConnected(getApplicationContext())) {
            Util.getToast(getApplicationContext(), "The phone is not currently connected to the internet - please fix and try again", true).show();
            return;
        }

        if (prefs.getGroupName() != null) {
            Util.getToast(getApplicationContext(), "Already registered - press UNREGISTER first (if you really want to change group)", true).show();
            return;
        }
        // Check that the group name is valid, at least 4 characters.
        String group = ((EditText) findViewById(R.id.setupGroupNameInput)).getText().toString();
        if (group.length() < 1) {
            Util.getToast(getApplicationContext(), "Please enter a group name of at least 4 characters (no spaces)", true).show();
            return;
        } else if (group.length() < 4) {
            Log.i(TAG, "Invalid group name: " + group);

            Util.getToast(getApplicationContext(), group + " is not a valid group name. Please use at least 4 characters (no spaces)", true).show();
            return;
        }

        // Check that the device name is valid, at least 4 characters.
        String deviceName = ((EditText) findViewById(R.id.setupDeviceNameInput)).getText().toString();
        if (deviceName.length() < 1) {
            Util.getToast(getApplicationContext(), "Please enter a device name of at least 4 characters (no spaces)", true).show();
            return;
        } else if (deviceName.length() < 4) {
            Log.i(TAG, "Invalid device name: " + deviceName);

            Util.getToast(getApplicationContext(), deviceName + " is not a valid device name. Please use at least 4 characters (no spaces)", true).show();
            return;
        }

        Util.getToast(getApplicationContext(), "Attempting to register with server - please wait", false).show();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            Log.e(TAG, "imm is null");
            return;
        }

        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        String groupName = prefs.getGroupName();
        if (groupName != null && groupName.equals(group)) {
            // Try to login with username and password.
            Util.getToast(getApplicationContext(), "Already registered with that group", true).show();
            return;
        }

        register(group, deviceName, getApplicationContext());

    }

    /**
     * Will register the device in the given group saving the JSON Web Token, devicename, and password.
     *
     * @param group name of group to join.
     */
    private void register(final String group, final String deviceName, final Context context) {
        // Check that the group name is valid, at least 4 characters.
        if (group == null || group.length() < 4) {

            Log.e(TAG, "Invalid group name - this should have already been picked up");
            return;
        }

        disableFlightMode();

        // Now wait for network connection as setFlightMode takes a while
        if (!Util.waitForNetworkConnection(getApplicationContext(), true)) {
            Log.e(TAG, "Failed to disable airplane mode");
            return;
        }

        Thread registerThread = new Thread() {
            @Override
            public void run() {
                Server.register(group, deviceName, context);
                ;
            }
        };
        registerThread.start();
    }

    /**
     * Un-registered a device deleting the password, devicename, and JWT.
     *
     * @param v View
     */
    public void unRegisterButton(@SuppressWarnings("UnusedParameters") View v) {
        Prefs prefs = new Prefs(getApplicationContext());
        if (prefs.getGroupName() == null) {
            Util.getToast(getApplicationContext(), "Not currently registered - so can not unregister :-(", true).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Add the buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                unregister();
            }
        });
        builder.setNegativeButton("No/Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                return;
            }
        });
        builder.setMessage("Are you sure?")
                .setTitle("Un-register this phone");
        AlertDialog dialog = builder.create();
        dialog.show();


    }

    private void unregister() {


        try {

            Prefs prefs = new Prefs(getApplicationContext());
            prefs.setGroupName(null);
            prefs.setDevicePassword(null);
            prefs.setDeviceName(null);
            prefs.setDeviceToken(null);

            Util.getToast(getApplicationContext(), "Success - Device is no longer registered", false).show();
            displayOrHideGUIObjects();

        } catch (Exception ex) {
            Log.e(TAG, "Error Un-registering device.");
        }

    }


    public void next(@SuppressWarnings("UnusedParameters") View v) {
        try {
            Intent intent = new Intent(this, GPSActivity.class);
            startActivity(intent);
            //  finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }

    }

    public void back(@SuppressWarnings("UnusedParameters") View v) {
        try {
            finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.button_help:
                Util.displayHelp(this, "Register");
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private void disableFlightMode() {
        try {
            Util.disableFlightMode(getApplicationContext());


        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
            Util.getToast(getApplicationContext(), "Error disabling flight mode", true).show();
        }
    }

    private final BroadcastReceiver onNotice = new BroadcastReceiver() {
        //https://stackoverflow.com/questions/8802157/how-to-use-localbroadcastmanager

        // broadcast notification coming from ??
        @Override
        public void onReceive(Context context, Intent intent) {
            Prefs prefs = new Prefs(getApplicationContext());
            try {


                String jsonStringMessage = intent.getStringExtra("jsonStringMessage");
                if (jsonStringMessage != null) {

                    JSONObject joMessage = new JSONObject(jsonStringMessage);
                    String messageType = joMessage.getString("messageType");
                    String messageToDisplay = joMessage.getString("messageToDisplay");

                    if (messageType != null) {


                        if (messageType.equalsIgnoreCase("REGISTER_SUCCESS")) {
                            Util.getToast(getApplicationContext(), messageToDisplay, false).show();
                            //registerIdlingResource.decrement();
                            try {
                                displayOrHideGUIObjects();


                            } catch (Exception ex) {
                                Log.e(TAG, ex.getLocalizedMessage());
                            }

                        } else  {
                            Util.getToast(getApplicationContext(), messageToDisplay, true).show();
                        }
                    }
                }

            } catch (Exception ex) {
                Log.e(TAG, ex.getLocalizedMessage());
                Util.getToast(getApplicationContext(), "Oops, your phone did not register - not sure why", true).show();
            }
        }
    };

    @SuppressWarnings("SameReturnValue")
    public CountingIdlingResource getIdlingResource() {
        return registerIdlingResource;
    }

    @SuppressWarnings("SameReturnValue")
    public CountingIdlingResource getRecordNowIdlingResource() {
        return recordNowIdlingResource;
    }
}
