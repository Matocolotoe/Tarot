package fr.giovanni75.tarot;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.giovanni75.tarot.enums.Month;
import fr.giovanni75.tarot.stats.Leaderboards;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class Files {

	private static final int BUFFER_SIZE = 1024;

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

	private static void addZipEntry(ZipOutputStream zip, String path) {
		path = getJsonPath(path);
		File target = new File(path);
		try (FileInputStream is = new FileInputStream(target)) {
			ZipEntry entry = new ZipEntry(target.getName()); // Avoid messing up paths
			zip.putNextEntry(entry);
			byte[] buffer = new byte[BUFFER_SIZE];
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
				addZipEntry(zip, date.getFileName());
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
		File file = new File(getJsonPath(fileName));
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

	static DateRecord getDateFromFile(File file) {
		String name = file.getName();

		// Expected format :
		// split[0] = "games"
		// split[1] = YYYY
		// split[2] = MM.json
		String[] split = name.split("_");
		if (split.length != 3 || !split[0].equals("games"))
			throw new IllegalArgumentException("Invalid format for game file " + name + " (regex check failed)");

		try {
			int year = Integer.parseInt(split[1]);
			int month = Integer.parseInt(split[2].substring(0, 2));
			return new DateRecord(Month.ALL_MONTHS[month - 1], year);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid format for game file " + name + " (number parsing failed)");
		}
	}

	static JsonArray getJsonArrayFromFile(File file) {
		try (FileInputStream is = new FileInputStream(file)) {
			return (JsonArray) JsonParser.parseReader(new InputStreamReader(is, StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new RuntimeException("Could not read array from " + file.getName(), e);
		}
	}

	public static JsonArray getJsonArrayFromFile(String fileName) {
		createJsonFile(fileName); // File might not exist, for instance when serializing data
		return getJsonArrayFromFile(new File(getJsonPath(fileName)));
	}

	private static String getJsonPath(String fileName) {
		return "data/" + fileName + ".json";
	}

	static void forEachJson(JsonArray array, Consumer<JsonObject> consumer) {
		int size = array.size();
		for (int i = 0; i < size; i++)
			consumer.accept(array.get(i).getAsJsonObject());
	}

	static void forEachJson(JsonElement element, Consumer<JsonObject> consumer) {
		if (element != null)
			forEachJson(element.getAsJsonArray(), consumer);
	}

	public static void write(String fileName, JsonArray array) {
		try {
			FileWriter writer = new FileWriter(getJsonPath(fileName));
			writer.write(array.toString());
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException("Could not write to " + fileName + ".json", e);
		}
	}

}
