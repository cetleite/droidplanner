package org.droidplanner.android.fragments;

import java.util.List;
import java.util.Set;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.graphic.map.GraphicDrone;
import org.droidplanner.android.graphic.map.GraphicGuided;
import org.droidplanner.android.graphic.map.GraphicHome;
import org.droidplanner.android.maps.DPMap;
import org.droidplanner.android.maps.MarkerInfo;
import org.droidplanner.android.maps.providers.DPMapProvider;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.survey.CameraInfo;

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
import android.widget.TextView;

public abstract class DroneMap extends Fragment implements OnDroneListener {

	private final static String TAG = DroneMap.class.getSimpleName();

	private final Handler mHandler = new Handler();

    private static final String EVENTGPS = "EVENTGPS";


	private final Runnable mUpdateMap = new Runnable() {
		@Override
		public void run() {

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

	private GraphicHome home;
	public GraphicDrone graphicDrone, graphicDrone2;
	public GraphicGuided guided;

	protected MissionProxy missionProxy;
	public Drone drone, drone2;

	protected Context context;

	private CameraInfo camera = new CameraInfo();

	protected abstract boolean isMissionDraggable();

    private static final String NEW_DRONE = "NEW_DRONE";
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case "NEW_DRONE":
                    Log.d(NEW_DRONE, "DroneMap - NEW_DRONE");
                    newDrone();
                    graphicDrone.setTitle(Integer.toString(drone.getDroneID()));
                    graphicDrone2.setTitle(Integer.toString(drone2.getDroneID()));
                    break;
                case "NEW_DRONE_SELECTED":
                    Log.d(NEW_DRONE, "DroneMap - NEW_DRONE_SELECTED");
                    newDroneSelected(intent.getExtras().getInt("droneID"));
                    //missionProxy = ((DroidPlannerApp) getActivity().getApplication()).getMissionProxy();
                    break;
            }
        }
    };


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
		final View view = inflater.inflate(R.layout.fragment_drone_map, viewGroup, false);

		final Activity activity = getActivity();
		final DroidPlannerApp app = ((DroidPlannerApp) activity.getApplication());
		drone = app.getDrone();
        drone2 = app.createNewDrone(010101);
        missionProxy = app.getMissionProxy();

		home = new GraphicHome(drone);
		graphicDrone = new GraphicDrone(drone);


        graphicDrone2 = new GraphicDrone(drone2);

		guided = new GraphicGuided(drone);

        updateMapFragment();
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
		mHandler.removeCallbacksAndMessages(null);
		mMapFragment.saveCameraPosition();
        getActivity().unregisterReceiver(broadcastReceiver);
	}

	@Override
	public void onResume() {
		super.onResume();
		drone.addDroneListener(this);
		mMapFragment.loadCameraPosition();
		postUpdate();
        addBroadcastFilters();
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
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
		switch (event) {
		case MISSION_UPDATE:
			postUpdate();
			break;

		case GPS:
            drone2.getGps().setPosition(new Coord2D(drone.getGps().getPosition().getLat() + 0.0002, drone.getGps().getPosition().getLng() + 0.0002));
            mMapFragment.updateMarkerGraphic(graphicDrone2);



			mMapFragment.updateMarkerGraphic(graphicDrone);
			mMapFragment.updateDroneLeashPath(guided);
			if (drone.getGps().isPositionValid()) {
				mMapFragment.addFlightPathPoint(drone.getGps().getPosition());
			}


			break;

		case ATTITUDE:
			if (((DroidPlannerApp) getActivity().getApplication()).getPreferences()
					.isRealtimeFootprintsEnabled()) {
				if (drone.getGps().isPositionValid()) {
					mMapFragment.updateRealTimeFootprint(drone.getCamera().getCurrentFieldOfView());
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
			mMapFragment.updateMarker(graphicDrone);

            //drone2.getGps().setPosition(new Coord2D(drone.getGps().getPosition().getLat() + 0.0002, drone.getGps().getPosition().getLng() + 0.0002));
            //mMapFragment.updateMarker(graphicDrone2);
			break;

		case DISCONNECTED:
		case HEARTBEAT_TIMEOUT:
			mMapFragment.updateMarker(graphicDrone);
			break;
		case FOOTPRINT:
				mMapFragment.addCameraFootprint(drone.getCamera().getLastFootprint());
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

    public void newDrone()
    {
        final Activity activity = getActivity();
        final DroidPlannerApp app = ((DroidPlannerApp) activity.getApplication());
        drone = app.getDrone();


        drone.addDroneListener(this);

        home = new GraphicHome(drone);
        graphicDrone = new GraphicDrone(drone);
        guided = new GraphicGuided(drone);
    }

    public void newDroneSelected(int droneId)
    {
        final Activity activity = getActivity();
        final DroidPlannerApp app = ((DroidPlannerApp) activity.getApplication());
        drone = app.getDroneList().get(droneId);


        drone.addDroneListener(this);

        home = new GraphicHome(drone);
        graphicDrone = new GraphicDrone(drone);
        guided = new GraphicGuided(drone);

    }

    private void plotDrones()
    {
        graphicDrone2 = new GraphicDrone(drone2);
        drone2.getGps().setPosition(new Coord2D(drone.getGps().getPosition().getLat() + 0.0002, drone.getGps().getPosition().getLng() + 0.0002));
        mMapFragment.updateMarker(graphicDrone2);


/*


        */


    }
}
