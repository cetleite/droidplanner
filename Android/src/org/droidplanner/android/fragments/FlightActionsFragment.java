package org.droidplanner.android.fragments;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.MultipleActivity;
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
    private boolean typeIsSet = false;

    private static final String NEW_DRONE = "NEW_DRONE";
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case "NEW_DRONE":
                    Log.d(NEW_DRONE, "FlightActionsFragment - NEW_DRONE");
                    newDroneFlightFragment(intent.getExtras().getInt("droneID"));
                    break;
                case "TOWER_DISCONNECTED":
                    Log.d(NEW_DRONE, "FlightActionsFragment - TOWER_DISCONNECTED");

                    break;
                case "NEW_DRONE_SELECTED":
                    Log.d(NEW_DRONE, "FlightActionsFragment - NEW_DRONE_SELECTED");
                    newDroneSelected(intent.getExtras().getInt("droneID"));
                    break;
                case "NEW_TYPE":
                    //Log.d(NEW_DRONE, "FlightActionsFragment - NEW_TYPE === droneID: " + intent.getExtras().getInt("droneID"));
                    newType(intent.getExtras().getInt("droneID"));
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

        if(getArguments()!=null)
            selectActionsBar(drone.getType(), getArguments().getInt("num_map"));
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
        //if(getArguments()!=null)
        //    Log.d(FLIGHTACTIONS, "NUM_MAP => " + getArguments().getInt("num_map"));
        //else
        //    Log.d(FLIGHTACTIONS, "NUM_MAP => " + -1);

        Log.d(FLIGHTACTIONS, "DRONE ID => " + drone.getDroneID() + " ---- MAP_NUM => " + getArguments().getInt("num_map"));

        if(getArguments() != null)
        {
            int droneID;
            switch(getArguments().getInt("num_map"))
            {
                case 1:
                    droneID = MultipleActivity.getDroneIDFromMap(1);
                    drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone(droneID);
                    break;
                case 2:
                    droneID = MultipleActivity.getDroneIDFromMap(2);
                    if(droneID != -1)
                        drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone(droneID);
                    break;
                case 3:
                    droneID = MultipleActivity.getDroneIDFromMap(3);
                    if(droneID != -1)
                        drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone(droneID);
                    break;
                case 4:
                    droneID = MultipleActivity.getDroneIDFromMap(4);
                    if(droneID != -1)
                        drone = ((DroidPlannerApp) getActivity().getApplication()).getDrone(droneID);
                    break;
            }
        }

        if(drone!=null) {
            switch (event) {
                case TYPE:
                    Log.d(FLIGHTACTIONS, "Case TYpe!!! =-=-=-=->dID:  " + drone.getDroneID());
                    final int droneType = drone.getType();
                    selectActionsBar(droneType, getArguments().getInt("num_map"));
                    break;

            }
        }
    }

    private void selectActionsBar(int droneType, int num_map) {
        final FragmentManager fm = getChildFragmentManager();

        Fragment actionsBarFragment;
        if(Type.isCopter(droneType)){
            Log.d(FLIGHTACTIONS, "type COPTER !NUM_MAP => " + num_map);
            Log.d(NEWNEWNEW, "Match! ==> COPTERdroneTYPE:  " + droneType + " -- map: " + getArguments().getInt("num_map"));
            actionsBarFragment =  CopterFlightActionsFragment.newInstance(num_map);
        }
        else if(Type.isPlane(droneType)){
            actionsBarFragment = new PlaneFlightActionsFragment();
        }
        else{
            Log.d(FLIGHTACTIONS, "tpye >GENERIC< !NUM_MAP => " + num_map);
            Log.d(NEWNEWNEW, "Match! ==> GENERICdroneTYPE:  " + droneType + " -- map: " + getArguments().getInt("num_map"));
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
        final IntentFilter newTypeFilter = new IntentFilter();
        newTypeFilter.addAction("NEW_TYPE");
        getActivity().registerReceiver(broadcastReceiver, newTypeFilter);
    }


    public void newDroneSelected(int droneId)
    {
            Log.d(FLIGHTACTIONS, "Match!");
            Drone drone = ((DroidPlannerApp) getActivity().getApplication()).getDroneList().get(droneId);
            // if(drone!=null) {
            if (getArguments() != null)
                selectActionsBar(drone.getType(), getArguments().getInt("num_map"));
            drone.addDroneListener(this);
            //}

    }

    private String NEWNEWNEW = "NEWNEWNEW";
    public void newDroneFlightFragment(int droneId)
    {
        Log.d(NEWNEWNEW, "newDroneFlightFragmnet ==> " + droneId + " map no: " + getArguments().getInt("num_map"));
        Drone drone = ((DroidPlannerApp) getActivity().getApplication()).getDroneList().get(droneId);
        drone.addDroneListener(this);


        if((MultipleActivity.getDroneIDFromMap(getArguments().getInt("num_map")) == droneId)) {
            selectActionsBar(drone.getType(), getArguments().getInt("num_map"));
            Log.d(NEWNEWNEW, "Match! ==> droneid:  " + droneId + " -- map: " + getArguments().getInt("num_map") + "drone type: " + drone.getType());

            Intent intent3 = new Intent("NEW_DRONE_COPTER");
            intent3.putExtra("droneID", droneId);
            getActivity().getApplicationContext().sendBroadcast(intent3);
        }


    }

    public void newType(int droneId)
    {
        Drone drone = ((DroidPlannerApp) getActivity().getApplication()).getDroneList().get(droneId);
        if(!typeIsSet) {
            if ((MultipleActivity.getDroneIDFromMap(getArguments().getInt("num_map")) == droneId)&& drone.getType()!= 0) {
                typeIsSet = true;
                Log.d(NEW_DRONE, "typeIsSet == true droneId: " + droneId + "  --  map_num: " + getArguments().getInt("num_map") + " ----- " + drone.getType());
                selectActionsBar(drone.getType(), getArguments().getInt("num_map"));
            }
        }

    }

}
