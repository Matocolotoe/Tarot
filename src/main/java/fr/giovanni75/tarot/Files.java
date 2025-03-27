package fr.giovanni75.tarot;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import fr.giovanni75.tarot.objects.Player;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class Files {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

	private static void addZipEntry(ZipOutputStream zip, String path) {
		path = "data/" + path + ".json";
		File target = new File(path);
		try (FileInputStream fis = new FileInputStream(target)) {
			ZipEntry entry = new ZipEntry(target.getName()); // Avoid messing up paths
			zip.putNextEntry(entry);
			byte[] buffer = new byte[1024];
			int read;
			while ((read = fis.read(buffer)) >= 0)
				zip.write(buffer, 0, read);
		} catch (IOException e) {
			throw new RuntimeException("Could not add " + path + " to backup ZIP", e);
		}
	}

	public static void createBackup() {
		String backupDate = DATE_FORMAT.format(new Date(System.currentTimeMillis()));
		File backupTarget;
		int backupIndex = 1;
		while ((backupTarget = new File("data/backups/backup_" + backupDate + "_" + backupIndex + ".zip")).exists())
			backupIndex++;
		try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(backupTarget))) {
			for (DateRecord date : Tarot.ALL_GAMES.keySet())
				addZipEntry(zip, "games/games_" + date.getShortName("_"));
			addZipEntry(zip, "players");
		} catch (IOException e) {
			throw new RuntimeException("Could not create backup", e);
		}
	}

	static void createDirectory(String dataPath) {
		File directory = new File("data/" + dataPath);
		if (!directory.exists())
			if (!directory.mkdir())
				throw new RuntimeException("Could not create directory " + dataPath);
	}

	static void createJsonFile(String fileName) {
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

	public static void createLeaderboards(DateRecord date) {
		createDirectory("leaderboards/" + date.year());
		final List<Player> fiveLeaderboard = getLeaderboard(date, 5);
		final List<Player> fourLeaderboard = getLeaderboard(date, 4);
		final List<Player> threeLeaderboard = getLeaderboard(date, 3);
		int fiveSize = fiveLeaderboard.size();
		int fourSize = fourLeaderboard.size();
		int threeSize = threeLeaderboard.size();
		int limit = Math.max(fiveSize, Math.max(fourSize, threeSize));
		try {
			File file = new File("data/leaderboards/" + date.year() + "/" + date.month().getName() + ".csv");
			FileWriter writer = new FileWriter(file);
			writer.write("Score à 5,,,Score à 4,,,Score à 3,\n");
			for (int i = 0; i < limit; i++) {
				writer.write(i < fiveSize ? getLeaderboardEntry(fiveLeaderboard.get(i), date, 5) : ",,,");
				writer.write(i < fourSize ? getLeaderboardEntry(fourLeaderboard.get(i), date, 4) : ",,,");
				writer.write(i < threeSize ? getLeaderboardEntry(threeLeaderboard.get(i), date, 3) : ",,,");
				writer.write("\n");
			}
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException("Could not create leaderboard for month " + date.month().name() + " and year " + date.year(), e);
		}
	}

	static JsonArray getJsonArrayFromFile(File file) {
		JsonArray array;
		try (FileInputStream is = new FileInputStream(file)) {
			array = (JsonArray) JsonParser.parseReader(new InputStreamReader(is, StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new RuntimeException("Could not read array from " + file.getName() + ".json", e);
		}
		return array;
	}

	public static JsonArray getJsonArrayFromFile(String fileName) {
		return getJsonArrayFromFile(new File("data/" + fileName + ".json"));
	}

	private static List<Player> getLeaderboard(DateRecord date, int players) {
		final List<Player> leaderboard = new ArrayList<>();
		for (Player player : Tarot.ORDERED_PLAYERS)
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

	private static int getScore(Player player, DateRecord date, int players) {
		return player.getStats(date, players).totalScore;
	}

}
