package org.droidplanner.android.fragments.mode;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
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


    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case "NEW_DRONE":
                    Log.d(FLIGHTMODE, "FlightModePanel - NEW_DRONE");
                    break;
                case "NEW_DRONE_SELECTED":
                    Log.d(FLIGHTMODE, "FlightModePanel - NEW_DRONE_SELECTED");
                    newDrone();
                    break;
            }
        }
    };


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
		return inflater.inflate(R.layout.fragment_flight_mode_panel, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Update the mode info panel based on the current mode.
		onModeUpdate(mParentActivity.drone.getState().getMode());

        Log.d(FLIGHTMODE, "FlightModePanel - onActivityCreated() - chamando onModeUpdate");

	}

	@Override
	public void onStart() {
		super.onStart();

        addBroadcastFilters();
		if (mParentActivity != null) {
			mParentActivity.drone.addDroneListener(this);
		}
	}

	@Override
	public void onStop() {
		super.onStop();

        getActivity().unregisterReceiver(broadcastReceiver);
		if (mParentActivity != null) {
			mParentActivity.drone.removeDroneListener(this);
		}
	}

	@Override
	public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        Log.d(FLIGHTMODE, "FlightModePanel - onDroneEvent");

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
					infoPanel = new ModeGuidedFragment();
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

		getChildFragmentManager().beginTransaction().replace(R.id.modeInfoPanel, infoPanel)
				.commit();
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

    public void newDrone()
    {
        if (mParentActivity != null) {
            mParentActivity.app.getDrone().removeDroneListener(this);
            Log.d(FLIGHTMODE, "Removeu listener");
        }

        if (mParentActivity != null) {
            mParentActivity.app.getDrone().addDroneListener(this);
            Log.d(FLIGHTMODE, "Adicionou listener");
        }


    }
}