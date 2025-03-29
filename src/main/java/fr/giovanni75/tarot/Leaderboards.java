package fr.giovanni75.tarot;

import fr.giovanni75.tarot.objects.Player;
import org.dhatim.fastexcel.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

class Leaderboards {

	private static final DecimalFormat PERCENTAGE_DECIMAL_FORMAT = new DecimalFormat("#0.0%");

	private record IndividiualEntry(Player player, Number value) implements Comparable<IndividiualEntry> {

		@Override
		public int compareTo(IndividiualEntry other) {
			return Double.compare(value.doubleValue(), other.value.doubleValue());
		}

	}

	private enum LeaderboardEntry {

		TOTAL_SCORE("Score", "Score total", true,
				stats -> stats.totalScore,
				(Player.LocalStats stats, Number value) -> value.toString()),

		PLAYED_GAMES("Jouées", "Parties jouées", true,
				stats -> Maps.sum(stats.playedGames),
				(Player.LocalStats stats, Number value) -> value.toString()),

		SUCCESSFUL_TAKES("Réussies", null, false,
				stats -> Maps.sum(stats.successfulTakes),
				(Player.LocalStats stats, Number value) -> value + "/" + (value.intValue() + Maps.sum(stats.failedTakes))),

		WIN_RATE("Réussite", "Taux de réussite", true,
				stats -> {
					int successful = Maps.sum(stats.successfulTakes);
					int total = successful + Maps.sum(stats.failedTakes);
					return total == 0 ? -1 : (double) successful / total;
				},
				(Player.LocalStats stats, Number value) -> PERCENTAGE_DECIMAL_FORMAT.format(value));

		private final String name;
		private final String fullName;
		private final boolean includeInLeaderboards;
		private final Function<Player.LocalStats, Number> valueResolver;
		private final BiFunction<Player.LocalStats, Number, String> valueDisplayer;

		LeaderboardEntry(String name, String fullName, boolean includeInLeaderboards,
						 Function<Player.LocalStats, Number> valueResolver,
						 BiFunction<Player.LocalStats, Number, String> valueDisplayer) {
			this.name = name;
			this.fullName = fullName;
			this.includeInLeaderboards = includeInLeaderboards;
			this.valueResolver = valueResolver;
			this.valueDisplayer = valueDisplayer;
		}

		String getDisplay(DateRecord date, Player player, int players, Number value) {
			return valueDisplayer.apply(player.getStats(date, players), value);
		}

		Number getValue(DateRecord date, Player player, int players) {
			// Used for sorting only, will not be displayed directly
			return valueResolver.apply(player.getStats(date, players));
		}

	}

	private static final LeaderboardEntry[] LEADERBOARD_ENTRIES = LeaderboardEntry.values();

	private static final String FONT_NAME = "Arial";
	private static final int FONT_SIZE = 10;

	private static final double COLUMN_WIDTH = 12.5;
	private static final int MAX_COLUMN_NUMBER = 16;

	static void createScoreGrid(int year) {
		Set<DateRecord> dates = new TreeSet<>();
		for (DateRecord date : Tarot.ALL_GAMES.keySet())
			if (date.year() == year)
				dates.add(date);

		// Create one file per year, with one sheet per month inside it
		try (FileOutputStream os = new FileOutputStream("data/leaderboards/Score tarot " + year + ".xlsx");
			 Workbook wb = new Workbook(os, "Tarot", null)) {
			wb.setGlobalDefaultFont(FONT_NAME, FONT_SIZE);
			for (DateRecord date : dates)
				// Start at 5 players, then recursive calls will follow for 4 and 3 players
				createLeaderboards(date, 5, wb.newWorksheet(date.month().getName()), 0);
		} catch (IOException e) {
			throw new RuntimeException("Could not create leaderboard for year " + year, e);
		}
	}

