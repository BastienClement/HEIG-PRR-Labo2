package labo2.client;

import labo2.TestLauncher;
import labo2.protocol.ResolverClient;
import labo2.utils.Logger;
import labo2.utils.Task;

import java.io.IOException;

/**
 * Base class for test suite.
 */
public abstract class TestClient extends Task<Boolean> {
	protected Logger log;
	protected ResolverClient client;
	protected TestLauncher.TestContext ctx;

	private class RequirementFailed extends RuntimeException {}

	public void setContext(TestLauncher.TestContext ctx) {
		this.ctx = ctx;
	}

	protected abstract String name();
	protected abstract String desc();
	protected abstract void execute() throws IOException ;

	protected final void run() throws IOException {
		log = Logger.getLogger(name());
		log.printf("===[[ %s ]]===\n", desc());
		try (ResolverClient client = ResolverClient.withNewSocket().withLogger(log)) {
			this.client = client;
			execute();
		} catch (Throwable fail) {
			synchronized (Logger.class) {
				ctx.sleep(100);
				fail.printStackTrace();
			}
			done(false);
			return;
		}
		done(true);
	}

	protected void require(boolean cond) {
		if (!cond) throw new RequirementFailed();
	}
}
