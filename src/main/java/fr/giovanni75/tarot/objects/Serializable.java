package fr.giovanni75.tarot.objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.giovanni75.tarot.Files;

import java.io.FileWriter;
import java.io.IOException;

interface Serializable {

	JsonObject toJson();

	default void write(String fileName) {
		JsonArray array = Files.getJsonArrayFromFile(fileName);
		array.add(toJson());
		try {
			FileWriter writer = new FileWriter("data/" + fileName + ".json");
			writer.write(array.toString());
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException("Could not write to " + fileName + ".json", e);
		}
	}

}
