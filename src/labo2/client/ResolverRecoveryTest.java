package labo2.client;

import labo2.services.Echo;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ResolverRecoveryTest extends TestClient {
	protected String name() { return "recovery-test"; }
	protected String desc() { return "Testing Resolver recovery"; }

	protected void execute() throws IOException {
		InetSocketAddress first = client.resolve(Echo.SERVICE_ID);
		log.printf("first = %s\n", first);

		ctx.stopResolver(0, 2);
		ctx.startResolver(0);
		ctx.stopResolver(1);

		InetSocketAddress second = client.resolve(Echo.SERVICE_ID);
		log.printf("second = %s\n", second);

		require(first.equals(second));
		ctx.startResolver(1, 2);
	}
}
