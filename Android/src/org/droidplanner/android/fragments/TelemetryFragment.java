package org.droidplanner.android.fragments;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.widgets.AttitudeIndicator;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.model.Drone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TelemetryFragment extends Fragment implements OnDroneListener {

    private AttitudeIndicator attitudeIndicator;
    private Drone drone;
    private TextView roll;
    private TextView yaw;
    private TextView pitch;
    private TextView groundSpeed;
    private TextView airSpeed;
    private TextView climbRate;
    private TextView altitude;
    private TextView targetAltitude;
    private boolean headingModeFPV;


    private View view;

    private static final String NEW_DRONE = "NEW_DRONE";
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case "NEW_DRONE":
                    Log.d(NEW_DRONE, "TelemetryFragments - NEW_DRONE");
                    //newDrone();
                    break;
                case "NEW_DRONE_SELECTED":
                    Log.d(NEW_DRONE, "TelemetryFragments - NEW_DRONE_SELECTED");
                    newDroneSelected(intent.getExtras().getInt("droneID"));
                    break;
            }
        }
    };

    public TelemetryFragment()
    {

    }


    public static TelemetryFragment newInstance(int num_map) {
        TelemetryFragment fragment = new TelemetryFragment();

        Bundle args = new Bundle();
        args.putInt("num_map", num_map);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {



        Integer num_map;

        if(getArguments() == null)
        {
            view = inflater.inflate(R.layout.fragment_telemetry, container, false);
            attitudeIndicator = (AttitudeIndicator) view.findViewById(R.id.aiView);

            roll = (TextView) view.findViewById(R.id.rollValueText);
            yaw = (TextView) view.findViewById(R.id.yawValueText);
            pitch = (TextView) view.findViewById(R.id.pitchValueText);

            groundSpeed = (TextView) view.findViewById(R.id.groundSpeedValue);
            airSpeed = (TextView) view.findViewById(R.id.airSpeedValue);
            climbRate = (TextView) view.findViewById(R.id.climbRateValue);
            altitude = (TextView) view.findViewById(R.id.altitudeValue);
            targetAltitude = (TextView) view.findViewById(R.id.targetAltitudeValue);

            drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone();
            //addBroadcastFilters();
            return view;
        }
        else
        {
           num_map = getArguments().getInt("num_map");
            switch (num_map) {
              //case 1: is default
                case 2:

                    Log.d(TELEMETRY, "Case 2");
                    view = inflater.inflate(R.layout.fragment_telemetry2, container, false);
                    attitudeIndicator = (AttitudeIndicator) view.findViewById(R.id.aiView2);

                    roll = (TextView) view.findViewById(R.id.rollValueText2);
                    yaw = (TextView) view.findViewById(R.id.yawValueText2);
                    pitch = (TextView) view.findViewById(R.id.pitchValueText2);

                    groundSpeed = (TextView) view.findViewById(R.id.groundSpeedValue2);
                    airSpeed = (TextView) view.findViewById(R.id.airSpeedValue2);
                    climbRate = (TextView) view.findViewById(R.id.climbRateValue2);
                    altitude = (TextView) view.findViewById(R.id.altitudeValue2);
                    targetAltitude = (TextView) view.findViewById(R.id.targetAltitudeValue2);

                    drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone();
                    return view;
                case 3:
                    Log.d(TELEMETRY, "Case 3");
                    view = inflater.inflate(R.layout.fragment_telemetry, container, false);
                    attitudeIndicator = (AttitudeIndicator) view.findViewById(R.id.aiView);

                    roll = (TextView) view.findViewById(R.id.rollValueText);
                    yaw = (TextView) view.findViewById(R.id.yawValueText);
                    pitch = (TextView) view.findViewById(R.id.pitchValueText);

                    groundSpeed = (TextView) view.findViewById(R.id.groundSpeedValue);
                    airSpeed = (TextView) view.findViewById(R.id.airSpeedValue);
                    climbRate = (TextView) view.findViewById(R.id.climbRateValue);
                    altitude = (TextView) view.findViewById(R.id.altitudeValue);
                    targetAltitude = (TextView) view.findViewById(R.id.targetAltitudeValue);

                    drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone();
                    //addBroadcastFilters();
                    return view;
                case 4:
                    Log.d(TELEMETRY, "Case 4");
                    view = inflater.inflate(R.layout.fragment_telemetry, container, false);
                    attitudeIndicator = (AttitudeIndicator) view.findViewById(R.id.aiView);

                    roll = (TextView) view.findViewById(R.id.rollValueText);
                    yaw = (TextView) view.findViewById(R.id.yawValueText);
                    pitch = (TextView) view.findViewById(R.id.pitchValueText);

                    groundSpeed = (TextView) view.findViewById(R.id.groundSpeedValue);
                    airSpeed = (TextView) view.findViewById(R.id.airSpeedValue);
                    climbRate = (TextView) view.findViewById(R.id.climbRateValue);
                    altitude = (TextView) view.findViewById(R.id.altitudeValue);
                    targetAltitude = (TextView) view.findViewById(R.id.targetAltitudeValue);

                    drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone();
                    //addBroadcastFilters();
                    return view;
                default:
                    Log.d(TELEMETRY, "Case default");
                    view = inflater.inflate(R.layout.fragment_telemetry, container, false);
                    attitudeIndicator = (AttitudeIndicator) view.findViewById(R.id.aiView);

                    roll = (TextView) view.findViewById(R.id.rollValueText);
                    yaw = (TextView) view.findViewById(R.id.yawValueText);
                    pitch = (TextView) view.findViewById(R.id.pitchValueText);

                    groundSpeed = (TextView) view.findViewById(R.id.groundSpeedValue);
                    airSpeed = (TextView) view.findViewById(R.id.airSpeedValue);
                    climbRate = (TextView) view.findViewById(R.id.climbRateValue);
                    altitude = (TextView) view.findViewById(R.id.altitudeValue);
                    targetAltitude = (TextView) view.findViewById(R.id.targetAltitudeValue);

                    drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone();
                    //addBroadcastFilters();
                    return view;
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        drone.addDroneListener(this);
        addBroadcastFilters();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity()
                .getApplicationContext());
        headingModeFPV = prefs.getBoolean("pref_heading_mode", false);
    }

    @Override
    public void onStop() {
        super.onStop();
        drone.removeDroneListener(this);
        getActivity().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onDroneEvent(DroneEventsType event, Drone drone) {
        switch (event) {
            case NAVIGATION:
                break;
            case ATTITUDE:
                onOrientationUpdate(drone);
                break;
            case SPEED:
                onSpeedAltitudeAndClimbRateUpdate(drone);
                break;
            default:
                break;
        }

    }

    public void onOrientationUpdate(Drone drone) {
        float r = (float) drone.getOrientation().getRoll();
        float p = (float) drone.getOrientation().getPitch();
        float y = (float) drone.getOrientation().getYaw();

        if (!headingModeFPV & y < 0) {
            y = 360 + y;
        }

    //    if(getArguments()!= null)
    //        if(getArguments().getInt("num_map") == 2)
    //        {
    //            y=y+50;
    //       }

        attitudeIndicator.setAttitude(r, p, y);

        roll.setText(String.format("%3.0f\u00B0", r));
        pitch.setText(String.format("%3.0f\u00B0", p));
        yaw.setText(String.format("%3.0f\u00B0", y));

    }

    public void onSpeedAltitudeAndClimbRateUpdate(Drone drone) {
        airSpeed.setText(String.format("%3.1f", drone.getSpeed().getAirSpeed()
                .valueInMetersPerSecond()));
        groundSpeed.setText(String.format("%3.1f", drone.getSpeed().getGroundSpeed()
                .valueInMetersPerSecond()));
        climbRate.setText(String.format("%3.1f", drone.getSpeed().getVerticalSpeed()
                .valueInMetersPerSecond()));
        double alt = drone.getAltitude().getAltitude();
        double targetAlt = drone.getAltitude().getTargetAltitude();
        altitude.setText(String.format("%3.1f", alt));
        targetAltitude.setText(String.format("%3.1f", targetAlt));

    }

    public void newDroneSelected(int droneId)
    {
        drone.removeDroneListener(this); //Remove o listener anterior!
        drone = ((DroidPlannerApp) getActivity().getApplication()).getDroneList().get(droneId);
        if(drone!=null)
            drone.addDroneListener(this);
    }

    private void addBroadcastFilters()
    {
        final IntentFilter connectedFilter = new IntentFilter();
        connectedFilter.addAction("TOWER_CONNECTED");
        getActivity().registerReceiver(broadcastReceiver, connectedFilter);
        final IntentFilter disconnectedFilter = new IntentFilter();
        disconnectedFilter.addAction("TOWER_DISCONNECTED");
        getActivity().registerReceiver(broadcastReceiver, disconnectedFilter);
        final IntentFilter newDroneFilter = new IntentFilter();
        newDroneFilter.addAction("NEW_DRONE");
        getActivity().registerReceiver(broadcastReceiver, newDroneFilter);
        final IntentFilter newDroneSelectedFilter = new IntentFilter();
        newDroneSelectedFilter.addAction("NEW_DRONE_SELECTED");
        getActivity().registerReceiver(broadcastReceiver, newDroneSelectedFilter);
    }

    private static final String TELEMETRY = "TELEMETRY";


}