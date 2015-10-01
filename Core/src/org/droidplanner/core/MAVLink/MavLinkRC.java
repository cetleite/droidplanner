package org.droidplanner.core.MAVLink;

import org.droidplanner.core.model.Drone;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.common.msg_rc_channels_override;

public class MavLinkRC {
	public static void sendRcOverrideMsg(Drone drone, int[] rcOutputs) {
		msg_rc_channels_override msg = new msg_rc_channels_override();
		msg.chan1_raw = (short) rcOutputs[0];
		msg.chan2_raw = (short) rcOutputs[1];
		msg.chan3_raw = (short) rcOutputs[2];
		msg.chan4_raw = (short) rcOutputs[3];
		msg.chan5_raw = (short) rcOutputs[4];
		msg.chan6_raw = (short) rcOutputs[5];
		msg.chan7_raw = (short) rcOutputs[6];
		msg.chan8_raw = (short) rcOutputs[7];
		msg.target_system = (byte)drone.getDroneID();
		msg.target_component = 1;

        MAVLinkPacket packet = msg.pack();
        packet.setTargetSystem((byte) drone.getDroneID());
        drone.getMavClient().sendMavPacket(packet);
		//drone.getMavClient().sendMavPacket(msg.pack());
	}
}
