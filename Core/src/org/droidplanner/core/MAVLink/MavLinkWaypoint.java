package org.droidplanner.core.MAVLink;

import org.droidplanner.core.model.Drone;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.common.msg_mission_ack;
import com.MAVLink.common.msg_mission_count;
import com.MAVLink.common.msg_mission_request;
import com.MAVLink.common.msg_mission_request_list;
import com.MAVLink.common.msg_mission_set_current;
import com.MAVLink.enums.MAV_MISSION_RESULT;

public class MavLinkWaypoint {

	public static void sendAck(Drone drone) {
		msg_mission_ack msg = new msg_mission_ack();
		msg.target_system = (byte)drone.getDroneID();
		msg.target_component = 0;
		msg.type = MAV_MISSION_RESULT.MAV_MISSION_ACCEPTED;

        MAVLinkPacket packet = msg.pack();
        packet.setTargetSystem((byte) drone.getDroneID());
        drone.getMavClient().sendMavPacket(packet);
		//drone.getMavClient().sendMavPacket(msg.pack());

	}

	public static void requestWayPoint(Drone drone, int index) {
		msg_mission_request msg = new msg_mission_request();
		msg.target_system = (byte)drone.getDroneID();
		msg.target_component = 0;
		msg.seq = (short) index;

        MAVLinkPacket packet = msg.pack();
        packet.setTargetSystem((byte) drone.getDroneID());
        drone.getMavClient().sendMavPacket(packet);
		//drone.getMavClient().sendMavPacket(msg.pack());
	}

	public static void requestWaypointsList(Drone drone) {
		msg_mission_request_list msg = new msg_mission_request_list();
		msg.target_system = (byte)drone.getDroneID();
		msg.target_component = 0;

        MAVLinkPacket packet = msg.pack();
        packet.setTargetSystem((byte) drone.getDroneID());
        drone.getMavClient().sendMavPacket(packet);
		//drone.getMavClient().sendMavPacket(msg.pack());
	}

	public static void sendWaypointCount(Drone drone, int count) {
		msg_mission_count msg = new msg_mission_count();
		msg.target_system = (byte)drone.getDroneID();
		msg.target_component = 0;
		msg.count = (short) count;

        MAVLinkPacket packet = msg.pack();
        packet.setTargetSystem((byte) drone.getDroneID());
        drone.getMavClient().sendMavPacket(packet);
		//drone.getMavClient().sendMavPacket(msg.pack());
	}

	public static void sendSetCurrentWaypoint(Drone drone, short i) {
		msg_mission_set_current msg = new msg_mission_set_current();
		msg.target_system = (byte)drone.getDroneID();
		msg.target_component = 0;
		msg.seq = i;

        MAVLinkPacket packet = msg.pack();
        packet.setTargetSystem((byte) drone.getDroneID());
        drone.getMavClient().sendMavPacket(packet);
		//drone.getMavClient().sendMavPacket(msg.pack());
	}

}
