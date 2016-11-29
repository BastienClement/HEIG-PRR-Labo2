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

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ServiceInstance)) {
			return false;
		} else {
			ServiceInstance other = (ServiceInstance) o;
			return service == other.service && address.equals(other.address);
		}
	}
}
