package labo2.utils;

import java.time.LocalDateTime;

/**
 * A synchronous logger allowing multiple concurrent processes to output to stdout
 * without conflict. Output is prefixed with the client prefix and the current time.
 */
public class Logger {
	/**
	 * Creates a new logger that clients can use to output to stdout without
	 * concurrency issues.
	 *
	 * @param prefix the client prefix
	 */
	public static Logger getLogger(String prefix) {
		return new Logger(prefix);
	}

	private String prefix;

	private Logger(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * Prints to stdout without formatting.
	 *
	 * @param s the string to print
	 */
	public void print(String s) {
		synchronized (Logger.class) {
			LocalDateTime now = LocalDateTime.now();
			System.out.printf("[%15s] [%02d:%02d:%02d.%03d] %s", prefix,
				now.getHour(), now.getMinute(), now.getSecond(), now.getNano() / 1000000,
				s);
		}
	}

	/**
	 * Prints to stdout without formatting but with an end-of-line character appended.
	 *
	 * @param s the string to pritn
	 */
	public void println(String s) {
		print(s + "\n");
	}

	/**
	 * Prints formatted output.
	 *
	 * @param fmt  the format to use
	 * @param args arguments to the format string
	 */
	public void printf(String fmt, Object... args) {
		print(String.format(fmt, args));
	}
}
