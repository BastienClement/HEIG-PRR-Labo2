package labo2.protocol;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class Protocol {
	public static final int SERVICES_COUNT = 2;

	public static final InetSocketAddress MULTICAST_GROUP;
	public static final InetSocketAddress[] RESOLVERS = new InetSocketAddress[] {
		new InetSocketAddress(InetAddress.getLoopbackAddress(), 6000),
		new InetSocketAddress(InetAddress.getLoopbackAddress(), 6001),
		new InetSocketAddress(InetAddress.getLoopbackAddress(), 6002),
	};

	// Thanks java !
	// Shenanigans required to skip the UnknownHostException
	static {
		try {
			MULTICAST_GROUP = new InetSocketAddress(InetAddress.getByName("225.5.5.5"), 5454);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
			throw null;
		}
	}
}
