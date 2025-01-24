package fr.giovanni75.tarot;

import com.formdev.flatlaf.FlatLightLaf;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.giovanni75.tarot.frames.FrameMainMenu;
import fr.giovanni75.tarot.objects.Game;
import fr.giovanni75.tarot.objects.Player;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

public final class Tarot {

	public static final List<Player> ORDERED_PLAYERS = new ArrayList<>();
	public static final List<String> PLAYER_NAMES = new ArrayList<>();
	public static final Map<DateRecord, List<Game>> ALL_GAMES = new HashMap<>();
	public static final String NONE_STRING = "—";

	private static final Map<String, Player> PLAYER_NAME_MAP = new HashMap<>();
	private static final Map<UUID, Player> PLAYER_UUID_MAP = new HashMap<>();

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd_MM_yyyy");

	public static Player addPlayer(String name, UUID uuid) {
		Player player = new Player(name, uuid);
		ORDERED_PLAYERS.add(player);
		PLAYER_NAMES.add(name);
		PLAYER_NAME_MAP.put(name, player);
		PLAYER_UUID_MAP.put(uuid, player);
		return player;
	}

	public static void createBackup(String fileName) {
		File original = new File("data/" + fileName + ".json");
		File copy;
		try {
			String date = DATE_FORMAT.format(new Date(System.currentTimeMillis()));
			int backupIndex = 1;
			while ((copy = new File("data/backups/" + fileName + "_" + date + "_" + backupIndex + ".json")).exists())
				backupIndex++;
			Files.copy(original.toPath(), copy.toPath());
		} catch (IOException e) {
			throw new RuntimeException("Could not create backup of " + fileName + ".json", e);
		}
	}

	private static void createDirectory(String dataPath) {
		File directory = new File("data/" + dataPath);
		if (!directory.exists())
			if (!directory.mkdir())
				throw new RuntimeException("Could not create directory " + dataPath);
	}

	private static void createJsonFile(String fileName) {
		File file = new File("data/" + fileName + ".json");
		try {
			if (file.createNewFile()) {
				FileWriter writer = new FileWriter(file);
				writer.write("[]");
				writer.close();
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not initialize " + fileName + ".json", e);
		}
	}

	public static void createLeaderboards() {
		for (DateRecord dateRecord : ALL_GAMES.keySet()) {
			createDirectory("leaderboards/" + dateRecord.year());
			final List<Player> fiveLeaderboard = getLeaderboard(dateRecord, 5);
			final List<Player> fourLeaderboard = getLeaderboard(dateRecord, 4);
			final List<Player> threeLeaderboard = getLeaderboard(dateRecord, 3);
			int fiveSize = fiveLeaderboard.size();
			int fourSize = fourLeaderboard.size();
			int threeSize = threeLeaderboard.size();
			int limit = Math.max(fiveSize, Math.max(fourSize, threeSize));
			try {
				File file = new File("data/leaderboards/" + dateRecord.year() + "/" + dateRecord.month().getName() + ".csv");
				FileWriter writer = new FileWriter(file);
				writer.write("Score à 5,,,Score à 4,,,Score à 3,\n");
				for (int i = 0; i < limit; i++) {
					writer.write(i < fiveSize ? getLeaderboardEntry(fiveLeaderboard.get(i), dateRecord, 5) : ",,,");
					writer.write(i < fourSize ? getLeaderboardEntry(fourLeaderboard.get(i), dateRecord, 4) : ",,,");
					writer.write(i < threeSize ? getLeaderboardEntry(threeLeaderboard.get(i), dateRecord, 3) : ",,,");
					writer.write("\n");
				}
				writer.close();
			} catch (IOException e) {
				throw new RuntimeException("Could not create leaderboard for month " + dateRecord.month().name() + " and year " + dateRecord.year(), e);
			}
		}
	}

	public static JsonArray getJsonArrayFromFile(String fileName) {
		JsonArray array;
		File file = new File("data/" + fileName + ".json");
		try (FileInputStream is = new FileInputStream(file)) {
			array = (JsonArray) JsonParser.parseReader(new InputStreamReader(is, StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new RuntimeException("Could not read array from " + fileName + ".json", e);
		}
		return array;
	}

	private static List<Player> getLeaderboard(DateRecord date, int players) {
		final List<Player> leaderboard = new ArrayList<>();
		for (Player player : ORDERED_PLAYERS)
			if (getScore(player, date, players) != 0)
				leaderboard.add(player);

		leaderboard.sort((p1, p2) -> {
			int result = Integer.compare(getScore(p2, date, players), getScore(p1, date, players));
			return result == 0 ? p1.getName().compareTo(p2.getName()) : result;
		});

		return leaderboard;
	}

	private static String getLeaderboardEntry(Player player, DateRecord date, int players) {
		int score = getScore(player, date, players);
		return score == 0 ? ",,," : player.getName() + "," + score + ",,";
	}

	public static Player getPlayer(String name) {
		return PLAYER_NAME_MAP.get(name);
	}

	public static Player getPlayer(UUID uuid) {
		return PLAYER_UUID_MAP.get(uuid);
	}

	private static int getScore(Player player, DateRecord date, int players) {
		return player.getStats(date, players).totalScore;
	}

	public static void main(String[] args) {
		createDirectory(""); // Create data directory

		createDirectory("backups");
		createDirectory("leaderboards");
		createJsonFile("games");
		createJsonFile("players");

		JsonArray games = getJsonArrayFromFile("games");
		int size = games.size();
		for (int i = 0; i < size; i++) {
			Game game = new Game(games.get(size - i - 1).getAsJsonObject());
			ALL_GAMES.computeIfAbsent(game.getDate(), key -> new ArrayList<>()).add(game);
		}

		JsonArray players = getJsonArrayFromFile("players");
		size = players.size();
		for (int i = 0; i < size; i++) {
			JsonObject object = players.get(i).getAsJsonObject();
			UUID uuid = UUID.fromString(object.get("uuid").getAsString());
			String name = object.get("name").getAsString();
			addPlayer(name, uuid);
		}

		for (List<Game> list : ALL_GAMES.values())
			for (Game game : list)
				game.applyResults();

		ORDERED_PLAYERS.sort(Comparator.comparing(Player::getName));
		PLAYER_NAMES.sort(String::compareTo);
		PLAYER_NAMES.addFirst(NONE_STRING);

		FlatLightLaf.setup();
		new FrameMainMenu();
	}

}
