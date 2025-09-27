package fr.giovanni75.tarot.objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.giovanni75.tarot.DateRecord;
import fr.giovanni75.tarot.Files;
import fr.giovanni75.tarot.Maps;
import fr.giovanni75.tarot.Tarot;
import fr.giovanni75.tarot.stats.LocalStats;

import java.util.HashMap;
import java.util.Map;

public class Player implements Comparable<Player>, Serializable {

	private final int id;
	private final String name;

	private final Map<DateRecord, String> monthlyNicknames;
	private final Map<Integer, String> yearlyNicknames; // Overriden by monthly nicknames if they are present

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

	public void edit() {
		JsonArray array = Files.getJsonArrayFromFile("players");
		array.set(id - 1, toJson());
		Files.write("players", array);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Player && id == ((Player) other).id;
	}

	public String getDisplayName(DateRecord date) {
		return getNickname(date, yearlyNicknames.getOrDefault(date.year(), name));
	}

	public int getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getNickname(DateRecord date, String defaultValue) {
		return monthlyNicknames.getOrDefault(date, defaultValue);
	}

	public String getNickname(int year, String defaultValue) {
		return yearlyNicknames.getOrDefault(year, defaultValue);
	}

	public int getPlayedGames(DateRecord date, int players) {
		LocalStats stats = getStats(date, players);
		return Maps.sum(stats.playedGames);
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

	public void setNickname(DateRecord date, String nickname) {
		if (nickname == null) {
			monthlyNicknames.remove(date);
		} else {
			monthlyNicknames.put(date, nickname);
		}
	}

	public void setNickname(int year, String nickname) {
		if (nickname == null) {
			yearlyNicknames.remove(year);
		} else {
			yearlyNicknames.put(year, nickname);
		}
	}

	@Override
	public JsonObject toJson() {
		JsonObject object = new JsonObject();
		object.addProperty("id", id);
		object.addProperty("name", name);

		JsonArray nicknamesArray;
		if (monthlyNicknames != null && !monthlyNicknames.isEmpty()) {
			nicknamesArray = new JsonArray(monthlyNicknames.size());
			for (var entry : monthlyNicknames.entrySet()) {
				JsonObject nickname = new JsonObject();
				nickname.addProperty("date", entry.getKey().hashCode());
				nickname.addProperty("nick", entry.getValue());
				nicknamesArray.add(nickname);
			}
			object.add("monthNicks", nicknamesArray);
		}

		if (yearlyNicknames != null && !yearlyNicknames.isEmpty()) {
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
