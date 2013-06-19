package com.bsb.hike.models;

public class HikePacket {
	private byte[] message;
	private long msgId;
	private long timeStamp;
	private long packetId = -1;

	public long getPacketId() {
		return packetId;
	}

	public void setPacketId(long packetId) {
		this.packetId = packetId;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public byte[] getMessage() {
		return message;
	}

	public long getMsgId() {
		return msgId;
	}

	public HikePacket(byte[] message, long msgId, long timeStamp) {
		this(message, msgId, timeStamp, -1);
	}

	public HikePacket(byte[] message, long msgId, long timeStamp, long packetId) {
		this.message = message;
		this.msgId = msgId;
		this.timeStamp = timeStamp;
		this.packetId = packetId;
	}
}
