package org.droidplanner.core.MAVLink.connection;

import android.util.Log;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Parser;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

/**
 * Provides support for mavlink connection via udp.
 */
public abstract class UdpConnection extends MavLinkConnection {

	private DatagramSocket socket;
	private int serverPort;

	private int hostPort;

    List<Integer> hostPortList = new ArrayList<Integer>();


	private InetAddress hostAdd;

    private static final String UDP = "UDP";


	private void getUdpStream() throws IOException {
		socket = new DatagramSocket(serverPort);
		socket.setBroadcast(true);
		socket.setReuseAddress(true);
	}

	@Override
	public final void closeConnection() throws IOException {
		if (socket != null)
			socket.close();
	}

	@Override
	public final void openConnection() throws IOException {
		getUdpStream();
	}



    @Override
    public final void sendBuffer(byte[] buffer) throws IOException {

        //Só um canal de comunicação para todas as mensagens
        try {
            if (hostAdd != null) { // We can't send to our sister until they
                // have connected to us
                hostAdd = InetAddress.getByName("255.255.255.255"); //BROADCAST!!
                //Log.d(UDP, "<<Enviando UDP para porta>>:  " + hostPort);
                //Log.d(UDP, "<<Enviando UDP para endereço>>:  " + hostAdd);

                for (int port : hostPortList) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, hostAdd, port);
                    socket.send(packet);
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

	@Override
	public final int readDataBlock(byte[] readData) throws IOException {
		DatagramPacket packet = new DatagramPacket(readData, readData.length);
		socket.receive(packet);
		hostAdd = packet.getAddress();
		hostPort = packet.getPort();

        if (!hostPortList.contains(hostPort)) {
                hostPortList.add(hostPort);
        }

		return packet.getLength();
	}

	@Override
	public final void loadPreferences() {
		serverPort = loadServerPort();
	}

	@Override
	public final int getConnectionType() {
		return MavLinkConnectionTypes.MAVLINK_CONNECTION_UDP;
	}

	protected abstract int loadServerPort();

    public InetAddress getHostAdd()
    {
        return hostAdd;
    }
    public int getHostPort()
    {
        return hostPort;
    }

}
