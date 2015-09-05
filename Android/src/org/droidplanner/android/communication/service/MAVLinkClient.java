package org.droidplanner.android.communication.service;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicReference;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.core.MAVLink.MAVLinkStreams;
import org.droidplanner.core.MAVLink.connection.MavLinkConnection;
import org.droidplanner.core.MAVLink.connection.MavLinkConnectionListener;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;

/**
 * Provide a common class for some ease of use functionality
  */
public class MAVLinkClient implements MAVLinkStreams.MAVLinkOutputStream {

	private static final String TAG = MAVLinkClient.class.getSimpleName();

    /**
     * Used to post updates to the main thread.
     */
    private final Handler mHandler = new Handler();

    private final AtomicReference<String> mErrMsgRef = new AtomicReference<String>();

    private  String udpPort;

    private static final String FLUXO = "FLUXO";
    private static final String PARAM = "PARAM";
    private static final String MAVSERVICE = "MAVSERVICE";
    private static final String IDENV = "IDENV";
    private static final String NOVOFLUXO = "NOVOFLUXO";

    public IBinder binder;

    public static int flag=0;

    public static DroidPlannerApp app;

    private final MavLinkConnectionListener mConnectionListener = new MavLinkConnectionListener() {
        private final Runnable mConnectedNotification = new Runnable() {
            @Override
            public void run() {
                listener.notifyConnected(udpPort);
            }
        };

        private final Runnable mDisconnectedNotification = new Runnable() {
            @Override
            public void run() {
                listener.notifyDisconnected();
                closeConnection();
            }
        };

        private final Runnable mErrorNotification = new Runnable() {

            private Toast mErrToast;

            @Override
            public void run() {
                mHandler.removeCallbacks(this);

                final String errMsg = mErrMsgRef.get();
                if(errMsg != null) {
                    final String toastMsg = mMavLinkErrorPrefix + " " + errMsg;
                    if(mErrToast == null){
                        mErrToast = Toast.makeText(parent, toastMsg, Toast.LENGTH_LONG);
                    }
                    else{
                        mErrToast.setText(toastMsg);
                    }
                    mErrToast.show();
                }
            }
        };

        @Override
        public void onConnect() {
            Log.d(NOVOFLUXO, "MAVLinkClient  -  >>>>>>>onConnected()<<<<<<<<");
            mHandler.post(mConnectedNotification);
        }

        @Override
        public void onReceiveMessage(final MAVLinkMessage msg) {

            //Log.d(FLUXO, "MAVLinkClient  -  onReceivedMessage()!!!!");

  /*          try {
                app.access.acquire();
            } catch (InterruptedException e) {

            }*/

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.notifyReceivedData(msg);
                }
            });
        }

        @Override
        public void onDisconnect() {
            mHandler.post(mDisconnectedNotification);
        }

        @Override
        public void onComError(final String errMsg) {
            mErrMsgRef.set(errMsg);
            mHandler.post(mErrorNotification);
        }
    };

    /**
     *  Defines callbacks for service binding, passed to bindService()
     *  */
    private  ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(MAVSERVICE, "MAVLinkCliente  -  ONSERVICECONNECTED!!! = porta: ()" + udpPort);
            mService = (MAVLinkService.MavLinkServiceApi)service;
            Log.d(NOVOFLUXO, "MAVLinkClient  -  onServiceConnected()!!!!");
            mService.setUdpPortNumber(udpPort);

            onConnectedService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            onDisconnectService();
        }
    };

	private final Context parent;
	private final MAVLinkStreams.MavlinkInputStream listener;
    private final String mMavLinkErrorPrefix;

    private MAVLinkService.MavLinkServiceApi mService;
	private boolean mIsBound;

	public MAVLinkClient(Context context, MAVLinkStreams.MavlinkInputStream listener, DroidPlannerApp app) {
		parent = context;
		this.listener = listener;
        mMavLinkErrorPrefix = context.getString(R.string.MAVLinkError);
        this.app = app;
	}

	private void openConnection() {
        Log.d(MAVSERVICE, "MAVLinkCliente  -  openConnection()");
        if(mIsBound) {
            Log.d(MAVSERVICE, "MAVLinkCliente  -  openConnection() - misBound");
            connectMavLink();
        }
        else{
            Log.d(MAVSERVICE, "MAVLinkCliente  -  openConnection() - NÃO misBound");
            parent.bindService(new Intent(parent, MAVLinkService.class), mConnection,
                    Context.BIND_AUTO_CREATE);
        }
	}

	private void closeConnection() {
		if (mIsBound) {
            if(mService.getConnectionStatus() == MavLinkConnection.MAVLINK_CONNECTED){
                Toast.makeText(parent, R.string.status_disconnecting, Toast.LENGTH_SHORT).show();
                mService.disconnectMavLink();
            }

            mService.removeMavLinkConnectionListener(TAG);

            // Unbinding the service.
            parent.unbindService(mConnection);
            onDisconnectService();
		}
	}

	@Override
	public void sendMavPacket(MAVLinkPacket pack) {
		if (!isConnected()) {
            Log.d(PARAM, "MAVLinkClient  -  sendMavPacket!!! - NAO ENV");
			return;
		}

        //Log.d(SENDING, " 1) MAVLinkClient  -  sendMavPacket()");
        Log.d(IDENV, " MAVLinkClient - ENVIANDO p/: " + mService.getPortNumber());
        mService.sendData(pack);

	}

    private void connectMavLink(){
        Log.d(NOVOFLUXO, "MAVLinkClient  -  connectMavLink()!!!!");
        Log.d(MAVSERVICE, "MAVLinkCliente  -  connectMavLink()");
        Toast.makeText(parent, R.string.status_connecting, Toast.LENGTH_SHORT).show();
        mService.connectMavLink();
        mService.addMavLinkConnectionListener(TAG, mConnectionListener);
    }

	private void onConnectedService() {
        Log.d(NOVOFLUXO, "MAVLinkClient  -  onConnectedService()!!!!");
        mIsBound = true;
        connectMavLink();
	}

	private void onDisconnectService() {
		mIsBound = false;
		listener.notifyDisconnected();
	}

	@Override
	public void queryConnectionState() {
		if (isConnected()) {
			listener.notifyConnected(this.udpPort);
		} else {
			listener.notifyDisconnected();
		}
	}

	@Override
	public boolean isConnected() {
          return mIsBound && mService.getConnectionStatus() == MavLinkConnection.MAVLINK_CONNECTED;

	}

	@Override
	public void toggleConnectionState() {
        Log.d(MAVSERVICE, "MAVLinkCliente  -  toggleConnectionState porta: " + this.udpPort);
		if (isConnected()) {
            Log.d(MAVSERVICE, "MAVLinkCliente  -  toggleConnectionState IS_CONNECTED");
			closeConnection();
		} else {
			openConnection();
		}
	}

    public void setUdpPortNumber(String newUdpPort)
    {
        this.udpPort = newUdpPort;
    }

    public String getUdpPortNumber(){return this.udpPort;}

    public InetAddress getHostAdd()
    {
        return mService.getHostAdd();
    }
    public int getHostPort()
    {
        return mService.getHostPort();
    }

    public int getCurrentDroneID()
    {
        if(app!=null)
            return app.currentDrone.getDroneID();
        else return -1;
    }

}
