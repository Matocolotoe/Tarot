package fr.giovanni75.tarot.objects;

import com.google.gson.JsonObject;
import fr.giovanni75.tarot.DateRecord;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Player implements Serializable {

	private final String name;
	private final UUID uuid;

	private final Map<DateRecord, Integer> scoreFivePlayers = new HashMap<>();
	private final Map<DateRecord, Integer> scoreFourPlayers = new HashMap<>();
	private final Map<DateRecord, Integer> scoreThreePlayers = new HashMap<>();

	public Player(String name, UUID uuid) {
		this.name = name;
		this.uuid = uuid;
	}

	void addScore(DateRecord date, int score, int players) {
		Map<DateRecord, Integer> map = getMap(players);
		map.put(date, map.getOrDefault(date, 0) + score);
	}

	private Map<DateRecord, Integer> getMap(int players) {
		return switch (players) {
			case 3 -> scoreThreePlayers;
			case 4 -> scoreFourPlayers;
			case 5 -> scoreFivePlayers;
			default -> throw new IllegalArgumentException("Score is unavailable for " + players + " players");
		};
	}

	public String getName() {
		return name;
	}

	public int getScore(DateRecord date, int players) {
		return getMap(players).getOrDefault(date, 0);
	}

	public UUID getUniqueID() {
		return uuid;
	}

	@Override
	public JsonObject toJson() {
		JsonObject object = new JsonObject();
		object.addProperty("name", name);
		object.addProperty("uuid", uuid.toString());
		return object;
	}

}
