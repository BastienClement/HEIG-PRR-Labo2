package labo2.resolver;

import labo2.protocol.*;
import labo2.utils.Logger;
import labo2.utils.Task;

import java.io.IOException;
import java.net.*;
import java.util.LinkedList;

import static labo2.protocol.Protocol.RESOLVERS;

public class Resolver extends Task<Void> {
	/**
	 * Current state of the resolver
	 */
	private State state = State.SYNC;

	/**
	 * Index of this resolver
	 */
	private int id;

	/**
	 * The UDP datagram socket use by this resolver
	 */
	DatagramSocket socket;

	/**
	 * Logger instance
	 */
	private Logger log;

	/**
	 * Lists of services
	 */
	@SuppressWarnings("unchecked")
	private LinkedList<ServiceInstance>[] services = (LinkedList<ServiceInstance>[]) new LinkedList[Protocol.SERVICES_COUNT];

	private Resolver(int id) {
		this.id = id;
		this.log = Logger.getLogger("resolver:" + id);

		for (int i = 0; i < services.length; i++) {
			services[i] = new LinkedList<>();
		}
	}

	protected void run() throws IOException {
		socket = new DatagramSocket(RESOLVERS[id]);
		log.printf("Listening on %s:%d\n", socket.getLocalAddress(), socket.getLocalPort());
		byte[] buffer = new byte[512];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		try {
			while (true) {
				socket.receive(packet);
				receive(packet);
				packet.setData(buffer);
			}
		} catch (SocketException closed) {
			if (!socket.isClosed()) throw closed;
		}
	}

	protected void interrupt() {
		socket.close();
		log.println("Resolver stopped...");
	}

	/**
	 * Adds a new service to the local directory.
	 *
	 * @param instance the service instance
	 */
	private void registerService(ServiceInstance instance) {
		services[instance.service].add(instance);
	}

	/**
	 * Requests a service instance from the local directory.
	 *
	 * @param service the service id
	 * @return the address of one instance of the service, if any is available; else null
	 */
	private ServiceInstance requestService(int service) {
		LinkedList<ServiceInstance> list = services[service];
		ServiceInstance instance = list.poll();
		if (instance != null) list.add(instance);
		return instance;
	}

	private synchronized void receive(DatagramPacket packet) throws IOException {
		Message message = Message.parse(packet.getData(), packet.getOffset(), packet.getLength());
		switch (message.type()) {
			case SERVICE_REGISTER: {
				ServiceRegisterMessage msg = (ServiceRegisterMessage) message;
				log.printf("Received service offer for [%d] from %s:%d (agent:%d)\n",
					msg.service, packet.getAddress(), packet.getPort(), msg.agentPort);

				ServiceInstance instance = new ServiceInstance(msg.service, packet.getAddress(), packet.getPort(), msg.agentPort);
				registerService(instance);

				send(SimpleMessage.ofType(MessageType.SERVICE_REGISTERED), packet);
				break;
			}

			case SERVICE_REQUEST: {
				ServiceRequestMessage msg = (ServiceRequestMessage) message;
				log.printf("Received request for service [%d] from %s\n", msg.service, packet.getSocketAddress());

				ServiceInstance instance = requestService(msg.service);
				send(new ServiceOfferMessage(
					instance != null,
					instance != null ? new InetSocketAddress(instance.address, instance.port) : null
				), packet);
				break;
			}

			default:
				log.printf("Received unknown message: %s\n", message);
		}
	}

	/**
	 * Sends a message to every other resolvers.
	 *
	 * @param message the message to send
	 * @throws IOException
	 */
	private void broadcast(Message message) throws IOException {
		for (int i = 0; i < RESOLVERS.length; i++) {
			if (i != id) {
				send(message, RESOLVERS[i]);
			}
		}
	}

	/**
	 * Sends a message to the given address.
	 *
	 * @param message the message to send
	 * @param address the address to which the message should be sent
	 * @throws IOException
	 */
	private void send(Message message, InetSocketAddress address) throws IOException {
		send(message, address.getAddress(), address.getPort());
	}

	/**
	 * Sends a message to the given address and port.
	 *
	 * @param message the message to send
	 * @param address the address to which the message should be sent
	 * @param port    the port on which the message should be sent
	 * @throws IOException
	 */
	private void send(Message message, InetAddress address, int port) throws IOException {
		send(message, new DatagramPacket(new byte[0], 0, address, port));
	}

	/**
	 * Sends a message using the given packet object, packet address
	 * will be left untouched.
	 *
	 * @param message the message to send
	 * @param packet  the packed in which the payload should be encapsulated
	 * @throws IOException
	 */
	private void send(Message message, DatagramPacket packet) throws IOException {
		packet.setData(Message.serialize(message));
		socket.send(packet);
	}

	/**
	 * Launcher adapter
	 *
	 * @param args command line argument; first arg must be listening port
	 */
	public static Resolver intantiate(String... args) {
		return new Resolver(Integer.parseInt(args[0]));
	}
}
