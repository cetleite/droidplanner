package org.droidplanner.core.drone;

import android.util.Log;

import org.droidplanner.core.MAVLink.MAVLinkStreams;
import org.droidplanner.core.MAVLink.WaypointManager;
import org.droidplanner.core.drone.profiles.Parameters;
import org.droidplanner.core.drone.profiles.VehicleProfile;
import org.droidplanner.core.drone.variables.Altitude;
import org.droidplanner.core.drone.variables.Battery;
import org.droidplanner.core.drone.variables.Calibration;
import org.droidplanner.core.drone.variables.Camera;
import org.droidplanner.core.drone.variables.GPS;
import org.droidplanner.core.drone.variables.GuidedPoint;
import org.droidplanner.core.drone.variables.HeartBeat;
import org.droidplanner.core.drone.variables.Home;
import org.droidplanner.core.drone.variables.Magnetometer;
import org.droidplanner.core.drone.variables.MissionStats;
import org.droidplanner.core.drone.variables.Navigation;
import org.droidplanner.core.drone.variables.Orientation;
import org.droidplanner.core.drone.variables.RC;
import org.droidplanner.core.drone.variables.Radio;
import org.droidplanner.core.drone.variables.Speed;
import org.droidplanner.core.drone.variables.State;
import org.droidplanner.core.drone.variables.StreamRates;
import org.droidplanner.core.drone.variables.Type;
import org.droidplanner.core.firmware.FirmwareType;
import org.droidplanner.core.mission.Mission;
import org.droidplanner.core.model.Drone;
import java.net.InetAddress;


import com.MAVLink.common.msg_heartbeat;

public class DroneImpl implements Drone {

	private final DroneEvents events;
	private final Type type;
	private VehicleProfile profile;
	private final org.droidplanner.core.drone.variables.GPS GPS;

	private final org.droidplanner.core.drone.variables.RC RC;
	private final Speed speed;
	private final Battery battery;
	private final Radio radio;
	private final Home home;
	private final Mission mission;
	private final MissionStats missionStats;
	private final StreamRates streamRates;
	private final Altitude altitude;
	private final Orientation orientation;
	private final Navigation navigation;
	private final GuidedPoint guidedPoint;
	private final Calibration calibrationSetup;
	private final WaypointManager waypointManager;
	private final Magnetometer mag;
	private final Camera footprints;
	private final State state;
	public final HeartBeat heartbeat;
	private final Parameters parameters;
    private boolean droneConnected;

    private int droneID;

    private static final String DRONEIMPL = "DRONEIMPL";

	private static MAVLinkStreams.MAVLinkOutputStream MavClient;
	private final Preferences preferences;

	public DroneImpl(MAVLinkStreams.MAVLinkOutputStream mavClient, DroneInterfaces.Clock clock,
			DroneInterfaces.Handler handler, Preferences pref, int droneID) {
        this.MavClient = mavClient;
		this.preferences = pref;


        events = new DroneEvents(this, handler);
		state = new State(this, clock, handler);
		heartbeat = new HeartBeat(this, handler);
		parameters = new Parameters(this, handler);

        RC = new RC(this);
        GPS = new GPS(this);
        this.type = new Type(this);
        this.speed = new Speed(this);
        this.battery = new Battery(this);
        this.radio = new Radio(this);
        this.home = new Home(this);
        this.mission = new Mission(this);
        this.missionStats = new MissionStats(this);
        this.streamRates = new StreamRates(this);
        this.altitude = new Altitude(this);
        this.orientation = new Orientation(this);
        this.navigation = new Navigation(this);
        this.guidedPoint =  new GuidedPoint(this);
        this.calibrationSetup = new Calibration(this);
        this.waypointManager = new WaypointManager(this);
        this.mag = new Magnetometer(this);
        this.footprints = new Camera(this);

        this.droneID = droneID;
        this.droneConnected = true;

        loadVehicleProfile();
	}

	@Override
	public void setAltitudeGroundAndAirSpeeds(double altitude, double groundSpeed, double airSpeed,
			double climb) {
		this.altitude.setAltitude(altitude);
		speed.setGroundAndAirSpeeds(groundSpeed, airSpeed, climb);
	    notifyDroneEvent(DroneInterfaces.DroneEventsType.SPEED);
	}

