package org.droidplanner.android.activities;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;


import org.droidplanner.android.widgets.actionProviders.InfoBarActionProvider;
import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.fragments.FlightActionsFragment;
import org.droidplanner.android.fragments.FlightMapFragment;
import org.droidplanner.android.fragments.MultipleFragment;
import org.droidplanner.android.fragments.AlgorithmMenuFragment;
import org.droidplanner.android.fragments.TelemetryFragment;
import org.droidplanner.android.fragments.mode.FlightModePanel;
import org.droidplanner.android.utils.prefs.AutoPanMode;


import android.widget.Toast;

import org.droidplanner.android.fragments.InfoBarFragment;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.model.Drone;

import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.SlidingDrawer;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Iterator;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;


import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;


import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;


public class MultipleActivity extends DrawerNavigationUI implements MultipleFragment.OnFragmentInteractionListener, OnDroneListener,
        AlgorithmMenuFragment.OnFragmentInteractionListener{


    private static final int GOOGLE_PLAY_SERVICES_REQUEST_CODE = 101;
    private final AtomicBoolean mSlidingPanelCollapsing = new AtomicBoolean(false);
    private final AtomicBoolean mSlidingPanelCollapsing2 = new AtomicBoolean(false);
    private final AtomicBoolean mSlidingPanelCollapsing3 = new AtomicBoolean(false);
    private final AtomicBoolean mSlidingPanelCollapsing4 = new AtomicBoolean(false);

    private final SlidingUpPanelLayout.PanelSlideListener mDisablePanelSliding = new
            SlidingUpPanelLayout.PanelSlideListener() {
                @Override
                public void onPanelSlide(View view, float v) {


                    Log.d(ONDRONEEVENTZ, "SLIDING!!!! ");

                }

                @Override
                public void onPanelCollapsed(View view) {
                    mSlidingPanel.setSlidingEnabled(false);
                    mSlidingPanel.setPanelHeight(mFlightActionsView.getHeight());
                    mSlidingPanelCollapsing.set(false);

                    //Remove the panel slide listener
                    mSlidingPanel.setPanelSlideListener(null);
                }

                @Override
                public void onPanelExpanded(View view) {}

                @Override
                public void onPanelAnchored(View view) {}

                @Override
                public void onPanelHidden(View view) {}
            };

    private final SlidingUpPanelLayout.PanelSlideListener mDisablePanelSliding2 = new
            SlidingUpPanelLayout.PanelSlideListener() {
                @Override
                public void onPanelSlide(View view, float v) {


                    Log.d(ONDRONEEVENTZ, "SLIDING!!!! ");

                }

                @Override
                public void onPanelCollapsed(View view) {
                    mSlidingPanel.setSlidingEnabled(false);
                    mSlidingPanel.setPanelHeight(mFlightActionsView.getHeight());
                    mSlidingPanelCollapsing.set(false);

                    //Remove the panel slide listener
                    mSlidingPanel.setPanelSlideListener(null);
                }

                @Override
                public void onPanelExpanded(View view) {}

                @Override
                public void onPanelAnchored(View view) {}

                @Override
                public void onPanelHidden(View view) {}
            };

    private final SlidingUpPanelLayout.PanelSlideListener mDisablePanelSliding3 = new
            SlidingUpPanelLayout.PanelSlideListener() {
                @Override
                public void onPanelSlide(View view, float v) {


                    Log.d(ONDRONEEVENTZ, "SLIDING!!!! ");

                }

                @Override
                public void onPanelCollapsed(View view) {
                    mSlidingPanel.setSlidingEnabled(false);
                    mSlidingPanel.setPanelHeight(mFlightActionsView.getHeight());
                    mSlidingPanelCollapsing.set(false);

                    //Remove the panel slide listener
                    mSlidingPanel.setPanelSlideListener(null);
                }

                @Override
                public void onPanelExpanded(View view) {}

                @Override
                public void onPanelAnchored(View view) {}

                @Override
                public void onPanelHidden(View view) {}
            };

    private final SlidingUpPanelLayout.PanelSlideListener mDisablePanelSliding4 = new
            SlidingUpPanelLayout.PanelSlideListener() {
                @Override
                public void onPanelSlide(View view, float v) {


                    Log.d(ONDRONEEVENTZ, "SLIDING!!!! ");

                }

                @Override
                public void onPanelCollapsed(View view) {
                    mSlidingPanel.setSlidingEnabled(false);
                    mSlidingPanel.setPanelHeight(mFlightActionsView.getHeight());
                    mSlidingPanelCollapsing.set(false);

                    //Remove the panel slide listener
                    mSlidingPanel.setPanelSlideListener(null);
                }

                @Override
                public void onPanelExpanded(View view) {}

                @Override
                public void onPanelAnchored(View view) {}

                @Override
                public void onPanelHidden(View view) {}
            };



    private FragmentManager fragmentManager;
    private TextView warningView;

    private FlightMapFragment mapFragment, mapFragment2, mapFragment3, mapFragment4;
    private TelemetryFragment telemetryFragment, telemetryFragment2, telemetryFragment3, telemetryFragment4;
    private View mLocationButtonsContainer, mLocationButtonsContainer2, mLocationButtonsContainer3,mLocationButtonsContainer4;
    private ImageButton mGoToMyLocation, mGoToMyLocation2, mGoToMyLocation3, mGoToMyLocation4;
    private ImageButton mExpandMap, mExpandMap2, mExpandMap3, mExpandMap4;
    private ImageButton mAllPOIs, mAllPOIs2, mAllPOIs3, mAllPOIs4;
    private ImageButton mGoToDroneLocation, mGoToDroneLocation2, mGoToDroneLocation3, mGoToDroneLocation4;
    private boolean mAllPOIsOpen = false, mAllPOIsOpen2 = false, mAllPOIsOpen3 = false, mAllPOIsOpen4 = false;
    private InfoBarFragment infoBar1, infoBar2, infoBar3, infoBar4;


    Fragment flightModePanel, flightModePanel2, flightModePanel3, flightModePanel4;


    private InfoBarActionProvider infoBar;
    private MenuItem infoBarMenu;

    private SlidingUpPanelLayout mSlidingPanel, mSlidingPanel2, mSlidingPanel3, mSlidingPanel4;
    private View mFlightActionsView,mFlightActionsView2,mFlightActionsView3,mFlightActionsView4;
    private FlightActionsFragment flightActions, flightActions2, flightActions3, flightActions4;

    private int NUM_MAPS = 0;
    private boolean mapExpanded = false;

    public static HashMap<Integer, Integer> mapToDroneIDAssociation = new HashMap<Integer, Integer>();

    private boolean showInfoBar = false;
    private int newSelectedDroneId = -1;


    private static final String NEW_DRONE = "NEW_DRONE";
    private static final String INFOBARMENU = "INFOBARMENU";
    private static final String ORDEM = "ORDEM";
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case "NEW_DRONE":
                    Log.d(NEW_DRONE, "MULTIPLEACTIVITY  -  RECEBEU BROADCAST!!!() - NEW_DRONE");
                    //mDrone = ((DroidPlannerApp) getActivity().getApplication()).getDrone();
                    newDroneMap(intent.getExtras().getInt("droneID"), true);
                    break;
                case "NEW_DRONE_SELECTED":
                    Log.d(NEW_DRONE, "MULTIPLEACTIVITY - NEW_DRONE_SELECTED");
                    //newDrone();
                    newDroneMapSelected(intent.getExtras().getInt("droneID"));
                    break;
            }
        }
    };




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(NEW_DRONE, "ORDEM  -  onCreate()!!!");
        Log.d(NEW_DRONE, "MULTIPLEACTIVITY - ON CREATE!!!!!  SIZE=====> " + ((DroidPlannerApp) getApplication()).getDroneList().size());


        multipleMapView(NUM_MAPS);

        updateLayout();

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(NEW_DRONE, "ORDEM  -  onStart()!!!");
        //updateMultipleContext();
        updateLayout();

        switch(NUM_MAPS)
        {
            case 1:
                setupMapFragment();
                break;
            case 2:
                setupMapFragment();
                setupMapFragment2();
                break;
            case 3:
                setupMapFragment();
                setupMapFragment2();
                setupMapFragment3();
                break;
            case 4:
                setupMapFragment();
                setupMapFragment2();
                setupMapFragment3();
                setupMapFragment4();
                break;

        }

        addBroadcastFilters();

        refreshVariables();

    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.d(NEW_DRONE, "ORDEM  -  onResume()!!!  NUM_MAP = " + NUM_MAPS);
        //updateMultipleContext();
        updateLayout();
    }

    public void updateMultipleContext()
    {
        HashMap<Integer, Drone> droneList;
        droneList = ((DroidPlannerApp) getApplication()).getDroneList();

        if(NUM_MAPS != droneList.size())
        {
            Log.d(NEW_DRONE, "ORDEM  -  refazendo!()!!!");
            NUM_MAPS = 0;
            mapToDroneIDAssociation.clear();
            Iterator<Integer> keySetIterator = droneList.keySet().iterator();

            Integer key = keySetIterator.next();
            newDroneSelected(droneList.get(key).getDroneID());

            while(keySetIterator.hasNext())
            {
                key = keySetIterator.next();
                newDroneMap(droneList.get(key).getDroneID(), false);
            }
        }

    }

    public void updateLayout()
    {
        LinearLayout layout1;
        FrameLayout layout2;
        ViewGroup view;
        switch(NUM_MAPS)
        {
            case 2:
                layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout3);
                layout1.setVisibility(LinearLayout.VISIBLE);
                layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout2);
                layout1.setVisibility(LinearLayout.VISIBLE);



                layout2 = (FrameLayout) findViewById(R.id.infoBar1_bar);
                layout2.setVisibility(FrameLayout.VISIBLE);
                layout2 = (FrameLayout) findViewById(R.id.infoBar2_bar);
                layout2.setVisibility(FrameLayout.VISIBLE);

                setInfoBarInvisible();


                view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer);
                view.setVisibility(LinearLayout.VISIBLE);
                view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer2);
                view.setVisibility(LinearLayout.VISIBLE);
                view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer3);
                view.setVisibility(LinearLayout.GONE);
                view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer4);
                view.setVisibility(LinearLayout.GONE);
                break;
            case 3:
                layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout2);
                layout1.setVisibility(LinearLayout.VISIBLE);
                layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout3);
                layout1.setVisibility(LinearLayout.VISIBLE);

                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout41);
                layout2.setVisibility(FrameLayout.VISIBLE);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout42);
                layout2.setVisibility(FrameLayout.VISIBLE);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout43);
                layout2.setVisibility(FrameLayout.VISIBLE);

                layout2 = (FrameLayout) findViewById(R.id.infoBar1_bar);
                layout2.setVisibility(FrameLayout.VISIBLE);
                layout2 = (FrameLayout) findViewById(R.id.infoBar2_bar);
                layout2.setVisibility(FrameLayout.VISIBLE);
                layout2 = (FrameLayout) findViewById(R.id.infoBar3_bar);
                layout2.setVisibility(FrameLayout.VISIBLE);

                view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer);
                view.setVisibility(LinearLayout.VISIBLE);
                view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer2);
                view.setVisibility(LinearLayout.VISIBLE);
                view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer3);
                view.setVisibility(LinearLayout.VISIBLE);
                view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer4);
                view.setVisibility(LinearLayout.GONE);

                setInfoBarInvisible();


                break;
            case 4:
                layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout2);
                layout1.setVisibility(LinearLayout.VISIBLE);
                layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout3);
                layout1.setVisibility(LinearLayout.VISIBLE);

                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout41);
                layout2.setVisibility(FrameLayout.VISIBLE);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout42);
                layout2.setVisibility(FrameLayout.VISIBLE);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout43);
                layout2.setVisibility(FrameLayout.VISIBLE);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout44);
                layout2.setVisibility(FrameLayout.VISIBLE);


                layout2 = (FrameLayout) findViewById(R.id.infoBar1_bar);
                layout2.setVisibility(FrameLayout.VISIBLE);
                layout2 = (FrameLayout) findViewById(R.id.infoBar2_bar);
                layout2.setVisibility(FrameLayout.VISIBLE);
                layout2 = (FrameLayout) findViewById(R.id.infoBar3_bar);
                layout2.setVisibility(FrameLayout.VISIBLE);
                layout2 = (FrameLayout) findViewById(R.id.infoBar4_bar);
                layout2.setVisibility(FrameLayout.VISIBLE);


                view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer);
                view.setVisibility(LinearLayout.VISIBLE);
                view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer2);
                view.setVisibility(LinearLayout.VISIBLE);
                view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer3);
                view.setVisibility(LinearLayout.VISIBLE);
                view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer4);
                view.setVisibility(LinearLayout.VISIBLE);

                setInfoBarInvisible();
                break;
            default:break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        Log.d(INFOBARMENU, "INFOBARMENU  -  Criando menu na MultipleActivity");


        getMenuInflater().inflate(R.menu.menu_super_activiy, menu);

        infoBarMenu = menu.findItem(R.id.menu_info_bar);
        if (infoBarMenu != null) {
            this.infoBar = (InfoBarActionProvider) infoBarMenu.getActionProvider();

            if (showInfoBar) {
                if(NUM_MAPS == 1)
                {
                    this.infoBar.setDrone(((DroidPlannerApp) getApplication()).getDrone());
                    infoBarMenu.setEnabled(true);
                    infoBarMenu.setVisible(true);
                    newSelectedDroneId = -1;
                }
                else {
                    this.infoBar.setDrone(((DroidPlannerApp) getApplication()).getDrone(newSelectedDroneId));
                    infoBarMenu.setEnabled(true);
                    infoBarMenu.setVisible(true);
                    newSelectedDroneId = -1;
                }
            } else {
                infoBarMenu.setEnabled(false);
                infoBarMenu.setVisible(false);
            }

        }


        return true;// super.onCreateOptionsMenu(menu);


    }

    public void setInfoBarVisible(int droneID) {
        showInfoBar = true;
        newSelectedDroneId = droneID;
        invalidateOptionsMenu();

    }

    public void setInfoBarInvisible() {
        showInfoBar = false;
        invalidateOptionsMenu();

    }


    public void multipleMapView(int num_maps)
    {
        updateMultipleMaps2(1);
        otherFragments(3);

    }

    public void updateMultipleMaps(int num_maps)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        MultipleFragment fragment1 = new MultipleFragment();
        MultipleFragment fragment2 = new MultipleFragment();
        MultipleFragment fragment3 = new MultipleFragment();
        MultipleFragment fragment4 = new MultipleFragment();


        LinearLayout layout1;
        FrameLayout layout2;
        switch(num_maps)
        {
            case 1:
                //setContentView(R.layout.activity_multiple1);
                setContentView(R.layout.activity_multiple);
                fragmentTransaction.add(R.id.multi_layout1, fragment1, "1");

                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout42);
                layout2.setVisibility(FrameLayout.GONE);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout43);
                layout2.setVisibility(FrameLayout.GONE);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout44);
                layout2.setVisibility(FrameLayout.GONE);

                layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout3);
                layout1.setVisibility(LinearLayout.GONE);
                break;
            case 2:
                //setContentView(R.layout.activity_multiple2);
                setContentView(R.layout.activity_multiple);
                fragmentTransaction.add(R.id.multi_layout1, fragment1, "1");
                fragmentTransaction.add(R.id.multi_layout2, fragment2, "2");


                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout43);
                layout2.setVisibility(FrameLayout.GONE);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout44);
                layout2.setVisibility(FrameLayout.GONE);


                break;
            case 3:
                //setContentView(R.layout.activity_multiple3);
                setContentView(R.layout.activity_multiple);
                fragmentTransaction.add(R.id.multi_layout1, fragment1, "1");
                fragmentTransaction.add(R.id.multi_layout2, fragment2, "2");
                fragmentTransaction.add(R.id.multi_layout3, fragment3, "3");


                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout44);
                layout2.setVisibility(FrameLayout.GONE);

                break;
            case 4:
                //setContentView(R.layout.activity_multiple4);
                setContentView(R.layout.activity_multiple);
                fragmentTransaction.add(R.id.multi_layout1, fragment1, "1");
                fragmentTransaction.add(R.id.multi_layout2, fragment2, "2");
                fragmentTransaction.add(R.id.multi_layout3, fragment3, "3");
                fragmentTransaction.add(R.id.multi_layout4, fragment4, "4");

                //AlgorithmMenuFragment mAlgorithmFragment = AlgorithmMenuFragment.newInstance(1);
                //fragmentTransaction.add(R.id.multi_layout1, mAlgorithmFragment, "menu1");

                break;
        }

        fragmentTransaction.commit();
    }


    public void updateMultipleMaps2(int num_maps)
    {
        setContentView(R.layout.activity_multiple);

        LinearLayout layout1;
        FrameLayout layout2;
        ViewGroup view;
        switch(num_maps)
        {
            case 1:
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout42);
                layout2.setVisibility(FrameLayout.GONE);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout43);
                layout2.setVisibility(FrameLayout.GONE);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout44);
                layout2.setVisibility(FrameLayout.GONE);

                layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout3);
                layout1.setVisibility(LinearLayout.GONE);

                view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer3);
                view.setVisibility(LinearLayout.GONE);

                break;
            case 2:
                //setContentView(R.layout.activity_multiple2);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout43);
                layout2.setVisibility(FrameLayout.GONE);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout44);
                layout2.setVisibility(FrameLayout.GONE);

                view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer3);
                view.setVisibility(LinearLayout.GONE);
                view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer4);
                view.setVisibility(LinearLayout.GONE);

                break;
            case 3:
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout44);
                layout2.setVisibility(FrameLayout.GONE);


                view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer4);
                view.setVisibility(LinearLayout.GONE);
                break;
            case 4:
                break;
        }
    }

    public void otherFragments(int num_maps)
    {
        switch(num_maps)
        {
            case 1:
                otherFragments1();
                break;
            case 2:
                otherFragments1();
                otherFragments2();
                break;
            case 3:
                otherFragments1();
                otherFragments2();
                otherFragments3();
                break;
            case 4:
                otherFragments1();
                otherFragments2();
                otherFragments3();
                otherFragments4();
                break;
        }
    }

    public void otherFragments1()
    {
        fragmentManager = getSupportFragmentManager();


        mSlidingPanel = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer);
        mSlidingPanel.setEnableDragViewTouchEvents(true);
        mSlidingPanel.setSlidingEnabled(true);

       // enableSlidingUpPanel(this.drone, 1);

        warningView = (TextView) findViewById(R.id.failsafeTextView);

        setupMapFragment();

        mLocationButtonsContainer = findViewById(R.id.location_button_container);
        mGoToMyLocation = (ImageButton) findViewById(R.id.my_location_button);
        mGoToDroneLocation = (ImageButton) findViewById(R.id.drone_location_button);
        mExpandMap = (ImageButton) findViewById(R.id.expand_map_button);
        mAllPOIs = (ImageButton) findViewById(R.id.all_waypoints_button);

        final ImageButton resetMapBearing = (ImageButton) findViewById(R.id.map_orientation_button);
        resetMapBearing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMapBearing(0, mapFragment);
            }
        });

        mExpandMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapFragment != null) {

                    expandMap(1);

                    updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation, mGoToDroneLocation, mapFragment);
                }
            }
        });

        mAllPOIs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapFragment != null) {

                    enableAlgorithmMenu(1);


                    updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation, mGoToDroneLocation, mapFragment);
                }
            }
        });


        mGoToMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapFragment != null) {
                    mapFragment.goToMyLocation();
                    updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation, mGoToDroneLocation, mapFragment);
                }
            }
        });
        mGoToMyLocation.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mapFragment != null) {
                    mapFragment.goToMyLocation();
                    updateMapLocationButtons(AutoPanMode.USER, mGoToMyLocation, mGoToDroneLocation, mapFragment);
                    return true;
                }
                return false;
            }
        });

        mGoToDroneLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapFragment != null) {
                    mapFragment.goToDroneLocation();
                    updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation, mGoToDroneLocation, mapFragment);
                }
            }
        });
        mGoToDroneLocation.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mapFragment != null) {
                    mapFragment.goToDroneLocation();
                    updateMapLocationButtons(AutoPanMode.DRONE, mGoToMyLocation, mGoToDroneLocation, mapFragment);
                    return true;
                }
                return false;
            }
        });


        flightActions = (FlightActionsFragment) fragmentManager.findFragmentById(R.id
                .flightActionsFragment1);
        if (flightActions == null) {
            flightActions = FlightActionsFragment.newInstance(1);
            fragmentManager.beginTransaction().add(R.id.flightActionsFragment1, flightActions).commit();
        }

        mFlightActionsView = findViewById(R.id.flightActionsFragment1);
        mFlightActionsView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver
                .OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(!mSlidingPanelCollapsing.get()) {
                    mSlidingPanel.setPanelHeight(mFlightActionsView.getHeight());
                }
            }
        });


        // Add the telemetry fragment
        telemetryFragment = (TelemetryFragment) fragmentManager.findFragmentById(R.id.telemetryFragment);
        if (telemetryFragment == null) {
            telemetryFragment = TelemetryFragment.newInstance(1);
            fragmentManager.beginTransaction()
                    .add(R.id.telemetryFragment, telemetryFragment)
                    .commit();
        }

        infoBar1 = (InfoBarFragment) fragmentManager.findFragmentById(R.id.infoBar1_bar);
        if(infoBar1 == null)
        {
            infoBar1 = InfoBarFragment.newInstance(1);
            fragmentManager.beginTransaction()
                    .add(R.id.infoBar1_bar, infoBar1)
                    .commit();
        }

        // Add the mode info panel fragment
        flightModePanel = fragmentManager.findFragmentById(R.id.sliding_drawer_content);
        if (flightModePanel == null) {
            flightModePanel = FlightModePanel.newInstance(1);
            fragmentManager.beginTransaction()
                    .add(R.id.sliding_drawer_content, flightModePanel)
                    .commit();
        }




    }

    public void otherFragments2()
    {


        fragmentManager = getSupportFragmentManager();

        mSlidingPanel2 = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer2);
        mSlidingPanel2.setEnableDragViewTouchEvents(true);
        mSlidingPanel2.setSlidingEnabled(true);

        //enableSlidingUpPanel(this.drone, 2);

        setupMapFragment2();

        mLocationButtonsContainer2 = findViewById(R.id.location_button_container2);
        mGoToMyLocation2 = (ImageButton) findViewById(R.id.my_location_button2);
        mGoToDroneLocation2 = (ImageButton) findViewById(R.id.drone_location_button2);
        mExpandMap2 = (ImageButton) findViewById(R.id.expand_map_button2);
        mAllPOIs2 = (ImageButton) findViewById(R.id.all_waypoints_button2);

        final ImageButton resetMapBearing = (ImageButton) findViewById(R.id.map_orientation_button2);
        resetMapBearing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMapBearing(0, mapFragment2);
            }
        });

        mExpandMap2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapFragment2 != null) {
                    expandMap(2);
                    updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation2, mGoToDroneLocation2, mapFragment2);
                }
            }
        });

        mAllPOIs2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapFragment2 != null) {

                    enableAlgorithmMenu(2);

                    updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation2, mGoToDroneLocation2, mapFragment2);
                }
            }
        });


        mGoToMyLocation2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapFragment2 != null) {
                    mapFragment2.goToMyLocation();
                    updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation2, mGoToDroneLocation2, mapFragment2);
                }
            }
        });
        mGoToMyLocation2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mapFragment2 != null) {
                    mapFragment2.goToMyLocation();
                    updateMapLocationButtons(AutoPanMode.USER, mGoToMyLocation2, mGoToDroneLocation2, mapFragment2);
                    return true;
                }
                return false;
            }
        });

        mGoToDroneLocation2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapFragment2 != null) {
                    mapFragment2.goToDroneLocation();
                    updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation2, mGoToDroneLocation2, mapFragment2);
                }
            }
        });
        mGoToDroneLocation2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mapFragment2 != null) {
                    mapFragment2.goToDroneLocation();
                    updateMapLocationButtons(AutoPanMode.DRONE, mGoToMyLocation2, mGoToDroneLocation2, mapFragment2);
                    return true;
                }
                return false;
            }
        });

        flightActions2 = (FlightActionsFragment) fragmentManager.findFragmentById(R.id
                .flightActionsFragment2);
        if (flightActions2 == null) {
            flightActions2 = FlightActionsFragment.newInstance(2);
            fragmentManager.beginTransaction().add(R.id.flightActionsFragment2, flightActions2).commit();
        }


        mFlightActionsView2 = findViewById(R.id.flightActionsFragment2);
        mFlightActionsView2.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver
                .OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(!mSlidingPanelCollapsing2.get()) {
                    mSlidingPanel2.setPanelHeight(mFlightActionsView2.getHeight());
                }
            }
        });


        // Add the telemetry fragment
        telemetryFragment2 = (TelemetryFragment) fragmentManager.findFragmentById(R.id.telemetryFragment2);
        if (telemetryFragment2 == null) {
            telemetryFragment2 = TelemetryFragment.newInstance(2);// = new newInstance TelemetryFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.telemetryFragment2, telemetryFragment2)
                    .commit();
        }

        infoBar2 = (InfoBarFragment) fragmentManager.findFragmentById(R.id.infoBar2_bar);
        if(infoBar2 == null)
        {
            infoBar2 = InfoBarFragment.newInstance(2);
            fragmentManager.beginTransaction()
                    .add(R.id.infoBar2_bar, infoBar2)
                    .commit();
        }

        // Add the mode info panel fragment
        flightModePanel2 = fragmentManager.findFragmentById(R.id.sliding_drawer_content2);
        if (flightModePanel2 == null) {
            flightModePanel2 = FlightModePanel.newInstance(2);
            fragmentManager.beginTransaction()
                    .add(R.id.sliding_drawer_content2, flightModePanel2)
                    .commit();
        }



    }

    public void otherFragments3()
    {


        fragmentManager = getSupportFragmentManager();

        mSlidingPanel3 = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer3);
        mSlidingPanel3.setEnableDragViewTouchEvents(true);
        mSlidingPanel3.setSlidingEnabled(true);

        //enableSlidingUpPanel(this.drone, 3);



        setupMapFragment3();

        mLocationButtonsContainer3 = findViewById(R.id.location_button_container3);
        mGoToMyLocation3 = (ImageButton) findViewById(R.id.my_location_button3);
        mGoToDroneLocation3 = (ImageButton) findViewById(R.id.drone_location_button3);
        mExpandMap3 = (ImageButton) findViewById(R.id.expand_map_button3);
        mAllPOIs3 = (ImageButton) findViewById(R.id.all_waypoints_button3);

        final ImageButton resetMapBearing = (ImageButton) findViewById(R.id.map_orientation_button3);
        resetMapBearing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMapBearing(0, mapFragment3);
            }
        });

        mExpandMap3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapFragment3 != null) {
                    expandMap(3);
                    updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation3, mGoToDroneLocation3, mapFragment3);
                }
            }
        });

        mAllPOIs3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapFragment3 != null) {

                    enableAlgorithmMenu(3);

                    updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation3, mGoToDroneLocation3, mapFragment3);
                }
            }
        });


        mGoToMyLocation3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapFragment3 != null) {
                    mapFragment3.goToMyLocation();
                    updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation3, mGoToDroneLocation3, mapFragment3);
                }
            }
        });
        mGoToMyLocation3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mapFragment3 != null) {
                    mapFragment3.goToMyLocation();
                    updateMapLocationButtons(AutoPanMode.USER, mGoToMyLocation3, mGoToDroneLocation3, mapFragment3);
                    return true;
                }
                return false;
            }
        });

        mGoToDroneLocation3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapFragment3 != null) {
                    mapFragment3.goToDroneLocation();
                    updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation3, mGoToDroneLocation3, mapFragment3);
                }
            }
        });
        mGoToDroneLocation3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mapFragment3 != null) {
                    mapFragment3.goToDroneLocation();
                    updateMapLocationButtons(AutoPanMode.DRONE, mGoToMyLocation3, mGoToDroneLocation3, mapFragment3);
                    return true;
                }
                return false;
            }
        });



        flightActions3 = (FlightActionsFragment) fragmentManager.findFragmentById(R.id
                .flightActionsFragment3);
        if (flightActions3 == null) {
            flightActions3 = FlightActionsFragment.newInstance(3);
            fragmentManager.beginTransaction().add(R.id.flightActionsFragment3, flightActions3).commit();
        }

        mFlightActionsView3 = findViewById(R.id.flightActionsFragment3);
        mFlightActionsView3.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver
                .OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!mSlidingPanelCollapsing3.get()) {
                    mSlidingPanel3.setPanelHeight(mFlightActionsView3.getHeight());
                }
            }
        });


        // Add the telemetry fragment
        telemetryFragment3 = (TelemetryFragment)fragmentManager.findFragmentById(R.id.telemetryFragment3);
        if (telemetryFragment3 == null) {
            telemetryFragment3 = TelemetryFragment.newInstance(3);
            fragmentManager.beginTransaction()
                    .add(R.id.telemetryFragment3, telemetryFragment3)
                    .commit();
        }

        infoBar3 = (InfoBarFragment) fragmentManager.findFragmentById(R.id.infoBar3_bar);
        if(infoBar3 == null)
        {
            infoBar3 = InfoBarFragment.newInstance(3);
            fragmentManager.beginTransaction()
                    .add(R.id.infoBar3_bar, infoBar3)
                    .commit();
        }


        // Add the mode info panel fragment
        flightModePanel3 = fragmentManager.findFragmentById(R.id.sliding_drawer_content3);
        if (flightModePanel3 == null) {
            flightModePanel3 = FlightModePanel.newInstance(3);
            fragmentManager.beginTransaction()
                    .add(R.id.sliding_drawer_content3, flightModePanel3)
                    .commit();
        }

    }

    public void otherFragments4()
    {


        fragmentManager = getSupportFragmentManager();


        mSlidingPanel4 = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer4);
        mSlidingPanel4.setEnableDragViewTouchEvents(true);
        mSlidingPanel4.setSlidingEnabled(true);

        //enableSlidingUpPanel(this.drone, 4);

        setupMapFragment4();

        mLocationButtonsContainer4 = findViewById(R.id.location_button_container4);
        mGoToMyLocation4 = (ImageButton) findViewById(R.id.my_location_button4);
        mGoToDroneLocation4 = (ImageButton) findViewById(R.id.drone_location_button4);
        mExpandMap4 = (ImageButton) findViewById(R.id.expand_map_button4);
        mAllPOIs4 = (ImageButton) findViewById(R.id.all_waypoints_button4);

        final ImageButton resetMapBearing = (ImageButton) findViewById(R.id.map_orientation_button4);
        resetMapBearing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMapBearing(0, mapFragment4);
            }
        });

        mExpandMap4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapFragment4 != null) {
                    expandMap(4);
                    updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation4, mGoToDroneLocation4, mapFragment4);
                }
            }
        });

        mAllPOIs4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapFragment4 != null) {

                    enableAlgorithmMenu(4);

                    updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation4, mGoToDroneLocation4, mapFragment4);
                }
            }
        });



        mGoToMyLocation4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapFragment4 != null) {
                    mapFragment4.goToMyLocation();
                    updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation4, mGoToDroneLocation4, mapFragment4);
                }
            }
        });
        mGoToMyLocation4.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mapFragment4 != null) {
                    mapFragment4.goToMyLocation();
                    updateMapLocationButtons(AutoPanMode.USER, mGoToMyLocation4, mGoToDroneLocation4, mapFragment4);
                    return true;
                }
                return false;
            }
        });

        mGoToDroneLocation4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapFragment4 != null) {
                    mapFragment4.goToDroneLocation();
                    updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation4, mGoToDroneLocation4, mapFragment4);
                }
            }
        });
        mGoToDroneLocation4.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mapFragment4 != null) {
                    mapFragment4.goToDroneLocation();
                    updateMapLocationButtons(AutoPanMode.DRONE, mGoToMyLocation4, mGoToDroneLocation4, mapFragment4);
                    return true;
                }
                return false;
            }
        });

        flightActions4 = (FlightActionsFragment) fragmentManager.findFragmentById(R.id
                .flightActionsFragment4);
        if (flightActions4 == null) {
            flightActions4 = FlightActionsFragment.newInstance(4);
            fragmentManager.beginTransaction().add(R.id.flightActionsFragment4, flightActions4).commit();
        }

        mFlightActionsView4 = findViewById(R.id.flightActionsFragment4);
        mFlightActionsView4.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver
                .OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(!mSlidingPanelCollapsing4.get()) {
                    mSlidingPanel4.setPanelHeight(mFlightActionsView4.getHeight());
                }
            }
        });


        // Add the telemetry fragment
        telemetryFragment4 = (TelemetryFragment) fragmentManager.findFragmentById(R.id.telemetryFragment4);
        if (telemetryFragment4 == null) {
            telemetryFragment4 = TelemetryFragment.newInstance(4);
            fragmentManager.beginTransaction()
                    .add(R.id.telemetryFragment4, telemetryFragment4)
                    .commit();
        }

        infoBar4 = (InfoBarFragment) fragmentManager.findFragmentById(R.id.infoBar4_bar);
        if(infoBar4 == null)
        {
            infoBar4 = InfoBarFragment.newInstance(4);
            fragmentManager.beginTransaction()
                    .add(R.id.infoBar4_bar, infoBar4)
                    .commit();
        }

        // Add the mode info panel fragment
        flightModePanel4 = fragmentManager.findFragmentById(R.id.sliding_drawer_content4);
        if (flightModePanel4 == null) {
            flightModePanel4 = FlightModePanel.newInstance(4);
            fragmentManager.beginTransaction()
                    .add(R.id.sliding_drawer_content4, flightModePanel4)
                    .commit();
        }
    }

    private void updateMapLocationButtons(AutoPanMode mode, ImageButton mGoToMyLocation, ImageButton mGoToDroneLocation, FlightMapFragment mapFragment) {
        mGoToMyLocation.setActivated(false);
        mGoToDroneLocation.setActivated(false);

        if (mapFragment != null) {
            mapFragment.setAutoPanMode(mode);
        }

        switch (mode) {
            case DRONE:
                mGoToDroneLocation.setActivated(true);
                break;

            case USER:
                mGoToMyLocation.setActivated(true);
                break;
            default:
                break;
        }
    }

    public void updateMapBearing(float bearing, FlightMapFragment mapFragment){
        if(mapFragment != null)
            mapFragment.updateMapBearing(bearing);
    }


    /**
     * Account for the various ui elements and update the map padding so that it
     * remains 'visible'.
     */
    private void updateLocationButtonsMargin(boolean isOpened, int drawerWidth) {

        // Update the right margin for the my location button
        final ViewGroup.MarginLayoutParams marginLp = (ViewGroup.MarginLayoutParams) mLocationButtonsContainer
                .getLayoutParams();
        final int rightMargin = isOpened ? marginLp.leftMargin + drawerWidth : marginLp.leftMargin;
        marginLp.setMargins(marginLp.leftMargin, marginLp.topMargin, rightMargin,
                marginLp.bottomMargin);
        mLocationButtonsContainer.requestLayout();
    }


    private void setupMapFragment() {
        if (mapFragment == null && isGooglePlayServicesValid(true)) {
            mapFragment = (FlightMapFragment) fragmentManager.findFragmentById(R.id.mapFragment);
            if (mapFragment == null) {
                //mapFragment = new FlightMapFragment();
                mapFragment = FlightMapFragment.newInstance(1);
                fragmentManager.beginTransaction().add(R.id.mapFragment, mapFragment).commit();
            }
        }
    }

    private void setupMapFragment2() {
        if (mapFragment2 == null && isGooglePlayServicesValid(true)) {
            mapFragment2 = (FlightMapFragment) fragmentManager.findFragmentById(R.id.mapFragment2);
            if (mapFragment2 == null) {
                mapFragment2 = new FlightMapFragment();
                fragmentManager.beginTransaction().add(R.id.mapFragment2, mapFragment2).commit();
            }
        }
    }

    private void setupMapFragment3() {
        if (mapFragment3 == null && isGooglePlayServicesValid(true)) {
            mapFragment3 = (FlightMapFragment) fragmentManager.findFragmentById(R.id.mapFragment3);
            if (mapFragment3 == null) {
                mapFragment3 = new FlightMapFragment();
                fragmentManager.beginTransaction().add(R.id.mapFragment3, mapFragment3).commit();
            }
        }
    }

    private void setupMapFragment4() {
        if (mapFragment4 == null && isGooglePlayServicesValid(true)) {
            mapFragment4 = (FlightMapFragment) fragmentManager.findFragmentById(R.id.mapFragment4);
            if (mapFragment4 == null) {
                mapFragment4 = new FlightMapFragment();
                fragmentManager.beginTransaction().add(R.id.mapFragment4, mapFragment4).commit();
            }
        }
    }

    private boolean isGooglePlayServicesValid(boolean showErrorDialog) {
        // Check for the google play services is available
        final int playStatus = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getApplicationContext());
        final boolean isValid = playStatus == ConnectionResult.SUCCESS;

        if (!isValid && showErrorDialog) {
            final Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(playStatus, this,
                    GOOGLE_PLAY_SERVICES_REQUEST_CODE, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                    });

            if (errorDialog != null)
                errorDialog.show();
        }

        return isValid;
    }

    private void expandMap(int selected_map)
    {
        LinearLayout layout1;
        FrameLayout layout2;
        ViewGroup view;
        //Se mapa expandiu, retornar as miniaturas.

        if(mapExpanded && NUM_MAPS > 1)
        {
            mapExpanded = false;
            switch(NUM_MAPS)
            {
                case 2:
                    layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout3);
                    layout1.setVisibility(LinearLayout.VISIBLE);
                    layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout2);
                    layout1.setVisibility(LinearLayout.VISIBLE);



                    layout2 = (FrameLayout) findViewById(R.id.infoBar1_bar);
                    layout2.setVisibility(FrameLayout.VISIBLE);
                    layout2 = (FrameLayout) findViewById(R.id.infoBar2_bar);
                    layout2.setVisibility(FrameLayout.VISIBLE);

                    setInfoBarInvisible();


                    view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer);
                    view.setVisibility(LinearLayout.VISIBLE);
                    view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer2);
                    view.setVisibility(LinearLayout.VISIBLE);
                    view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer3);
                    view.setVisibility(LinearLayout.GONE);
                    view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer4);
                    view.setVisibility(LinearLayout.GONE);
                    break;
                case 3:
                    layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout2);
                    layout1.setVisibility(LinearLayout.VISIBLE);
                    layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout3);
                    layout1.setVisibility(LinearLayout.VISIBLE);

                    layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout41);
                    layout2.setVisibility(FrameLayout.VISIBLE);
                    layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout42);
                    layout2.setVisibility(FrameLayout.VISIBLE);
                    layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout43);
                    layout2.setVisibility(FrameLayout.VISIBLE);

                    layout2 = (FrameLayout) findViewById(R.id.infoBar1_bar);
                    layout2.setVisibility(FrameLayout.VISIBLE);
                    layout2 = (FrameLayout) findViewById(R.id.infoBar2_bar);
                    layout2.setVisibility(FrameLayout.VISIBLE);
                    layout2 = (FrameLayout) findViewById(R.id.infoBar3_bar);
                    layout2.setVisibility(FrameLayout.VISIBLE);

                    view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer);
                    view.setVisibility(LinearLayout.VISIBLE);
                    view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer2);
                    view.setVisibility(LinearLayout.VISIBLE);
                    view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer3);
                    view.setVisibility(LinearLayout.VISIBLE);
                    view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer4);
                    view.setVisibility(LinearLayout.GONE);

                    setInfoBarInvisible();


                    break;
                case 4:
                    layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout2);
                    layout1.setVisibility(LinearLayout.VISIBLE);
                    layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout3);
                    layout1.setVisibility(LinearLayout.VISIBLE);

                    layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout41);
                    layout2.setVisibility(FrameLayout.VISIBLE);
                    layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout42);
                    layout2.setVisibility(FrameLayout.VISIBLE);
                    layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout43);
                    layout2.setVisibility(FrameLayout.VISIBLE);
                    layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout44);
                    layout2.setVisibility(FrameLayout.VISIBLE);


                    layout2 = (FrameLayout) findViewById(R.id.infoBar1_bar);
                    layout2.setVisibility(FrameLayout.VISIBLE);
                    layout2 = (FrameLayout) findViewById(R.id.infoBar2_bar);
                    layout2.setVisibility(FrameLayout.VISIBLE);
                    layout2 = (FrameLayout) findViewById(R.id.infoBar3_bar);
                    layout2.setVisibility(FrameLayout.VISIBLE);
                    layout2 = (FrameLayout) findViewById(R.id.infoBar4_bar);
                    layout2.setVisibility(FrameLayout.VISIBLE);


                    view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer);
                    view.setVisibility(LinearLayout.VISIBLE);
                    view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer2);
                    view.setVisibility(LinearLayout.VISIBLE);
                    view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer3);
                    view.setVisibility(LinearLayout.VISIBLE);
                    view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer4);
                    view.setVisibility(LinearLayout.VISIBLE);

                    setInfoBarInvisible();
                    break;
                default:break;
            }


        }
        //Se miniatura, expandir mapa selecionado
        else
        {

            switch(NUM_MAPS)
            {
                case 2:
                    if(selected_map == 1)
                    {
                        layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout3);
                        layout1.setVisibility(LinearLayout.GONE);

                        setInfoBarVisible(MultipleActivity.getDroneIDFromMap(1));

                        layout2 = (FrameLayout) findViewById(R.id.infoBar1_bar);
                        layout2.setVisibility(FrameLayout.GONE);



                        view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer3);
                        view.setVisibility(LinearLayout.GONE);

                    }
                    else
                    {
                        layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout2);
                        layout1.setVisibility(LinearLayout.GONE);

                        setInfoBarVisible(MultipleActivity.getDroneIDFromMap(2));
                        layout2 = (FrameLayout) findViewById(R.id.infoBar2_bar);
                        layout2.setVisibility(FrameLayout.GONE);

                        view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer4);
                        view.setVisibility(LinearLayout.GONE);
                    }
                    break;
                case 3:
                    if(selected_map == 1)
                    {
                        layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout43);
                        layout2.setVisibility(FrameLayout.GONE);

                        layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout3);
                        layout1.setVisibility(LinearLayout.GONE);

                        setInfoBarVisible(MultipleActivity.getDroneIDFromMap(1));

                        layout2 = (FrameLayout) findViewById(R.id.infoBar1_bar);
                        layout2.setVisibility(FrameLayout.GONE);



                        view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer3);
                        view.setVisibility(LinearLayout.GONE);
                    }
                    else if(selected_map == 2)
                    {
                        layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout41);
                        layout2.setVisibility(FrameLayout.GONE);
                        layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout43);
                        layout2.setVisibility(FrameLayout.GONE);

                        layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout2);
                        layout1.setVisibility(LinearLayout.GONE);

                        setInfoBarVisible(MultipleActivity.getDroneIDFromMap(2));

                        layout2 = (FrameLayout) findViewById(R.id.infoBar2_bar);
                        layout2.setVisibility(FrameLayout.GONE);

                        view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer4);
                        view.setVisibility(LinearLayout.GONE);
                    }
                    else
                    {
                        layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout41);
                        layout2.setVisibility(FrameLayout.GONE);
                        layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout3);
                        layout1.setVisibility(LinearLayout.GONE);

                        setInfoBarVisible(MultipleActivity.getDroneIDFromMap(3));

                        layout2 = (FrameLayout) findViewById(R.id.infoBar2_bar);
                        layout2.setVisibility(FrameLayout.GONE);


                        view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer);
                        view.setVisibility(LinearLayout.GONE);
                    }
                    break;
                case 4:
                    if(selected_map == 1)
                    {
                        layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout42);
                        layout2.setVisibility(FrameLayout.GONE);
                        layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout43);
                        layout2.setVisibility(FrameLayout.GONE);
                        layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout44);
                        layout2.setVisibility(FrameLayout.GONE);

                        layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout3);
                        layout1.setVisibility(LinearLayout.GONE);

                        setInfoBarVisible(MultipleActivity.getDroneIDFromMap(1));

                        layout2 = (FrameLayout) findViewById(R.id.infoBar1_bar);
                        layout2.setVisibility(FrameLayout.GONE);


                        view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer3);
                        view.setVisibility(LinearLayout.GONE);
                    }
                    else if (selected_map == 2)
                    {
                        layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout41);
                        layout2.setVisibility(FrameLayout.GONE);
                        layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout43);
                        layout2.setVisibility(FrameLayout.GONE);
                        layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout44);
                        layout2.setVisibility(FrameLayout.GONE);

                        layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout2);
                        layout1.setVisibility(LinearLayout.GONE);

                        setInfoBarVisible(MultipleActivity.getDroneIDFromMap(2));

                        view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer4);
                        view.setVisibility(LinearLayout.GONE);

                        layout2 = (FrameLayout) findViewById(R.id.infoBar2_bar);
                        layout2.setVisibility(FrameLayout.GONE);
                    }
                    else if(selected_map == 3)
                    {
                        layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout41);
                        layout2.setVisibility(FrameLayout.GONE);
                        layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout42);
                        layout2.setVisibility(FrameLayout.GONE);
                        layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout44);
                        layout2.setVisibility(FrameLayout.GONE);

                        layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout3);
                        layout1.setVisibility(LinearLayout.GONE);

                        setInfoBarVisible(MultipleActivity.getDroneIDFromMap(3));

                        layout2 = (FrameLayout) findViewById(R.id.infoBar3_bar);
                        layout2.setVisibility(FrameLayout.GONE);

                        view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer);
                        view.setVisibility(LinearLayout.GONE);
                    }
                    else
                    {
                        layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout41);
                        layout2.setVisibility(FrameLayout.GONE);
                        layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout42);
                        layout2.setVisibility(FrameLayout.GONE);
                        layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout43);
                        layout2.setVisibility(FrameLayout.GONE);

                        layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout2);
                        layout1.setVisibility(LinearLayout.GONE);

                        setInfoBarVisible(MultipleActivity.getDroneIDFromMap(4));

                        layout2 = (FrameLayout) findViewById(R.id.infoBar4_bar);
                        layout2.setVisibility(FrameLayout.GONE);

                        view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer2);
                        view.setVisibility(LinearLayout.GONE);

                    }
                    break;
                default:break;
            }

            mapExpanded = true;


        }


    }

    @Override
    protected int getNavigationDrawerEntryId() {
        return R.id.navigation_flight_data;
    }


    private static final String ONDRONEEVENTZ = "ONDRONEEVENTZ";
    @Override
    public void onDroneEvent(DroneInterfaces.DroneEventsType event, Drone drone) {
        super.onDroneEvent(event, drone);


        //Log.d(ONDRONEEVENTZ, "onDroneEvent - DRONE ID => " + drone.getDroneID());

        switch (event) {
            case AUTOPILOT_WARNING:
                onWarningChanged(drone);
                break;

            case ARMING:
            case CONNECTED:
            case DISCONNECTED:
            case STATE:
                onWarningChanged(drone);
                break;

            case FOLLOW_START:
                //Extend the sliding drawer if collapsed.
                if(!mSlidingPanelCollapsing.get() && mSlidingPanel.isSlidingEnabled() &&
                        !mSlidingPanel.isPanelExpanded()){
                    mSlidingPanel.expandPanel();
                }
                break;

            default:
                break;
        }
    }

    public void onWarningChanged(Drone drone) {
        if (drone.getState().isWarning()) {
            warningView.setText(drone.getState().getWarning());
            warningView.setVisibility(View.VISIBLE);
        } else {
            warningView.setVisibility(View.GONE);
        }
    }



    private void enableSlidingUpPanel(Drone drone, int num_map){

        SlidingUpPanelLayout mSlidingPanel;
        FlightActionsFragment flightActions;
        SlidingUpPanelLayout.PanelSlideListener mDisablePanelSliding;
        AtomicBoolean mSlidingPanelCollapsing;


        switch(num_map) {
            case 1:
                mSlidingPanel = this.mSlidingPanel;
                flightActions = this.flightActions;
                mDisablePanelSliding = this.mDisablePanelSliding;
                mSlidingPanelCollapsing = this.mSlidingPanelCollapsing;
                break;
            case 2:
                mSlidingPanel = this.mSlidingPanel2;
                flightActions = this.flightActions2;
                mDisablePanelSliding = this.mDisablePanelSliding2;
                mSlidingPanelCollapsing = this.mSlidingPanelCollapsing2;
                break;
            case 3:
                mSlidingPanel = this.mSlidingPanel3;
                flightActions = this.flightActions3;
                mDisablePanelSliding = this.mDisablePanelSliding3;
                mSlidingPanelCollapsing = this.mSlidingPanelCollapsing3;
                break;
            case 4:
                mSlidingPanel = this.mSlidingPanel4;
                flightActions = this.flightActions4;
                mDisablePanelSliding = this.mDisablePanelSliding4;
                mSlidingPanelCollapsing = this.mSlidingPanelCollapsing4;
                break;
            default:
                mSlidingPanel = this.mSlidingPanel;
                flightActions = this.flightActions;
                mDisablePanelSliding = this.mDisablePanelSliding;
                mSlidingPanelCollapsing = this.mSlidingPanelCollapsing;
                break;
        }


        if (mSlidingPanel == null) {
            return;
        }

        final boolean isEnabled = flightActions != null && flightActions.isSlidingUpPanelEnabled
                (drone);

        if (isEnabled) {
            Log.d(ONDRONEEVENTZ, "enableSlidingUpPanel - ENABLE!!! ");
            mSlidingPanel.setSlidingEnabled(true);
        } else {
            if(!mSlidingPanelCollapsing.get()) {
                if (mSlidingPanel.isPanelExpanded()) {
                    Log.d(ONDRONEEVENTZ, "enableSlidingUpPanel - NÃO ENABLE EXPANDED ");
                    mSlidingPanel.setPanelSlideListener(mDisablePanelSliding);
                    mSlidingPanel.collapsePanel();
                    mSlidingPanelCollapsing.set(true);
                } else {
                    Log.d(ONDRONEEVENTZ, "enableSlidingUpPanel - NÃO ENABLE just like that ");
                    mSlidingPanel.setSlidingEnabled(false);
                    mSlidingPanelCollapsing.set(false);
                }
            }
        }
    }

    @Override
    protected boolean enableMissionMenus(){
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        updateMapLocationButtons(mAppPrefs.getAutoPanMode());
    }

    private void updateMapLocationButtons(AutoPanMode mode) {
        mGoToMyLocation.setActivated(false);
        mGoToDroneLocation.setActivated(false);

        if (mapFragment != null) {
            mapFragment.setAutoPanMode(mode);
        }

        switch (mode) {
            case DRONE:
                mGoToDroneLocation.setActivated(true);
                break;

            case USER:
                mGoToMyLocation.setActivated(true);
                break;
            default:
                break;
        }
    }

    public void lunchAlgorithmMenu(int num_map)
    {

        AlgorithmMenuFragment mAlgorithmFragment = AlgorithmMenuFragment.newInstance(num_map);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch(num_map)
        {
            case 1:
                fragmentTransaction.add(R.id.multi_layout1, mAlgorithmFragment, "menu1");
                break;
            case 2:
                fragmentTransaction.add(R.id.multi_layout2, mAlgorithmFragment, "menu2");
                break;
            case 3:
                fragmentTransaction.add(R.id.multi_layout3, mAlgorithmFragment, "menu3");
                break;
            case 4:
                fragmentTransaction.add(R.id.multi_layout4, mAlgorithmFragment, "menu4");
                break;
        }

        fragmentTransaction.commit();

    }


    private static final String CLICK = "CLICK";
    public void onClick(View view)
    {
        Log.d(CLICK, "CLICK: " + view.getTag());
        /**
         *
         * Criar Switch case com o tipo de algoritmo selecionado, levando em consideração qual mapa que selecionou
         */


    }

    private static final String CLICK_MENU = "CLICK_MENU";
    public void enableAlgorithmMenu(int num_map)
    {
        switch(num_map)
        {
            case 1:
                Log.d(CLICK_MENU, "CLICK_MENU 11111");
                if(mAllPOIsOpen)
                {
                    mAllPOIsOpen = false;
                    View menu_view = findViewById(R.id.alg_menu1);
                    menu_view.setVisibility(View.GONE);
                }
                else {
                    mAllPOIsOpen = true;
                    View menu_view = findViewById(R.id.alg_menu1);
                    menu_view.setVisibility(View.VISIBLE);
                }
                break;
            case 2:
                Log.d(CLICK_MENU, "CLICK_MENU 2222");
                if(mAllPOIsOpen2)
                {
                    mAllPOIsOpen2 = false;
                    View menu_view = findViewById(R.id.alg_menu2);
                    menu_view.setVisibility(View.GONE);
                }
                else {
                    mAllPOIsOpen2 = true;
                    View menu_view = findViewById(R.id.alg_menu2);
                    menu_view.setVisibility(View.VISIBLE);
                }
                break;
            case 3:
                Log.d(CLICK_MENU, "CLICK_MENU 3333");
                if(mAllPOIsOpen3)
                {
                    mAllPOIsOpen3 = false;
                    View menu_view = findViewById(R.id.alg_menu3);
                    menu_view.setVisibility(View.GONE);
                }
                else {
                    mAllPOIsOpen3 = true;
                    View menu_view = findViewById(R.id.alg_menu3);
                    menu_view.setVisibility(View.VISIBLE);
                }
                break;
            case 4:
                Log.d(CLICK_MENU, "CLICK_MENU 4444");
                if(mAllPOIsOpen4)
                {
                    mAllPOIsOpen4 = false;
                    View menu_view = findViewById(R.id.alg_menu4);
                    menu_view.setVisibility(View.GONE);
                }
                else {
                    mAllPOIsOpen4 = true;
                    View menu_view = findViewById(R.id.alg_menu4);
                    menu_view.setVisibility(View.VISIBLE);
                }
                break;
        }

    }

    private static final String NUM_MAPS_S = "NUM_MAPS_S";
    public void newDroneMap(int droneID, boolean fromBroadcast)
    {
        if(fromBroadcast)
            NUM_MAPS++;

        mapToDroneIDAssociation.put(NUM_MAPS, droneID);
        //multipleMapView(NUM_MAPS);

        LinearLayout layout1;
        FrameLayout layout2;
        ViewGroup view;
        Log.d(NUM_MAPS_S, "NUM_MAPS => " + NUM_MAPS);

        //Como tem mais de um drone, infoBar não aparece mais no actionbar
        setInfoBarInvisible();
        switch(NUM_MAPS)
        {
            case 1:
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout42);
                layout2.setVisibility(FrameLayout.GONE);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout43);
                layout2.setVisibility(FrameLayout.GONE);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout44);
                layout2.setVisibility(FrameLayout.GONE);

                layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout3);
                layout1.setVisibility(LinearLayout.GONE);


                layout2 = (FrameLayout) findViewById(R.id.infoBar1_bar);
                layout2.setVisibility(FrameLayout.VISIBLE);


                telemetryNewDrone(droneID, 1);
                flightFragmentNewDrone(droneID, 1);

                mapFragment.setDroneMapDrone(droneID);


                break;
            case 2:
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout42);
                layout2.setVisibility(FrameLayout.VISIBLE);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout43);
                layout2.setVisibility(FrameLayout.GONE);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout44);
                layout2.setVisibility(FrameLayout.GONE);


                layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout2);
                layout1.setVisibility(LinearLayout.VISIBLE);
                layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout3);
                layout1.setVisibility(LinearLayout.VISIBLE);


                layout2 = (FrameLayout) findViewById(R.id.infoBar1_bar);
                layout2.setVisibility(FrameLayout.VISIBLE);
                layout2 = (FrameLayout) findViewById(R.id.infoBar2_bar);
                layout2.setVisibility(FrameLayout.VISIBLE);



                view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer3);
                view.setVisibility(LinearLayout.GONE);
                view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer4);
                view.setVisibility(LinearLayout.GONE);

                otherFragments2();

                telemetryNewDrone(droneID, 2);
                flightFragmentNewDrone(droneID, 2);


                mSlidingPanel2.setSlidingEnabled(true);

                mapFragment2.setDroneMapDrone(droneID);

                break;
            case 3:
                otherFragments3();
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout42);
                layout2.setVisibility(FrameLayout.VISIBLE);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout43);
                layout2.setVisibility(FrameLayout.VISIBLE);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout44);
                layout2.setVisibility(FrameLayout.GONE);

                layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout3);
                layout1.setVisibility(LinearLayout.VISIBLE);

                layout2 = (FrameLayout) findViewById(R.id.infoBar1_bar);
                layout2.setVisibility(FrameLayout.VISIBLE);
                layout2 = (FrameLayout) findViewById(R.id.infoBar2_bar);
                layout2.setVisibility(FrameLayout.VISIBLE);
                layout2 = (FrameLayout) findViewById(R.id.infoBar3_bar);
                layout2.setVisibility(FrameLayout.VISIBLE);


                view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer3);
                view.setVisibility(LinearLayout.VISIBLE);
                view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer4);
                view.setVisibility(LinearLayout.GONE);

                telemetryNewDrone(droneID, 3);
                flightFragmentNewDrone(droneID, 3);

                mSlidingPanel3.setSlidingEnabled(true);

                mapFragment3.setDroneMapDrone(droneID);

                break;
            case 4:
                otherFragments4();
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout42);
                layout2.setVisibility(FrameLayout.VISIBLE);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout43);
                layout2.setVisibility(FrameLayout.VISIBLE);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout44);
                layout2.setVisibility(FrameLayout.VISIBLE);

                layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout3);
                layout1.setVisibility(LinearLayout.VISIBLE);

                layout2 = (FrameLayout) findViewById(R.id.infoBar1_bar);
                layout2.setVisibility(FrameLayout.VISIBLE);
                layout2 = (FrameLayout) findViewById(R.id.infoBar2_bar);
                layout2.setVisibility(FrameLayout.VISIBLE);
                layout2 = (FrameLayout) findViewById(R.id.infoBar3_bar);
                layout2.setVisibility(FrameLayout.VISIBLE);
                layout2 = (FrameLayout) findViewById(R.id.infoBar4_bar);
                layout2.setVisibility(FrameLayout.VISIBLE);


                view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer3);
                view.setVisibility(LinearLayout.VISIBLE);
                view = (SlidingUpPanelLayout) findViewById(R.id.slidingPanelContainer4);
                view.setVisibility(LinearLayout.VISIBLE);

                telemetryNewDrone(droneID, 4);
                flightFragmentNewDrone(droneID, 4);

                mSlidingPanel4.setSlidingEnabled(true);

                mapFragment4.setDroneMapDrone(droneID);
                break;
        }


        ///Intent intent3 = new Intent("NEW_DRONE_FOR_EDITOR");
       /// intent3.putExtra("droneID", droneID);
        ///getApplicationContext().sendBroadcast(intent3);




    }



    public void newDroneMapSelected(int droneID)
    {
        NUM_MAPS++;
        mapToDroneIDAssociation.put(NUM_MAPS, droneID);
        telemetryNewDrone(droneID, 1);
        flightFragmentNewDrone(droneID, 1);
        mapFragment.newDroneSelected(droneID);

        setInfoBarVisible(droneID);

        //enableSlidingUpPanel(((DroidPlannerApp) getApplication()).getDroneList().get(droneID), 1);
        mSlidingPanel.setSlidingEnabled(true);


     ///   Intent intent3 = new Intent("NEW_DRONE_FOR_EDITOR");
    ///    intent3.putExtra("droneID", droneID);
    ///    getApplicationContext().sendBroadcast(intent3);

    }

    public void newDroneSelected(int droneID)
    {
        mapToDroneIDAssociation.put(NUM_MAPS, droneID);
        telemetryNewDrone(droneID, 1);
        flightFragmentNewDrone(droneID, 1);
        mapFragment.newDroneSelected(droneID);

        setInfoBarVisible(droneID);

        //enableSlidingUpPanel(((DroidPlannerApp) getApplication()).getDroneList().get(droneID), 1);
        mSlidingPanel.setSlidingEnabled(true);
    }


    public void refreshVariables()
    {
        int droneID;
        Drone drone;
        switch(NUM_MAPS)
        {
            case 1:
                droneID = MultipleActivity.getDroneIDFromMap(1);
                drone = ((DroidPlannerApp) getApplication()).getDroneList().get(droneID);
                mapFragment.setDroneMapDrone(drone);
                break;
            case 2:
                droneID = MultipleActivity.getDroneIDFromMap(1);
                drone = ((DroidPlannerApp) getApplication()).getDroneList().get(droneID);
                mapFragment.setDroneMapDrone(drone);


                droneID = MultipleActivity.getDroneIDFromMap(2);
                drone = ((DroidPlannerApp) getApplication()).getDroneList().get(droneID);
                mapFragment2.setDroneMapDrone(drone);
                break;
            case 3:
                droneID = MultipleActivity.getDroneIDFromMap(1);
                drone = ((DroidPlannerApp) getApplication()).getDroneList().get(droneID);
                mapFragment.setDroneMapDrone(drone);


                droneID = MultipleActivity.getDroneIDFromMap(2);
                drone = ((DroidPlannerApp) getApplication()).getDroneList().get(droneID);
                mapFragment2.setDroneMapDrone(drone);


                droneID = MultipleActivity.getDroneIDFromMap(3);
                drone = ((DroidPlannerApp) getApplication()).getDroneList().get(droneID);
                mapFragment3.setDroneMapDrone(drone);

                break;
            case 4:
                droneID = MultipleActivity.getDroneIDFromMap(1);
                drone = ((DroidPlannerApp) getApplication()).getDroneList().get(droneID);
                mapFragment.setDroneMapDrone(drone);


                droneID = MultipleActivity.getDroneIDFromMap(2);
                drone = ((DroidPlannerApp) getApplication()).getDroneList().get(droneID);
                mapFragment2.setDroneMapDrone(drone);


                droneID = MultipleActivity.getDroneIDFromMap(3);
                drone = ((DroidPlannerApp) getApplication()).getDroneList().get(droneID);
                mapFragment3.setDroneMapDrone(drone);

                droneID = MultipleActivity.getDroneIDFromMap(4);
                drone = ((DroidPlannerApp) getApplication()).getDroneList().get(droneID);
                mapFragment4.setDroneMapDrone(drone);
                break;
        }
    }


    private void addBroadcastFilters() {
        final IntentFilter connectedFilter = new IntentFilter();
        connectedFilter.addAction("TOWER_CONNECTED");
        registerReceiver(broadcastReceiver, connectedFilter);
        final IntentFilter disconnectedFilter = new IntentFilter();
        disconnectedFilter.addAction("TOWER_DISCONNECTED");
        registerReceiver(broadcastReceiver, disconnectedFilter);
        final IntentFilter newDroneFilter = new IntentFilter();
        newDroneFilter.addAction("NEW_DRONE");
        registerReceiver(broadcastReceiver, newDroneFilter);
        final IntentFilter newDroneSelectedFilter = new IntentFilter();
        newDroneSelectedFilter.addAction("NEW_DRONE_SELECTED");
        registerReceiver(broadcastReceiver, newDroneSelectedFilter);
    }

    public void telemetryNewDrone(int droneID, int num_map)
    {
        switch(num_map)
        {
            case 1:
                telemetryFragment.newDrone(droneID);

                infoBar1.setDroneById(droneID);

                mapFragment.newDrone(droneID);
                break;
            case 2:
                telemetryFragment2.newDrone(droneID);

                infoBar2.setDroneById(droneID);

                mapFragment2.newDrone(droneID);
                break;
            case 3:
                telemetryFragment3.newDrone(droneID);

                infoBar3.setDroneById(droneID);

                mapFragment3.newDrone(droneID);
                break;
            case 4:
                telemetryFragment4.newDrone(droneID);

                infoBar4.setDroneById(droneID);

                mapFragment4.newDrone(droneID);
                break;
        }
    }

    public void flightFragmentNewDrone(int droneID, int num_map)
    {
        switch(num_map)
        {
            case 1:
                flightActions.newDroneFlightFragment(droneID);
                break;
            case 2:
                flightActions2.newDroneFlightFragment(droneID);
                break;
            case 3:
                flightActions3.newDroneFlightFragment(droneID);
                break;
            case 4:
                flightActions4.newDroneFlightFragment(droneID);
                break;
        }
    }

    public void onFragmentInteraction(Uri uri)
    {

    }

    public void onFragmentInteraction_AlgorithmMenu(Uri uri)
    {

    }

    public static HashMap<Integer, Integer>  getdroneIdToMapAssociation()
    {
        return mapToDroneIDAssociation;
    }

    public static int getDroneIDFromMap(int droneID)
    {
        if(mapToDroneIDAssociation.get(droneID) == null)
            return -1;
        else
            return mapToDroneIDAssociation.get(droneID);
    }



}
