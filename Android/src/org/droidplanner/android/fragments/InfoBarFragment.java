package org.droidplanner.android.fragments;

import android.app.Activity;
import org.droidplanner.android.DroidPlannerApp;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import org.droidplanner.R;
import org.droidplanner.android.widgets.actionProviders.InfoBarItem;
import org.droidplanner.android.widgets.actionProviders.InfoBarItemMulti;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;


public class InfoBarFragment extends Fragment implements OnDroneListener {
    /**
     * Application context.
     */
    private Context mContext;// = getActivity().getApplicationContext();

    /**
     * Current drone state.
     */
    private Drone mDrone;

    /**
     * Action provider's view.
     */
    private View mView;

    /*
     * Info bar items
     */
    private InfoBarItemMulti.HomeInfo mHomeInfo;
    private InfoBarItemMulti.GpsInfo mGpsInfo;
    private InfoBarItemMulti.BatteryInfo mBatteryInfo;
    private InfoBarItemMulti.FlightTimeInfo mFlightTimeInfo;
    private InfoBarItemMulti.SignalInfo mSignalInfo;
    private InfoBarItemMulti.FlightModesInfo mFlightModesInfo;

    private static final String INFOBAR = "INFOBAR";
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case "NEW_DRONE":

                    break;
                case "NEW_DRONE_SELECTED":

                    break;
                case "NEW_TELEMETRY_INFO":

                    break;
            }
        }
    };


    public InfoBarFragment()
    {

    }


    public static InfoBarFragment newInstance(int num_map) {
        InfoBarFragment fragment = new InfoBarFragment();

        Bundle args = new Bundle();
        args.putInt("num_map", num_map);
        fragment.setArguments(args);



        return fragment;
    }


    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mContext = getActivity().getApplicationContext();
    }
    private final String INFOBAR2 = "INFOBAR2";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.info_bar_multi, container, false);
        setupActionView();
        updateInfoBar();



        if(mContext == null)
            Log.d(INFOBAR2, "context null");
        else
            Log.d(INFOBAR2, "context não null");



        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    /**
     * This is used to update the current drone state.
     *
     * @param drone
     */
    public void setDrone(Drone drone) {
        mDrone = drone;
    }

    public void setDroneById(int drone_id) {
        mDrone =  ((DroidPlannerApp) getActivity().getApplication()).getDrone(drone_id);
        mDrone.addDroneListener(this);
    }
    private static int count = 0;
    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {

        //Log.d(INFOBAR, "---InfoBarActionProveider  - onDroneEvent");
        setDrone(drone);

        boolean updateExtra = true;
        switch (event) {
            case BATTERY:
                if (mBatteryInfo != null)
                    mBatteryInfo.updateItemView(mContext, mDrone);
                break;

            case CONNECTED:
                updateInfoBar();
                updateExtra = false;
                break;

            case DISCONNECTED:
                setDrone(null);
                updateInfoBar();
                updateExtra = false;
                break;

            case GPS_FIX:
            case GPS_COUNT:
                if (mGpsInfo != null)
                    mGpsInfo.updateItemView(mContext, mDrone);
                break;

            case GPS:
            case HOME:
                if (mHomeInfo != null)
                    mHomeInfo.updateItemView(mContext, mDrone);
                break;

            case RADIO:
                if (mSignalInfo != null)
                    mSignalInfo.updateItemView(mContext, mDrone);
                break;

            case STATE:
                if (mFlightTimeInfo != null)
                    mFlightTimeInfo.updateItemView(mContext, mDrone);
                break;

            case MODE:
            case TYPE:
                Log.d(INFOBAR, "InfoBarActionProveider  - MODE-TYPE!");
                if (mFlightModesInfo != null)
                    mFlightModesInfo.updateItemView(mContext, mDrone);
                break;

            default:
                updateExtra = false;
                break;
        }

    }

    private void setupActionView() {
        int map_num;

        if(getArguments()!= null)
        {
            map_num = getArguments().getInt("num_map");


        mHomeInfo = new InfoBarItemMulti.HomeInfo(mContext, mView, mDrone, map_num);
        mGpsInfo = new InfoBarItemMulti.GpsInfo(mContext, mView, mDrone, map_num);
        mBatteryInfo = new InfoBarItemMulti.BatteryInfo(mContext, mView, mDrone, map_num);
        mFlightTimeInfo = new InfoBarItemMulti.FlightTimeInfo(mContext, mView, mDrone, map_num);
        mSignalInfo = new InfoBarItemMulti.SignalInfo(mContext, mView, mDrone, map_num);
        mFlightModesInfo = new InfoBarItemMulti.FlightModesInfo(mContext, mView, mDrone, map_num);
        }
        else
        {

        }
    }

    /**
     * This updates the info bar with the current drone state.
     */
    private void updateInfoBar() {
        if (mHomeInfo != null)
            mHomeInfo.updateItemView(mContext, mDrone);

        if (mGpsInfo != null)
            mGpsInfo.updateItemView(mContext, mDrone);

        if (mBatteryInfo != null)
            mBatteryInfo.updateItemView(mContext, mDrone);

        if (mFlightTimeInfo != null)
            mFlightTimeInfo.updateItemView(mContext, mDrone);

        if (mSignalInfo != null)
            mSignalInfo.updateItemView(mContext, mDrone);

        if (mFlightModesInfo != null)
            mFlightModesInfo.updateItemView(mContext, mDrone);

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

        final IntentFilter newTelemetryInfo = new IntentFilter();
        newTelemetryInfo.addAction("NEW_TELEMETRY_INFO");
        getActivity().registerReceiver(broadcastReceiver, newTelemetryInfo);
    }

}
