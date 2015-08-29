package org.droidplanner.core.MAVLink;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import java.net.InetAddress;

public class MAVLinkStreams {

	public interface MAVLinkOutputStream {

		void sendMavPacket(MAVLinkPacket pack);

		boolean isConnected();

		void toggleConnectionState();

		void queryConnectionState();

        public void setUdpPortNumber(String udpPort);

        public String getUdpPortNumber();

        public InetAddress getHostAdd();

        public int getHostPort();

        public int getCurrentDroneID();



	}

	public interface MavlinkInputStream {
		public void notifyConnected(String udpPort);

		public void notifyDisconnected();

		public void notifyReceivedData(MAVLinkMessage m);


    }

    public interface MavLinkTesteUdp{

        public void setUdpPortNumber(String udpPort);
    }
}
