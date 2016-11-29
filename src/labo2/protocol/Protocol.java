package labo2.protocol;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * This class contains all sockets we need. They are instantiated staticly.
 * So every socket is created only one time.
 */

public class Protocol {
	public static final int SERVICES_COUNT = 2;

	public static final InetSocketAddress[] RESOLVERS = new InetSocketAddress[] {
		new InetSocketAddress(InetAddress.getLoopbackAddress(), 6000),
		new InetSocketAddress(InetAddress.getLoopbackAddress(), 6001),
		new InetSocketAddress(InetAddress.getLoopbackAddress(), 6002),
	};
}
