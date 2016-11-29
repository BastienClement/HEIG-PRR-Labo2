package labo2.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;


/**
 * A message used to add a service
 * <p>
 * This message is used to register the given service.
 */
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
		AdresseEncoder.serialize(output, address);
		output.writeInt(agentPort);
	}

	static ListAddMessage deserialize(DataInputStream input) throws IOException {
		return new ListAddMessage(
			input.readByte(),
			AdresseEncoder.unserialize(input),
			input.readInt()
		);
	}
}
