package fr.giovanni75.tarot;

import com.formdev.flatlaf.FlatLightLaf;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.giovanni75.tarot.frames.FrameMainMenu;
import fr.giovanni75.tarot.objects.Game;
import fr.giovanni75.tarot.objects.Player;
import fr.giovanni75.tarot.stats.GlobalStats;

import java.io.File;
import java.util.*;

public final class Tarot {

	private static final Map<DateRecord, GlobalStats> GLOBAL_STATS_FIVE = new HashMap<>();
	private static final Map<DateRecord, GlobalStats> GLOBAL_STATS_FOUR = new HashMap<>();
	private static final Map<DateRecord, GlobalStats> GLOBAL_STATS_THREE = new HashMap<>();

	private static final Map<Integer, Player> PLAYER_ID_MAP = new HashMap<>();
	private static final Map<String, Player> PLAYER_NAME_MAP = new HashMap<>();

	public static final List<Player> ORDERED_PLAYERS = new ArrayList<>();
	public static final List<String> PLAYER_NAMES = new ArrayList<>();
	public static final Map<DateRecord, List<Game>> ALL_GAMES = new TreeMap<>();
	public static final String NONE_STRING = "—";

	public static Player addPlayer(int id, String name) {
		Player player = new Player(id, name);
		ORDERED_PLAYERS.add(player);
		PLAYER_NAMES.add(name);
		PLAYER_ID_MAP.put(id, player);
		PLAYER_NAME_MAP.put(name, player);
		return player;
	}

	public static GlobalStats getGlobalStats(DateRecord date, int players) {
		Map<DateRecord, GlobalStats> stats = switch (players) {
			case 3 -> GLOBAL_STATS_THREE;
			case 4 -> GLOBAL_STATS_FOUR;
			case 5 -> GLOBAL_STATS_FIVE;
			default -> throw new IllegalArgumentException("Global stats are unavailable for " + players + " players");
		};
		return stats.getOrDefault(date, new GlobalStats());
	}

	public static Player getPlayer(int id) {
		return PLAYER_ID_MAP.get(id);
	}

	public static Player getPlayer(String name) {
		return PLAYER_NAME_MAP.get(name);
	}

	public static void main(String[] args) {
		Files.createDirectory(""); // Create data directory

		Files.createDirectory("backups");
		Files.createDirectory("games");
		Files.createDirectory("leaderboards");
		Files.createJsonFile("players");

		// Load players before games to allow proper name reordering in games
		JsonArray players = Files.getJsonArrayFromFile("players");
		int size = players.size();
		for (int i = 0; i < size; i++) {
			JsonObject object = players.get(i).getAsJsonObject();
			int id = object.get("id").getAsInt();
			String name = object.get("name").getAsString();
			addPlayer(id, name);
		}

		File[] gameFiles = new File("data/games").listFiles();
		if (gameFiles != null) {
			for (File file : gameFiles) {
				DateRecord date = Files.getDateFromFile(file);
				JsonArray games = Files.getJsonArrayFromFile(file);
				List<Game> gameList = new ArrayList<>();
				for (JsonElement gameElement : games)
					gameList.add(new Game(gameElement.getAsJsonObject(), date));
				ALL_GAMES.put(date, gameList);
				for (Player player : ORDERED_PLAYERS)
					player.createStats(date); // Must be done after games are scanned and before game results are applied
			}
		}

		for (DateRecord date : ALL_GAMES.keySet()) {
			GLOBAL_STATS_FIVE.put(date, new GlobalStats());
			GLOBAL_STATS_FOUR.put(date, new GlobalStats());
			GLOBAL_STATS_THREE.put(date, new GlobalStats());
		}

		for (List<Game> list : ALL_GAMES.values())
			for (Game game : list)
				game.applyResults();

		ORDERED_PLAYERS.sort(Player::compareTo);
		PLAYER_NAMES.sort(String::compareTo);
		PLAYER_NAMES.addFirst(NONE_STRING);

		FlatLightLaf.setup();
		new FrameMainMenu();
	}

}
