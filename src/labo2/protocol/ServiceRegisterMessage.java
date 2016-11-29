package labo2.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ServiceRegisterMessage extends Message {
	public MessageType type() { return MessageType.SERVICE_REGISTER; }

	public final byte service;
	public final int agentPort;

	public ServiceRegisterMessage(byte service, int agentPort) {
		this.service = service;
		this.agentPort = agentPort;
	}

	void serialize(DataOutputStream output) throws IOException {
		output.writeByte(service);
		output.writeInt(agentPort);
	}

	static ServiceRegisterMessage deserialize(DataInputStream input) throws IOException {
		return new ServiceRegisterMessage(input.readByte(), input.readInt());
	}
}
