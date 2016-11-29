package labo2.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * A message used to remove a service
 * <p>
 * This message is used to unregister the given service.
 */
public class ListRemoveMessage extends Message {
	public MessageType type() { return MessageType.LIST_REMOVE; }

	public final byte service;
	public final InetSocketAddress address;

	public ListRemoveMessage(byte service, InetSocketAddress address) {
		this.service = service;
		this.address = address;
	}

	void serialize(DataOutputStream output) throws IOException {
		output.writeByte(service);
		AdresseEncoder.serialize(output, address);
	}

	static ListRemoveMessage deserialize(DataInputStream input) throws IOException {
		return new ListRemoveMessage(
			input.readByte(),
			AdresseEncoder.unserialize(input)
		);
	}
}
