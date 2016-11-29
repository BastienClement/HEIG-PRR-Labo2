package labo2.utils;

import java.io.IOException;

/**
 * An abstract task executing asynchronously and producing a value.
 * <p>
 * A task is a wrapper around a raw Java thread, providing a simple
 * framework to handle task interruption and waiting on result.
 *
 * @param <T> the result type of this task
 */
public abstract class Task<T> {
	// Managed thread instance
	private Thread thread;

	// Status flags
	private boolean started = false;
	private boolean ready = false;
	private boolean done = false;

	// Result
	private T result;

	/**
	 * Use this method to implement the main task logic.
	 * It is expected for this method to take possibly infinite time to complete.
	 *
	 * @throws IOException
	 */
	protected abstract void run() throws IOException;

	/**
	 * Use this method to implement mid-task interruption logic.
	 * This method will be called when user code calls the stop() method.
	 *
	 * @throws IOException
	 */
	protected void interrupt() throws IOException {}

	/**
	 * Starts this task.
	 * This is a non-blocking call, the task itself executes in a
	 * newly spawned thread. Tasks cannot be started more than once.
	 */
	public final synchronized void start() {
		if (started) throw new IllegalStateException();
		thread = new Thread(() -> {
			try {
				run();
				synchronized (Task.this) {
					if (!done) done(null);
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		});
		thread.start();
		started = true;
	}

	/**
	 * Blocks the calling thread until this Task is ready.
	 * This is used to synchronize the tests runner with resolver and services
	 * initialization process.
	 */
	public synchronized void sync() {
		while (!ready) {
			try {
				wait();
			} catch (InterruptedException ignored) {}
		}
	}

	/**
	 * Sets the ready flag of this task.
	 * When the task provides a service, this method should be called once the
	 * initialization is complete and the task is ready to be used.
	 */
	protected synchronized void ready() {
		if (!ready) {
			ready = true;
			notifyAll();
		}
	}

	/**
	 * Defines the result value of this task and notify threads waiting on the result.
	 * The task run() method must return shortly after calling this method.
	 *
	 * @param result the task result value
	 */
	protected synchronized void done(T result) {
		if (done) throw new IllegalStateException();
		this.result = result;
		this.ready = true;
		this.done = true;
		notifyAll();
	}

	/**
	 * Gracefully shuts this task down.
	 * A shut down task cannot be started again.
	 */
	public final synchronized void stop() {
		if (thread != null) {
			try {
				if (!done) interrupt();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	/**
	 * Waits and returns this task result.
	 *
	 * @return the task result
	 */
	public synchronized T result() {
		while (!done) {
			try {
				wait();
			} catch (InterruptedException ignored) {}
		}
		return result;
	}
}
