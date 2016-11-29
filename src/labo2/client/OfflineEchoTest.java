package labo2.client;

import labo2.services.Echo;

import java.io.IOException;
import java.net.InetSocketAddress;

public class OfflineEchoTest extends TestClient {
	protected String name() { return "off-echo-test"; }
	protected String desc() { return "Testing offline Echo notification service"; }

	protected void execute() throws IOException {
		// Resolution
		InetSocketAddress address = client.resolve(Echo.SERVICE_ID);
		log.printf("address = %s\n", address);

		ctx.stopService(0);
		boolean retry = client.offline(Echo.SERVICE_ID, address);
		require(!retry);

		ctx.startService(0, Echo::instantiate, "echo:1");
		ctx.sleep(100);

		InetSocketAddress address2 = client.resolve(Echo.SERVICE_ID);
		log.printf("address2 = %s\n", address);
		require(address2 != null);

		boolean retry2 = client.offline(Echo.SERVICE_ID, address2);
		require(retry2);
	}
}