	@Override
	public void setDisttowpAndSpeedAltErrors(double disttowp, double alt_error, double aspd_error) {
		missionStats.setDistanceToWp(disttowp);
        altitude.setAltitudeError(alt_error);
        speed.setSpeedError(aspd_error);
		notifyDroneEvent(DroneInterfaces.DroneEventsType.ORIENTATION);
	}

	@Override
	public boolean isConnectionAlive() {
		return heartbeat.isConnectionAlive();
	}

	@Override
	public void addDroneListener(DroneInterfaces.OnDroneListener listener) {
		events.addDroneListener(listener);
	}

	@Override
	public void removeDroneListener(DroneInterfaces.OnDroneListener listener) {
        events.removeDroneListener(listener);
	}

	@Override
	public void notifyDroneEvent(final DroneInterfaces.DroneEventsType event) {
        //Só atualiza interface se drone for o selecionado para exibir na interface

        //Log.d(DRONEIMPL, "DroneImpl: " + MavClient.getCurrentDroneID());

        if(MavClient.getCurrentDroneID() == this.droneID || MavClient.getCurrentDroneID() == -1)
            events.notifyDroneEvent(event);
	}

	@Override
	public GPS getGps() {
		return GPS;
	}

	@Override
	public int getMavlinkVersion() {
		return heartbeat.getMavlinkVersion();
	}

	@Override
	public void onHeartbeat(msg_heartbeat msg) {
		heartbeat.onHeartbeat(msg);
	}

	@Override
	public State getState() {
		return state;
	}

	@Override
	public Parameters getParameters() {
		return parameters;
	}

	@Override
	public void setType(int type) {
		this.type.setType(type);
	}

	@Override
	public int getType() {
		return type.getType();
	}

	@Override
	public FirmwareType getFirmwareType() {
        return type.getFirmwareType();
	}

	@Override
	public void loadVehicleProfile() {
		profile = preferences.loadVehicleProfile(getFirmwareType());
	}

	@Override
	public VehicleProfile getVehicleProfile() {
		return profile;
	}

	@Override
	public MAVLinkStreams.MAVLinkOutputStream getMavClient() {
		return MavClient;
	}

	@Override
	public Preferences getPreferences() {
		return preferences;
	}

	@Override
	public WaypointManager getWaypointManager() {
		return waypointManager;
	}

	@Override
	public RC getRC() {
		return RC;
	}

	@Override
	public Speed getSpeed() {
		return speed;
	}

	@Override
	public Battery getBattery() {
		return battery;
	}

	@Override
	public Radio getRadio() {
		return radio;
	}

	@Override
	public Home getHome() {
		return home;
	}

	@Override
	public Mission getMission() {
		return mission;
	}

	@Override
	public MissionStats getMissionStats() {
		return missionStats;
	}

	@Override
	public StreamRates getStreamRates() {
		return streamRates;
	}

	@Override
	public Altitude getAltitude() {
		return altitude;
	}

	@Override
	public Orientation getOrientation() {
		return orientation;
	}

	@Override
	public Navigation getNavigation() {
		return navigation;
	}

	@Override
	public GuidedPoint getGuidedPoint() {
		return guidedPoint;
	}

	@Override
	public Calibration getCalibrationSetup() {
		return calibrationSetup;
	}

	@Override
	public String getFirmwareVersion() {
		return type.getFirmwareVersion();
	}

	@Override
	public void setFirmwareVersion(String message) {
		type.setFirmwareVersion(message);
	}

	@Override
	public Magnetometer getMagnetometer() {
		return mag;
	}
	
	public Camera getCamera() {
		return footprints;
	}

    public String getUdpPortNumber()
    {
        return getMavClient().getUdpPortNumber();
    }

    public InetAddress getHostAdd()
    {
        return this.getMavClient().getHostAdd();
    }

    public int getHostPort()
    {
        return this.getMavClient().getHostPort();
    }

    public int getDroneID()
    {
        return this.droneID;
    }

    public void setDroneConnected(boolean yesNo)
    {
        this.droneConnected = yesNo;
    }

    public boolean isDroneConnected()
    {
        return this.droneConnected;
    }

    public void setDroneID(int id)
    {
        this.droneID = id;
    }

}
