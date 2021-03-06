package org.droidplanner.android.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.MultipleActivity;
import org.droidplanner.android.graphic.map.GraphicDrone;
import org.droidplanner.android.graphic.map.GraphicGuided;
import org.droidplanner.android.graphic.map.GraphicHome;
import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.android.maps.providers.google_map.GoogleMapFragment;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.survey.CameraInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public abstract class DroneMapEditor extends Fragment implements OnDroneListener {

	private final static String TAG = DroneMapEditor.class.getSimpleName();

	private final Handler mHandler = new Handler();

    private static final String EVENTGPS = "EVENTGPS";

    private List<GraphicDrone> graphicDroneList = new ArrayList();


	private final Runnable mUpdateMap = new Runnable() {
		@Override
		public void run() {

            Log.d(EVENTGPS, "Updating map!!!!!!");

			final List<MarkerInfo> missionMarkerInfos = missionProxy.getMarkersInfos();

			final boolean isThereMissionMarkers = !missionMarkerInfos.isEmpty();
			final boolean isHomeValid = home.isValid();
            final boolean isGuidedVisible = guided.isVisible();



            // Get the list of markers currently on the map.
			final Set<MarkerInfo> markersOnTheMap = mMapFragment.getMarkerInfoList();

			if (!markersOnTheMap.isEmpty()) {
				if (isHomeValid) {
					markersOnTheMap.remove(home);
				}

                if(isGuidedVisible){
                    markersOnTheMap.remove(guided);
                }

				if (isThereMissionMarkers) {
					markersOnTheMap.removeAll(missionMarkerInfos);
				}

				mMapFragment.removeMarkers(markersOnTheMap);
			}

			if (isHomeValid) {
				mMapFragment.updateMarker(home);
			}

            if(isGuidedVisible){
                mMapFragment.updateMarker(guided);
            }

			if (isThereMissionMarkers) {
				mMapFragment.updateMarkers(missionMarkerInfos, isMissionDraggable());
			}

			mMapFragment.updateMissionPath(missionProxy);

			mMapFragment.updatePolygonsPaths(missionProxy.getPolygonsPath());

			mHandler.removeCallbacks(this);
		}
	};

	protected DPMap mMapFragment;

    public DroidPlannerApp dp;
	private GraphicHome home;
	public GraphicDrone graphicDrone;//, graphicDrone2, graphicDrone3;
	public GraphicGuided guided;

	protected MissionProxy missionProxy;
	public Drone drone;
    public int num_map;

	protected Context context;

	private CameraInfo camera = new CameraInfo();

	protected abstract boolean isMissionDraggable();

    private static final String NEW_DRONE = "NEW_DRONE";
    private static final String DRONE_MAP = "DRONE_MAP";
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case "NEW_DRONE":
                    Log.d(NEW_DRONE, "DroneMap - NEW_DRONE");
                    break;
                case "NEW_DRONE_SELECTED":
                    Log.d(NEW_DRONE, "DroneMap - NEW_DRONE_SELECTED");
                    break;
            }
        }
    };

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
		final View view = inflater.inflate(R.layout.fragment_drone_map, viewGroup, false);

		final Activity activity = getActivity();
		final DroidPlannerApp app = ((DroidPlannerApp) activity.getApplication());

        int droneID = MultipleActivity.getDroneIDFromMap(num_map);
        drone = app.getDrone(droneID);

        if(drone!=null) {
            missionProxy = app.getMissionProxyFromDroneID(droneID);

            home = new GraphicHome(drone);
            graphicDrone = new GraphicDrone(drone);


            guided = new GraphicGuided(drone);

            updateMapFragment();
        }
        else
        {
            drone = app.getDrone();

            missionProxy = app.getMissionProxy();

            home = new GraphicHome(drone);
            graphicDrone = new GraphicDrone(drone);


            guided = new GraphicGuided(drone);

            updateMapFragment();
        }
		return view;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mHandler.removeCallbacksAndMessages(null);
	}

	private void updateMapFragment() {
		// Add the map fragment instance (based on user preference)
		final DPMapProvider mapProvider = Utils.getMapProvider(getActivity()
				.getApplicationContext());

		final FragmentManager fm = getChildFragmentManager();
		mMapFragment = (DPMap) fm.findFragmentById(R.id.map_fragment_container);
		if (mMapFragment == null || mMapFragment.getProvider() != mapProvider) {

            Log.d(NEW_DRONE, "DroneMap - updateMapFragment!!! entrou no IF");
			final Bundle mapArgs = new Bundle();
			mapArgs.putInt(DPMap.EXTRA_MAX_FLIGHT_PATH_SIZE, getMaxFlightPathSize());

			mMapFragment = mapProvider.getMapFragment();
			((Fragment) mMapFragment).setArguments(mapArgs);
			fm.beginTransaction().replace(R.id.map_fragment_container, (Fragment) mMapFragment)
					.commit();
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		drone.removeDroneListener(this);
     //   onPauseList();
		mHandler.removeCallbacksAndMessages(null);
		mMapFragment.saveCameraPosition();
        getActivity().unregisterReceiver(broadcastReceiver);
	}

    public void onPauseList()
    {
        HashMap<Integer, Drone> droneList;
        droneList = ((DroidPlannerApp) getActivity().getApplication()).getDroneList();

        if(droneList.size()>0) {
            Iterator<Integer> keySetIterator = droneList.keySet().iterator();

            Integer key = keySetIterator.next();
            Drone drone;

            while (keySetIterator.hasNext()) {
                key = keySetIterator.next();
                drone = droneList.get(key);
                drone.removeDroneListener(this);
            }
        }
    }


	@Override
	public void onResume() {
		super.onResume();
	//	drone.addDroneListener(this);
      //  onResumeList();
		mMapFragment.loadCameraPosition();
		postUpdate();
        addBroadcastFilters();
	}

    public void onResumeList()
    {
        HashMap<Integer, Drone> droneList;
        droneList = ((DroidPlannerApp) getActivity().getApplication()).getDroneList();

        if(droneList.size()>0) {
            Iterator<Integer> keySetIterator = droneList.keySet().iterator();

            Integer key = keySetIterator.next();
            Drone drone;
            drone = droneList.get(key);
            drone.addDroneListener(this);

            while (keySetIterator.hasNext()) {
                key = keySetIterator.next();
                drone = droneList.get(key);
                //drone.addDroneListener(this);
            }
        }
    }


	@Override
	public void onStart() {
		super.onStart();
		updateMapFragment();
        addBroadcastFilters();
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = activity.getApplicationContext();
        dp = (DroidPlannerApp) getActivity().getApplication();
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {

     //   Log.d(DRONE_MAP, "DroneMap - DRONE_ID: " + drone.getDroneID());
        if(drone.getDroneID() != this.drone.getDroneID())
            Log.d(DRONE_MAP, "{{====DIFERENTE====}}");


		switch (event) {
		case MISSION_UPDATE:
            Log.d(EVENTGPS, "MISSION_UPDATE!!!");
			postUpdate();
			break;

		case GPS:
            Log.d(EVENTGPS, "GPS!!!");
    //        mMapFragment.updateMarkersGraphic(graphicDroneList);


			mMapFragment.updateMarkerGraphic(graphicDrone);
			mMapFragment.updateDroneLeashPath(guided);
			if (drone.getGps().isPositionValid()) {
		//		mMapFragment.addFlightPathPoint(drone.getGps().getPosition());
			}


			break;

		case ATTITUDE:
            Log.d(EVENTGPS, "ATTITUDE!!!");
            if(getActivity()!=null )

			if (((DroidPlannerApp) getActivity().getApplication()).getPreferences()
					.isRealtimeFootprintsEnabled()) {
				if (drone.getGps().isPositionValid()) {
					//mMapFragment.updateRealTimeFootprint(drone.getCamera().getCurrentFieldOfView());
				}

			}
			break;
		case GUIDEDPOINT:
            Log.d(EVENTGPS, "GUIDEDPOINT!!!");
			mMapFragment.updateMarker(guided);
			mMapFragment.updateDroneLeashPath(guided);
			break;

		case HEARTBEAT_RESTORED:
		case HEARTBEAT_FIRST:
            Log.d(EVENTGPS, "HEARTBEAT RESTORED/FIRST!!!");
			mMapFragment.updateMarker(graphicDrone);

            //drone2.getGps().setPosition(new Coord2D(drone.getGps().getPosition().getLat() + 0.0002, drone.getGps().getPosition().getLng() + 0.0002));
            //mMapFragment.updateMarker(graphicDrone2);
			break;

		case DISCONNECTED:
		case HEARTBEAT_TIMEOUT:
            Log.d(EVENTGPS, "DISCONNECTED!!!");
			mMapFragment.updateMarker(graphicDrone);
			break;
		case FOOTPRINT:
				//mMapFragment.addCameraFootprint(drone.getCamera().getLastFootprint());
			break;
		default:
			break;
		}
	}

	public final void postUpdate() {
		mHandler.post(mUpdateMap);
	}

	protected int getMaxFlightPathSize() {
		return 0;
	}

	/**
	 * Adds padding around the edges of the map.
	 * 
	 * @param left
	 *            the number of pixels of padding to be added on the left of the
	 *            map.
	 * @param top
	 *            the number of pixels of padding to be added on the top of the
	 *            map.
	 * @param right
	 *            the number of pixels of padding to be added on the right of
	 *            the map.
	 * @param bottom
	 *            the number of pixels of padding to be added on the bottom of
	 *            the map.
	 */
	public void setMapPadding(int left, int top, int right, int bottom) {
		mMapFragment.setMapPadding(left, top, right, bottom);
	}

	public void saveCameraPosition() {
		mMapFragment.saveCameraPosition();
	}

	public List<Coord2D> projectPathIntoMap(List<Coord2D> path) {
		return mMapFragment.projectPathIntoMap(path);
	}

	/**
	 * Set map panning mode on the specified target.
	 * 
	 * @param target
	 */
	public abstract boolean setAutoPanMode(AutoPanMode target);

	/**
	 * Move the map to the user location.
	 */
	public void goToMyLocation() {
		mMapFragment.goToMyLocation();
	}

	/**
	 * Move the map to the drone location.
	 */
	public void goToDroneLocation() {
        GoogleMapFragment googlemaps = (GoogleMapFragment)mMapFragment;
        googlemaps.googleNewDrone(this.drone);

		mMapFragment.goToDroneLocation();
	}

    /**
     * Update the map rotation.
     * @param bearing
     */
    public void updateMapBearing(float bearing){
        mMapFragment.updateCameraBearing(bearing);
    }

    /**
     * Ignore marker clicks on the map and instead report the event as a mapClick
     * @param skip if it should skip further events
     */
    public void skipMarkerClickEvents(boolean skip){
    	mMapFragment.skipMarkerClickEvents(skip);
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

    public void newDrone(int droneId) {
        final Activity activity = getActivity();
        final DroidPlannerApp app = ((DroidPlannerApp) activity.getApplication());
        drone = app.getDroneList().get(droneId);


        drone.addDroneListener(this);

        home = new GraphicHome(drone);
        graphicDrone = new GraphicDrone(drone);
        graphicDroneList.add(graphicDrone);
        graphicDrone.setTitle(Integer.toString(drone.getDroneID()));

        updateMapFragment();
    }

    public void newDroneSelected(int droneId)
    {

        if(drone!=null)
            drone.removeDroneListener(this);

        final Activity activity = getActivity();
        final DroidPlannerApp app = ((DroidPlannerApp) activity.getApplication());
        drone = app.getDroneList().get(droneId);

        //Pode ter sido inutilizado ao desconectar torre (acesso concorrente)
        if(drone!=null) {
            drone.addDroneListener(this);

            home = new GraphicHome(drone);
            missionProxy = app.getMissionProxy();
            graphicDrone = new GraphicDrone(drone);
            graphicDrone.setTitle(Integer.toString(drone.getDroneID()));
            graphicDroneList.add(graphicDrone);

            guided = new GraphicGuided(drone);
      }

        updateMapFragment();


    }

    public void setMapId(int num_map)
    {
        this.num_map = num_map;
    }

    public void setDroneMapDrone(int drone_id)
    {
        final Activity activity = getActivity();
        final DroidPlannerApp app = ((DroidPlannerApp) activity.getApplication());

        drone = app.getDroneList().get(drone_id);

        if(drone!=null) {
            drone.addDroneListener(this);

            home = new GraphicHome(drone);
            missionProxy = app.getMissionProxy();
            graphicDrone = new GraphicDrone(drone);
            graphicDrone.setTitle(Integer.toString(drone.getDroneID()));
            graphicDroneList.add(graphicDrone);

            guided = new GraphicGuided(drone);
        }

        updateMapFragment();
    }

    public void setDroneMapDrone(Drone newDrone)
    {
        if(drone!=null)
            drone.removeDroneListener(this);

        drone = newDrone;

        if(drone!=null) {
            Log.d(EVENTGPS, "DRONE NÃO EH NULLL!!!!!!! - droneId => " + drone.getDroneID());
            drone.addDroneListener(this);

            home = new GraphicHome(drone);
            graphicDrone = new GraphicDrone(drone);
            graphicDrone.setTitle(Integer.toString(drone.getDroneID()));
            graphicDroneList.add(graphicDrone);

            guided = new GraphicGuided(drone);
        }

        updateMapFragment();
    }


    public void setMissionProxy(MissionProxy mp)
    {
        this.missionProxy = mp;
    }
}
