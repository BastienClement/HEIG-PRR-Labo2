package labo2.utils;

import java.io.IOException;

public abstract class Task<T> {
	private Thread thread;
	private boolean started = false;
	private boolean done = false;
	private boolean running = false;
	private T result;

	protected abstract void run() throws IOException;
	protected void interrupt() throws IOException {}

	public final synchronized void start() {
		if (started) throw new IllegalStateException();
		thread = new Thread(() -> {
			try {
				synchronized (Task.this) {
					running = true;
				}
				run();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		});
		thread.start();
		started = true;
	}

	protected synchronized void done(T result) {
		if (done) throw new IllegalStateException();
		this.result = result;
		this.done = true;
		notifyAll();
	}

	public synchronized T result() {
		while (!done) {
			if (running && !thread.isAlive()) {
				done = true;
				notifyAll();
			} else {
				try {
					wait();
				} catch (InterruptedException ignored) {}
			}
		}
		return result;
	}

	public final synchronized void stop() {
		if (thread != null) {
			try {
				interrupt();
				synchronized (this) {
					if (!done) done(null);
				}
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}
