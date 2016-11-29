package labo2.client;

import labo2.services.Time;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Tests the round-robin load balancing feature of the resolver.
 *
 * This is done by requesting the same service three times and checking
 * that the first and third are a match while the second is not.
 *
 * This test requires that exactly two Time services are started by the
 * test launcher.
 */
public class RoundRobinTest extends TestClient {
	protected String name() { return "rr-lb-test"; }
	protected String desc() { return "Testing RoundRobin load balancing"; }

	protected void execute() throws IOException {
		InetSocketAddress first = client.resolve(Time.SERVICE_ID);
		log.printf("first = %s\n", first);

		InetSocketAddress second = client.resolve(Time.SERVICE_ID);
		log.printf("second = %s\n", second);

		InetSocketAddress third = client.resolve(Time.SERVICE_ID);
		log.printf("third = %s\n", third);

		require(!first.equals(second));
		require(!second.equals(third));
		require(first.equals(third));
	}
}
