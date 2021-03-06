package org.droidplanner.android.activities.helpers;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.MultipleActivity;
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

import android.view.MenuInflater;

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
import android.widget.PopupMenu.OnMenuItemClickListener;
import java.lang.reflect.Field;
import android.view.ViewConfiguration;
import android.view.View;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import android.content.BroadcastReceiver;

/**
 * Parent class for the app activity classes.
 */
public abstract class SuperUI extends FragmentActivity implements OnDroneListener {

	public final static String ACTION_TOGGLE_DRONE_CONNECTION = SuperUI.class.getName()
			+ ".ACTION_TOGGLE_DRONE_CONNECTION";

    private ScreenOrientation screenOrientation = new ScreenOrientation(this);
	private static InfoBarActionProvider infoBar;
	private static GCSHeartbeat gcsHeartbeat;
	public static DroidPlannerApp app;
	public static Drone drone;

    static boolean connectedTower = false;
    static boolean connectedDrone = false;

	private static final String FLUXO2 = "FLUXO2";
    private static final String MAVSERVICE = "MAVSERVICE";
    private static final String NOVOFLUXO3 = "NOVOFLUXO3";
    private static final String NOVOFLUXO = "NOVOFLUXO";
    private static final String ACTIVITY = "ACTIVITY";


    private static List<Integer> dronesList = new ArrayList<Integer>();


	/**
	 * Handle to the app preferences.
	 */
	protected static DroidPlannerPrefs mAppPrefs;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(NOVOFLUXO3, "SuperUI  -  RECEBEU BROADCAST!!!");

