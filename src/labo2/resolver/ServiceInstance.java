package labo2.resolver;

import java.net.InetAddress;

public class ServiceInstance {
	public final byte service;
	public final InetAddress address;
	public final int port;
	public final int agentPort;

	public ServiceInstance(byte service, InetAddress address, int port, int agentPort) {
		this.service = service;
		this.address = address;
		this.port = port;
		this.agentPort = agentPort;
	}
}
