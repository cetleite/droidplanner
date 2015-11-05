package org.droidplanner.core.drone;

import android.util.Log;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.drone.DroneInterfaces.OnDroneListener;
import org.droidplanner.core.model.Drone;

public class DroneEvents extends DroneVariable {

	private final ConcurrentLinkedQueue<DroneEventsType> eventsQueue = new ConcurrentLinkedQueue<DroneEventsType>();

	private final DroneInterfaces.Handler handler;

    private static final String DRONEEVENTS = "DRONEEVENTS";

	private final Runnable eventsDispatcher = new Runnable() {
		@Override
		public void run() {
            do {
                handler.removeCallbacks(this);
                final DroneEventsType event = eventsQueue.poll();
                if (event != null && !droneListeners.isEmpty()) {
                    for (OnDroneListener listener : droneListeners) {
                        listener.onDroneEvent(event, myDrone);
                    }
                }
            }while(!eventsQueue.isEmpty());
		}
	};

	public DroneEvents(Drone myDrone, DroneInterfaces.Handler handler) {
		super(myDrone);
		this.handler = handler;
	}

	/*public DroneEvents(DroneInterfaces.Handler handler) {
		this.handler = handler;
	}*/


	private final ConcurrentLinkedQueue<OnDroneListener> droneListeners = new ConcurrentLinkedQueue<OnDroneListener>();

	public void addDroneListener(OnDroneListener listener) {
		if (listener != null & !droneListeners.contains(listener))
			droneListeners.add(listener);
	}

	public void removeDroneListener(OnDroneListener listener) {
		if (listener != null && droneListeners.contains(listener))
			droneListeners.remove(listener);
	}

	public void notifyDroneEvent(DroneEventsType event) {
        //Log.d(DRONEEVENTS, "DRONEEVENTS  -  notifyDroneEvents");
        eventsQueue.offer(event);
		handler.post(eventsDispatcher);
	}

	/*RECEBE O DRONE A SER MODIFICADO NA MAVLINKMSGHANDLER*/
	/*public void setMyDrone(Drone drone)
	{
		super.myDrone = drone;
	}*/
}
