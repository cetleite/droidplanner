package org.droidplanner.android.activities;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

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


    private final SlidingUpPanelLayout.PanelSlideListener mDisablePanelSliding = new
            SlidingUpPanelLayout.PanelSlideListener() {
                @Override
                public void onPanelSlide(View view, float v) {}

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


    private ContextMenu cMenu;

    private SlidingUpPanelLayout mSlidingPanel;
    private View mFlightActionsView;
    private FlightActionsFragment flightActions, flightActions2, flightActions3, flightActions4;

    private int NUM_MAPS = 0;
    private boolean mapExpanded = false;

    public static HashMap<Integer, Integer> mapToDroneIDAssociation = new HashMap<Integer, Integer>();

    private static final String NEW_DRONE = "NEW_DRONE";
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case "NEW_DRONE":
                    Log.d(NEW_DRONE, "MULTIPLEACTIVITY  -  RECEBEU BROADCAST!!!() - NEW_DRONE");
                    //mDrone = ((DroidPlannerApp) getActivity().getApplication()).getDrone();
                    newDroneMap(intent.getExtras().getInt("droneID"));
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

        multipleMapView(NUM_MAPS);


        View view =findViewById(R.id.all_waypoints_button); //
        registerForContextMenu(findViewById(R.id.all_waypoints_button));
        view.setLongClickable(false);



    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Context Menu");
        menu.add(0, v.getId(), 0, "Algorithm 1");
        menu.add(0, v.getId(), 0, "Algorithm 2");
        menu.add(0, v.getId(), 0, "Algorithm 3");

        cMenu = menu;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle() == "Action 1") {
            Toast.makeText(this, "Action 1 invoked", Toast.LENGTH_SHORT).show();
        } else if (item.getTitle() == "Action 2") {
            Toast.makeText(this, "Action 2 invoked", Toast.LENGTH_SHORT).show();
        } else if (item.getTitle() == "Action 3") {
            Toast.makeText(this, "Action 3 invoked", Toast.LENGTH_SHORT).show();
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        //enableSlidingUpPanel(this.drone);

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

    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_multiple, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void multipleMapView(int num_maps)
    {
        updateMultipleMaps2(1);
        otherFragments(4);

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

  /*
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        MultipleFragment fragment1 = new MultipleFragment();
        MultipleFragment fragment2 = new MultipleFragment();
        MultipleFragment fragment3 = new MultipleFragment();
        MultipleFragment fragment4 = new MultipleFragment();
*/
        setContentView(R.layout.activity_multiple);
/*
        fragmentTransaction.add(R.id.multi_layout1, fragment1, "1");
        fragmentTransaction.add(R.id.multi_layout2, fragment2, "2");
        fragmentTransaction.add(R.id.multi_layout3, fragment3, "3");
        fragmentTransaction.add(R.id.multi_layout4, fragment4, "4");
*/
        LinearLayout layout1;
        FrameLayout layout2;
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
                break;
            case 2:
                //setContentView(R.layout.activity_multiple2);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout43);
                layout2.setVisibility(FrameLayout.GONE);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout44);
                layout2.setVisibility(FrameLayout.GONE);


                break;
            case 3:
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout44);
                layout2.setVisibility(FrameLayout.GONE);

                break;
            case 4:
                break;
        }

        //fragmentTransaction.commit();
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
        //enableSlidingUpPanel(this.drone);

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

                    lunchAlgorithmMenu(1);

                    /*ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo*/
                    //openContextMenu(findViewById(R.id.all_waypoints_button));
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
            fragmentManager.beginTransaction().add(R.id.multiple_fragment_layout41, flightActions).commit();
        }


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


    }

    public void otherFragments2()
    {


        fragmentManager = getSupportFragmentManager();


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
            fragmentManager.beginTransaction().add(R.id.multiple_fragment_layout42, flightActions2).commit();
        }


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


    }

    public void otherFragments3()
    {


        fragmentManager = getSupportFragmentManager();


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
            fragmentManager.beginTransaction().add(R.id.multiple_fragment_layout43, flightActions3).commit();
        }

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

    }

    public void otherFragments4()
    {


        fragmentManager = getSupportFragmentManager();


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
            fragmentManager.beginTransaction().add(R.id.multiple_fragment_layout44, flightActions4).commit();
        }

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
                mapFragment = new FlightMapFragment();
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
                    }
                    else
                    {
                        layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout2);
                        layout1.setVisibility(LinearLayout.GONE);
                    }
                    break;
                case 3:
                    if(selected_map == 1)
                    {
                        layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout43);
                        layout2.setVisibility(FrameLayout.GONE);

                        layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout3);
                        layout1.setVisibility(LinearLayout.GONE);
                    }
                    else if(selected_map == 2)
                    {
                        layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout41);
                        layout2.setVisibility(FrameLayout.GONE);
                        layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout43);
                        layout2.setVisibility(FrameLayout.GONE);

                        layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout2);
                        layout1.setVisibility(LinearLayout.GONE);
                    }
                    else
                    {
                        layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout41);
                        layout2.setVisibility(FrameLayout.GONE);
                        layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout3);
                        layout1.setVisibility(LinearLayout.GONE);
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

    private void enableSlidingUpPanel(Drone drone){
        if (mSlidingPanel == null) {
            return;
        }

        final boolean isEnabled = flightActions != null && flightActions.isSlidingUpPanelEnabled
                (drone);

        if (isEnabled) {
            mSlidingPanel.setSlidingEnabled(true);
        } else {
            if(!mSlidingPanelCollapsing.get()) {
                if (mSlidingPanel.isPanelExpanded()) {
                    mSlidingPanel.setPanelSlideListener(mDisablePanelSliding);
                    mSlidingPanel.collapsePanel();
                    mSlidingPanelCollapsing.set(true);
                } else {
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
    public void newDroneMap(int droneID)
    {
        NUM_MAPS++;
        mapToDroneIDAssociation.put(NUM_MAPS, droneID);
        //multipleMapView(NUM_MAPS);

        LinearLayout layout1;
        FrameLayout layout2;
        Log.d(NUM_MAPS_S, "NUM_MAPS => " + NUM_MAPS);
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

                telemetryNewDrone(droneID, 1);
                flightFragmentNewDrone(droneID, 1);

                break;
            case 2:
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout42);
                layout2.setVisibility(FrameLayout.VISIBLE);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout43);
                layout2.setVisibility(FrameLayout.GONE);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout44);
                layout2.setVisibility(FrameLayout.GONE);

                layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout3);
                layout1.setVisibility(LinearLayout.VISIBLE);

                telemetryNewDrone(droneID, 2);
                flightFragmentNewDrone(droneID, 2);
                break;
            case 3:
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout42);
                layout2.setVisibility(FrameLayout.VISIBLE);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout43);
                layout2.setVisibility(FrameLayout.VISIBLE);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout44);
                layout2.setVisibility(FrameLayout.GONE);

                layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout3);
                layout1.setVisibility(LinearLayout.VISIBLE);

                telemetryNewDrone(droneID, 3);
                flightFragmentNewDrone(droneID, 3);
                break;
            case 4:
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout42);
                layout2.setVisibility(FrameLayout.VISIBLE);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout43);
                layout2.setVisibility(FrameLayout.VISIBLE);
                layout2 = (FrameLayout) findViewById(R.id.multiple_fragment_layout44);
                layout2.setVisibility(FrameLayout.VISIBLE);

                layout1 = (LinearLayout) findViewById(R.id.multiple_fragment_layout3);
                layout1.setVisibility(LinearLayout.VISIBLE);

                telemetryNewDrone(droneID, 4);
                flightFragmentNewDrone(droneID, 4);
                break;
        }

        //NUM_MAPS++;



    }

    public void newDroneMapSelected(int droneID)
    {
        NUM_MAPS++;
        mapToDroneIDAssociation.put(NUM_MAPS, droneID);
        telemetryNewDrone(droneID, 1);
        flightFragmentNewDrone(droneID, 1);
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
                break;
            case 2:
                telemetryFragment2.newDrone(droneID);

                infoBar2.setDroneById(droneID);
                break;
            case 3:
                telemetryFragment3.newDrone(droneID);

                infoBar3.setDroneById(droneID);
                break;
            case 4:
                telemetryFragment4.newDrone(droneID);

                infoBar4.setDroneById(droneID);
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
