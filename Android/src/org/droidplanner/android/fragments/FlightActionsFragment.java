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

    Drone drone;

    private static final String NEW_DRONE = "NEW_DRONE";
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case "NEW_DRONE":
                    Log.d(NEW_DRONE, "FlightActionsFragment - NEW_DRONE");
                    setNewDrone();
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        addBroadcastFilters();
        return inflater.inflate(R.layout.fragment_flight_actions_bar, container, false);
    }

    @Override
    public void onStart(){
        super.onStart();

        //Drone drone = ((DroidPlannerApp)getActivity().getApplication()).getDrone();
        //selectActionsBar(drone.getType());
        //drone.addDroneListener(this);
    }

    @Override
    public void onStop(){
        super.onStop();

        //Drone drone = ((DroidPlannerApp)getActivity().getApplication()).getDrone();
        drone.removeDroneListener(this);
    }

    @Override
    public void onDroneEvent(DroneEventsType event, Drone drone) {
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
    }

    public void setNewDrone()
    {
        drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone();
        selectActionsBar(drone.getType());
        drone.addDroneListener(this);
    }
}
