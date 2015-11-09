package org.droidplanner.android.fragments.mode;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.MultipleActivity;
import org.droidplanner.android.activities.helpers.SuperUI;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.model.Drone;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.MAVLink.Messages.ApmModes;

/**
 * Implements the flight/apm mode panel description.
 */
public class FlightModePanel extends Fragment implements OnDroneListener {

	/**
	 * This is the parent activity for this fragment.
	 */
	private SuperUI mParentActivity;

    private static final String FLIGHTMODE = "FLIGHTMODE";
    private boolean typeIset = false;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case "NEW_DRONE":
                    Log.d(FLIGHTMODE, "FlightModePanel - NEW_DRONE");

                 //   int droneID = intent.getExtras().getInt("droneID");
                 //   if(droneID == MultipleActivity.getDroneIDFromMap(getArguments().getInt("num_map")))
                        newDroneFlightMode(intent.getExtras().getInt("droneID"));
                    break;
                case "NEW_DRONE_SELECTED":
                    Log.d(FLIGHTMODE, "FlightModePanel - NEW_DRONE_SELECTED");
                    newDroneFlightMode(intent.getExtras().getInt("droneID"));
                    break;
                case "NEW_TYPE":
                    //Log.d(FLIGHTMODE, "FlightModePanel - NEW TYPE");
                  // if(!typeIset) {
              //         onModeUpdate(mParentActivity.app.getDrone(intent.getExtras().getInt("droneID")).getState().getMode());
                     //   typeIset = true;
                   //}
                    break;
            }
        }
    };


    public FlightModePanel()
    {

    }


    public static FlightModePanel newInstance(int num_map) {
        FlightModePanel fragment = new FlightModePanel();

        Bundle args = new Bundle();
        args.putInt("num_map", num_map);
        fragment.setArguments(args);

        return fragment;
    }

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

        if (!(activity instanceof SuperUI)) {
			throw new IllegalStateException("Parent activity must be an instance of "
					+ SuperUI.class.getName());
		}

		mParentActivity = (SuperUI) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mParentActivity = null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(FLIGHTMODE, "FlightModePanel - ON CREATE VIEW !!!!!");

        switch(getArguments().getInt("num_map"))
        {
            case 1:return inflater.inflate(R.layout.fragment_flight_mode_panel, container, false);
            case 2:return inflater.inflate(R.layout.fragment_flight_mode_panel2, container, false);
            case 3:return inflater.inflate(R.layout.fragment_flight_mode_panel3, container, false);
            case 4:return inflater.inflate(R.layout.fragment_flight_mode_panel4, container, false);

            default: return inflater.inflate(R.layout.fragment_flight_mode_panel, container, false);
        }

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        Log.d(FLIGHTMODE, "FlightModePanel - ON ACTIVITY CREATED!!!!!");


        Drone drone = null;
        int droneID;
        if(getActivity() !=null) {
            // Update the mode info panel based on the current mode.
            switch (getArguments().getInt("num_map")) {
                case 1:
                    droneID = MultipleActivity.getDroneIDFromMap(1);
                    drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone(droneID);
                    //onModeUpdate(drone.getState().getMode());
                    break;
                case 2:
                    droneID = MultipleActivity.getDroneIDFromMap(2);
                    drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone(droneID);
                    //onModeUpdate(drone.getState().getMode());
                    break;
                case 3:
                    droneID = MultipleActivity.getDroneIDFromMap(3);
                    drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone(droneID);
                    //onModeUpdate(drone.getState().getMode());
                    break;
                case 4:
                    droneID = MultipleActivity.getDroneIDFromMap(4);
                    drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone(droneID);
                    //onModeUpdate(drone.getState().getMode());
                    break;
            }
        }

        Log.d(FLIGHTMODE, "FlightModePanel - onActivityCreated() - chamando onModeUpdate -- map_num => " + getArguments().getInt("num_map"));
        if(drone!=null)
            onModeUpdate(drone.getState().getMode());

    }

    @Override
	public void onStart() {

        Log.d(FLIGHTMODE, "FlightModePanel - ON start !!!!!");
		super.onStart();

        addBroadcastFilters();

        Drone drone = null;
        int droneID;
        if(getActivity() !=null) {
            // Update the mode info panel based on the current mode.
            switch (getArguments().getInt("num_map")) {
                case 1:
                    droneID = MultipleActivity.getDroneIDFromMap(1);
                    drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone(droneID);
                    //drone.addDroneListener(this);
                    break;
                case 2:
                    droneID = MultipleActivity.getDroneIDFromMap(2);
                    drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone(droneID);
                    //drone.addDroneListener(this);
                    break;
                case 3:
                    droneID = MultipleActivity.getDroneIDFromMap(3);
                    drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone(droneID);
                   // drone.addDroneListener(this);
                    break;
                case 4:
                    droneID = MultipleActivity.getDroneIDFromMap(4);
                    drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone(droneID);
                    //drone.addDroneListener(this);
                    break;
            }
        }

        if(drone!=null)
            drone.addDroneListener(this);
    }

    @Override
	public void onStop() {
		super.onStop();

        getActivity().unregisterReceiver(broadcastReceiver);

        Drone drone = null;
        int droneID;
        if(getActivity() !=null) {
            // Update the mode info panel based on the current mode.
            switch (getArguments().getInt("num_map")) {
                case 1:
                    droneID = MultipleActivity.getDroneIDFromMap(1);
                    drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone(droneID);
                    //drone.removeDroneListener(this);
                    break;
                case 2:
                    droneID = MultipleActivity.getDroneIDFromMap(2);
                    drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone(droneID);
                    //drone.removeDroneListener(this);
                    break;
                case 3:
                    droneID = MultipleActivity.getDroneIDFromMap(3);
                    drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone(droneID);
                    //drone.removeDroneListener(this);
                    break;
                case 4:
                    droneID = MultipleActivity.getDroneIDFromMap(4);
                    drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone(droneID);
                    //drone.removeDroneListener(this);
                    break;
            }
        }

        if(drone!=null)
            drone.removeDroneListener(this);
	}

	@Override
	public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {


        if(MultipleActivity.getDroneIDFromMap(getArguments().getInt("num_map")) == drone.getDroneID())
        {
            switch (event) {
            case CONNECTED:
            case DISCONNECTED:
            case MODE:
            case TYPE:
            case FOLLOW_START:
            case FOLLOW_STOP:
                // Update the mode info panel
                Log.d(FLIGHTMODE, "FlightModePanel - onDroneEvent - chamando onModeUpdate");
                onModeUpdate(drone.getState().getMode());
                break;
            default:
                break;
            }
        }

    }

	private void onModeUpdate(ApmModes mode) {
		// Update the info panel fragment
		Fragment infoPanel;

		if (mParentActivity == null || !mParentActivity.drone.getMavClient().isConnected()) {
			infoPanel = new ModeDisconnectedFragment();
		} else {
			switch (mode) {
			case ROTOR_RTL:
			case FIXED_WING_RTL:
			case ROVER_RTL:
				infoPanel = new ModeRTLFragment();
				break;

			case ROTOR_AUTO:
			case FIXED_WING_AUTO:
			case ROVER_AUTO:
				infoPanel = new ModeAutoFragment();
				break;

			case ROTOR_LAND:
				infoPanel = new ModeLandFragment();
				break;

			case ROTOR_LOITER:
			case FIXED_WING_LOITER:
				infoPanel = new ModeLoiterFragment();
				break;

			case ROTOR_STABILIZE:
			case FIXED_WING_STABILIZE:
				infoPanel = new ModeStabilizeFragment();
				break;

			case ROTOR_ACRO:
				infoPanel = new ModeAcroFragment();
				break;

			case ROTOR_ALT_HOLD:
				infoPanel = new ModeAltholdFragment();
				break;

			case ROTOR_CIRCLE:
			case FIXED_WING_CIRCLE:
				infoPanel = new ModeCircleFragment();
				break;

			case ROTOR_GUIDED:
			case FIXED_WING_GUIDED:
			case ROVER_GUIDED:
				if (((DroidPlannerApp) getActivity().getApplication()).getFollowMe().isEnabled()) {
					infoPanel = new ModeFollowFragment();
				} else {
                    Log.d(FLIGHTMODE, "FlightModePanel - onModeUpdate - ModeGuidedFragment");
					infoPanel = ModeGuidedFragment.newInstance(getArguments().getInt("num_map"));
				}
				break;

			case ROTOR_TOY:
				infoPanel = new ModeDriftFragment();
				break;

			case ROTOR_SPORT:
				infoPanel = new ModeSportFragment();
				break;

			case ROTOR_POSHOLD:
				infoPanel = new ModePosHoldFragment();
				break;

			default:
				infoPanel = new ModeDisconnectedFragment();
				break;
			}
		}


        switch(getArguments().getInt("num_map"))
        {
            case 1:
                getChildFragmentManager().beginTransaction().replace(R.id.modeInfoPanel, infoPanel)
                        .commit();
                break;
            case 2:
                getChildFragmentManager().beginTransaction().replace(R.id.modeInfoPanel2, infoPanel)
                        .commit();
                break;
            case 3:
                getChildFragmentManager().beginTransaction().replace(R.id.modeInfoPanel3, infoPanel)
                        .commit();
                break;
            case 4:
                getChildFragmentManager().beginTransaction().replace(R.id.modeInfoPanel4, infoPanel)
                        .commit();
                break;
        }


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
        final IntentFilter newTypeFilter = new IntentFilter();
        newTypeFilter.addAction("NEW_TYPE");
        getActivity().registerReceiver(broadcastReceiver, newTypeFilter);
    }

    public void newDroneFlightMode(int droneID)
    {
            if(getActivity() != null)
            {
                Drone drone = ((DroidPlannerApp) getActivity().getApplication()).getDroneList().get(droneID);
                drone.removeDroneListener(this);
                drone.addDroneListener(this);
                onModeUpdate(drone.getState().getMode());
            }


    }

}