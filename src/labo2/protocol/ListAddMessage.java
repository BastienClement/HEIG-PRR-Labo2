package labo2.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;

public class ListAddMessage extends Message {
	public MessageType type() { return MessageType.LIST_ADD; }

	public final byte service;
	public final InetSocketAddress address;
	public final int agentPort;

	public ListAddMessage(byte service, InetSocketAddress address, int agentPort) {
		this.service = service;
		this.address = address;
		this.agentPort = agentPort;
	}

	void serialize(DataOutputStream output) throws IOException {
		output.writeByte(service);
		SharedEncoders.serializeSocketAddress(output, address);
		output.writeInt(agentPort);
	}

	static ListAddMessage deserialize(DataInputStream input) throws IOException {
		return new ListAddMessage(
			input.readByte(),
			SharedEncoders.unserializeSocketAddress(input),
			input.readInt()
		);
	}
}