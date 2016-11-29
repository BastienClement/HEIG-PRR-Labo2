package labo2.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Shares serializers
 */
abstract class SharedEncoders {
	static void serializeSocketAddress(DataOutputStream output, InetSocketAddress address) throws IOException {
		output.write(address.getAddress().getAddress());
		output.writeInt(address.getPort());
	}

	static InetSocketAddress unserializeSocketAddress(DataInputStream input) throws IOException {
		byte[] address = new byte[4];
		input.read(address);
		int port = input.readInt();
		return new InetSocketAddress(InetAddress.getByAddress(address), port);
	}
}
