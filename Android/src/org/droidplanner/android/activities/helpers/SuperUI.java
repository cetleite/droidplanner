package org.droidplanner.android.activities.helpers;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.dialogs.YesNoDialog;
import org.droidplanner.android.dialogs.YesNoWithPrefsDialog;
import org.droidplanner.android.fragments.helpers.BTDeviceListFragment;
import org.droidplanner.android.maps.providers.google_map.GoogleMapFragment;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.android.widgets.actionProviders.InfoBarActionProvider;
import org.droidplanner.core.MAVLink.MavLinkROI;
import org.droidplanner.core.drone.DroneImpl;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.gcs.GCSHeartbeat;
import org.droidplanner.core.model.Drone;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.PopupMenu;
import java.lang.reflect.Field;
import android.view.ViewConfiguration;
import android.view.View;


import android.content.BroadcastReceiver;

/**
 * Parent class for the app activity classes.
 */
public abstract class SuperUI extends FragmentActivity implements OnDroneListener {

	public final static String ACTION_TOGGLE_DRONE_CONNECTION = SuperUI.class.getName()
			+ ".ACTION_TOGGLE_DRONE_CONNECTION";

    private ScreenOrientation screenOrientation = new ScreenOrientation(this);
	private InfoBarActionProvider infoBar;
	private GCSHeartbeat gcsHeartbeat;
	public DroidPlannerApp app;
	public Drone drone;

    boolean connectedTower = false;
    boolean connectedDrone = false;

	private static final String FLUXO = "FLUXO";
    private static final String MAVSERVICE = "MAVSERVICE";
    private static final String NOVOFLUXO = "NOVOFLUXO";
    private static final String ACTIVITY = "ACTIVITY";

    private Menu _menu = null;

	/**
	 * Handle to the app preferences.
	 */
	protected DroidPlannerPrefs mAppPrefs;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(NOVOFLUXO, "SuperUI  -  RECEBEU BROADCAST!!!");

