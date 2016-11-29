package labo2.client;

import labo2.services.Time;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

public class TimeTest extends TestClient {
	protected String name() { return "echo-test"; }
	protected String desc() { return "Testing Time service"; }

	protected void execute() throws IOException {
		InetSocketAddress address = client.resolve(Time.SERVICE_ID);
		log.printf("address = %s\n", address);

		try (DatagramSocket socket = new DatagramSocket(null)) {
			byte[] data = new byte[8];

			DatagramPacket packet = new DatagramPacket(data, data.length);
			packet.setAddress(address.getAddress());
			packet.setPort(address.getPort());
			socket.send(packet);
			socket.receive(packet);

			long now = System.currentTimeMillis();
			long received = 0;
			for (int i = 0; i < 8; i++) {
				received <<= 8;
				received |= (data[i] & 0xFF);
			}

			log.printf("time = %d ; received = %d ; delta = %d\n", now, received, (now - received));
			require(Math.abs(now - received) < 5000);
		}
	}
}
