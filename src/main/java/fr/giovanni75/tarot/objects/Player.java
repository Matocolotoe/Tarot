package fr.giovanni75.tarot.objects;

import com.google.gson.JsonObject;
import fr.giovanni75.tarot.DateRecord;
import fr.giovanni75.tarot.Tarot;
import fr.giovanni75.tarot.stats.LocalStats;

import java.util.HashMap;
import java.util.Map;

public class Player implements Comparable<Player>, Serializable {

	private final int id;
	private final String name;

	private final Map<DateRecord, LocalStats> statsFivePlayers = new HashMap<>();
	private final Map<DateRecord, LocalStats> statsFourPlayers = new HashMap<>();
	private final Map<DateRecord, LocalStats> statsThreePlayers = new HashMap<>();

	public Player(int id, String name) {
		this.id = id;
		this.name = name;
		for (DateRecord record : Tarot.ALL_GAMES.keySet()) {
			statsFivePlayers.put(record, new LocalStats());
			statsFourPlayers.put(record, new LocalStats());
			statsThreePlayers.put(record, new LocalStats());
		}
	}

	@Override
	public int compareTo(Player other) {
		return name.compareTo(other.name);
	}

	public int getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public LocalStats getStats(DateRecord date, int players) {
		Map<DateRecord, LocalStats> stats = switch (players) {
			case 3 -> statsThreePlayers;
			case 4 -> statsFourPlayers;
			case 5 -> statsFivePlayers;
			default -> throw new IllegalArgumentException("Local stats are unavailable for " + players + " players");
		};
		return stats.getOrDefault(date, new LocalStats());
	}

	@Override
	public JsonObject toJson() {
		JsonObject object = new JsonObject();
		object.addProperty("id", id);
		object.addProperty("name", name);
		return object;
	}

}
