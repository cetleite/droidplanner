package org.droidplanner.android;

import org.droidplanner.android.activities.MultipleActivity;
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

	private Drone dummyDrone;
    public Drone currentDrone;
	private Follow followMe;
	private MissionProxy missionProxy;
	private MavLinkMsgHandler mavLinkMsgHandler;
	private DroidPlannerPrefs prefs;
    public Context superUIContext;

    HashMap<Integer, Drone> droneList = new HashMap<Integer, Drone>();

    HashMap<Integer, MissionProxy> missionProxyList = new HashMap<Integer, MissionProxy>();

    private boolean connectedTower = false;
    MAVLinkClient MAVClient;


    public static Semaphore access = new Semaphore(1);


	private static final String FLUXO = "FLUXO";
    private static final String NOVOFLUXO = "NOVOFLUXO";
    private static final String LISTADRONES = "LISTADRONES";
    private static final String EDITORFRAG = "EDITORFRAG";
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
           // Log.d(NOVOFLUXO, "DroidPlannerApp  - post(Runable thread)!!!");
           // Log.d(FLUXO, "DroidPlannerApp  -  post(Runable thread)!!!");

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
		//dummyDrone = new DroneImpl(MAVClient, clock, handler, prefs, -1);
		//getDrone().addDroneListener(this);

        currentDrone = new DroneImpl(MAVClient, clock, handler, prefs, -1);
        currentDrone.setDroneConnected(false);
        dummyDrone = currentDrone;
        dummyDrone.setDroneConnected(false);

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
        //Log.d(LISTADRONES, Integer.toString(msg.msgid));

            if (!droneList.containsKey(msg.sysid)) {
                Log.d(EDITORFRAG, "DroidPlanner - CRIANDO NOVO DRONE");

                //Cria novo Drone
                Drone new_drone = createNewDrone(msg.sysid);

                //Verifica se é o primeiro Drone sendo adicionado
                if(droneList.isEmpty())
                {
                    //Caso for já é automaticamente o Drone selecionado
                    currentDrone = new_drone;
                    missionProxy. setNewMission(currentDrone.getMission());
                    new_drone.notifyDroneEvent(DroneEventsType.CONNECTED);
                }

                //Acrescenta novo drone na lista de drones
                droneList.put(msg.sysid, new_drone);

                //Acrescenta MissionProxy criado na lista de MissionProxy
                /****************************/
                MissionProxy newMissionProxy = new MissionProxy(new_drone.getMission());
                missionProxyList.put(new_drone.getDroneID(), newMissionProxy);
                /****************************/

                //Já existe drone ativo
                if(droneList.size() > 1) {
                    //Envia broadcast indicando que novo Drone foi criado
                    Intent intent = new Intent("NEW_DRONE");
                    intent.putExtra("droneID", msg.sysid);
                    sendBroadcast(intent);
                }
                //Primeiro drone ativo = Automaticamente selecionado
                else
                {
                    //Envia broadcast indicando que novo Drone foi criado
                    Intent intent2 = new Intent("NEW_DRONE_SELECTED");
                    intent2.putExtra("droneID", msg.sysid);
                    sendBroadcast(intent2);
                }

                //Trata mensagem recebida
                mavLinkMsgHandler.receiveData(msg, currentDrone, getApplicationContext());

            } else {
                //Seleciona o drone destinatário da mensagem
                Drone droneDestino = droneList.get(msg.sysid);
                //Trata mensagem de drone recebida
                mavLinkMsgHandler.receiveData(msg, droneDestino, getApplicationContext());

            }

        //access.release();

	}

	@Override
	public void notifyConnected() {
        this.connectedTower = true;
        Log.d(NOVOFLUXO, "DroidPlannerApp  -  notifyConnected!!!");
            Log.d(FLUXO, "DroidPlannerApp  -  notifyConnected()!!");
            //getDrone().notifyDroneEvent(DroneEventsType.CONNECTED);

        /*ATIVAR O BOTÃO DE DESCONECTAR A INTEFACE!!!*/

        if(superUIContext!=null) {
            sendBroadcast(new Intent().setAction("TOWER_CONNECTED"));
            //superUIContext.getApplicationContext().sendBroadcast(new Intent().setAction("TOWER_CONNECTED"));
        }
            else
            Log.d(NOVOFLUXO, "DroidPlannerApp  -  notifyConnected!!! -NULL!!!!!!!><><><><");
	}

	@Override
	public void notifyDisconnected() {
        Log.d(NOVOFLUXO, "DroidPlannerApp  -  notifyDisconnected!!!");
		Log.d(FLUXO, "DroidPlannerApp  -  notifyDisconnected()!!");

        currentDrone = dummyDrone;
        currentDrone.setDroneConnected(false);

        getDrone().notifyDroneEvent(DroneEventsType.DISCONNECTED);
        this.connectedTower = false;

        if(superUIContext!=null)
            superUIContext.getApplicationContext().sendBroadcast(new Intent().setAction("TOWER_DISCONNECTED"));
        else
            Log.d(NOVOFLUXO, "DroidPlannerApp  -  notifyConnected!!! -NULL!!!!!!!><><><><");

        /*APAGANDO DRONES DO HASH (VER DEPOIS SE AQUI SÓ ENTRA QUANDO PERDER A CONEXÃO COM A TORRE!*/
        droneList.clear();



    }

	@Override
	public void onDroneEvent(DroneEventsType event, Drone drone) {
        //Log.d(NOVOFLUXO, "DroidPlannerApp  -  onDroneEvent!!!");
        //Log.d(FLUXO, "DroidPlannerApp  -  onDroneEvent()!!==> " + drone.getMavClient().getUdpPortNumber());
		mNotificationHandler.onDroneEvent(event, drone);

		switch (event) {
		case MISSION_RECEIVED:
			// Refresh the mission render state
		//	missionProxy.refresh();

            getMissionProxyFromDroneID(drone.getDroneID()).refresh();

			break;
		default:
			break;
		}
	}

	public DroidPlannerPrefs getPreferences() {
		return prefs;
	}

	public Drone getDrone() {
		//return drone;
        return currentDrone;
	}

    public Follow getFollowMe() {
        return followMe;
    }

    public MissionProxy getMissionProxy() {
        return missionProxy;
    }

	public Drone createNewDrone(int droneID)
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


		drone = new DroneImpl(MAVClient, clock, handler, new_Prefs, -1);
		drone.setDroneID(droneID);
        drone.addDroneListener(this);


		return drone;
	}

    public boolean isTowerConnected()
    {
        return this.connectedTower;
    }

    public Drone getDrone(int droneId)
    {
        Drone drone;

        try {
            access.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        drone = droneList.get(droneId);
        access.release();


        return drone;
    }

    public int getTotalDrones()
    {
        return droneList.size();
    }

    public HashMap<Integer, Drone> getDroneList()
    {
        return droneList;
    }

    public void onNewDroneSelected(int newDroneId)
    {
        if(droneList.containsKey(newDroneId))
        {
            getDrone().removeDroneListener(this);
            currentDrone = droneList.get(newDroneId);
            getDrone().addDroneListener(this)
            ;
            missionProxy.setNewMission(currentDrone.getMission());

            Intent intent = new Intent("NEW_DRONE_SELECTED");
            intent.putExtra("droneID", newDroneId);
            sendBroadcast(intent);
        }
    }

    public MissionProxy getMissionProxyFromDroneID(int droneID)
    {
        if(missionProxyList.containsKey(droneID))
            return missionProxyList.get(droneID);
        else return null;
    }

}