            switch (action) {
                case "TOWER_CONNECTED":
                    connectedTower = true;
                    Log.d(NOVOFLUXO3, "SuperUI  -  RECEBEU BROADCAST!!!() - TOWER_CONNECTED");
                    gcsHeartbeat.setActive(true);
                    invalidateOptionsMenu();
                    screenOrientation.requestLock();
                    break;
                case "TOWER_DISCONNECTED":
                    connectedTower = false;
                    connectedDrone = false;
                    Log.d(NOVOFLUXO3, "SuperUI  -  RECEBEU BROADCAST!!!() - TOWER_DIISCONNECTED");
                    gcsHeartbeat.setActive(false);
                    invalidateOptionsMenu();

                    //APAGAR ESTRUTURA COM OS IDS DOS DRONES
                    dronesList.clear();
                    break;
                case "NEW_DRONE":
                    Log.d(NOVOFLUXO3, "SuperUI  - NEW_DRONE");


                    //Indica que há pelo menos um Drone conectado
                    connectedDrone = true;
                    //Inicializa estruturas para novo Drone
                    newDrone(intent.getExtras().getInt("droneID"));
                    invalidateOptionsMenu();
                    break;

                case "NEW_DRONE_SELECTED":
                    Log.d(NOVOFLUXO3, "SuperUI  - NEW_DRONE_SELECTED");


                    connectedDrone = true;
                    newDroneSelected(intent.getExtras().getInt("droneID"));
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
        Log.d(FLUXO2, "SuperUI  -  onCreate()");

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
        final IntentFilter newDroneSelectedFilter = new IntentFilter();
        newDroneSelectedFilter.addAction("NEW_DRONE_SELECTED");
        registerReceiver(broadcastReceiver, newDroneSelectedFilter);

        final IntentFilter showInfoBar = new IntentFilter();
        showInfoBar.addAction("SHOW_INFO_BAR");
        registerReceiver(broadcastReceiver, showInfoBar);
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
/*TALVEZ PRECISE MUDAR ISSO DEPOIS...PARA CASO TIVER MAIS DE UM DRONE CONECTADO AINDA MANDAR HEARTBEAT*/
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

        /*
        Log.d(FLUXO2, "SuperUI  -  onCreateOptionsMenu()");


        if (this.infoBar != null) {
            this.infoBar.setDrone(null);
            this.infoBar = null;
        }


		getMenuInflater().inflate(R.menu.menu_super_activiy, menu);


        final MenuItem infoBarItem = menu.findItem(R.id.menu_info_bar);
        if (infoBarItem != null) {
            this.infoBar = (InfoBarActionProvider) infoBarItem.getActionProvider();
            Log.d(FLUXO2, "SuperUI  -  INICIALIZOU INFOBAR!!!");
        }

	final MenuItem toggleConnectionItem = menu.findItem(R.id.menu_connect);
        if(connectedTower)
        {
            Log.d(FLUXO2, "SuperUI  -  onCreateOptionsMenu() - CONECTADO!");
            menu.setGroupEnabled(R.id.menu_group_connected, true);
            menu.setGroupVisible(R.id.menu_group_connected, true);

            if(connectedDrone) {
                final MenuItem sendMission = menu.findItem(R.id.menu_send_mission);
                sendMission.setEnabled(true);
                sendMission.setVisible(true);

                final MenuItem loadMission = menu.findItem(R.id.menu_load_mission);
                loadMission.setEnabled(true);
                loadMission.setVisible(true);

                final MenuItem infoBar = menu.findItem(R.id.menu_info_bar);
                infoBar.setEnabled(true);
                infoBar.setVisible(true);

                final MenuItem droneSelection = menu.findItem(R.id.menu_popup_drone);
                droneSelection.setEnabled(true);
                droneSelection.setVisible(true);

                if(this.infoBar!=null) {
                    this.infoBar.setDrone(app.getDrone());
                    Log.d(FLUXO2, "SuperUI  -  SETOU INFOBAR!!!");
                }
            }
            else
            {
                final MenuItem infoBar = menu.findItem(R.id.menu_info_bar);
                infoBar.setEnabled(false);
                infoBar.setVisible(false);

                final MenuItem droneSelection = menu.findItem(R.id.menu_popup_drone);
                droneSelection.setEnabled(false);
                droneSelection.setVisible(false);
            }

        } else
        {
            Log.d(FLUXO2, "SuperUI  -  onCreateOptionsMenu() - DISCONECTADO!");
            menu.setGroupEnabled(R.id.menu_group_connected, false);
            menu.setGroupVisible(R.id.menu_group_connected, false);

            final MenuItem infoBar = menu.findItem(R.id.menu_info_bar);
            infoBar.setEnabled(false);
            infoBar.setVisible(false);

            toggleConnectionItem.setTitle(R.string.menu_connect);

            final MenuItem droneSelection = menu.findItem(R.id.menu_popup_drone);
            droneSelection.setEnabled(false);
            droneSelection.setVisible(false);

            if (this.infoBar != null) {
                this.infoBar.setDrone(null);
            }
        }

		return super.onCreateOptionsMenu(menu);
*/

        return true;

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
        case R.id.menu_popup_drone:

            View vItem = findViewById(R.id.menu_popup_drone);
            PopupMenu popMenu = new PopupMenu(this, vItem);

            for (int i = 0; i < dronesList.size(); i++) {
                popMenu.getMenu().add(0, i, i, dronesList.get(i).toString());
            }

            popMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int droneIdSelected = Integer.parseInt(item.getTitle().toString());

                    if(droneIdSelected != app.getDrone().getDroneID()){
                        Log.d(FLUXO2, "SELECIONOU DRONE DIFERENTE!!!!!");
                        app.onNewDroneSelected(droneIdSelected);
                    }
                    else{
                        Log.d(FLUXO2, "MESMO DRONE SELECIONADO!!!!!");
                    }

                    return true;
                }
            });

            popMenu.show();
            return true;

        case R.id.menu_multiple:
            Intent intent = new Intent(this, MultipleActivity.class);
            startActivity(intent);
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

        if(drone!=null) {
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

    public void newDrone(int newDroneID)
    {
        dronesList.add(newDroneID);
    }

    public void newDroneSelected(int droneId)
    {
        drone.removeDroneListener(this);

        if(!dronesList.contains(droneId))
            dronesList.add(droneId);

        drone = app.getDroneList().get(droneId);
        if(drone!=null) {
            drone.addDroneListener(this);
            drone.getMavClient().queryConnectionState();
            gcsHeartbeat = new GCSHeartbeat(drone, 1);
            drone.notifyDroneEvent(DroneEventsType.MISSION_UPDATE);
        }

    }

}