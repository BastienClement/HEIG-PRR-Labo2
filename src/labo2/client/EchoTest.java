package labo2.client;

import labo2.services.Echo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Arrays;

public class EchoTest extends TestClient {
	protected String name() { return "echo-test"; }
	protected String desc() { return "Testing Echo service"; }

	protected void execute() throws IOException {
		InetSocketAddress address = client.resolve(Echo.SERVICE_ID);
		log.printf("address = %s\n", address);

		try (DatagramSocket socket = new DatagramSocket(null)) {
			String payload = "Hello World!";
			byte[] data = payload.getBytes("utf-8");

			DatagramPacket packet = new DatagramPacket(data, data.length);
			packet.setAddress(address.getAddress());
			packet.setPort(address.getPort());
			socket.send(packet);

			Arrays.fill(data, (byte) 0);
			socket.receive(packet);
			String received = new String(data, "utf-8");

			log.printf("sent = %s ; received = %s\n", payload, received);
			require(payload.equals(received));
		}
	}
}
