package labo2.services;

import labo2.protocol.ResolverAgent;
import labo2.utils.Logger;
import labo2.utils.Task;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * A simple echo service sending back messages it receives.
 */
public class Echo extends Task<Void> {
	public static byte SERVICE_ID = 0;

	private String name;
	private DatagramSocket socket;
	private ResolverAgent agent;
	private Logger log;

	private Echo(String name) {
		this.name = name;
	}

	protected void run() throws IOException {
		socket = new DatagramSocket(null);
		agent = ResolverAgent.register(socket, name, SERVICE_ID);
		log = Logger.getLogger(name);
		DatagramPacket packet = new DatagramPacket(new byte[512], 512);
		log.println("Echo service ready");
		ready();
		try {
			while (true) {
				socket.receive(packet);
				log.printf("Echoing %d bytes to %s:%d\n",
					packet.getLength() - packet.getOffset(), packet.getAddress(), packet.getPort());
				socket.send(packet);
			}
		} catch (SocketException closed) {
			if (!socket.isClosed()) throw closed;
		}
	}

	protected void interrupt() throws IOException {
		socket.close();
		agent.stop();
		log.println("Echo service stopped");
	}

	public static Echo instantiate(String... args) {
		return new Echo(args[0]);
	}
}
