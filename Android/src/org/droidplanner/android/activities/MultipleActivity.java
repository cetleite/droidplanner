package org.droidplanner.android.activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import org.droidplanner.R;
import org.droidplanner.android.fragments.FlightActionsFragment;
import org.droidplanner.android.fragments.FlightMapFragment;
import org.droidplanner.android.fragments.MultipleFragment;
import org.droidplanner.android.fragments.MultipleFragment2;
import org.droidplanner.android.fragments.TelemetryFragment;
import org.droidplanner.android.fragments.mode.FlightModePanel;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.core.drone.DroneImpl;
import org.droidplanner.core.model.Drone;

import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.SlidingDrawer;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.concurrent.atomic.AtomicBoolean;


public class MultipleActivity extends FragmentActivity implements MultipleFragment.OnFragmentInteractionListener,
        MultipleFragment2.OnFragmentInteractionListener {


    private static final int GOOGLE_PLAY_SERVICES_REQUEST_CODE = 101;



    private FragmentManager fragmentManager;

    private FlightMapFragment mapFragment, mapFragment2, mapFragment3, mapFragment4;
    private View mLocationButtonsContainer, mLocationButtonsContainer2, mLocationButtonsContainer3,mLocationButtonsContainer4;
    private ImageButton mGoToMyLocation, mGoToMyLocation2, mGoToMyLocation3, mGoToMyLocation4;
    private ImageButton mGoToDroneLocation, mGoToDroneLocation2, mGoToDroneLocation3, mGoToDroneLocation4;

    private int NUM_MAPS = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        multipleMapView(NUM_MAPS);

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

    public void onFragmentInteraction(Uri uri)
    {

    }

    public void onFragmentInteraction2(Uri uri)
    {

    }

    public void multipleMapView(int num_maps)
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        MultipleFragment fragment1 = new MultipleFragment();
        MultipleFragment fragment2 = new MultipleFragment();
        MultipleFragment fragment3 = new MultipleFragment();
        MultipleFragment fragment4 = new MultipleFragment();
        switch(num_maps)
        {
            case 1:
                setContentView(R.layout.activity_multiple1);
                fragmentTransaction.add(R.id.multi_layout1, fragment1, "1");
                break;
            case 2:
                setContentView(R.layout.activity_multiple2);
                fragmentTransaction.add(R.id.multi_layout1, fragment1, "1");
                fragmentTransaction.add(R.id.multi_layout2, fragment2, "2");
                break;
            case 3:
                setContentView(R.layout.activity_multiple3);
                fragmentTransaction.add(R.id.multi_layout1, fragment1, "1");
                fragmentTransaction.add(R.id.multi_layout2, fragment2, "2");
                fragmentTransaction.add(R.id.multi_layout3, fragment3, "3");

                break;
            case 4:
                setContentView(R.layout.activity_multiple4);
                fragmentTransaction.add(R.id.multi_layout1, fragment1, "1");
                fragmentTransaction.add(R.id.multi_layout2, fragment2, "2");
                fragmentTransaction.add(R.id.multi_layout3, fragment3, "3");
                fragmentTransaction.add(R.id.multi_layout4, fragment4, "4");
                break;
        }

        fragmentTransaction.commit();


        otherFragments(num_maps);

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


        setupMapFragment();

        mLocationButtonsContainer = findViewById(R.id.location_button_container);
        mGoToMyLocation = (ImageButton) findViewById(R.id.my_location_button);
        mGoToDroneLocation = (ImageButton) findViewById(R.id.drone_location_button);

        final ImageButton resetMapBearing = (ImageButton) findViewById(R.id.map_orientation_button);
        resetMapBearing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMapBearing(0, mapFragment);
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
    }

    public void otherFragments2()
    {


        fragmentManager = getSupportFragmentManager();


        setupMapFragment2();

        mLocationButtonsContainer2 = findViewById(R.id.location_button_container2);
        mGoToMyLocation2 = (ImageButton) findViewById(R.id.my_location_button2);
        mGoToDroneLocation2 = (ImageButton) findViewById(R.id.drone_location_button2);

        final ImageButton resetMapBearing = (ImageButton) findViewById(R.id.map_orientation_button2);
        resetMapBearing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMapBearing(0, mapFragment2);
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
    }

    public void otherFragments3()
    {


        fragmentManager = getSupportFragmentManager();


        setupMapFragment3();

        mLocationButtonsContainer3 = findViewById(R.id.location_button_container3);
        mGoToMyLocation3 = (ImageButton) findViewById(R.id.my_location_button3);
        mGoToDroneLocation3 = (ImageButton) findViewById(R.id.drone_location_button3);

        final ImageButton resetMapBearing = (ImageButton) findViewById(R.id.map_orientation_button3);
        resetMapBearing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMapBearing(0, mapFragment3);
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
    }

    public void otherFragments4()
    {


        fragmentManager = getSupportFragmentManager();


        setupMapFragment4();

        mLocationButtonsContainer4 = findViewById(R.id.location_button_container4);
        mGoToMyLocation4 = (ImageButton) findViewById(R.id.my_location_button4);
        mGoToDroneLocation4 = (ImageButton) findViewById(R.id.drone_location_button4);

        final ImageButton resetMapBearing = (ImageButton) findViewById(R.id.map_orientation_button4);
        resetMapBearing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMapBearing(0, mapFragment4);
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

}
