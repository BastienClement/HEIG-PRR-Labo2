package labo2;

import labo2.client.FailOverTest;
import labo2.client.RoundRobinTest;
import labo2.client.TestClient;
import labo2.resolver.Resolver;
import labo2.services.Echo;
import labo2.services.Time;
import labo2.utils.Logger;
import labo2.utils.Task;

import java.util.function.Function;

import static labo2.protocol.Protocol.RESOLVERS;

public class TestLauncher {
	private static TestConstructor[] tests =  new TestConstructor[] {
		RoundRobinTest::new,
		FailOverTest::new
	};

	public static class TestContext {
		public final Task<?>[] resolvers;
		public final Task<?>[] services;

		public TestContext() {
			resolvers = new Task<?>[RESOLVERS.length];
			services = new Task<?>[3];
		}

		public <T> Task<T> spawn(Function<String[], Task<T>> factory, String... args) {
			Task<T> task = factory.apply(args);
			task.start();
			return task;
		}

		public void stopResolver(int... indices) {
			for (int index : indices) {
				if (resolvers[index] != null) {
					resolvers[index].stop();
					resolvers[index].result();
					resolvers[index] = null;
				}
			}
		}

		public void startResolver(int... indices) {
			for (int index : indices) {
				if (resolvers[index] == null) {
					resolvers[index] = spawn(Resolver::intantiate, String.valueOf(index));
				}
			}
			sleep(100);
		}

		public void sleep(long millis) {
			try {
				Thread.sleep(millis);
			} catch (InterruptedException ignored) {}
		}
	}

	@FunctionalInterface
	public interface TestConstructor {
		TestClient construct();
	}

	private static void test(TestContext ctx, TestConstructor constructor) {
		TestClient test = constructor.construct();
		test.setContext(ctx);
		test.start();
		if (!test.result()) {
			System.err.println("### TEST FAILED ###");
			System.exit(1);
		}
	}

	/**
	 * System simulation launcher.
	 *
	 * @param args useless
	 */
	public static void main(String[] args) {
		// Launcher logger
		Logger log = Logger.getLogger("launcher");
		TestContext ctx = new TestContext();

		log.println("*** Launching resolvers...");
		ctx.startResolver(0, 1, 2);

		log.println("*** Launching default services");
		ctx.services[0] = ctx.spawn(Echo::instantiate, "echo:1");
		ctx.services[1] = ctx.spawn(Time::instantiate, "time:1");
		ctx.services[2] = ctx.spawn(Time::instantiate, "time:2");
		ctx.sleep(100);

		log.println("*** Starting test suite");
		for (TestConstructor t : tests) {
			test(ctx, t);
		}

		log.println("*** Tests successful, shutting down.");
		for (int i = 0; i < 3; i++) {
			ctx.stopResolver(0, 1, 2);
			ctx.services[i].stop();
		}
	}
}
