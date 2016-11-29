package labo2.protocol;

import labo2.utils.Logger;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static labo2.protocol.Protocol.RESOLVERS;

public class ResolverClient implements AutoCloseable {
	public static final int DEFAULT_TIMEOUT = 1000;

	public static ResolverClient using(DatagramSocket socket) {
		return new ResolverClient(socket);
	}

	public static ResolverClient withNewSocket() throws SocketException {
		return new ResolverClient(new DatagramSocket(null));
	}

	private final DatagramSocket socket;
	private final byte[] buffer = new byte[512];
	private final DatagramPacket packet = new DatagramPacket(new byte[0], 0);
	private int resolverIndex = new Random().nextInt(RESOLVERS.length);
	private Logger logger;

	private ResolverClient(DatagramSocket socket) {
		this.socket = socket;
	}

	public ResolverClient withLogger(Logger logger) {
		this.logger = logger;
		return this;
	}

	public ResolverClient setResolverIndex(int index) {
		this.resolverIndex = index % RESOLVERS.length;
		return this;
	}

	public Message request(Message message) throws IOException {
		int origResolverIndex = resolverIndex;
		byte[] data = Message.serialize(message);
		do {
			InetSocketAddress resolver = RESOLVERS[resolverIndex];
			packet.setAddress(resolver.getAddress());
			packet.setPort(resolver.getPort());
			packet.setData(data);
			if (logger != null) {
				String name = (message instanceof SimpleMessage) ? message.toString() : message.getClass().getSimpleName();
				logger.printf("Sending %s to %s\n", name, resolver);
			}
			socket.send(packet);
			Message response = receive();
			if (response == null) {
				if (logger != null) logger.printf("No answer from resolver, skipping...\n");
				resolverIndex = (resolverIndex + 1) % RESOLVERS.length;
			} else {
				return response;
			}
		} while (resolverIndex != origResolverIndex);
		throw new ResolverClientException("No resolvers available");
	}

	private Message receive() throws IOException {
		int origSoTimeout = socket.getSoTimeout();
		try {
			socket.setSoTimeout(DEFAULT_TIMEOUT);
			packet.setData(buffer);
			socket.receive(packet);
			return Message.parse(packet.getData(), packet.getOffset(), packet.getLength());
		} catch (SocketTimeoutException e) {
			return null;
		} finally {
			socket.setSoTimeout(origSoTimeout);
		}
	}

	public boolean register(byte service, int agentPort) throws IOException {
		Message request = new ServiceRegisterMessage(service, agentPort);
		Message response = request(request);
		return response.type() == MessageType.SERVICE_REGISTERED;
	}

	public InetSocketAddress resolve(byte service) throws IOException {
		Message request = new ServiceRequestMessage(service);
		ServiceOfferMessage response = (ServiceOfferMessage) request(request);
		return response.address;
	}

	public List<ListAddMessage> sync() throws IOException {
		Message response = request(SimpleMessage.ofType(MessageType.LIST_SYNC_REQUEST));
		List<ListAddMessage> instances = new ArrayList<>();
		while (response.type() != MessageType.LIST_SYNC_COMMIT) {
			if (response.type() == MessageType.LIST_ADD) {
				instances.add((ListAddMessage) response);
				response = receive();
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
