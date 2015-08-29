package org.droidplanner.android.fragments.mode;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.widgets.spinnerWheel.CardWheelHorizontalView;
import org.droidplanner.android.widgets.spinnerWheel.adapters.NumericWheelAdapter;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.core.gcs.follow.FollowAlgorithm.FollowModes;
import org.droidplanner.core.model.Drone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class ModeFollowFragment extends ModeGuidedFragment implements
		OnItemSelectedListener, OnDroneListener {

	private Follow followMe;
	private Spinner spinner;
	private ArrayAdapter<FollowModes> adapter;

    private CardWheelHorizontalView mRadiusWheel;

    private static final String NEW_DRONE = "NEW_DRONE";
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case "NEW_DRONE":
                    Log.d(NEW_DRONE, "ModeFollowFragment - NEW_DRONE");
                    setNewDrone();
                    break;
            }
        }
    };

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//DroidPlannerApp app = (DroidPlannerApp) getActivity().getApplication();
		//followMe = app.getFollowMe();
		//drone = app.getDrone();

        addBroadcastFilters();

		return inflater.inflate(R.layout.fragment_mode_follow, container, false);
	}

	@Override
	public void onViewCreated(View parentView, Bundle savedInstanceState) {
        super.onViewCreated(parentView, savedInstanceState);

        final Context context = getActivity().getApplicationContext();

		final NumericWheelAdapter radiusAdapter = new NumericWheelAdapter(context,
                R.layout.wheel_text_centered, 0, 200, "%d m");

        mRadiusWheel = (CardWheelHorizontalView) parentView.findViewById(R.id.radius_spinner);
        mRadiusWheel.setViewAdapter(radiusAdapter);
        updateCurrentRadius();
        mRadiusWheel.addChangingListener(this);

		spinner = (Spinner) parentView.findViewById(R.id.follow_type_spinner);
		adapter = new ArrayAdapter<FollowModes>(getActivity(),
				android.R.layout.simple_spinner_item, FollowModes.values());
		spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        //drone.addDroneListener(this);
	}

    @Override
    public void onDestroyView(){
        super.onDestroyView();

        if(mRadiusWheel != null){
            mRadiusWheel.removeChangingListener(this);
        }
    }

    @Override
    public void onChanged(CardWheelHorizontalView cardWheel, int oldValue, int newValue){
        switch(cardWheel.getId()){
            case R.id.radius_spinner:
                followMe.changeRadius(newValue);
                break;

            default:
                super.onChanged(cardWheel, oldValue, newValue);
                break;
        }
    }

    private void updateCurrentRadius(){
        if(mRadiusWheel != null){
            mRadiusWheel.setCurrentValue((int) followMe.getRadius().valueInMeters());
        }
    }

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		followMe.setType(adapter.getItem(position));
		updateCurrentRadius();
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case FOLLOW_CHANGE_TYPE:
			spinner.setSelection(adapter.getPosition(followMe.getType()));
			break;
		default:
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
    }

    public void setNewDrone()
    {
        DroidPlannerApp app = (DroidPlannerApp) getActivity().getApplication();
        followMe = app.getFollowMe();
        drone = app.getDrone();

        drone.addDroneListener(this);



    }

}
