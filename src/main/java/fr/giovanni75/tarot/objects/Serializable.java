package fr.giovanni75.tarot.objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.giovanni75.tarot.Files;

interface Serializable {

	void edit();

	JsonObject toJson();

	default void write(String fileName) {
		JsonArray array = Files.getJsonArrayFromFile(fileName);
		array.add(toJson());
		Files.write(fileName, array);
	}

}
