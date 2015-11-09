package org.droidplanner.android.fragments.mode;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.MultipleActivity;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;
import org.droidplanner.core.drone.variables.GuidedPoint;
import org.droidplanner.core.model.Drone;

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

public class ModeGuidedFragment extends Fragment implements CardWheelHorizontalView.OnCardWheelChangedListener {

	public Drone drone;

    private CardWheelHorizontalView mAltitudeWheel;



    private static final String NEW_DRONE2 = "NEW_DRONE2";
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case "NEW_DRONE":
                    Log.d(NEW_DRONE2, "ModeGuidedFragment - NEW_DRONE");
                    break;
                case "NEW_DRONE_SELECTED":
                    Log.d(NEW_DRONE2, "ModeGuidedFragment - NEW_DRONE_SELECTED");
                    break;
            }
        }
    };


    public ModeGuidedFragment()
    {

    }


    public static ModeGuidedFragment newInstance(int num_map) {
        ModeGuidedFragment fragment = new ModeGuidedFragment();

        Bundle args = new Bundle();
        args.putInt("num_map", num_map);
        fragment.setArguments(args);

        return fragment;
    }



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d(NEW_DRONE2, "CRIOU GUIDED!!!!!");

	////	drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone();
        addBroadcastFilters();

        int num_map = getArguments().getInt("num_map");
        int droneID = MultipleActivity.getDroneIDFromMap(num_map);
        switch(num_map)
        {
            case 1:
                drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone(droneID);
                return inflater.inflate(R.layout.fragment_mode_guided, container, false);
            case 2:
                drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone(droneID);
                return inflater.inflate(R.layout.fragment_mode_guided2, container, false);
            case 3:
                drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone(droneID);
                return inflater.inflate(R.layout.fragment_mode_guided3, container, false);
            case 4:
                drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone(droneID);
                return inflater.inflate(R.layout.fragment_mode_guided4, container, false);
            default:
                drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone(droneID);
                return inflater.inflate(R.layout.fragment_mode_guided, container, false);
        }
	}

    @Override
	public void onViewCreated(View parentView, Bundle savedInstanceState) {
        super.onViewCreated(parentView, savedInstanceState);

        final NumericWheelAdapter altitudeAdapter = new NumericWheelAdapter(getActivity()
                .getApplicationContext(), R.layout.wheel_text_centered, 2, 200, "%d m");


        switch(getArguments().getInt("num_map"))
        {
            case 1: mAltitudeWheel = (CardWheelHorizontalView) parentView.findViewById(R.id.altitude_spinner);break;
            case 2: mAltitudeWheel = (CardWheelHorizontalView) parentView.findViewById(R.id.altitude_spinner2);break;
            case 3: mAltitudeWheel = (CardWheelHorizontalView) parentView.findViewById(R.id.altitude_spinner3);break;
            case 4: mAltitudeWheel = (CardWheelHorizontalView) parentView.findViewById(R.id.altitude_spinner4);break;
            default: mAltitudeWheel = (CardWheelHorizontalView) parentView.findViewById(R.id.altitude_spinner);break;
        }

    ///    mAltitudeWheel = (CardWheelHorizontalView) parentView.findViewById(R.id.altitude_spinner);
        mAltitudeWheel.setViewAdapter(altitudeAdapter);

        if(drone!=null) {
            final int initialValue = (int) Math.max(drone.getGuidedPoint().getAltitude()
                    .valueInMeters(), GuidedPoint.getMinAltitude(drone));
            mAltitudeWheel.setCurrentValue(initialValue);
            mAltitudeWheel.addChangingListener(this);
        }
	}

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        if(mAltitudeWheel != null) {
            mAltitudeWheel.removeChangingListener(this);
        }

        getActivity().unregisterReceiver(broadcastReceiver);

    }

    @Override
    public void onChanged(CardWheelHorizontalView cardWheel, int oldValue, int newValue) {
        switch(cardWheel.getId()){
            case R.id.altitude_spinner:
            case R.id.altitude_spinner2:
            case R.id.altitude_spinner3:
            case R.id.altitude_spinner4:
                drone.getGuidedPoint().changeGuidedAltitude(newValue); break;
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
    }
}
