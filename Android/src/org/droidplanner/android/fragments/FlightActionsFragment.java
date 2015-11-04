package org.droidplanner.android.fragments;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.drone.variables.Type;
import org.droidplanner.core.model.Drone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FlightActionsFragment extends Fragment implements OnDroneListener {

    interface SlidingUpHeader{
        boolean isSlidingUpPanelEnabled(Drone drone);
    }

    private SlidingUpHeader header;

    private static final String FLIGHTACTIONS = "FLIGHTACTIONS";
    private int num_map = -1;

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
                case "TOWER_DISCONNECTED":
                    Log.d(NEW_DRONE, "TelemetryFragments - TOWER_DISCONNECTED");

                    break;
                case "NEW_DRONE_SELECTED":
                    Log.d(NEW_DRONE, "TelemetryFragments - NEW_DRONE_SELECTED");
                    newDroneSelected(intent.getExtras().getInt("droneID"));
                    break;
            }
        }
    };

    public FlightActionsFragment()
    {

    }

    public static FlightActionsFragment newInstance(int num_map) {
        FlightActionsFragment fragment = new FlightActionsFragment();

        Bundle args = new Bundle();
        args.putInt("num_map", num_map);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_flight_actions_bar, container, false);
    }

    @Override
    public void onStart(){
        Log.d(FLIGHTACTIONS, "ONSTART");
        super.onStart();
        addBroadcastFilters();

        Drone drone = ((DroidPlannerApp)getActivity().getApplication()).getDrone();
        selectActionsBar(drone.getType());
        drone.addDroneListener(this);
    }

    @Override
    public void onStop(){
        super.onStop();

        Drone drone = ((DroidPlannerApp)getActivity().getApplication()).getDrone();
        drone.removeDroneListener(this);
        getActivity().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onDroneEvent(DroneEventsType event, Drone drone) {
        if(getArguments()!=null)
            Log.d(FLIGHTACTIONS, "NUM_MAP => " + getArguments().getInt("num_map"));
        else
            Log.d(FLIGHTACTIONS, "NUM_MAP => " + -1);

        switch(event){
            case TYPE:
                final int droneType = drone.getType();
                selectActionsBar(droneType);
                break;
        }
    }

    private void selectActionsBar(int droneType) {
        final FragmentManager fm = getChildFragmentManager();

        Fragment actionsBarFragment;
        if(Type.isCopter(droneType)){
            actionsBarFragment = new CopterFlightActionsFragment();
        }
        else if(Type.isPlane(droneType)){
            actionsBarFragment = new PlaneFlightActionsFragment();
        }
        else{
            actionsBarFragment = new GenericActionsFragment();
        }

        fm.beginTransaction().replace(R.id.flight_actions_bar, actionsBarFragment).commit();
        header = (SlidingUpHeader) actionsBarFragment;
    }

    public boolean isSlidingUpPanelEnabled(Drone drone){
        return header != null && header.isSlidingUpPanelEnabled(drone);
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


    public void newDroneSelected(int droneId)
    {
        Drone drone = ((DroidPlannerApp)getActivity().getApplication()).getDroneList().get(droneId);
        if(drone!=null) {
            selectActionsBar(drone.getType());
            drone.addDroneListener(this);
        }
    }

    public void attatchMap(int num_map)
    {
        this.num_map = num_map;
    }
}