            switch (action) {
                case "TOWER_CONNECTED":
                    connectedTower = true;
                    Log.d(NOVOFLUXO, "SuperUI  -  RECEBEU BROADCAST!!!() - TOWER_CONNECTED");
                    gcsHeartbeat.setActive(true);
                    invalidateOptionsMenu();
                    screenOrientation.requestLock();
                    break;
                case "TOWER_DISCONNECTED":
                    connectedTower = false;
                    Log.d(NOVOFLUXO, "SuperUI  -  RECEBEU BROADCAST!!!() - TOWER_DIISCONNECTED");
                    gcsHeartbeat.setActive(false);
                    invalidateOptionsMenu();
                    break;
                case "NEW_DRONE":
                    Log.d(NOVOFLUXO, "SuperUI  - NEW_DRONE");
                    connectedDrone = true;
                    newDrone();
                    invalidateOptionsMenu();
                    break;
            }
        }
    };


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Log.d(ACTIVITY, "SuperUI  -  onCreate()!!!");
        Log.d(NOVOFLUXO, "SuperUI  - onCreate()");

		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
		}

		app = (DroidPlannerApp) getApplication();
        app.superUIContext = this;

		this.drone = app.getDrone();
		gcsHeartbeat = new GCSHeartbeat(drone, 1);
		mAppPrefs = new DroidPlannerPrefs(getApplicationContext());

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

		/*
		 * Used to supplant wake lock acquisition (previously in
		 * org.droidplanner.android.service .MAVLinkService) as suggested by the
		 * android android.os.PowerManager#newWakeLock documentation.
		 */
		if (mAppPrefs.keepScreenOn()) {
			getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

        screenOrientation.unlock();
        Utils.updateUILanguage(getApplicationContext());

        handleIntent(getIntent());



        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }


	}

    private void addBroadcastFilters()
    {
        final IntentFilter connectedFilter = new IntentFilter();
        connectedFilter.addAction("TOWER_CONNECTED");
        registerReceiver(broadcastReceiver, connectedFilter);
        final IntentFilter disconnectedFilter = new IntentFilter();
        disconnectedFilter.addAction("TOWER_DISCONNECTED");
        registerReceiver(broadcastReceiver, disconnectedFilter);
        final IntentFilter newDroneFilter = new IntentFilter();
        newDroneFilter.addAction("NEW_DRONE");
        registerReceiver(broadcastReceiver, newDroneFilter);
    }


	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (intent == null)
			return;

		final String action = intent.getAction();
		if (ACTION_TOGGLE_DRONE_CONNECTION.equals(action)) {
			toggleDroneConnection();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		maxVolumeIfEnabled();

        addBroadcastFilters();
       // drone.addDroneListener(this);
		//drone.getMavClient().queryConnectionState();
		//drone.notifyDroneEvent(DroneEventsType.MISSION_UPDATE);
	}

	private void maxVolumeIfEnabled() {
		if (mAppPrefs.maxVolumeOnStart()) {
			AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
					audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
        drone.removeDroneListener(this);
        unregisterReceiver(broadcastReceiver);

		if (infoBar != null) {
			infoBar.setDrone(null);
			infoBar = null;
		}
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {

		if (infoBar != null) {
			infoBar.onDroneEvent(event, drone);
		}

		switch (event) {
            case CONNECTED:
                gcsHeartbeat.setActive(true);
			invalidateOptionsMenu();
                screenOrientation.requestLock();
			break;
		case DISCONNECTED:
			gcsHeartbeat.setActive(false);
			invalidateOptionsMenu();
			screenOrientation.unlock();
			break;
		default:
            break;
        }

    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {

        Log.d(FLUXO, "SuperUI  -  onCreateOptionsMenu()");

		getMenuInflater().inflate(R.menu.menu_super_activiy, menu);


	final MenuItem toggleConnectionItem = menu.findItem(R.id.menu_connect);
        if(connectedTower)
        {
            Log.d(FLUXO, "SuperUI  -  onCreateOptionsMenu() - CONECTADO!");
            menu.setGroupEnabled(R.id.menu_group_connected, true);
            menu.setGroupVisible(R.id.menu_group_connected, true);

            if(connectedDrone) {
          /*      if (infoBar != null) {
                    infoBar.setDrone(null);
                    infoBar = null;
                }*/
                final MenuItem sendMission = menu.findItem(R.id.menu_send_mission);
                sendMission.setEnabled(true);
                sendMission.setVisible(true);

                final MenuItem loadMission = menu.findItem(R.id.menu_load_mission);
                loadMission.setEnabled(true);
                loadMission.setVisible(true);

                final MenuItem infoBar = menu.findItem(R.id.menu_info_bar);
                infoBar.setEnabled(true);
                infoBar.setVisible(true);

            }
            else
            {
                final MenuItem infoBar = menu.findItem(R.id.menu_info_bar);
                infoBar.setEnabled(false);
                infoBar.setVisible(false);
            }

        } else
        {
            Log.d(FLUXO, "SuperUI  -  onCreateOptionsMenu() - DISCONECTADO!");
            menu.setGroupEnabled(R.id.menu_group_connected, false);
            menu.setGroupVisible(R.id.menu_group_connected, false);

            final MenuItem infoBar = menu.findItem(R.id.menu_info_bar);
            infoBar.setEnabled(false);
            infoBar.setVisible(false);

            toggleConnectionItem.setTitle(R.string.menu_connect);
        /*
            if (infoBar != null) {
                infoBar.setDrone(null);
            }*/
        }
		return super.onCreateOptionsMenu(menu);


	}

    protected boolean enableMissionMenus(){
        return false;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_send_mission:
            final MissionProxy missionProxy = app.getMissionProxy();

			if (missionProxy.getItems().isEmpty() || drone.getMission().hasTakeoffAndLandOrRTL()) {
                missionProxy.sendMissionToAPM();
			} else {
                YesNoWithPrefsDialog dialog = YesNoWithPrefsDialog.newInstance(getApplicationContext(),
                        "Mission Upload", "Do you want to append a Takeoff and RTL to your " +
                                "mission?", "Ok", "Skip", new YesNoDialog.Listener() {

                            @Override
                            public void onYes() {
                                missionProxy.addTakeOffAndRTL();
                                missionProxy.sendMissionToAPM();
                            }

                            @Override
                            public void onNo() {
                                missionProxy.sendMissionToAPM();
                            }
                        },
                        getString(R.string.pref_auto_insert_mission_takeoff_rtl_land_key));

                if(dialog != null) {
                    dialog.show(getSupportFragmentManager(), "Mission Upload check.");
                }
			}
			return true;

		case R.id.menu_load_mission:
			drone.getWaypointManager().getWaypoints();
			return true;
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.menu_connect:
			//app.notifyDisconnected();
			toggleDroneConnection();
			return true;
			default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_connect:
			toggleDroneConnection();
			return true;

		case R.id.menu_map_type_hybrid:
		case R.id.menu_map_type_normal:
		case R.id.menu_map_type_terrain:
		case R.id.menu_map_type_satellite:
			setMapTypeFromItemId(item.getItemId());
			return true;

		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

	public void toggleDroneConnection() {

        Log.d(MAVSERVICE, "SuperUI  -  toggleDroneConnection() - SÓ ENTRAR AQUI NA PRIMEIRA CONEXÃO");

		if (!drone.getMavClient().isConnected()) {
			final String connectionType = mAppPrefs.getMavLinkConnectionType();

			if (Utils.ConnectionType.BLUETOOTH.name().equals(connectionType)) {
				// Launch a bluetooth device selection screen for the user
				final String address = mAppPrefs.getBluetoothDeviceAddress();
				if (address == null || address.isEmpty()) {
					new BTDeviceListFragment().show(getSupportFragmentManager(),
							"Device selection dialog");
					return;
				}
			}
		}
        drone.getMavClient().setUdpPortNumber("24550");
		drone.getMavClient().toggleConnectionState();
	}

	private void setMapTypeFromItemId(int itemId) {
		final String mapType;
		switch (itemId) {
		case R.id.menu_map_type_hybrid:
			mapType = GoogleMapFragment.MAP_TYPE_HYBRID;
			break;
		case R.id.menu_map_type_normal:
			mapType = GoogleMapFragment.MAP_TYPE_NORMAL;
			break;
		case R.id.menu_map_type_terrain:
			mapType = GoogleMapFragment.MAP_TYPE_TERRAIN;
			break;
		default:
			mapType = GoogleMapFragment.MAP_TYPE_SATELLITE;
			break;
		}

		PreferenceManager.getDefaultSharedPreferences(this).edit()
				.putString(GoogleMapFragment.PREF_MAP_TYPE, mapType).commit();

		 //drone.notifyMapTypeChanged();
	}

    public void newDrone()
    {
        drone = app.getDrone();
        drone.addDroneListener(this);
        drone.getMavClient().queryConnectionState();
        //drone.notifyDroneEvent(DroneEventsType.MISSION_UPDATE);
    }

}