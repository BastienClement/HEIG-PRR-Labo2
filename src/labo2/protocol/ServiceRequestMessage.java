package labo2.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ServiceRequestMessage extends Message {
	public MessageType type() { return MessageType.SERVICE_REQUEST; }

	public final byte service;

	public ServiceRequestMessage(byte service) {
		this.service = service;
	}

	void serialize(DataOutputStream output) throws IOException {
		output.writeByte(service);
	}

	static ServiceRequestMessage deserialize(DataInputStream input) throws IOException {
		return new ServiceRequestMessage(input.readByte());
	}
}
