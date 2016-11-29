package labo2.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
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
			output.write(address.getAddress().getAddress());
			output.writeInt(address.getPort());
		}
	}

	static ServiceOfferMessage deserialize(DataInputStream input) throws IOException {
		boolean available = input.readBoolean();
		if (available) {
			byte[] address = new byte[4];
			input.read(address);
			int port = input.readInt();
			return new ServiceOfferMessage(true, new InetSocketAddress(InetAddress.getByAddress(address), port));
		} else {
			return new ServiceOfferMessage(false, null);
		}
	}
}
