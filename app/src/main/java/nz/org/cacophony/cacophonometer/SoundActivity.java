package nz.org.cacophony.cacophonometer;

import android.app.Activity;
import android.content.Intent;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Switch;

public class SoundActivity extends AppCompatActivity implements IdlingResourceForEspressoTesting{
    private static final String TAG = SoundActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound);
    }

    @Override
    public void onResume() {
        super.onResume();
        Prefs prefs = new Prefs(getApplicationContext());
        boolean playWarningSound = prefs.getPlayWarningSound();

        final Switch switchPlayWarningSound = findViewById(R.id.swPlayWarningSound);
        switchPlayWarningSound.setChecked(playWarningSound);

    }

    void setPlaySound(){
        final Switch switchPlayWarningSound = findViewById(R.id.swPlayWarningSound);
        boolean playWarningSound = switchPlayWarningSound.isChecked();
        Prefs prefs = new Prefs(getApplicationContext());
        prefs.setPlayWarningSound(playWarningSound);
    }

//    public void onCheckboxWarningSoundClicked(View v) {
//        Prefs prefs = new Prefs(getApplicationContext());
//        // Is the view now checked?
//        boolean checked = ((CheckBox) v).isChecked();
//        prefs.setPlayWarningSound(checked);
//    }

    public void next(@SuppressWarnings("UnusedParameters") View v) {
        try {
            setPlaySound();
            Intent intent = new Intent(this, BatteryActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }

    public void back(@SuppressWarnings("UnusedParameters") View v) {
        try {
            setPlaySound();
            Intent intent = new Intent(this, InternetConnectionActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
    }
    @SuppressWarnings("SameReturnValue")
    public CountingIdlingResource getIdlingResource() {
        return registerIdlingResource;
    }

    @SuppressWarnings("SameReturnValue")
    public CountingIdlingResource getRecordNowIdlingResource() {
        return recordNowIdlingResource;
    }
}
