package labo2.protocol;

import java.io.DataOutputStream;
import java.io.IOException;

public class SimpleMessage extends Message {
	private static SimpleMessage[] INSTANCES = null;

	private MessageType type;

	public MessageType type() {
		return type;
	}

	private SimpleMessage(MessageType type) {
		this.type = type;
	}

	public String toString() {
		return "SimpleMessage<" + type + ">";
	}

	void serialize(DataOutputStream output) throws IOException {}

	public static synchronized SimpleMessage ofType(MessageType type) {
		if (!type.isSimple()) {
			throw new IllegalArgumentException();
		} else if (INSTANCES == null) {
			MessageType[] types = MessageType.values();
			INSTANCES = new SimpleMessage[types.length];
			for (MessageType t : types) {
				if (t.isSimple()) {
					INSTANCES[t.ordinal()] = new SimpleMessage(t);
				}
			}
		}
		return INSTANCES[type.ordinal()];
	}
}
