package labo2.protocol;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Represents all the message types used in our communications
 * <p>
 * This class is mostly used to determine the receipt messages type and so is responsible for chosing response actions
 * Every type corresponds to a particulary deserializer which is used to deserialize the message
 * If the type has no deserializer, it is considerated as a SimpleMessage
 */
public enum MessageType {
	// Public Resolver API
	SERVICE_REGISTER(ServiceRegisterMessage::deserialize),
	SERVICE_REGISTERED(null),
	SERVICE_REQUEST(ServiceRequestMessage::deserialize),
	SERVICE_OFFER(ServiceOfferMessage::deserialize),
	SERVICE_OFFLINE(ServiceOfflineMessage::deserialize),
	SERVICE_THANKS(ServiceThanksMessage::deserialize),
	SERVICE_PING(null),
	SERVICE_PONG(null),

	// Private Resolver API
	LIST_SYNC_REQUEST(null),
	LIST_SYNC_COMMIT(null),
	LIST_ADD(ListAddMessage::deserialize),
	LIST_REMOVE(ListRemoveMessage::deserialize),

	/**
	 * This message is an implementation detail and not part of the protocol.
	 * To ensure list correctness post-synchronization, resolvers need to defer
	 * handling of message received during the synchronization process to after
	 * the process is complete. However, we should only handle LIST_ADD and
	 * LIST_REMOVE messages and drop everything else.
	 * This message is sent to ourselves once the synchronization is complete
	 * to mark the ready-state transition inside the incoming datagrams queue.
	 * It is critical for this message to not be dropped by the kernel due to
	 * a full receive buffer in the underlying UDP socket or the resolver will
	 * be stuck in the sync state forever.
	 */
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
