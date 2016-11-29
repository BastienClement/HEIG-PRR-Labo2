package labo2.services;

import labo2.protocol.ResolverAgent;
import labo2.utils.Logger;
import labo2.utils.Task;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * A simple time service replying with the current timestamp to
 * every message it receives.
 */
public class Time extends Task<Void> {
	public static byte SERVICE_ID = 1;

	private String name;
	private DatagramSocket socket;
	private ResolverAgent agent;
	private Logger log;

	private Time(String name) {
		this.name = name;
	}

	protected void run() throws IOException {
		socket = new DatagramSocket(null);
		agent = ResolverAgent.register(socket, name, SERVICE_ID);
		log = Logger.getLogger(name);
		byte[] buffer = new byte[8];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		log.println("Time service ready");
		try {
			while (true) {
				socket.receive(packet);
				log.printf("Sending time to %s:%d\n", packet.getAddress(), packet.getPort());
				long time = System.currentTimeMillis();
				buffer[0] = (byte) (time >>> 56);
				buffer[1] = (byte) (time >>> 48);
				buffer[2] = (byte) (time >>> 40);
				buffer[3] = (byte) (time >>> 32);
				buffer[4] = (byte) (time >>> 24);
				buffer[5] = (byte) (time >>> 16);
				buffer[6] = (byte) (time >>> 8);
				buffer[7] = (byte) (time >>> 0);
				socket.send(packet);
			}
		} catch (SocketException closed) {
			if (!socket.isClosed()) throw closed;
		}
	}

	protected void interrupt() throws IOException {
		socket.close();
		agent.stop();
		log.println("Time service stopped");
	}

	public static Time instantiate(String... args) {
		return new Time(args[0]);
	}
}
