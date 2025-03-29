package fr.giovanni75.tarot;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class Files {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

	private static void addZipEntry(ZipOutputStream zip, String path) {
		path = "data/" + path + ".json";
		File target = new File(path);
		try (FileInputStream is = new FileInputStream(target)) {
			ZipEntry entry = new ZipEntry(target.getName()); // Avoid messing up paths
			zip.putNextEntry(entry);
			byte[] buffer = new byte[1024];
			int read;
			while ((read = is.read(buffer)) >= 0)
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

	public static void createLeaderboards() {
		createDirectory("leaderboards");
		Set<Integer> years = new HashSet<>();
		for (DateRecord date : Tarot.ALL_GAMES.keySet())
			years.add(date.year());
		for (int year : years)
			Leaderboards.createScoreGrid(year);
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

}
