package labo2.protocol;

import labo2.utils.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ResolverAgent {
	private Logger log;
	private DatagramSocket serviceSocket;
	private DatagramSocket agentSocket;

	private ResolverAgent(DatagramSocket socket, String name, byte service) throws IOException {
		log = Logger.getLogger("agent:" + name);
		log.printf("Starting agent for service: %s [%d]\n", name, service);

		// Open agent socket
		serviceSocket = socket;
		agentSocket = new DatagramSocket(0, socket.getLocalAddress());

		// Registration
		ResolverClient client = ResolverClient.using(serviceSocket).withLogger(log);
		if (client.register(service, agentSocket.getLocalPort())) {
			log.println("Registration successful");
		} else {
			throw new IllegalStateException();
		}

		// Listener
		new Thread(this::listener).start();
	}

	private void listener() {
		try {
			byte[] buffer = new byte[512];
			byte[] ping = Message.serialize(SimpleMessage.ofType(MessageType.SERVICE_PONG));
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			while (true) {
				agentSocket.receive(packet);
				Message message = Message.parse(packet.getData(), packet.getOffset(), packet.getLength());
				switch (message.type()) {
					case SERVICE_PING:
						packet.setData(ping);
						agentSocket.send(packet);
						packet.setData(buffer);
						break;
					default:
						log.printf("Received unexpected message: %s\n", message.toString());
				}
			}
		} catch (SocketException closed) {
			if (!agentSocket.isClosed()) {
				closed.printStackTrace();
				System.exit(1);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void stop() {
		agentSocket.close();
	}

	public static ResolverAgent register(DatagramSocket socket, String name, byte service) throws IOException {
		return new ResolverAgent(socket, name, service);
	}
}
