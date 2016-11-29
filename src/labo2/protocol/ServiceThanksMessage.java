package labo2.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ServiceThanksMessage extends Message {
	public MessageType type() { return MessageType.SERVICE_THANKS; }

	public final boolean retry;

	public ServiceThanksMessage(boolean retry) {
		this.retry = retry;
	}

	void serialize(DataOutputStream output) throws IOException {
		output.writeBoolean(retry);
	}

	static ServiceThanksMessage deserialize(DataInputStream input) throws IOException {
		return new ServiceThanksMessage(input.readBoolean());
	}
}
