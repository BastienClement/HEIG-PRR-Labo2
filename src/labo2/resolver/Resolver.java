package labo2.resolver;

import labo2.protocol.*;
import labo2.protocol.MessageType;
import labo2.utils.Logger;
import labo2.utils.Task;

import java.io.IOException;
import java.net.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static labo2.protocol.MessageType.*;
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
	private DatagramSocket socket;

	/**
	 * Logger instance
	 */
	private Logger log;

	/**
	 * Lists of services instances
	 */
	@SuppressWarnings("unchecked")
	private LinkedList<ServiceInstance>[] services = (LinkedList<ServiceInstance>[]) new LinkedList[Protocol.SERVICES_COUNT];

	/**
	 * Constructs a new Resolver instance.
	 *
	 * @param id the pre-defined configuration index of this resolver
	 */
	private Resolver(int id) {
		this.id = id;
		this.log = Logger.getLogger("resolver:" + id);

		for (int i = 0; i < services.length; i++) {
			services[i] = new LinkedList<>();
		}
	}

	/**
	 * Main process loop.
	 *
	 * @throws IOException
	 */
	protected void run() throws IOException {
		// Open resolver socket
		socket = new DatagramSocket(RESOLVERS[id]);
		log.printf("Listening on %s:%d\n", socket.getLocalAddress(), socket.getLocalPort());

		// Initialize instances list from another resolver
		init();

		// Main loop
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

	/**
	 * Initializes this resolver by loading the services list from another resolver.
	 */
	private void init() throws IOException {
		try (ResolverClient client = ResolverClient.withNewSocket().withLogger(log)) {
			List<ListAddMessage> instances = client.sync();
			log.printf("Received %d services\n", instances.size());
			for (ListAddMessage add : instances) {
				registerService(new ServiceInstance(add.service, add.address, add.agentPort));
			}
		} catch (ResolverClientException ignored) {
			log.println("No resolver available, starting with an empty directory...");
			// Ignore ResolverClientException, just start with an empty list
		} finally {
			// Send SELF_READY message
			byte[] data = Message.serialize(SimpleMessage.ofType(SELF_READY));
			DatagramPacket packet = new DatagramPacket(data, data.length, socket.getLocalAddress(), socket.getLocalPort());
			socket.send(packet);
		}
	}

	/**
	 * Stops this resolver.
	 */
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
		for (ServiceInstance i : services[instance.service]) {
			if (i.equals(instance)) return;
		}
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

	private int serviceAgentPort(byte service, InetSocketAddress address) {
		for (ServiceInstance i : services[service]) {
			if (i.address.equals(address)) {
				return i.agentPort;
			}
		}
		return -1;
	}

	private void removeService(byte service, InetSocketAddress address) {
		Iterator<ServiceInstance> it = services[service].listIterator();
		while (it.hasNext()) {
			ServiceInstance instance = it.next();
			if (instance.address.equals(address)) {
				it.remove();
				return;
			}
		}
	}

	private synchronized void receive(DatagramPacket packet) throws IOException {
		Message message = Message.parse(packet.getData(), packet.getOffset(), packet.getLength());
		MessageType type = message.type();

		if (state != State.READY && type != SELF_READY && type != LIST_ADD && type != LIST_REMOVE) {
			return;
		}

		switch (type) {
			case SELF_READY: {
				// Set the resolver as ready
				state = State.READY;
				ready();
				break;
			}

			case SERVICE_REGISTER: {
				ServiceRegisterMessage msg = (ServiceRegisterMessage) message;
				log.printf("Received service offer for [%d] from %s:%d (agent:%d)\n",
					msg.service, packet.getAddress(), packet.getPort(), msg.agentPort);

				InetSocketAddress address = new InetSocketAddress(packet.getAddress(), packet.getPort());
				ServiceInstance instance = new ServiceInstance(msg.service, address, msg.agentPort);
				registerService(instance);

				broadcast(new ListAddMessage(instance.service, instance.address, instance.agentPort));
				send(SimpleMessage.ofType(MessageType.SERVICE_REGISTERED), packet);
				break;
			}

			case SERVICE_REQUEST: {
				ServiceRequestMessage msg = (ServiceRequestMessage) message;
				log.printf("Received request for service [%d] from %s\n", msg.service, packet.getSocketAddress());

				ServiceInstance instance = requestService(msg.service);
				send(new ServiceOfferMessage(
					instance != null,
					instance != null ? instance.address : null
				), packet);
				break;
			}

			case SERVICE_OFFLINE: {
				ServiceOfflineMessage msg = (ServiceOfflineMessage) message;
				log.printf("Received service offline notification for service [%d] from %s\n",
					msg.service, packet.getSocketAddress());

				int agentPort = serviceAgentPort(msg.service, msg.address);
				if (agentPort < 0) {
					send(new ServiceThanksMessage(false), packet);
					break;
				}

				InetAddress clientAddress = packet.getAddress();
				int clientPort = packet.getPort();

				new Thread(() -> {
					try (DatagramSocket socket = new DatagramSocket(null)) {
						byte[] data = Message.serialize(SimpleMessage.ofType(SERVICE_PING));
						DatagramPacket p = new DatagramPacket(data, data.length, msg.address.getAddress(), agentPort);

					} catch (IOException e) {
						e.printStackTrace();
						System.exit(1);
					}
				});
				break;
			}

			case LIST_SYNC_REQUEST: {
				log.printf("Received sync request from %s\n", packet.getSocketAddress());
				for (LinkedList<ServiceInstance> serviceInstances : services) {
					for (ServiceInstance instance : serviceInstances) {
						send(new ListAddMessage(instance.service, instance.address, instance.agentPort), packet);
					}
				}
				send(SimpleMessage.ofType(MessageType.LIST_SYNC_COMMIT), packet);
				break;
			}

			case LIST_ADD: {
				ListAddMessage msg = (ListAddMessage) message;
				log.printf("Received list add notification from %s\n", packet.getSocketAddress());
				registerService(new ServiceInstance(msg.service, msg.address, msg.agentPort));
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
