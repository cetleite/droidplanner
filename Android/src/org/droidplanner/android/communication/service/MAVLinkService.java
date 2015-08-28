package org.droidplanner.android.communication.service;

import java.lang.ref.WeakReference;

import org.droidplanner.android.communication.connection.AndroidMavLinkConnection;
import org.droidplanner.android.communication.connection.AndroidUdpConnection;
import org.droidplanner.android.utils.Utils;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.core.MAVLink.connection.MavLinkConnection;
import org.droidplanner.core.MAVLink.connection.MavLinkConnectionListener;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.MAVLink.MAVLinkPacket;
import com.google.android.gms.analytics.HitBuilders;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Connects to the drone through a mavlink connection, and takes care of sending
 * and/or receiving messages to/from the drone.
 * 
 */
public class MAVLinkService  extends Service {

	private static final String LOG_TAG = MAVLinkService.class.getSimpleName();

	private final MavLinkServiceApi mServiceApi = new MavLinkServiceApi(this);

	private DroidPlannerPrefs mAppPrefs;
	private AndroidMavLinkConnection mavConnection;
    private String connType;
    private String udpPort;


    private static final String MAVSERVICE = "MAVSERVICE";
    private static final String MAVSERVICE2 = "MAVSERVICE2";
    private static final String SENDING = "SENDING";
    private static final String NOVOFLUXO = "NOVOFLUXO";

    public AndroidUdpConnection udpConn;

    public MAVLinkService()
    {

    }


	@Override
	public IBinder onBind(Intent intent) {
		return mServiceApi;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// Initialise the app preferences handle.
		mAppPrefs = new DroidPlannerPrefs(getApplicationContext());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		disconnectMAVConnection();
	}

	/**
	 * Toggle the current state of the MAVlink connection. Starting and closing
	 * the as needed. May throw a onConnect or onDisconnect callback
	 */
	private void connectMAVConnection() {
		String connectionType = mAppPrefs.getMavLinkConnectionType();
        Log.d(NOVOFLUXO, "MAVLinkService  -  connectMavLink()!!!!");
        switch(connectionType)
        {
            case "UDP":
                Log.d(MAVSERVICE, "case " + connectionType + ": ");

                udpConn = new AndroidUdpConnection(this);
                udpConn.setPortNumber(mAppPrefs.getUdpPortNumber());
                //udpConn.setPortNumber(MavLinkServiceApi.getPortNumber());

                if (mavConnection == null
                        || mavConnection.getConnectionType() != udpConn.getConnectionType()) {
                    Log.d(MAVSERVICE, "MAVLinkService  -  connectMAVConnection() - mavConnection == null");
                    mavConnection = udpConn;
                    Log.d(MAVSERVICE, "MAVLinkService mavConnection.getConnectionType(): " + mavConnection.getConnectionType());

                    if (mavConnection.getConnectionStatus() == MavLinkConnection.MAVLINK_DISCONNECTED) {
                        //mavConnection.connect(MavLinkServiceApi.getPortNumber());
                        mavConnection.connect(mAppPrefs.getUdpPortNumber());
                    }
                }
                else
                {
                    Log.d(MAVSERVICE, "MAVLinkService TESTE! mavConnection.getConnectionType(): " + mavConnection.getConnectionType());
                    AndroidMavLinkConnection teste;
                    mavConnection = udpConn;
                    //mavConnection.connect(MavLinkServiceApi.getPortNumber());
                    mavConnection.connect(mAppPrefs.getUdpPortNumber());
                }



                break;
            default:

                Log.d(MAVSERVICE, "MAVLinkService  -  connectMAVConnection() - DEFAULT");
                Log.d(MAVSERVICE, "==> " + connectionType);

                Utils.ConnectionType connType = Utils.ConnectionType.valueOf(connectionType);

                if (mavConnection == null
                        || mavConnection.getConnectionType() != connType.getConnectionType()) {
                    mavConnection = connType.getConnection(this);
                }

                if (mavConnection.getConnectionStatus() == MavLinkConnection.MAVLINK_DISCONNECTED) {
                    mavConnection.connect("");
                }

                // Record which connection type is used.
                GAUtils.sendEvent(new HitBuilders.EventBuilder()
                        .setCategory(GAUtils.Category.MAVLINK_CONNECTION).setAction("MavLink connect")
                        .setLabel(connectionType + " (" + mavConnection.toString() + ") "));
                break;
        }

	}

	private void disconnectMAVConnection() {
		Log.d(LOG_TAG, "Pre disconnect");

		if (mavConnection != null
				&& mavConnection.getConnectionStatus() != MavLinkConnection.MAVLINK_DISCONNECTED) {
			mavConnection.disconnect();
		}

		GAUtils.sendEvent(new HitBuilders.EventBuilder().setCategory(
                GAUtils.Category.MAVLINK_CONNECTION).setAction("MavLink disconnect"));
	}


    public InetAddress getHostAdd()
    {
        return udpConn.getHostAdd();
    }
    public int getHostPort()
    {
        return udpConn.getHostPort();
    }

	/**
	 * MavLinkService app api.
	 */
	public class MavLinkServiceApi extends Binder {

		private final WeakReference<MAVLinkService> mServiceRef;


        public String port;

		MavLinkServiceApi(MAVLinkService service) {
			mServiceRef = new WeakReference<MAVLinkService>(service);
		}

		public void sendData(MAVLinkPacket packet) {
			final MAVLinkService service = mServiceRef.get();
			if (service == null) {
				return;
			}

			if (service.mavConnection != null
					&& service.mavConnection.getConnectionStatus() != MavLinkConnection.MAVLINK_DISCONNECTED) {
                Log.d(MAVSERVICE, "2) MAVLinkService  -  sendData()");
				service.mavConnection.sendMavPacket(packet);
			}
		}

		public int getConnectionStatus() {
			final MAVLinkService service = mServiceRef.get();
			if (service == null) {
				return MavLinkConnection.MAVLINK_DISCONNECTED;
			}

			return service.mavConnection.getConnectionStatus();
		}

		public void connectMavLink() {
            Log.d(NOVOFLUXO, "MAVLinkService  -  connectMavLink()!!!!");
            Log.d(MAVSERVICE, "MAVLinkService  -  connectMavLink()");
			final MAVLinkService service = mServiceRef.get();
			if (service == null) {
				return;
			}

			service.connectMAVConnection();
		}

		public void disconnectMavLink() {
			final MAVLinkService service = mServiceRef.get();
			if (service == null) {
				return;
			}

			service.disconnectMAVConnection();
		}

		public void addMavLinkConnectionListener(String tag, MavLinkConnectionListener listener) {
			final MAVLinkService service = mServiceRef.get();
			if (service == null || service.mavConnection == null) {
				return;
			}

			service.mavConnection.addMavLinkConnectionListener(tag, listener);
		}

		public void removeMavLinkConnectionListener(String tag) {
			final MAVLinkService service = mServiceRef.get();
			if (service == null || service.mavConnection == null) {
				return;
			}

			service.mavConnection.removeMavLinkConnectionListener(tag);
		}

        public  void setUdpPortNumber(String new_port)
        {
            //port = new_port;
        }

        public  String getPortNumber()
        {
            //return port;
            return "";
        }

        public InetAddress getHostAdd()
        {
            return udpConn.getHostAdd();
        }
        public int getHostPort()
        {
            return udpConn.getHostPort();
        }

	}



}
