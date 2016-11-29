package labo2.resolver;

import java.net.InetSocketAddress;

public class ServiceInstance {
	public final byte service;
	public final InetSocketAddress address;
	public final int agentPort;

	public ServiceInstance(byte service, InetSocketAddress address, int agentPort) {
		this.service = service;
		this.address = address;
		this.agentPort = agentPort;
	}
}
