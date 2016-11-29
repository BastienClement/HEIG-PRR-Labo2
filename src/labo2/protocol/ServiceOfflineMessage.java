package labo2.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;

public class ServiceOfflineMessage extends Message {
	public MessageType type() { return MessageType.SERVICE_OFFLINE; }

	public final byte service;
	public final InetSocketAddress address;

	public ServiceOfflineMessage(byte service, InetSocketAddress address) {
		this.service = service;
		this.address = address;
	}

	void serialize(DataOutputStream output) throws IOException {
		output.writeByte(service);
		AdresseEncoder.serialize(output, address);
	}

	static ServiceOfflineMessage deserialize(DataInputStream input) throws IOException {
		return new ServiceOfflineMessage(
			input.readByte(),
			AdresseEncoder.unserialize(input)
		);
	}
}
