package labo2;

import labo2.resolver.Resolver;
import labo2.services.Echo;
import labo2.services.Time;
import labo2.utils.Task;

import java.util.function.Function;

public class Launcher {
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("No argument given");
			return;
		}

		String[] appArgs = new String[args.length - 1];
		System.arraycopy(args, 1, appArgs, 0, args.length - 1);
		select(args[0]).apply(appArgs).start();
	}

	private static Function<String[], Task> select(String name) {
		switch (name) {
			case "time":
				return Time::instantiate;
			case "echo":
				return Echo::instantiate;
			case "resolver":
				return Resolver::intantiate;
			default:
				throw new RuntimeException("Undefined run mode: " + name);
		}
	}
}
