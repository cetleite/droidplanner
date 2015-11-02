package com.MAVLink.common;

import com.MAVLink.MAVLinkPacket;
import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;

/**
 * Created by carloseduardo on 02/11/15.
 */
public class msg_recognition_pattern extends MAVLinkMessage {

    public static final int MAVLINK_MSG_LENGTH = 6;
    public static final int MAVLINK_MSG_ID_RECOGNITION_PATTERN = 200;

    /**
     * System ID
     */
    public byte target_system;
    /**
     * Component ID
     */
    public byte target_component;
    /**
     * Recognition Pattern
     */
    public int recognition_pattern;

    @Override
    public MAVLinkPacket pack() {
        MAVLinkPacket packet = new MAVLinkPacket();
        packet.len = MAVLINK_MSG_LENGTH;
        packet.sysid = 255;
        packet.compid = 190;
        packet.msgid = MAVLINK_MSG_ID_RECOGNITION_PATTERN;

        packet.payload.putByte(target_system);
        packet.payload.putByte(target_component);
        packet.payload.putInt(recognition_pattern);

        return packet;
    }

    @Override
    public void unpack(MAVLinkPayload payload) {
        payload.resetIndex();
        this.target_system = payload.getByte();
        this.target_component = payload.getByte();
        this.recognition_pattern = payload.getInt();
    }


    /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_recognition_pattern(){
        msgid = MAVLINK_MSG_ID_RECOGNITION_PATTERN;
    }


    /**
     * Constructor for a new message, initializes the message with the payload
     * from a mavlink packet
     *
     */
    public msg_recognition_pattern(MAVLinkPacket mavLinkPacket){
        this.sysid = mavLinkPacket.sysid;
        this.compid = mavLinkPacket.compid;
        this.msgid = MAVLINK_MSG_ID_RECOGNITION_PATTERN;
        unpack(mavLinkPacket.payload);
        //Log.d("MAVLink", "PARAM_REQUEST_LIST");
        //Log.d("MAVLINK_MSG_ID_PARAM_REQUEST_LIST", toString());
    }


    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
        return "MAVLINK_MSG_ID_RECOGNITION_PATTERN -"+" target_system:"+target_system+" target_component:"+target_component+" recognition_pattern:"+recognition_pattern+"";
    }
}

