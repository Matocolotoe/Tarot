package fr.giovanni75.tarot.objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.giovanni75.tarot.DateRecord;
import fr.giovanni75.tarot.Tarot;
import fr.giovanni75.tarot.stats.LocalStats;

import java.util.HashMap;
import java.util.Map;

public class Player implements Comparable<Player>, Serializable {

	private final int id;
	private final String name;
	private final Map<DateRecord, String> monthlyNicknames; // Overriden by yearly nicknames
	private final Map<Integer, String> yearlyNicknames;

	private final Map<DateRecord, LocalStats> statsFivePlayers = new HashMap<>();
	private final Map<DateRecord, LocalStats> statsFourPlayers = new HashMap<>();
	private final Map<DateRecord, LocalStats> statsThreePlayers = new HashMap<>();

	public Player(int id, String name, Map<DateRecord, String> monthlyNicknames, Map<Integer, String> yearlyNicknames) {
		this.id = id;
		this.name = name;
		this.monthlyNicknames = monthlyNicknames;
		this.yearlyNicknames = yearlyNicknames;
	}

	@Override
	public int compareTo(Player other) {
		return name.compareTo(other.name);
	}

	public Player copy() {
		Player player = new Player(-id, name, null, null);
		for (DateRecord date : Tarot.ALL_GAMES.keySet())
			player.createStats(date);
		return player;
	}

	public void createStats(DateRecord date) {
		statsFivePlayers.put(date, new LocalStats());
		statsFourPlayers.put(date, new LocalStats());
		statsThreePlayers.put(date, new LocalStats());
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Player && id == ((Player) other).id;
	}

	public int getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getNickname(DateRecord date) {
		return monthlyNicknames.getOrDefault(date, name);
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

		JsonArray nicknamesArray;
		if (!monthlyNicknames.isEmpty()) {
			nicknamesArray = new JsonArray(monthlyNicknames.size());
			for (var entry : monthlyNicknames.entrySet()) {
				JsonObject nickname = new JsonObject();
				nickname.addProperty("date", entry.getKey().hashCode());
				nickname.addProperty("nick", entry.getValue());
				nicknamesArray.add(nickname);
			}
			object.add("monthNicks", nicknamesArray);
		}

		if (!yearlyNicknames.isEmpty()) {
			nicknamesArray = new JsonArray(yearlyNicknames.size());
			for (var entry : yearlyNicknames.entrySet()) {
				JsonObject nickname = new JsonObject();
				nickname.addProperty("year", entry.getKey());
				nickname.addProperty("nick", entry.getValue());
				nicknamesArray.add(nickname);
			}
			object.add("yearNicks", nicknamesArray);
		}

		return object;
	}

}
