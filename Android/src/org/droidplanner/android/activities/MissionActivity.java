package org.droidplanner.android.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.fragments.FlightActionsFragment;
import org.droidplanner.android.fragments.FlightMapFragmentMission;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.model.Drone;

import org.droidplanner.android.proxy.mission.MissionProxy;

import java.util.HashMap;

public class MissionActivity extends DrawerNavigationUI implements View.OnClickListener {


    private static final int GOOGLE_PLAY_SERVICES_REQUEST_CODE = 101;

    private FragmentManager fragmentManager;
    private FlightMapFragmentMission mapFragment;

    private View mLocationButtonsContainer;
    private ImageButton mGoToMyLocation;
    private ImageButton mGoToDroneLocation;

    private ImageButton mAllPOIs;

    private boolean mAllPOIsOpen = false;
    private int NUM_MAPS;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission);

        fragmentManager = getSupportFragmentManager();

        setupMapFragment();


        mLocationButtonsContainer = findViewById(R.id.location_button_container);
        mGoToMyLocation = (ImageButton) findViewById(R.id.my_location_button);
        mGoToDroneLocation = (ImageButton) findViewById(R.id.drone_location_button);
        mAllPOIs = (ImageButton) findViewById(R.id.all_waypoints_button);

        //Do not show buttons that we don't need
        findViewById(R.id.expand_map_button).setVisibility(ImageButton.GONE);

        final ImageButton resetMapBearing = (ImageButton) findViewById(R.id.map_orientation_button);
        resetMapBearing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMapBearing(0);
            }
        });


        mAllPOIs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapFragment != null) {

                    enableAlgorithmMenu();


                    updateMapLocationButtons(AutoPanMode.DISABLED);
                }
            }
        });


        mGoToMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapFragment != null) {
                    mapFragment.goToMyLocation();
                    updateMapLocationButtons(AutoPanMode.DISABLED);
                }
            }
        });
        mGoToMyLocation.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mapFragment != null) {
                    mapFragment.goToMyLocation();
                    updateMapLocationButtons(AutoPanMode.USER);
                    return true;
                }
                return false;
            }
        });

        mGoToDroneLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapFragment != null) {
                    mapFragment.goToDroneLocation();
                    updateMapLocationButtons(AutoPanMode.DISABLED);
                }
            }
        });
        mGoToDroneLocation.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mapFragment != null) {
                    mapFragment.goToDroneLocation();
                    updateMapLocationButtons(AutoPanMode.DRONE);
                    return true;
                }
                return false;
            }
        });


        updateMissionProxyVariable();







    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mission, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int getNavigationDrawerEntryId() {
        return R.id.navigation_mission;
    }

    private void updateMapLocationButtons(AutoPanMode mode) {
        mGoToMyLocation.setActivated(false);
        mGoToDroneLocation.setActivated(false);

        if (mapFragment != null) {
            mapFragment.setAutoPanMode(mode);
        }

        switch (mode) {
            case DRONE:
                mGoToDroneLocation.setActivated(true);
                break;

            case USER:
                mGoToMyLocation.setActivated(true);
                break;
            default:
                break;
        }
    }

    public void updateMapBearing(float bearing){
        if(mapFragment != null)
            mapFragment.updateMapBearing(bearing);
    }

    private void setupMapFragment() {
        if (mapFragment == null && isGooglePlayServicesValid(true)) {
            mapFragment = (FlightMapFragmentMission) fragmentManager.findFragmentById(R.id.mapFragment_mission);
            if (mapFragment == null) {
                mapFragment = new FlightMapFragmentMission();
                fragmentManager.beginTransaction().add(R.id.mapFragment_mission, mapFragment).commit();
            }
        }
    }

    private boolean isGooglePlayServicesValid(boolean showErrorDialog) {
        // Check for the google play services is available
        final int playStatus = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getApplicationContext());
        final boolean isValid = playStatus == ConnectionResult.SUCCESS;

        if (!isValid && showErrorDialog) {
            final Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(playStatus, this,
                    GOOGLE_PLAY_SERVICES_REQUEST_CODE, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                    });

            if (errorDialog != null)
                errorDialog.show();
        }

        return isValid;
    }


    @Override
    public void onStart() {
        super.onStart();
        setupMapFragment();
        updateMissionProxyVariable();
    }



    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        updateMapLocationButtons(mAppPrefs.getAutoPanMode());
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        super.onDroneEvent(event, drone);
        switch (event) {
            case AUTOPILOT_WARNING:
                break;
            case ARMING:
            case CONNECTED:
            case DISCONNECTED:
            case STATE:
                break;
            case FOLLOW_START:
                break;

            default:
                break;
        }
    }

    private final String CLICKMISSION = "CLICKMISSION";
    @Override
    public void onClick(View v) {

        View menu_view;
        switch(v.getId())
        {
            case R.id.algorithm_button1:
                Log.d(CLICKMISSION, "CLICKED algorithm_button1 ");
                break;
            case R.id.algorithm_button2:
                Log.d(CLICKMISSION, "CLICKED algorithm_button2 ");
                break;
            case R.id.algorithm_button3:
                Log.d(CLICKMISSION, "CLICKED algorithm_button3 ");
                break;
            case R.id.algorithm_button4:
                Log.d(CLICKMISSION, "CLICKED algorithm_button4 ");
                break;
            case R.id.algorithm_button5:
                Log.d(CLICKMISSION, "CLICKED algorithm_button5 ");
                break;
            case R.id.algorithm_send:
                Log.d(CLICKMISSION, "CLICKED send ");
                //ENVIA PARA TODOS OS DRONES ATIVOS!!!

                break;
        }

        mAllPOIsOpen = false;
        menu_view = findViewById(R.id.alg_menu);
        menu_view.setVisibility(View.GONE);


    }

        public void enableAlgorithmMenu()
    {
        if(mAllPOIsOpen)
        {
            mAllPOIsOpen = false;
            View menu_view = findViewById(R.id.alg_menu);
            menu_view.setVisibility(View.GONE);
        }
        else {
            mAllPOIsOpen = true;
            View menu_view = findViewById(R.id.alg_menu);
            menu_view.setVisibility(View.VISIBLE);
        }

    }

    public void updateMissionProxyVariable()
    {
        NUM_MAPS = ((DroidPlannerApp) getApplication()).getDroneList().size();

        int droneID;
        MissionProxy missionProxy;
        switch(NUM_MAPS)
        {
            case 1:
                droneID = MultipleActivity.getDroneIDFromMap(1);
                missionProxy = ((DroidPlannerApp) getApplication()).getMissionProxyFromDroneID(droneID);
                mapFragment.setMissionProxy(1, missionProxy);
                break;
            case 2:
                droneID = MultipleActivity.getDroneIDFromMap(1);
                missionProxy = ((DroidPlannerApp) getApplication()).getMissionProxyFromDroneID(droneID);
                mapFragment.setMissionProxy(1, missionProxy);

                droneID = MultipleActivity.getDroneIDFromMap(2);
                missionProxy = ((DroidPlannerApp) getApplication()).getMissionProxyFromDroneID(droneID);
                mapFragment.setMissionProxy(2, missionProxy);
                break;
            case 3:
                droneID = MultipleActivity.getDroneIDFromMap(1);
                missionProxy = ((DroidPlannerApp) getApplication()).getMissionProxyFromDroneID(droneID);
                mapFragment.setMissionProxy(1, missionProxy);

                droneID = MultipleActivity.getDroneIDFromMap(2);
                missionProxy = ((DroidPlannerApp) getApplication()).getMissionProxyFromDroneID(droneID);
                mapFragment.setMissionProxy(2, missionProxy);

                droneID = MultipleActivity.getDroneIDFromMap(3);
                missionProxy = ((DroidPlannerApp) getApplication()).getMissionProxyFromDroneID(droneID);
                mapFragment.setMissionProxy(3, missionProxy);
                break;
            case 4:
                droneID = MultipleActivity.getDroneIDFromMap(1);
                missionProxy = ((DroidPlannerApp) getApplication()).getMissionProxyFromDroneID(droneID);
                mapFragment.setMissionProxy(1, missionProxy);

                droneID = MultipleActivity.getDroneIDFromMap(2);
                missionProxy = ((DroidPlannerApp) getApplication()).getMissionProxyFromDroneID(droneID);
                mapFragment.setMissionProxy(2, missionProxy);

                droneID = MultipleActivity.getDroneIDFromMap(3);
                missionProxy = ((DroidPlannerApp) getApplication()).getMissionProxyFromDroneID(droneID);
                mapFragment.setMissionProxy(3, missionProxy);

                droneID = MultipleActivity.getDroneIDFromMap(4);
                missionProxy = ((DroidPlannerApp) getApplication()).getMissionProxyFromDroneID(droneID);
                mapFragment.setMissionProxy(4, missionProxy);
                break;
        }

    }



}
