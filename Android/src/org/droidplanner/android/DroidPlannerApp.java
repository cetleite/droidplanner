package org.droidplanner.android;

import org.droidplanner.android.communication.service.MAVLinkClient;
import org.droidplanner.android.communication.service.UploaderService;
import org.droidplanner.android.gcs.location.FusedLocation;
import org.droidplanner.android.notifications.NotificationHandler;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.core.MAVLink.MAVLinkStreams;
import org.droidplanner.core.MAVLink.MavLinkMsgHandler;
import org.droidplanner.core.MAVLink.MavLinkHeartbeat;
import org.droidplanner.core.drone.DroneEvents;
import org.droidplanner.core.drone.DroneImpl;
import org.droidplanner.core.drone.DroneInterfaces;
import org.droidplanner.core.drone.DroneInterfaces.Clock;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.Handler;
import org.droidplanner.core.gcs.follow.Follow;
import org.droidplanner.core.model.Drone;

import java.util.concurrent.Semaphore;


import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.MAVLink.Messages.MAVLinkMessage;

public class DroidPlannerApp extends ErrorReportApp implements MAVLinkStreams.MavlinkInputStream,
		DroneInterfaces.OnDroneListener {

	private Drone drone, drone2;
	private Follow followMe;
	private MissionProxy missionProxy;
	private MavLinkMsgHandler mavLinkMsgHandler, mavLinkMsgHandler2;
	private DroidPlannerPrefs prefs, prefs2;

    public static Semaphore access = new Semaphore(1);


	private static final String FLUXO = "FLUXO";
    private static final String SENDING = "SENDING";
    private static final String MAVMSGDRONE2 = "MAVMSGDRONE2";
	/**
	 * Handles dispatching of status bar, and audible notification.
	 */
	public NotificationHandler mNotificationHandler;

	@Override
	public void onCreate() {
		super.onCreate();

		final Context context = getApplicationContext();

		MAVLinkClient MAVClient = new MAVLinkClient(this, this);
        MAVLinkClient MAVClient2 = new MAVLinkClient(this, this);

		Clock clock = new Clock() {
			@Override
			public long elapsedRealtime() {
				return SystemClock.elapsedRealtime();
			}
		};
		Handler handler = new Handler() {
			android.os.Handler handler = new android.os.Handler();

			@Override
			public void removeCallbacks(Runnable thread) {
				handler.removeCallbacks(thread);
			}

            @Override
            public void post(Runnable thread){
				Log.d(FLUXO, "DroidPlannerApp  -  post(Runable thread)!!!");
                //Log.d(MAVSERVICE, "DroidPlannerApp  -  post(Runable thread)!!!");

                handler.post(thread);
            }

			@Override
			public void postDelayed(Runnable thread, long timeout) {
				handler.postDelayed(thread, timeout);
			}
		};
		mNotificationHandler = new NotificationHandler(context);

		prefs = new DroidPlannerPrefs(context);
        prefs2 = new DroidPlannerPrefs(context);
		drone = new DroneImpl(MAVClient, clock, handler, prefs);
        drone2 = new DroneImpl(MAVClient2, clock, handler, prefs2);
		getDrone().addDroneListener(this);
        getDrone2().addDroneListener(this);


		missionProxy = new MissionProxy(getDrone().getMission());
		mavLinkMsgHandler = new org.droidplanner.core.MAVLink.MavLinkMsgHandler(getDrone());
        mavLinkMsgHandler2 = new org.droidplanner.core.MAVLink.MavLinkMsgHandler(getDrone2());


		followMe = new Follow(getDrone(), handler, new FusedLocation(context));

		GAUtils.initGATracker(this);
		GAUtils.startNewSession(context);

		// Any time the application is started, do a quick scan to see if we
		// need any uploads
		//startService(UploaderService.createIntent(this));
	}

	@Override
	public void notifyReceivedData(MAVLinkMessage msg) {

		Log.d(FLUXO, "DroidPlannerApp  -  notifyReceivedData! - Recebeu mensagem!!!");

		/*VERIFICA QUAL DRONE AQUI
		* E CHAMA O MAVLINGMSGHANDLER ADEQUADO*/

		/*
		* 1) Obtem o sysid e comid
		* 2) Verifica qual drone da lista de mavLinkMsgHandler que corresponde ao da msg
		* */

		//Log.d(FLUXO, "HEARTBEAT: sys_id = " + msg.sysid + " comp_id: " + msg.compid);
        Log.d(FLUXO, "IP_ADD: " + msg.sysid  + " PORT: " + msg.compid + " TESTE" + drone.getHostPort());
        if(msg.compid == drone.getHostPort())
            mavLinkMsgHandler.receiveData(msg);
        /*
        if(msg!=null) {
            if (Integer.parseInt(drone.getMavClient().getUdpPortNumber()) == msg.sysid) {
                Log.d(SENDING, "recebendo ID => " + msg.msgid);
                mavLinkMsgHandler.receiveData(msg);
            } else {
                //Mostra o tipo de mensagem recebido pelo segundo Drone
                Log.d(MAVMSGDRONE2, "=> " + msg.msgid);
            }
        }
*/
       // access.release();
	}

	@Override
	public void notifyConnected(String udpPort) {
       // if(udpPort == drone.getMavClient().getUdpPortNumber()) {
            Log.d(FLUXO, "DroidPlannerApp  -  notifyConnected()!!");
            getDrone().notifyDroneEvent(DroneEventsType.CONNECTED);
       // }
	}

	@Override
	public void notifyDisconnected() {
		Log.d(FLUXO, "DroidPlannerApp  -  notifyDisconnected()!!");
		getDrone().notifyDroneEvent(DroneEventsType.DISCONNECTED);
	}

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
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

    public Drone getDrone2() {
        return drone2;
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
		MavLinkMsgHandler new_MavLingMsgHandler;
		DroidPlannerPrefs new_Prefs;

		/*
		* MAVLinkClient
		* Clock
		* Handler
		* Prefs
		* */
		MAVLinkClient MAVClient = new MAVLinkClient(this, this);
		Clock clock = new Clock() {
			@Override
			public long elapsedRealtime() {
				return SystemClock.elapsedRealtime();
			}
		};
		Handler handler = new Handler() {
			android.os.Handler handler = new android.os.Handler();

			@Override
			public void removeCallbacks(Runnable thread) {
				handler.removeCallbacks(thread);
			}

			@Override
			public void post(Runnable thread){
				Log.d(FLUXO, "DroidPlannerApp  -  post(Runable thread)!!!");
				handler.post(thread);
			}

			@Override
			public void postDelayed(Runnable thread, long timeout) {
				handler.postDelayed(thread, timeout);
			}
		};
		new_Prefs = new DroidPlannerPrefs(getApplicationContext());


		drone = new DroneImpl(MAVClient, clock, handler, new_Prefs);
		drone.addDroneListener(this);

		new_MavLingMsgHandler = new org.droidplanner.core.MAVLink.MavLinkMsgHandler(getDrone());

		return drone;
	}
}
