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

/**
 * The test launcher
 * Mostly plumbing.
 */
public class TestLauncher {
	/**
	 * Tests to run
	 */
	private static TestConstructor[] tests = new TestConstructor[]{
		RoundRobinTest::new,
		FailOverTest::new
	};

	/**
	 * The test context.
	 * An instance of this class is given to tests and allow controlling
	 * the system status during the test.
	 */
	public static class TestContext {
		/**
		 * Resolver instances
		 */
		public final Resolver[] resolvers;

		/**
		 * Service instances
		 */
		public final Task<?>[] services;

		/**
		 * Constructs a new test context with as much resolvers as defined and 3 services.
		 */
		public TestContext() {
			resolvers = new Resolver[RESOLVERS.length];
			services = new Task<?>[3];
		}

		/**
		 * Spawns a new task and immediately starts it.
		 *
		 * @param factory the task factory method
		 * @param args    task factory arguments
		 * @param <T>     the return type of the task
		 * @param <R>     the effective task type
		 * @return the newly created task
		 */
		public <T, R extends Task<T>> R spawn(Function<String[], R> factory, String... args) {
			R task = factory.apply(args);
			task.start();
			return task;
		}

		/**
		 * Stops instances of resolvers or services.
		 * This method will block until the task is fully stopped.
		 * See stopResolver() and stopService().
		 *
		 * @param instances the task array
		 * @param indices   the indices to stop
		 */
		private void stopInstances(Task<?>[] instances, int... indices) {
			for (int index : indices) {
				if (instances[index] != null) {
					instances[index].stop();
					instances[index].result();
					instances[index] = null;
				}
			}
		}

		/**
		 * Stops resolver instances.
		 * This method will block until the resolver is fully stopped.
		 *
		 * @param indices resolver indices to stop
		 */
		public void stopResolver(int... indices) {
			stopInstances(resolvers, indices);
		}

		/**
		 * Starts resolver instances.
		 * This method will block until the resolver is ready.
		 *
		 * @param indices resolver indices to start
		 */
		public void startResolver(int... indices) {
			for (int index : indices) {
				if (resolvers[index] == null) {
					resolvers[index] = spawn(Resolver::intantiate, String.valueOf(index));
					resolvers[index].sync();
				}
			}
		}

		/**
		 * Stops service instances.
		 * This method will block until the resolver is fully stopped.
		 *
		 * @param indices service indices to stop
		 */
		public void stopService(int... indices) {
			stopInstances(services, indices);
		}

		/**
		 * Starts services instances.
		 * This method will block until the resolver is ready.
		 *
		 * @param index   the service index
		 * @param factory the service factory method
		 * @param args    arguments to the service factory
		 * @param <T>     the return type of the service
		 * @param <R>     the actual type of the service
		 * @return the newly started service instance
		 * @throws IllegalStateException if called with an index matching an
		 *                               already running service
		 */
		public <T, R extends Task<T>> R startService(int index, Function<String[], R> factory, String... args) {
			if (services[index] == null) {
				R task = spawn(factory, args);
				services[index] = task;
				task.sync();
				return task;
			} else {
				throw new IllegalStateException();
			}
		}

		/**
		 * Sleeps for the given duration.
		 *
		 * @param millis milliseconds to sleep for.
		 */
		public void sleep(long millis) {
			try {
				Thread.sleep(millis);
			} catch (InterruptedException ignored) {}
		}
	}

	/**
	 * Interface of a test constructor.
	 */
	@FunctionalInterface
	public interface TestConstructor {
		TestClient construct();
	}

	/**
	 * Executes a test in the given context.
	 *
	 * @param ctx         the context for the test
	 * @param constructor the test constructor to invoke
	 */
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
		ctx.startService(0, Echo::instantiate, "echo:1");
		ctx.startService(1, Time::instantiate, "time:1");
		ctx.startService(2, Time::instantiate, "time:2");

		log.println("*** Starting test suite");
		for (TestConstructor t : tests) test(ctx, t);

		log.println("*** Tests successful, shutting down.");
		ctx.stopResolver(0, 1, 2);
		ctx.stopService(0, 1, 2);
	}
}
