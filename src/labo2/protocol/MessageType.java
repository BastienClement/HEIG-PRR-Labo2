package labo2.protocol;

import java.io.DataInputStream;
import java.io.IOException;

public enum MessageType {
	// Public Resolver API
	SERVICE_REGISTER(ServiceRegisterMessage::deserialize),
	SERVICE_REGISTERED(null),
	SERVICE_REQUEST(ServiceRequestMessage::deserialize),
	SERVICE_OFFER(ServiceOfferMessage::deserialize),
	SERVICE_PING(null),
	SERVICE_PONG(null),

	// Private Resolver API
	LIST_SYNC_REQUEST(null),
	LIST_SYNC_COMMIT(null),
	LIST_ADD(ListAddMessage::deserialize),
	LIST_REMOVE(ListRemoveMessage::deserialize),

	// Whatever
	SELF_READY(null);

	/**
	 * The deserializer associated with this message type.
	 * If the deserializer is null, the message is considered a SimpleMessage and
	 * have no associated data requiring parsing or serializing.
	 */
	private final MessageDeserializer deserializer;

	MessageType(MessageDeserializer deserializer) {
		this.deserializer = deserializer;
	}

	public boolean isSimple() {
		return deserializer == null;
	}

	public Message deserialize(DataInputStream input) throws IOException {
		return (isSimple()) ? SimpleMessage.ofType(this) : deserializer.deserialize(input);
	}
}
