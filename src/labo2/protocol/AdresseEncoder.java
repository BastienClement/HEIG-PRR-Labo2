package labo2.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Shares serializers
 */
abstract class AdresseEncoder {
	static void serialize(DataOutputStream output, InetSocketAddress address) throws IOException {
		output.write(address.getAddress().getAddress());
		output.writeInt(address.getPort());
	}

	static InetSocketAddress unserialize(DataInputStream input) throws IOException {
		byte[] address = new byte[4];
		input.read(address);
		int port = input.readInt();
		return new InetSocketAddress(InetAddress.getByAddress(address), port);
	}
}
