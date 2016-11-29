package labo2.protocol;

import java.io.*;

/**
 * An abstract message reprenting a generic message
 * <p>
 * This class is used to send and receive message. It offers the possibilities to serialize a message to
 * byte[] (serialize) and to parse byte[] to Message (parse)
 */
public abstract class Message {
	public abstract MessageType type();
	abstract void serialize(DataOutputStream output) throws IOException;

	public static Message parse(byte[] buffer, int offset, int length) throws IOException {
		DataInputStream input = new DataInputStream(new ByteArrayInputStream(buffer, offset, length));
		MessageType type = MessageType.values()[input.readByte()];
		Message message = type.deserialize(input);
		input.close();
		return message;
	}

	public static byte[] serialize(Message message) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutputStream output = new DataOutputStream(buffer);
		output.writeByte(message.type().ordinal());
		message.serialize(output);
		byte[] data = buffer.toByteArray();
		output.close();
		buffer.close();
		return data;
	}
}