	private static void createLeaderboards(DateRecord date, int players, Worksheet ws, int initialRow) {
		// We're done
		if (players < 3)
			return;

		final List<Player> playerList = new ArrayList<>();
		final Map<LeaderboardEntry, List<IndividiualEntry>> playerEntries = new EnumMap<>(LeaderboardEntry.class);
		final Map<LeaderboardEntry, List<IndividiualEntry>> playerLeaderboards = new EnumMap<>(LeaderboardEntry.class);

		for (Player player : Tarot.ORDERED_PLAYERS)
			if (Maps.sum(player.getStats(date, players).playedGames) != 0)
				playerList.add(player);

		for (LeaderboardEntry entry : LEADERBOARD_ENTRIES) {
			List<IndividiualEntry> entries = new ArrayList<>();
			for (Player player : playerList) {
				Number value = entry.getValue(date, player, players);
				entries.add(new IndividiualEntry(player, value));
			}
			// Store entries that will be displayed in the left column, sorted by player names
			entries.sort(Comparator.comparing(individualEntry -> individualEntry.player.getName()));
			playerEntries.put(entry, entries);
			// Store entries that will be displayed in the leaderboards on the right, only if needed
			if (entry.includeInLeaderboards) {
				// Copy to avoid modifying the previous reference
				List<IndividiualEntry> copy = new ArrayList<>(entries);
				copy.sort(Comparator.reverseOrder());
				playerLeaderboards.put(entry, copy);
			}
		}

		int lastRow = Leaderboards.writeLeaderboards(date, players, playerList, playerEntries, playerLeaderboards, ws, initialRow);
		createLeaderboards(date, players - 1, ws, lastRow + 10);
	}

	private static int writeLeaderboards(DateRecord date, int players, List<Player> playerList,
										 Map<LeaderboardEntry, List<IndividiualEntry>> playerEntries,
										 Map<LeaderboardEntry, List<IndividiualEntry>> playerLeaderboards,
										 Worksheet ws, int initialRow) {
		/* Column width */
		int column;
		for (column = 0; column <= MAX_COLUMN_NUMBER; column++)
			ws.width(column, COLUMN_WIDTH);

		/* Header */
		ws.value(initialRow, 0, "Tarot à " + players);
		ws.range(initialRow, 0, initialRow, MAX_COLUMN_NUMBER).style()
				.bold().fontColor(Color.RED).fontSize(13)
				.horizontalAlignment("center")
				.verticalAlignment("center")
				.merge().set();

		/* Player names */
		int row = initialRow + 2;
		for (Player player : playerList) {
			ws.value(row, 0, player.getName());
			row++;
		}
		ws.range(initialRow + 2, 0, row - 1, 0).style()
				.horizontalAlignment("center")
				.verticalAlignment("center")
				.set();

		/* Individual entries, player names sorted alphabetically */
		column = 1;
		for (var mapEntry : playerEntries.entrySet()) {
			LeaderboardEntry entry = mapEntry.getKey();
			ws.value(initialRow + 1, column, entry.name);
			ws.style(initialRow + 1, column).bold()
					.horizontalAlignment("center")
					.verticalAlignment("center")
					.set();

			row = initialRow + 2;
			for (IndividiualEntry individualEntry : mapEntry.getValue()) {
				ws.value(row, column, entry.getDisplay(date, individualEntry.player, players, individualEntry.value));
				row++;
			}

			ws.range(initialRow + 2, column, row - 1, column).style()
					.horizontalAlignment("center")
					.verticalAlignment("center")
					.set();

			column++;
		}

		/* Leaderboards */
		column += 2;
		row = initialRow + 1;
		for (var mapEntry : playerLeaderboards.entrySet()) {
			LeaderboardEntry entry = mapEntry.getKey();
			ws.value(initialRow + 1, column, entry.fullName);
			ws.range(initialRow + 1, column, initialRow + 1, column + 1).style()
					.bold()
					.horizontalAlignment("center")
					.verticalAlignment("center")
					.borderStyle(BorderStyle.THIN)
					.merge().set();

			int place = 1;
			row = initialRow + 2;
			for (IndividiualEntry individualEntry : mapEntry.getValue()) {
				ws.value(row, column - 1, place);
				ws.value(row, column, individualEntry.player.getName());
				ws.value(row, column + 1, entry.getDisplay(date, individualEntry.player, players, individualEntry.value));
				place++;
				row++;
			}

			// Horizontal alignment is right by default for numbers, keep it that way
			ws.range(initialRow + 1, column - 1, row - 1, column - 1).style()
					.fontSize(7)
					.verticalAlignment("center")
					.set();

			// Horizontal alignment is left by default, keep it that way
			ws.range(initialRow + 2, column, row - 1, column).style()
					.borderStyle(BorderStyle.THIN)
					.verticalAlignment("center")
					.set();

			// Force right alignment since we are displaying strings
			ws.range(initialRow + 2, column + 1, row - 1, column + 1).style()
					.borderStyle(BorderStyle.THIN)
					.horizontalAlignment("right")
					.verticalAlignment("center")
					.set();

			column += 4;
		}

		return row + 8;
	}

}
