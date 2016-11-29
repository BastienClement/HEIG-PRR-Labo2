package labo2.protocol;

import labo2.utils.Logger;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static labo2.protocol.Protocol.RESOLVERS;

/**
 * A client class for the Resolver API.
 * <p>
 * This client implement the resolver fail-over system that will attempt to
 * switch to another resolver if the current one is not responsive.
 * On initialization, the first resolver to use is chosen at random.
 * As long as the current resolver is responsive, the client does not attempt
 * to switch to a new resolver.
 * If after testing each defined resolver once, none of them are responsive,
 * the request fails with an ResolverClientException.
 */
public class ResolverClient implements AutoCloseable {
	/**
	 * Default timeout on request: 1 sec.
	 */
	public static final int DEFAULT_TIMEOUT = 1000;

	/**
	 * Creates a new resolver client using the given datagram socket.
	 *
	 * @param socket the datagram socket to user
	 * @return a new client instance
	 */
	public static ResolverClient using(DatagramSocket socket) {
		return new ResolverClient(socket);
	}

	/**
	 * Creates a new resolver client using a new socket.
	 * This socket must be closed by a call to close() once the client is
	 * no longer useful.
	 *
	 * @return a new client instance
	 * @throws SocketException
	 */
	public static ResolverClient withNewSocket() throws SocketException {
		return new ResolverClient(new DatagramSocket(null));
	}

	/**
	 * The datagram socket to use
	 */
	private final DatagramSocket socket;

	/**
	 * Transmission buffer
	 */
	private final byte[] buffer = new byte[512];

	/**
	 * Reusable instance of DatagramPacket.
	 */
	private final DatagramPacket packet = new DatagramPacket(new byte[0], 0);

	/**
	 * The current resolver index to query.
	 * On initialization, the initial resolver is chosen at random.
	 */
	private int resolverIndex = new Random().nextInt(RESOLVERS.length);

	/**
	 * The logger to use, if any.
	 */
	private Logger logger;

	/**
	 * Constructor.
	 *
	 * @param socket the socket to use for communication with the resolver
	 */
	private ResolverClient(DatagramSocket socket) {
		this.socket = socket;
	}

	/**
	 * Sets the logger instance to use.
	 * If set to null, logging is disabled in this client.
	 *
	 * @param logger the logger instance
	 * @return this object
	 */
	public ResolverClient withLogger(Logger logger) {
		this.logger = logger;
		return this;
	}

	/**
	 * Manually sets the resolver index to use.
	 * Note that this setting only affects the initial queried resolver: if the given
	 * resolver is not responsive, the next one will be tried automatically.
	 *
	 * @param index
	 * @return
	 */
	public ResolverClient setResolverIndex(int index) {
		this.resolverIndex = index % RESOLVERS.length;
		return this;
	}

	/**
	 * Sends a generic request to the resolver and wait for a reply.
	 *
	 * @param message the request message
	 * @return the response message
	 * @throws IOException
	 * @throws ResolverClientException if no resolver are available
	 */
	public Message request(Message message, int timeout) throws IOException {
		int origResolverIndex = resolverIndex;
		byte[] data = Message.serialize(message);
		do {
			// Request
			InetSocketAddress resolver = RESOLVERS[resolverIndex];
			packet.setAddress(resolver.getAddress());
			packet.setPort(resolver.getPort());
			packet.setData(data);
			if (logger != null) {
				String name = (message instanceof SimpleMessage) ? message.toString() : message.getClass().getSimpleName();
				logger.printf("Sending %s to %s\n", name, resolver);
			}
			socket.send(packet);

			// Response
			Message response = receive(timeout);
			if (response == null) {
				if (logger != null) logger.printf("No answer from resolver, skipping...\n");
				resolverIndex = (resolverIndex + 1) % RESOLVERS.length;
			} else {
				return response;
			}
		} while (resolverIndex != origResolverIndex);
		throw new ResolverClientException("No resolvers available");
	}

	/**
	 * Receives a generic message.
	 * The operation will timeout after DEFAULT_TIMEOUT milliseconds.
	 *
	 * @return the received message, or null in case of a timeout
	 * @throws IOException
	 */
	private Message receive(int timeout) throws IOException {
		int origSoTimeout = socket.getSoTimeout();
		try {
			socket.setSoTimeout(timeout);
			packet.setData(buffer);
			socket.receive(packet);
			return Message.parse(packet.getData(), packet.getOffset(), packet.getLength());
		} catch (SocketTimeoutException e) {
			return null;
		} finally {
			socket.setSoTimeout(origSoTimeout);
		}
	}

	/**
	 * Performs a service registration request.
	 *
	 * @param service   the service id
	 * @param agentPort the agent port of the instance
	 * @return the registration success flag
	 * @throws IOException
	 */
	public boolean register(byte service, int agentPort) throws IOException {
		Message request = new ServiceRegisterMessage(service, agentPort);
		Message response = request(request, DEFAULT_TIMEOUT);
		return response.type() == MessageType.SERVICE_REGISTERED;
	}

	/**
	 * Requests a service address resolution.
	 *
	 * @param service the service id to resolve
	 * @return the service address, or null if unavailable
	 * @throws IOException
	 */
	public InetSocketAddress resolve(byte service) throws IOException {
		Message request = new ServiceRequestMessage(service);
		ServiceOfferMessage response = (ServiceOfferMessage) request(request, DEFAULT_TIMEOUT);
		return response.address;
	}

	/**
	 * Notifies a resolver that the service is offline.
	 *
	 * @param service the service id
	 * @param address the instance address
	 * @return true, if the client should try again using the same address
	 * @throws IOException
	 */
	public boolean offline(byte service, InetSocketAddress address) throws IOException {
		return ((ServiceThanksMessage) request(new ServiceOfflineMessage(service, address), DEFAULT_TIMEOUT * 5)).retry;
	}

	/**
	 * Requests list synchronization from another resolver.
	 *
	 * @return the instance list
	 * @throws IOException
	 */
	public List<ListAddMessage> sync() throws IOException {
		Message response = request(SimpleMessage.ofType(MessageType.LIST_SYNC_REQUEST), DEFAULT_TIMEOUT);
		List<ListAddMessage> instances = new ArrayList<>();
		while (response.type() != MessageType.LIST_SYNC_COMMIT) {
			if (response.type() == MessageType.LIST_ADD) {
				instances.add((ListAddMessage) response);
				response = receive(DEFAULT_TIMEOUT);
				if (response != null) continue;
			}
			throw new IllegalStateException();
		}
		return instances;
	}

	public void close() {
		socket.close();
	}
}
