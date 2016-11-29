package labo2.client;

import labo2.services.Echo;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Tests the resolver recovery features.
 *
 * - First, the address of the Echo service is resolved one time. The first and
 *   third resolver are then stopped, leaving only the second one with the correct
 *   services list.
 * - Then, the first resolver is restarted and syncs itself with the second one.
 * - The second one is then stopped to ensure that future resolve requests are
 *   handled by the first one and the Echo service is resolved once again.
 *
 * If everything went fine, the first resolver should have received the full
 * services list before the second one shut down allowing it to correctly
 * resolve the second request. The test is valid if both the first and
 * second request resolved to the same address.
 */
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
