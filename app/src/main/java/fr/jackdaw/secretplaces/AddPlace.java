package fr.jackdaw.secretplaces;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import fr.jackdaw.camera.CameraLollipop;
import fr.jackdaw.utils.SSLCertificateHandler;

@SuppressLint("NewApi")
public class AddPlace extends ActionBarActivity {
    public Activity activity;
    protected String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);

        SSLCertificateHandler.nuke();

        activity = this;

        initFields();
        initialisationActionBar();

        if (checkCameraHardware(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (null == savedInstanceState) {
                    getFragmentManager().beginTransaction()
                            .replace(R.id.add_place_container, CameraLollipop.newInstance())
                            .commit();

                }
            } else {
                //camera 1
            }
        } else {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_place, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.pull_in_from_top, R.anim.pull_out_to_top);
    }

    public void initFields() {
    }

    public void initialisationActionBar() {
        ActionBar actionbar = getSupportActionBar();

        actionbar.setDisplayShowHomeEnabled(true);
        actionbar.setDisplayShowCustomEnabled(false);
        actionbar.setDisplayHomeAsUpEnabled(false);
        actionbar.setDisplayShowTitleEnabled(true);
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }
}