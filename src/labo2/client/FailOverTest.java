package labo2.client;

import labo2.services.Time;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Tests the fail-over feature when a resolver is unresponsive.
 */
public class FailOverTest extends TestClient {
	protected String name() { return "fail-over-test"; }
	protected String desc() { return "Testing Resolver fail-over"; }

	protected void execute() throws IOException {
		client.setResolverIndex(0);
		InetSocketAddress first = client.resolve(Time.SERVICE_ID);
		log.printf("first = %s\n", first);
		ctx.stopResolver(0);

		InetSocketAddress second = client.resolve(Time.SERVICE_ID);
		log.printf("second = %s\n", second);
		InetSocketAddress third = client.resolve(Time.SERVICE_ID);
		log.printf("third = %s\n", third);

		require(first.equals(second) || first.equals(third));
		ctx.startResolver(0);
	}
}
