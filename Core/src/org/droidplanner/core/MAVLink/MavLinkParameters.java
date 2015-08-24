package org.droidplanner.core.MAVLink;

import android.util.Log;

import org.droidplanner.core.model.Drone;
import org.droidplanner.core.parameters.Parameter;

import com.MAVLink.common.msg_param_request_list;
import com.MAVLink.common.msg_param_request_read;
import com.MAVLink.common.msg_param_set;

public class MavLinkParameters {
	private static final String PARAM = "PARAM";


	public static void requestParametersList(Drone drone) {
		Log.d(PARAM, "MavLinkParameters  -  requestParametersList!!!");

		msg_param_request_list msg = new msg_param_request_list();
		msg.target_system = 1;
		msg.target_component = 1;
		drone.getMavClient().sendMavPacket(msg.pack());
	}

	public static void sendParameter(Drone drone, Parameter parameter) {

		Log.d(PARAM, "MavLinkParameters  -  sendParameter!!!");
		msg_param_set msg = new msg_param_set();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.setParam_Id(parameter.name);
		msg.param_type = (byte) parameter.type;
		msg.param_value = (float) parameter.value;
		drone.getMavClient().sendMavPacket(msg.pack());
	}

	public static void readParameter(Drone drone, String name) {

		Log.d(PARAM, "MavLinkParameters  -  readParameter!!!");
		msg_param_request_read msg = new msg_param_request_read();
		msg.param_index = -1;
		msg.target_system = 1;
		msg.target_component = 1;
		msg.setParam_Id(name);
		drone.getMavClient().sendMavPacket(msg.pack());
	}

	public static void readParameter(Drone drone, int index) {

		Log.d(PARAM, "MavLinkParameters  - readParameter!!!");
		msg_param_request_read msg = new msg_param_request_read();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.param_index = (short) index;
		drone.getMavClient().sendMavPacket(msg.pack());
	}
}
