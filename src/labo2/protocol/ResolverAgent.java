package labo2.protocol;

import labo2.utils.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * A agent class for the Resolver API.
 * <p>
 * This agent receive the service socket and create a new one for the agent with the same address.
 * The agent socket is first used to register.
 * Then it is use to listen for ping request (listener).
 * So we can determine if the agent is running.
 */
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

	/**
	 * Listener thread waiting for PINGs message and replying with PONGs.
	 */
	private void listener() {
		try {
			byte[] buffer = new byte[512];
			byte[] ping = Message.serialize(SimpleMessage.ofType(MessageType.SERVICE_PONG));
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			while (true) {
				agentSocket.receive(packet);
				// Retrieve message from data
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

	/**
	 * Constructs a new resolver agent using the given socket et service id.
	 *
	 * @param socket  the service socket to user
	 * @param name    the service name, for logging purposes
	 * @param service the service ID
	 * @return the new resolver agent
	 * @throws IOException
	 */
	public static ResolverAgent register(DatagramSocket socket, String name, byte service) throws IOException {
		return new ResolverAgent(socket, name, service);
	}
}
