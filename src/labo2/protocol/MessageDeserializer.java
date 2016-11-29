package labo2.protocol;

import java.io.DataInputStream;
import java.io.IOException;

@FunctionalInterface
public interface MessageDeserializer {
	Message deserialize(DataInputStream input) throws IOException;
}
