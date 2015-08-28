package org.droidplanner.android;

import org.droidplanner.android.communication.service.MAVLinkClient;
import org.droidplanner.android.gcs.location.FusedLocation;
import org.droidplanner.android.notifications.NotificationHandler;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.core.MAVLink.MAVLinkStreams;
import org.droidplanner.core.MAVLink.MavLinkMsgHandler;
import org.droidplanner.core.drone.DroneImpl;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneInterfaces.Clock;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.core.model.Drone;

import java.util.concurrent.Semaphore;


import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.MAVLink.Messages.MAVLinkMessage;
import java.util.HashMap;


public class DroidPlannerApp extends ErrorReportApp implements MAVLinkStreams.MavlinkInputStream,
		DroneInterfaces.OnDroneListener {

	private Drone drone, drone2;
	private Follow followMe;
	private MissionProxy missionProxy;
	private MavLinkMsgHandler mavLinkMsgHandler, mavLinkMsgHandler2;
	private DroidPlannerPrefs prefs, prefs2;
    public Context superUIContext;

    HashMap<Integer, Drone> droneList = new HashMap<Integer, Drone>();

    private boolean connectedTower = false;
    MAVLinkClient MAVClient;


    public static Semaphore access = new Semaphore(1);


	private static final String FLUXO = "FLUXO";
    private static final String NOVOFLUXO = "NOVOFLUXO";
    private static final String SENDING = "SENDING";
    private static final String MAVMSGDRONE2 = "MAVMSGDRONE2";
    private static final String LISTADRONES = "LISTADRONES";
	/**
	 * Handles dispatching of status bar, and audible notification.
	 */
	public NotificationHandler mNotificationHandler;

    Handler handler = new Handler() {
        android.os.Handler handler = new android.os.Handler();

        @Override
        public void removeCallbacks(Runnable thread) {
            handler.removeCallbacks(thread);
        }

        @Override
        public void post(Runnable thread){
            Log.d(NOVOFLUXO, "DroidPlannerApp  - post(Runable thread)!!!");
            Log.d(FLUXO, "DroidPlannerApp  -  post(Runable thread)!!!");
            //Log.d(MAVSERVICE, "DroidPlannerApp  -  post(Runable thread)!!!");

            handler.post(thread);
        }

        @Override
        public void postDelayed(Runnable thread, long timeout) {
            handler.postDelayed(thread, timeout);
        }
    };


	@Override
	public void onCreate() {
        Log.d(NOVOFLUXO, "DroidPlannerApp  -  onCreate!!!");

		super.onCreate();

		final Context context = getApplicationContext();

		MAVClient = new MAVLinkClient(this, this);


		Clock clock = new Clock() {
			@Override
			public long elapsedRealtime() {
				return SystemClock.elapsedRealtime();
			}
		};

		mNotificationHandler = new NotificationHandler(context);

		prefs = new DroidPlannerPrefs(context);
		drone = new DroneImpl(MAVClient, clock, handler, prefs);
		getDrone().addDroneListener(this);


		missionProxy = new MissionProxy(getDrone().getMission());
		//mavLinkMsgHandler = new org.droidplanner.core.MAVLink.MavLinkMsgHandler(getDrone());
        mavLinkMsgHandler = new MavLinkMsgHandler();

		followMe = new Follow(getDrone(), handler, new FusedLocation(context));

		GAUtils.initGATracker(this);
		GAUtils.startNewSession(context);

		// Any time the application is started, do a quick scan to see if we
		// need any uploads
		//startService(UploaderService.createIntent(this));
	}

	@Override
	public void notifyReceivedData(MAVLinkMessage msg) {
        Log.d(NOVOFLUXO, "DroidPlannerApp  -  notifyReceivedData!!!");
		Log.d(FLUXO, "DroidPlannerApp  -  notifyReceivedData! - Recebeu mensagem!!!");

		//Log.d(FLUXO, "HEARTBEAT: sys_id = " + msg.sysid + " comp_id: " + msg.compid);
        //Log.d(LISTADRONES, "IP_ADD: " + msg.sysid  + " PORT: " + msg.compid + " TESTE" + drone.getHostPort());
       // if(msg.compid == drone.getHostPort())
       //     mavLinkMsgHandler.receiveData(msg, drone);

        Log.d(LISTADRONES, Integer.toString(msg.msgid));
        //EXCLUIR ESSE IF NO FUTURO!!!
        if(msg.compid!=1) { //EXCLUIR ESSE IF NO FUTURO!!!

            if (!droneList.containsKey(msg.compid)) {
                Log.d(LISTADRONES, "NAO CONTEM DRONE NA LISTA!!");
                Drone new_drone = createNewDrone();
                droneList.put(msg.compid, new_drone);
                new_drone.notifyDroneEvent(DroneEventsType.CONNECTED);

                //mavLinkMsgHandler.receiveData(msg, new_drone);
                mavLinkMsgHandler.receiveData(msg, drone);


                if(superUIContext!=null)
                    superUIContext.getApplicationContext().sendBroadcast(new Intent().setAction("NEW_DRONE"));

            } else {
                //Log.d(LISTADRONES, "ESTA NA LISTA!!!!!");
                //mavLinkMsgHandler.receiveData(msg, droneList.get(msg.compid));
                mavLinkMsgHandler.receiveData(msg, drone);
            }
        }
       // access.release();
	}

	@Override
	public void notifyConnected(String udpPort) {
        this.connectedTower = true;

        Log.d(NOVOFLUXO, "DroidPlannerApp  -  notifyConnected!!!");
            Log.d(FLUXO, "DroidPlannerApp  -  notifyConnected()!!");
            //getDrone().notifyDroneEvent(DroneEventsType.CONNECTED);

        /*ATIVAR O BOTÃO DE DESCONECTAR A INTEFACE!!!*/

        if(superUIContext!=null)
           superUIContext.getApplicationContext().sendBroadcast(new Intent().setAction("TOWER_CONNECTED"));
        else
            Log.d(NOVOFLUXO, "DroidPlannerApp  -  notifyConnected!!! -NULL!!!!!!!><><><><");
	}

	@Override
	public void notifyDisconnected() {
        Log.d(NOVOFLUXO, "DroidPlannerApp  -  notifyDisconnected!!!");
		Log.d(FLUXO, "DroidPlannerApp  -  notifyDisconnected()!!");
		//getDrone().notifyDroneEvent(DroneEventsType.DISCONNECTED);
        this.connectedTower = true;

        if(superUIContext!=null)
            superUIContext.getApplicationContext().sendBroadcast(new Intent().setAction("TOWER_DISCONNECTED"));
        else
            Log.d(NOVOFLUXO, "DroidPlannerApp  -  notifyConnected!!! -NULL!!!!!!!><><><><");
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
        Log.d(NOVOFLUXO, "DroidPlannerApp  -  onDroneEvent!!!");
        Log.d(FLUXO, "DroidPlannerApp  -  onDroneEvent()!!==> " + drone.getMavClient().getUdpPortNumber());
		mNotificationHandler.onDroneEvent(event, drone);

		switch (event) {
		case MISSION_RECEIVED:
			// Refresh the mission render state
			missionProxy.refresh();
			break;
		default:
			break;
		}
	}

	public DroidPlannerPrefs getPreferences() {
		return prefs;
	}

	public Drone getDrone() {
		return drone;
	}

    public Follow getFollowMe() {
        return followMe;
    }

    public MissionProxy getMissionProxy() {
        return missionProxy;
    }

	public Drone createNewDrone()
	{
		Drone drone;
		DroidPlannerPrefs new_Prefs;

		Clock clock = new Clock() {
			@Override
			public long elapsedRealtime() {
				return SystemClock.elapsedRealtime();
			}
		};
		new_Prefs = new DroidPlannerPrefs(getApplicationContext());


		drone = new DroneImpl(MAVClient, clock, handler, new_Prefs);
		drone.addDroneListener(this);

		return drone;
	}

    public boolean isTowerConnected()
    {
        return this.connectedTower;
    }
}
