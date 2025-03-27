package fr.giovanni75.tarot;

import com.formdev.flatlaf.FlatLightLaf;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.giovanni75.tarot.frames.FrameMainMenu;
import fr.giovanni75.tarot.objects.Game;
import fr.giovanni75.tarot.objects.Player;

import java.io.File;
import java.util.*;

public final class Tarot {

	public static final List<Player> ORDERED_PLAYERS = new ArrayList<>();
	public static final List<String> PLAYER_NAMES = new ArrayList<>();
	public static final Map<DateRecord, List<Game>> ALL_GAMES = new TreeMap<>();
	public static final String NONE_STRING = "—";

	private static final Map<Integer, Player> PLAYER_ID_MAP = new HashMap<>();
	private static final Map<String, Player> PLAYER_NAME_MAP = new HashMap<>();

	public static Player addPlayer(int id, String name) {
		Player player = new Player(id, name);
		ORDERED_PLAYERS.add(player);
		PLAYER_NAMES.add(name);
		PLAYER_ID_MAP.put(id, player);
		PLAYER_NAME_MAP.put(name, player);
		return player;
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

		File[] gameFiles = new File("data/games").listFiles();
		if (gameFiles != null) {
			for (File file : gameFiles) {
				JsonArray games = Files.getJsonArrayFromFile(file);
				int size = games.size();
				for (int i = 0; i < size; i++) {
					Game game = new Game(games.get(size - i - 1).getAsJsonObject());
					ALL_GAMES.computeIfAbsent(game.date, key -> new ArrayList<>()).add(game);
				}
			}
		}

		JsonArray players = Files.getJsonArrayFromFile("players");
		int size = players.size();
		for (int i = 0; i < size; i++) {
			JsonObject object = players.get(i).getAsJsonObject();
			int id = object.get("id").getAsInt();
			String name = object.get("name").getAsString();
			addPlayer(id, name);
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
