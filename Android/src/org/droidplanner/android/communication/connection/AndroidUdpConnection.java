package org.droidplanner.android.communication.connection;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import org.droidplanner.core.MAVLink.connection.UdpConnection;
import org.droidplanner.core.model.Logger;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class AndroidUdpConnection extends AndroidMavLinkConnection {

	private final UdpConnection mConnectionImpl;

    private String port_number = "14550";

    private static final String MAVSERVICE = "MAVSERVICE";
    private static final String UDP = "UDP";

	public AndroidUdpConnection(Context context) {
		super(context);

		mConnectionImpl = new UdpConnection() {
			@Override
			protected int loadServerPort() {
                Log.d(MAVSERVICE, "AndroidUdpConnection - PORTA:  " + port_number);
                //Log.d(MAVSERVICE, "AndroidUdpConnection - O que retorna:  " + Integer.parseInt(prefs.prefs.getString("pref_udp_server_port", port_number)));
				//return Integer.parseInt(prefs.prefs.getString("pref_udp_server_port", port_number));
                return Integer.parseInt(port_number);
			}

			@Override
			protected Logger initLogger() {
				return AndroidUdpConnection.this.initLogger();
			}

			@Override
			protected File getTempTLogFile() {
				return AndroidUdpConnection.this.getTempTLogFile();
			}

			@Override
			protected void commitTempTLogFile(File tlogFile) {
				AndroidUdpConnection.this.commitTempTLogFile(tlogFile);
			}
		};
	}



	@Override
	protected void closeAndroidConnection() throws IOException {
		mConnectionImpl.closeConnection();
	}

	@Override
	protected void loadPreferences(SharedPreferences prefs) {
		mConnectionImpl.loadPreferences();
	}

	@Override
	protected void openAndroidConnection() throws IOException {
		mConnectionImpl.openConnection();
	}

	@Override
	protected int readDataBlock(byte[] buffer) throws IOException {
		return mConnectionImpl.readDataBlock(buffer);
	}

	@Override
	protected void sendBuffer(byte[] buffer) throws IOException {

        //Log.d(UDP, "AndroidUdpConnection - sendBuffer");
		mConnectionImpl.sendBuffer(buffer);
	}



	@Override
	public int getConnectionType() {
		return mConnectionImpl.getConnectionType();
	}

    public void setPortNumber(String port)
    {
        Log.d(MAVSERVICE, "AndroidUdpConnection - PORTA SETTED:  " + port);
        this.port_number = port;
    }

    public InetAddress getHostAdd()
    {
        return mConnectionImpl.getHostAdd();
    }
    public int getHostPort()
    {
        return mConnectionImpl.getHostPort();
    }
}
