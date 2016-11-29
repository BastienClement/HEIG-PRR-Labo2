package labo2.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;

public class ServiceOfferMessage extends Message {
	public MessageType type() { return MessageType.SERVICE_OFFER; }

	public final boolean available;
	public final InetSocketAddress address;

	public ServiceOfferMessage(boolean available, InetSocketAddress address) {
		this.available = available;
		this.address = address;
	}

	void serialize(DataOutputStream output) throws IOException {
		output.writeBoolean(available);
		if (available) {
			SharedEncoders.serializeSocketAddress(output, address);
		}
	}

	static ServiceOfferMessage deserialize(DataInputStream input) throws IOException {
		boolean available = input.readBoolean();
		if (available) {
			InetSocketAddress address = SharedEncoders.unserializeSocketAddress(input);
			return new ServiceOfferMessage(true, address);
		} else {
			return new ServiceOfferMessage(false, null);
		}
	}
}
