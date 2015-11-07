package org.droidplanner.android.fragments;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.FlightActivity;
import org.droidplanner.android.activities.MultipleActivity;
import org.droidplanner.android.activities.helpers.SuperUI;
import org.droidplanner.android.dialogs.YesNoDialog;
import org.droidplanner.android.dialogs.YesNoWithPrefsDialog;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.core.MAVLink.MavLinkArm;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.variables.State;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.core.helpers.units.Altitude;
import org.droidplanner.core.model.Drone;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.MAVLink.Messages.ApmModes;
import com.google.android.gms.analytics.HitBuilders;

/**
 * Provide functionality for flight action button specific to copters.
 */
public class CopterFlightActionsFragment extends Fragment implements View.OnClickListener,
        DroneInterfaces.OnDroneListener, FlightActionsFragment.SlidingUpHeader {

    private static final String ACTION_FLIGHT_ACTION_BUTTON = "Copter flight action button";
    private static final double TAKEOFF_ALTITUDE = 10.0;

    private Drone drone;
    private MissionProxy missionProxy;
    private Follow followMe;

    private View mDisconnectedButtons;
    private View mDisarmedButtons;
    private View mArmedButtons;
    private View mInFlightButtons;

    private Button followBtn;
    private Button homeBtn;
    private Button landBtn;
    private Button pauseBtn;
    private Button autoBtn;

    private static final String COPTER = "COPTER";
    private static final String FLUXO4 = "FLUXO4";
    private static final String BUTTON = "BUTTON";


    private static final String NEW_DRONECOPTTER = "NEW_DRONECOPTTER";
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case "NEW_DRONE_COPTER":
                    Log.d(NEW_DRONECOPTTER, "CopterFlightActionsFragment - NEW_DRONE");
                    newDroneCopter(intent.getExtras().getInt("droneID"));
                    break;
                case "NEW_DRONE_SELECTED_COPTER":
                    Log.d(NEW_DRONECOPTTER, "CopterFlightActionsFragment - NEW_DRONE_SELECTED");
                    //newDroneSelected(intent.getExtras().getInt("droneID"));
                    break;
            }
        }
    };

    public CopterFlightActionsFragment()
    {

    }

    public static CopterFlightActionsFragment newInstance(int num_map) {
        CopterFlightActionsFragment fragment = new CopterFlightActionsFragment();

        Bundle args = new Bundle();
        args.putInt("num_map", num_map);
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if(!(activity instanceof FlightActivity) && !(activity instanceof MultipleActivity)){
            throw new IllegalStateException("Parent activity must be an instance of " +
                    FlightActivity.class.getName());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view;
        DroidPlannerApp droidPlannerApp = (DroidPlannerApp) getActivity().getApplication();
        if(getArguments()!=null)
        {
            switch(getArguments().getInt("num_map"))
            {
                case 2:
                    view = inflater.inflate(R.layout.fragment_copter_mission_control2, container, false);

                    Log.d(FLUXO4, "FLUXO4!!!!");
                    drone = droidPlannerApp.getDrone(MultipleActivity.getDroneIDFromMap(2));
                    followMe = droidPlannerApp.getFollowMe();
                    missionProxy = droidPlannerApp.getMissionProxy();

                    if(drone!=null)
                        drone.addDroneListener(this);

                    if(drone == null)
                        Log.d(FLUXO4, "Drone == NUL!! no onCreate!");
                    return view;
                case 3:
                    view = inflater.inflate(R.layout.fragment_copter_mission_control3, container, false);

                    drone = droidPlannerApp.getDrone(MultipleActivity.getDroneIDFromMap(3));
                    followMe = droidPlannerApp.getFollowMe();
                    missionProxy = droidPlannerApp.getMissionProxy();

                    if(drone!=null)
                        drone.addDroneListener(this);

                    return view;
                case 4:
                    view = inflater.inflate(R.layout.fragment_copter_mission_control4, container, false);

                    drone = droidPlannerApp.getDrone(MultipleActivity.getDroneIDFromMap(4));
                    followMe = droidPlannerApp.getFollowMe();
                    missionProxy = droidPlannerApp.getMissionProxy();

                    if(drone!=null)
                        drone.addDroneListener(this);

                    return view;
                default:
                    view = inflater.inflate(R.layout.fragment_copter_mission_control, container, false);

                    drone = droidPlannerApp.getDrone(MultipleActivity.getDroneIDFromMap(1));
                    followMe = droidPlannerApp.getFollowMe();
                    missionProxy = droidPlannerApp.getMissionProxy();

                    if(drone!=null)
                        drone.addDroneListener(this);

                    return view;
            }
        }
        else
        {
            view = inflater.inflate(R.layout.fragment_copter_mission_control, container, false);

            drone = droidPlannerApp.getDrone(MultipleActivity.getDroneIDFromMap(1));
            followMe = droidPlannerApp.getFollowMe();
            missionProxy = droidPlannerApp.getMissionProxy();

            if(drone!=null)
                drone.addDroneListener(this);
            return view;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.d(COPTER, "onViewCreated()");
        super.onViewCreated(view, savedInstanceState);

        initializeAccordingToMap(view);
    }

    @Override
    public void onStart(){
        Log.d(COPTER, "onStart()");
        Log.d(FLUXO4, "onStart()!");
        super.onStart();

        DroidPlannerApp droidPlannerApp = (DroidPlannerApp) getActivity().getApplication();

        if(getArguments()!=null) {
            drone = droidPlannerApp.getDrone(MultipleActivity.getDroneIDFromMap(getArguments().getInt("num_map")));
            if(drone!=null) {
                followMe = droidPlannerApp.getFollowMe();
                missionProxy = droidPlannerApp.getMissionProxy();

                setupButtonsByFlightState();
                updateFlightModeButtons();
                updateFollowButton();
                drone.addDroneListener(this);
            }
            else
            {
                Log.d(FLUXO4, "Drone == NUL!! - onStart()!!");
            }
        }
       addBroadcastFilters();
    }

    @Override
    public void onStop(){
        super.onStop();
        if(drone!=null)
            drone.removeDroneListener(this);
        getActivity().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onClick(View v) {
        HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                .setCategory(GAUtils.Category.FLIGHT);


        Log.d(BUTTON, "CLICADO!! => num map:" + getArguments().getInt("num_map"));

        switch (v.getId()) {
            case R.id.mc_connectBtn:
            case R.id.mc_connectBtn2:
            case R.id.mc_connectBtn3:
            case R.id.mc_connectBtn4:
                ((SuperUI) getActivity()).toggleDroneConnection();
                break;

            case R.id.mc_armBtn:
            case R.id.mc_armBtn2:
            case R.id.mc_armBtn3:
            case R.id.mc_armBtn4:
                Log.d(BUTTON, "ARM!! => num map:" + getArguments().getInt("num_map") + "  - DroneId: " + drone.getDroneID());
                getArmingConfirmation();
                eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel("Arm");
                break;

            case R.id.mc_disarmBtn:
            case R.id.mc_disarmBtn2:
            case R.id.mc_disarmBtn3:
            case R.id.mc_disarmBtn4:
                MavLinkArm.sendArmMessage(drone, false);
                eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel("Disarm");
                break;

            case R.id.mc_land:
            case R.id.mc_land2:
            case R.id.mc_land3:
            case R.id.mc_land4:
                drone.getState().changeFlightMode(ApmModes.ROTOR_LAND);
                eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel(ApmModes.ROTOR_LAND.getName());
                break;

            case R.id.mc_takeoff:
            case R.id.mc_takeoff2:
            case R.id.mc_takeoff3:
            case R.id.mc_takeoff4:
                drone.getState().doTakeoff(new Altitude(TAKEOFF_ALTITUDE));
                eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel("Takeoff");
                break;

            case R.id.mc_homeBtn:
            case R.id.mc_homeBtn2:
            case R.id.mc_homeBtn3:
            case R.id.mc_homeBtn4:
                Log.d(BUTTON, "HOME!!!!! => num map:" + getArguments().getInt("num_map") + "  - DroneId: " + drone.getDroneID());
                drone.getState().changeFlightMode(ApmModes.ROTOR_RTL);
                eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel(ApmModes.ROTOR_RTL.getName());
                break;

            case R.id.mc_pause:
            case R.id.mc_pause2:
            case R.id.mc_pause3:
            case R.id.mc_pause4:
                if (followMe.isEnabled()) {
                    followMe.toggleFollowMeState();
                }

                drone.getGuidedPoint().pauseAtCurrentLocation();
                eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel("Pause");
                break;

            case R.id.mc_autoBtn:
            case R.id.mc_autoBtn2:
            case R.id.mc_autoBtn3:
            case R.id.mc_autoBtn4:
                drone.getState().changeFlightMode(ApmModes.ROTOR_AUTO);
                eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel(ApmModes.ROTOR_AUTO.getName());
                break;

            case R.id.mc_TakeoffInAutoBtn:
            case R.id.mc_TakeoffInAutoBtn2:
            case R.id.mc_TakeoffInAutoBtn3:
            case R.id.mc_TakeoffInAutoBtn4:
                getTakeOffInAutoConfirmation();
                eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel(ApmModes.ROTOR_AUTO.getName());
                break;

            case R.id.mc_follow:
            case R.id.mc_follow2:
            case R.id.mc_follow3:
            case R.id.mc_follow4:
                followMe.toggleFollowMeState();
                String eventLabel = null;

                switch (followMe.getState()) {
                    case FOLLOW_START:
                        eventLabel = "FollowMe enabled";
                        break;

                    case FOLLOW_RUNNING:
                        eventLabel = "FollowMe running";
                        break;

                    case FOLLOW_END:
                        eventLabel = "FollowMe disabled";
                        break;

                    case FOLLOW_INVALID_STATE:
                        eventLabel = "FollowMe error: invalid state";
                        break;

                    case FOLLOW_DRONE_DISCONNECTED:
                        eventLabel = "FollowMe error: drone not connected";
                        break;

                    case FOLLOW_DRONE_NOT_ARMED:
                        eventLabel = "FollowMe error: drone not armed";
                        break;
                }

                if (eventLabel != null) {
                    eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel(eventLabel);
                    Toast.makeText(getActivity(), eventLabel, Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.mc_dronieBtn:
            case R.id.mc_dronieBtn2:
            case R.id.mc_dronieBtn3:
            case R.id.mc_dronieBtn4:
                getDronieConfirmation();
                eventBuilder.setAction(ACTION_FLIGHT_ACTION_BUTTON).setLabel("Dronie uploaded");
                break;

            default:
                eventBuilder = null;
                break;
        }

        if (eventBuilder != null) {
            GAUtils.sendEvent(eventBuilder);
        }

    }

    private void getDronieConfirmation() {
        YesNoWithPrefsDialog ynd = YesNoWithPrefsDialog.newInstance(getActivity()
                        .getApplicationContext(), getString(R.string.pref_dronie_creation_title),
                getString(R.string.pref_dronie_creation_message), new YesNoDialog.Listener() {
                    @Override
                    public void onYes() {
                        final float bearing = missionProxy.makeAndUploadDronie();
                        if (bearing >= 0) {
                            final FlightActivity flightActivity = (FlightActivity) getActivity();
                            if (flightActivity != null) {
                                flightActivity.updateMapBearing(bearing);
                            }
                        }
                    }

                    @Override
                    public void onNo() {
                    }
                }, getString(R.string.pref_warn_on_dronie_creation_key));

        if(ynd != null){
            ynd.show(getChildFragmentManager(), "Confirm dronie creation");
        }
    }

    private void getTakeOffInAutoConfirmation() {
        YesNoWithPrefsDialog ynd = YesNoWithPrefsDialog.newInstance(getActivity()
                .getApplicationContext(), getString(R.string.dialog_confirm_take_off_in_auto_title),
                getString(R.string.dialog_confirm_take_off_in_auto_msg), new YesNoDialog.Listener() {
            @Override
            public void onYes() {
                drone.getState().doTakeoff(new Altitude(TAKEOFF_ALTITUDE));
                drone.getState().changeFlightMode(ApmModes.ROTOR_AUTO);
            }

            @Override
            public void onNo() {
            }
        }, getString(R.string.pref_warn_on_takeoff_in_auto_key));

        if(ynd != null){
            ynd.show(getChildFragmentManager(), "Confirm take off in auto");
        }
    }

    private void getArmingConfirmation() {
        YesNoWithPrefsDialog ynd = YesNoWithPrefsDialog.newInstance(getActivity().getApplicationContext(),
                getString(R.string.dialog_confirm_arming_title),
                getString(R.string.dialog_confirm_arming_msg), new YesNoDialog.Listener() {
                    @Override
                    public void onYes() {
                        MavLinkArm.sendArmMessage(drone, true);
                    }

                    @Override
                    public void onNo() {}
                }, getString(R.string.pref_warn_on_arm_key));

        if(ynd != null) {
            ynd.show(getChildFragmentManager(), "Confirm arming");
        }
    }

    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        //Log.d(COPTER, "onDroneEvent!!!");
        //Log.d(COPTER, "DRONE ID => " + drone.getDroneID() + " ---- MAP_NUM => " + getArguments().getInt("num_map"));
        //Log.d(COPTER, "DRONE ID => " + getArguments().getInt("num_map"));
        if(getArguments()!=null) {
            if(MultipleActivity.getDroneIDFromMap(getArguments().getInt("num_map")) == drone.getDroneID())
            {
                switch (event) {

                    case ARMING:
                    case CONNECTED:
                    case DISCONNECTED:
                    case STATE:
                        //Log.d(COPTER, "DRONE ID => " + getArguments().getInt("num_map"));
                        setupButtonsByFlightState();
                        break;

                    case MODE:
                        updateFlightModeButtons();
                        break;

                    case FOLLOW_START:
                    case FOLLOW_STOP:
                    case FOLLOW_UPDATE:
                        updateFlightModeButtons();
                        updateFollowButton();
                        break;

                    default:
                        break;
                }
            }
        }
    }

    private void updateFlightModeButtons() {
        resetFlightModeButtons();

        final ApmModes flightMode = drone.getState().getMode();
        switch (flightMode) {
            case ROTOR_AUTO:
                autoBtn.setActivated(true);
                break;

            case ROTOR_GUIDED:
                if (drone.getGuidedPoint().isInitialized() && !followMe.isEnabled()) {
                    pauseBtn.setActivated(true);
                }
                break;

            case ROTOR_RTL:
                homeBtn.setActivated(true);
                break;

            case ROTOR_LAND:
                landBtn.setActivated(true);
                break;
            default:
                break;
        }
    }

    private void resetFlightModeButtons() {
        homeBtn.setActivated(false);
        landBtn.setActivated(false);
        pauseBtn.setActivated(false);
        autoBtn.setActivated(false);
    }

    private void updateFollowButton() {
        switch (followMe.getState()) {
            case FOLLOW_START:
                followBtn.setBackgroundColor(Color.RED);
                break;
            case FOLLOW_RUNNING:
                followBtn.setActivated(true);
                followBtn.setBackgroundResource(R.drawable.flight_action_row_bg_selector);
                break;
            default:
                followBtn.setActivated(false);
                followBtn.setBackgroundResource(R.drawable.flight_action_row_bg_selector);
                break;
        }
    }

    private void resetButtonsContainerVisibility() {
        mDisconnectedButtons.setVisibility(View.GONE);
        mDisarmedButtons.setVisibility(View.GONE);
        mArmedButtons.setVisibility(View.GONE);
        mInFlightButtons.setVisibility(View.GONE);
    }

    private void setupButtonsByFlightState() {
        Log.d(FLUXO4, "setupButtonsByFlightState");
        //if (drone.getMavClient().isConnected()) {
        if(drone.isDroneConnected()){
            Log.d(FLUXO4, "setupButtonsByFlightState - conectado");
            if (drone.getState().isArmed()) {
                Log.d(FLUXO4, "setupButtonsByFlightState - armado");
                if (drone.getState().isFlying()) {
                    Log.d(FLUXO4, "setupButtonsByFlightState - voando");
                    setupButtonsForFlying();
                } else {
                    Log.d(FLUXO4, "setupButtonsByFlightState - !!!!");
                    setupButtonsForArmed();
                }
            } else {
                setupButtonsForDisarmed();
            }
        } else {
            setupButtonsForDisconnected();
        }
    }

    private void setupButtonsForDisconnected() {
        resetButtonsContainerVisibility();
        mDisconnectedButtons.setVisibility(View.VISIBLE);
    }

    private void setupButtonsForDisarmed() {
        resetButtonsContainerVisibility();
        mDisarmedButtons.setVisibility(View.VISIBLE);
    }

    private void setupButtonsForArmed() {
        Log.d(COPTER, "setupButtonsByFlightState - setupButtonsForArmed");
        resetButtonsContainerVisibility();
        mArmedButtons.setVisibility(View.VISIBLE);
    }

    private void setupButtonsForFlying() {
        resetButtonsContainerVisibility();
        mInFlightButtons.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean isSlidingUpPanelEnabled(Drone drone) {
        final State droneState = drone.getState();

        return drone.getMavClient().isConnected() && droneState.isArmed()
                && droneState.isFlying();
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
        newDroneFilter.addAction("NEW_DRONE_COPTER");
        getActivity().registerReceiver(broadcastReceiver, newDroneFilter);
        final IntentFilter newDroneSelectedFilter = new IntentFilter();
        newDroneSelectedFilter.addAction("NEW_DRONE_SELECTED_COPTER");
        getActivity().registerReceiver(broadcastReceiver, newDroneSelectedFilter);
    }

    private void newDroneSelected(int droneID)
    {
        if(drone!=null)
            drone.removeDroneListener(this);

        DroidPlannerApp droidPlannerApp = (DroidPlannerApp) getActivity().getApplication();
        drone = droidPlannerApp.getDrone(droneID);

        if(drone!=null) {
            drone.addDroneListener(this);
            //addBroadcastFilters();

            missionProxy = droidPlannerApp.getMissionProxy();

            setupButtonsByFlightState();
            updateFlightModeButtons();
            updateFollowButton();


        }


    }


    public void initializeAccordingToMap(View view)
    {
        if(getArguments()!=null)
        {
            Log.d(BUTTON, "Initialize according to MAP!!! num_map:" + getArguments().getInt("num_map"));
            switch(getArguments().getInt("num_map"))
            {
                case 1:
                    initButton1(view);
                    break;

                case 2:
                    initButton2(view);
                    break;
                case 3:
                    initButton3(view);
                    break;
                case 4:
                    initButton4(view);
                    break;

            }
        }
    }

    public void initButton1(View view)
    {
        mDisconnectedButtons = view.findViewById(R.id.mc_disconnected_buttons);
        mDisarmedButtons = view.findViewById(R.id.mc_disarmed_buttons);
        mArmedButtons = view.findViewById(R.id.mc_armed_buttons);
        mInFlightButtons = view.findViewById(R.id.mc_in_flight_buttons);

        final Button connectBtn = (Button) view.findViewById(R.id.mc_connectBtn);
        connectBtn.setOnClickListener(this);

        homeBtn = (Button) view.findViewById(R.id.mc_homeBtn);
        homeBtn.setOnClickListener(this);

        final Button armBtn = (Button) view.findViewById(R.id.mc_armBtn);
        armBtn.setOnClickListener(this);

        final Button disarmBtn = (Button) view.findViewById(R.id.mc_disarmBtn);
        disarmBtn.setOnClickListener(this);

        landBtn = (Button) view.findViewById(R.id.mc_land);
        landBtn.setOnClickListener(this);

        final Button takeoffBtn = (Button) view.findViewById(R.id.mc_takeoff);
        takeoffBtn.setOnClickListener(this);

        pauseBtn = (Button) view.findViewById(R.id.mc_pause);
        pauseBtn.setOnClickListener(this);

        autoBtn = (Button) view.findViewById(R.id.mc_autoBtn);
        autoBtn.setOnClickListener(this);

        final Button takeoffInAuto = (Button) view.findViewById(R.id.mc_TakeoffInAutoBtn);
        takeoffInAuto.setOnClickListener(this);

        followBtn = (Button) view.findViewById(R.id.mc_follow);
        followBtn.setOnClickListener(this);

        final Button dronieBtn = (Button) view.findViewById(R.id.mc_dronieBtn);
        dronieBtn.setOnClickListener(this);
    }

    public void initButton2(View view)
    {
        Log.d(BUTTON, "init button 2!!!!!!");

        mDisconnectedButtons = view.findViewById(R.id.mc_disconnected_buttons2);
        mDisarmedButtons = view.findViewById(R.id.mc_disarmed_buttons2);
        mArmedButtons = view.findViewById(R.id.mc_armed_buttons2);
        mInFlightButtons = view.findViewById(R.id.mc_in_flight_buttons2);

        final Button connectBtn = (Button) view.findViewById(R.id.mc_connectBtn2);
        connectBtn.setOnClickListener(this);

        homeBtn = (Button) view.findViewById(R.id.mc_homeBtn2);
        homeBtn.setOnClickListener(this);

        final Button armBtn = (Button) view.findViewById(R.id.mc_armBtn2);
        armBtn.setOnClickListener(this);

        final Button disarmBtn = (Button) view.findViewById(R.id.mc_disarmBtn2);
        disarmBtn.setOnClickListener(this);

        landBtn = (Button) view.findViewById(R.id.mc_land2);
        landBtn.setOnClickListener(this);

        final Button takeoffBtn = (Button) view.findViewById(R.id.mc_takeoff2);
        takeoffBtn.setOnClickListener(this);

        pauseBtn = (Button) view.findViewById(R.id.mc_pause2);
        pauseBtn.setOnClickListener(this);

        autoBtn = (Button) view.findViewById(R.id.mc_autoBtn2);
        autoBtn.setOnClickListener(this);

        final Button takeoffInAuto = (Button) view.findViewById(R.id.mc_TakeoffInAutoBtn2);
        takeoffInAuto.setOnClickListener(this);

        followBtn = (Button) view.findViewById(R.id.mc_follow2);
        followBtn.setOnClickListener(this);

        final Button dronieBtn = (Button) view.findViewById(R.id.mc_dronieBtn2);
        dronieBtn.setOnClickListener(this);
    }

    public void initButton3(View view)
    {
        mDisconnectedButtons = view.findViewById(R.id.mc_disconnected_buttons3);
        mDisarmedButtons = view.findViewById(R.id.mc_disarmed_buttons3);
        mArmedButtons = view.findViewById(R.id.mc_armed_buttons3);
        mInFlightButtons = view.findViewById(R.id.mc_in_flight_buttons3);

        final Button connectBtn = (Button) view.findViewById(R.id.mc_connectBtn3);
        connectBtn.setOnClickListener(this);

        homeBtn = (Button) view.findViewById(R.id.mc_homeBtn3);
        homeBtn.setOnClickListener(this);

        final Button armBtn = (Button) view.findViewById(R.id.mc_armBtn3);
        armBtn.setOnClickListener(this);

        final Button disarmBtn = (Button) view.findViewById(R.id.mc_disarmBtn3);
        disarmBtn.setOnClickListener(this);

        landBtn = (Button) view.findViewById(R.id.mc_land3);
        landBtn.setOnClickListener(this);

        final Button takeoffBtn = (Button) view.findViewById(R.id.mc_takeoff3);
        takeoffBtn.setOnClickListener(this);

        pauseBtn = (Button) view.findViewById(R.id.mc_pause3);
        pauseBtn.setOnClickListener(this);

        autoBtn = (Button) view.findViewById(R.id.mc_autoBtn3);
        autoBtn.setOnClickListener(this);

        final Button takeoffInAuto = (Button) view.findViewById(R.id.mc_TakeoffInAutoBtn3);
        takeoffInAuto.setOnClickListener(this);

        followBtn = (Button) view.findViewById(R.id.mc_follow3);
        followBtn.setOnClickListener(this);

        final Button dronieBtn = (Button) view.findViewById(R.id.mc_dronieBtn3);
        dronieBtn.setOnClickListener(this);
    }

    public void initButton4(View view)
    {
        mDisconnectedButtons = view.findViewById(R.id.mc_disconnected_buttons4);
        mDisarmedButtons = view.findViewById(R.id.mc_disarmed_buttons4);
        mArmedButtons = view.findViewById(R.id.mc_armed_buttons4);
        mInFlightButtons = view.findViewById(R.id.mc_in_flight_buttons4);

        final Button connectBtn = (Button) view.findViewById(R.id.mc_connectBtn4);
        connectBtn.setOnClickListener(this);

        homeBtn = (Button) view.findViewById(R.id.mc_homeBtn4);
        homeBtn.setOnClickListener(this);

        final Button armBtn = (Button) view.findViewById(R.id.mc_armBtn4);
        armBtn.setOnClickListener(this);

        final Button disarmBtn = (Button) view.findViewById(R.id.mc_disarmBtn4);
        disarmBtn.setOnClickListener(this);

        landBtn = (Button) view.findViewById(R.id.mc_land4);
        landBtn.setOnClickListener(this);

        final Button takeoffBtn = (Button) view.findViewById(R.id.mc_takeoff4);
        takeoffBtn.setOnClickListener(this);

        pauseBtn = (Button) view.findViewById(R.id.mc_pause4);
        pauseBtn.setOnClickListener(this);

        autoBtn = (Button) view.findViewById(R.id.mc_autoBtn4);
        autoBtn.setOnClickListener(this);

        final Button takeoffInAuto = (Button) view.findViewById(R.id.mc_TakeoffInAutoBtn4);
        takeoffInAuto.setOnClickListener(this);

        followBtn = (Button) view.findViewById(R.id.mc_follow4);
        followBtn.setOnClickListener(this);

        final Button dronieBtn = (Button) view.findViewById(R.id.mc_dronieBtn4);
        dronieBtn.setOnClickListener(this);
    }


    public void newDroneCopter(int droneId) {


            if(MultipleActivity.getDroneIDFromMap(getArguments().getInt("num_map"))== droneId)
            {
                drone = ((DroidPlannerApp) getActivity().getApplication()).getDroneList().get(droneId);
                drone.addDroneListener(this);
            }

    }
}
