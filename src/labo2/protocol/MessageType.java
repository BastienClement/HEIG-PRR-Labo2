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
	/**
	 * Registration request from a service to a resolver.
	 */
	SERVICE_REGISTER(ServiceRegisterMessage::deserialize),

	/**
	 * Acknowledgment that the registration is successfully completed.
	 */
	SERVICE_REGISTERED(null),

	/**
	 * Request of a service from a client to a resolver.
	 */
	SERVICE_REQUEST(ServiceRequestMessage::deserialize),

	/**
	 * Offer of service from a resolver to a client.
	 */
	SERVICE_OFFER(ServiceOfferMessage::deserialize),

	/**
	 * Notification from a client to a resolver to indicates that a
	 * service is no longer available.
	 */
	SERVICE_OFFLINE(ServiceOfflineMessage::deserialize),

	/**
	 * Response from the resolver to the SERVICE_OFFLINE message.
	 * Indicates if the client should try again using the same address.
	 */
	SERVICE_THANKS(ServiceThanksMessage::deserialize),

	/**
	 * Requests from a resolver to a serviec agent to test service status.
	 */
	SERVICE_PING(null),

	/**
	 * Response from a service agent if a service is still alive.
	 */
	SERVICE_PONG(null),

	// Private Resolver API
	/**
	 * Sent by a resolver to another resolver to request list synchronization.
	 */
	LIST_SYNC_REQUEST(null),

	/**
	 * Sent by a resolver after the list synchronization process is complete.
	 */
	LIST_SYNC_COMMIT(null),

	/**
	 * Sent by a resolver to other resolvers when an entry is added to the list.
	 */
	LIST_ADD(ListAddMessage::deserialize),

	/**
	 * Sent by a resolver to other resolvers when an entry is removed from the list.
	 */
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
