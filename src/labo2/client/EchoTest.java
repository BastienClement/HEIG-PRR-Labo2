package labo2.client;

import labo2.services.Echo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Arrays;

/**
 * Tests for the Echo service.
 *
 * Sends an UTF-8 encoded string to the service socket and verify
 * that the received echo does match the one sent.
 */
public class EchoTest extends TestClient {
	protected String name() { return "echo-test"; }
	protected String desc() { return "Testing Echo service"; }

	protected void execute() throws IOException {
		// Resolution
		InetSocketAddress address = client.resolve(Echo.SERVICE_ID);
		log.printf("address = %s\n", address);

		try (DatagramSocket socket = new DatagramSocket(null)) {
			// Prepare payload
			String payload = "Hello World!";
			byte[] data = payload.getBytes("utf-8");

			// Sending
			DatagramPacket packet = new DatagramPacket(data, data.length);
			packet.setAddress(address.getAddress());
			packet.setPort(address.getPort());
			socket.send(packet);

			// Wipe the packet buffer to ensure that received bytes
			// are actually received and not just a leftover.
			Arrays.fill(data, (byte) 0);
			packet.setData(data);

			// Receiving
			socket.receive(packet);
			String received = new String(data, "utf-8");

			// Checks
			log.printf("sent = %s ; received = %s\n", payload, received);
			require(payload.equals(received));
		}
	}
}
