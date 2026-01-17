package fr.giovanni75.tarot.stats;

import fr.giovanni75.tarot.*;
import fr.giovanni75.tarot.enums.Contract;
import fr.giovanni75.tarot.enums.Nameable;
import fr.giovanni75.tarot.objects.Player;
import org.dhatim.fastexcel.BorderStyle;
import org.dhatim.fastexcel.Color;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;

public final class Leaderboards {

	private static final DecimalFormat PERCENTAGE_DECIMAL_FORMAT = new DecimalFormat("#0.0%");

	private static final int WINRATE_MINIMUM_TAKES = 3;
	private static final String NONE_STRING = "–";

	private enum GlobalData {

		TAKES("Prises", false, false, false, stats -> stats.contracts),
		OUDLERS("Bouts en moyenne", true, false, false, stats -> stats.oudlers),
		SELF_CALLS("Appels à soi-même", false, false, true, stats -> stats.selfCalls),
		HANDFULS("Poignées", false, true, false, stats -> stats.handfuls),
		MISERIES("Misères", false, true, false, stats -> stats.miseries),
		PETITS("Petits au bout", false, false, false, stats -> stats.petits);

		private final String header;
		private final boolean averaged;
		private final boolean pluralizeKey;
		private final boolean fivePlayersOnly;
		private final Function<GlobalStats, Map<? extends Nameable, Integer>> resolver;

		GlobalData(String header, boolean averaged, boolean pluralizeKey, boolean fivePlayersOnly, Function<GlobalStats, Map<? extends Nameable, Integer>> resolver) {
			this.header = header;
			this.averaged = averaged;
			this.pluralizeKey = pluralizeKey;
			this.fivePlayersOnly = fivePlayersOnly;
			this.resolver = resolver;
		}

	}

	private enum PlayerData {

		TOTAL_SCORE("Score", "Score total", false, stats -> stats.totalScore, Object::toString),

		PLAYED_GAMES("Jouées", "Parties jouées", false, stats -> Maps.sum(stats.playedGames), Object::toString),

		CALLED_TIMES("Appelé·e", null, true,
				stats -> Maps.sum(stats.calledTimes),
				value -> value.intValue() == 0 ? NONE_STRING : value + " fois"),

		SELF_CALLED_TIMES("Seul·e", null, true,
				stats -> Maps.sum(stats.selfCalls),
				value -> value.intValue() == 0 ? NONE_STRING : value + " fois"),

		TAKES("Prises", null, false,
				stats -> {
					int successes = Maps.sum(stats.successfulTakes);
					int total = successes + Maps.sum(stats.failedTakes);
					return total == 0 ? -1 : new Fraction(successes, total);
				}, value -> value.intValue() == -1 ? NONE_STRING : value.toString()),

		WIN_RATE("Réussite", "Taux de réussite", false,
				stats -> {
					int successes = Maps.sum(stats.successfulTakes);
					int total = successes + Maps.sum(stats.failedTakes);
					return total < WINRATE_MINIMUM_TAKES ? -1 : new Fraction(successes, total);
				},
				value -> value.intValue() == -1 ? "" : PERCENTAGE_DECIMAL_FORMAT.format(value));

		private final String name;
		private final String leaderboardName;
		private final boolean fivePlayersOnly;
		private final Function<LocalStats, Number> valueResolver;
		private final Function<Number, String> valueDisplayer;

		PlayerData(String name, String leaderboardName, boolean fivePlayersOnly, Function<LocalStats, Number> valueResolver, Function<Number, String> valueDisplayer) {
			this.name = name;
			this.leaderboardName = leaderboardName;
			this.fivePlayersOnly = fivePlayersOnly;
			this.valueResolver = valueResolver;
			this.valueDisplayer = valueDisplayer;
		}

		String getDisplay(Number value) {
			return valueDisplayer.apply(value);
		}

		Number getValue(DateRecord date, Player player, int players) {
			// Used for sorting only, will not be displayed directly
			return valueResolver.apply(player.getStats(date, players));
		}

	}

	private record StatsPair(Player player, Number value) implements Comparable<StatsPair> {

		@Override
		public int compareTo(StatsPair other) {
			// Put higher denominators first when win rates are equal
			if (value instanceof Fraction && value.equals(other.value))
				return ((Fraction) value).compareDenominators((Fraction) other.value);
			return Double.compare(value.doubleValue(), other.value.doubleValue());
		}

	}

	private static final GlobalData[] GLOBAL_DATA = GlobalData.values();
	private static final PlayerData[] PLAYER_DATA = PlayerData.values();

	private static final String FONT_NAME = "Arial";
	private static final int FONT_SIZE = 10;

	private static final double COLUMN_WIDTH = 12.5;
	private static final double HEADER_HEIGHT = 20.5;

	private static final int GLOBAL_DATA_ORIGIN = 0;
	private static final int MAX_COLUMN_NUMBER_FIVE;
	private static final int MAX_COLUMN_NUMBER_FOUR_THREE;
	private static final int STRUCTURE_COLUMN_MARGIN = 3;

	static {
		// Player names (starts at 0) + Individiual data + Margin with leaderboards
		int column = PLAYER_DATA.length + 1;
		int fivePlayersOnlyStats = 0;
		for (PlayerData data : PLAYER_DATA) {
			if (data.leaderboardName != null)
				column += STRUCTURE_COLUMN_MARGIN;
			if (data.fivePlayersOnly)
				fivePlayersOnlyStats++;
		}
		MAX_COLUMN_NUMBER_FIVE = column;
		MAX_COLUMN_NUMBER_FOUR_THREE = column - fivePlayersOnlyStats;
	}

	private static void createLeaderboards(DateRecord date, int players, Worksheet ws, int initialRow) {
		// We're done
		if (players < 3)
			return;

		List<Player> playerList = new ArrayList<>();
		for (Player player : Tarot.ORDERED_PLAYERS)
			if (player.getPlayedGames(date, players) > 0)
				playerList.add(player);

		// No stats recorded, just skip
		if (playerList.isEmpty()) {
			createLeaderboards(date, players - 1, ws, initialRow);
			return;
		}

		// Compare against names that will actually be shown in the left column
		// Convert to lowercase to avoid capitalized names being put first
		playerList.sort(Comparator.comparing(player -> player.getDisplayName(date).toLowerCase()));

		Map<PlayerData, List<StatsPair>> unsortedPairs = new EnumMap<>(PlayerData.class);
		Map<PlayerData, List<StatsPair>> sortedPairs = new EnumMap<>(PlayerData.class);
		for (PlayerData data : PLAYER_DATA) {
			List<StatsPair> entries = new ArrayList<>();
			for (Player player : playerList) {
				Number value = data.getValue(date, player, players);
				entries.add(new StatsPair(player, value));
			}
			// Store entries that will be displayed in the left column, sorted by player names/nicknames (same thing for lowercase)
			entries.sort(Comparator.comparing(individualEntry -> individualEntry.player.getDisplayName(date).toLowerCase()));
			unsortedPairs.put(data, entries);
			// Store entries that will be displayed in the leaderboards on the right, only if needed
			if (data.leaderboardName != null) {
				// Copy to avoid modifying the previous reference
				List<StatsPair> copy = new ArrayList<>(entries);
				copy.sort(Comparator.reverseOrder());
				sortedPairs.put(data, copy);
			}
		}

		int lastRow = writeLeaderboards(date, players, playerList, unsortedPairs, sortedPairs, ws, initialRow);
		createLeaderboards(date, players - 1, ws, lastRow + 10);
	}

	public static void createScoreGrid(int year) {
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

	private static int writeLeaderboards(DateRecord date, int players, List<Player> playerList,
										 Map<PlayerData, List<StatsPair>> unsortedPairs,
										 Map<PlayerData, List<StatsPair>> sortedPairs,
										 Worksheet ws, int initialRow) {
		/* Global column width */
		int column;
		int maxColumn = players == 5 ? MAX_COLUMN_NUMBER_FIVE : MAX_COLUMN_NUMBER_FOUR_THREE;
		for (column = 0; column <= maxColumn; column++)
			ws.width(column, COLUMN_WIDTH);

		/* Header */
		ws.rowHeight(initialRow, HEADER_HEIGHT);
		ws.value(initialRow, 0, "Tarot à " + players + " – " + date.getName());
		ws.range(initialRow, 0, initialRow, maxColumn).style()
				.bold().fontColor(Color.RED).fontSize(13)
				.horizontalAlignment("center")
				.verticalAlignment("center")
				.merge().set();

		/* Player names */
		int row = initialRow + 2;
		for (Player player : playerList) {
			ws.value(row, 0, player.getDisplayName(date));
			row++;
		}

		ws.range(initialRow + 2, 0, row - 1, 0).style()
				.horizontalAlignment("center")
				.verticalAlignment("center")
				.set();

		/* Individual entries, player names sorted alphabetically */
		column = 1;
		for (var entry : unsortedPairs.entrySet()) {
			PlayerData data = entry.getKey();
			if (players < 5 && data.fivePlayersOnly)
				continue;

			ws.value(initialRow + 1, column, data.name);
			ws.style(initialRow + 1, column).bold()
					.horizontalAlignment("center")
					.verticalAlignment("center")
					.set();

			row = initialRow + 2;
			for (StatsPair pair : entry.getValue()) {
				ws.value(row, column, data.getDisplay(pair.value));
				row++;
			}

			ws.range(initialRow + 2, column, row - 1, column).style()
					.horizontalAlignment("center")
					.verticalAlignment("center")
					.set();

			column++;
		}

		/* Leaderboards */
		column += STRUCTURE_COLUMN_MARGIN - 1;
		row = initialRow + 1;
		for (var entry : sortedPairs.entrySet()) {
			PlayerData data = entry.getKey();
			if (players < 5 && data.fivePlayersOnly)
				continue;

			ws.value(initialRow + 1, column, data.leaderboardName);
			ws.range(initialRow + 1, column, initialRow + 1, column + 1).style()
					.bold()
					.borderStyle(BorderStyle.THIN)
					.horizontalAlignment("center")
					.verticalAlignment("center")
					.merge().set();

			row = initialRow + 2;
			for (StatsPair pair : entry.getValue()) {
				ws.value(row, column - 1, row - initialRow - 1); // Place in the leaderboard
				ws.value(row, column, pair.player.getDisplayName(date));
				ws.value(row, column + 1, data.getDisplay(pair.value));
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

			column += STRUCTURE_COLUMN_MARGIN;
		}

		/* Global entries */
		GlobalStats stats = Tarot.getGlobalStats(date, players);
		column = GLOBAL_DATA_ORIGIN;
		int globalDataMargin = 0;
		for (GlobalData data : GLOBAL_DATA) {
			if (players < 5 && data.fivePlayersOnly)
				continue;

			ws.value(row + 2, column, data.header);
			ws.range(row + 2, column, row + 2, column + 1).style()
					.bold()
					.horizontalAlignment("center")
					.verticalAlignment("center")
					.merge().set();

			int i = 3;
			var map = data.resolver.apply(stats);
			if (map.size() > globalDataMargin)
				globalDataMargin = map.size();

			for (var entry : map.entrySet()) {
				String name = entry.getKey().getName();
				if (data.pluralizeKey)
					name += "s";
				ws.value(row + i, column, name);
				if (data.averaged) {
					Nameable nameable = entry.getKey();
					if (nameable instanceof Contract contract) {
						double value = (double) entry.getValue();
						if (value == 0) {
							ws.value(row + i, column + 1, NONE_STRING);
						} else {
							ws.value(row + i, column + 1, Utils.format(value / stats.contracts.get(contract)));
						}
					} else {
						throw new IllegalArgumentException("Keys need to extend Contract for averaged data");
					}
				} else {
					ws.value(row + i, column + 1, entry.getValue());
				}
				i++;
			}

			ws.value(row + i, column, "Total");
			ws.style(row + i, column).bold().verticalAlignment("center").set();

			if (data.averaged) {
				ws.value(row + i, column + 1, Utils.format((double) Maps.sum(map) / Maps.sum(stats.contracts)));
			} else {
				ws.value(row + i, column + 1, Maps.sum(map));
			}

			// Horizontal alignment is left by default, keep it that way
			ws.range(row + 3, column, row + i, column).style()
					.verticalAlignment("center")
					.set();

			// Force right alignment since we might be displaying strings
			ws.range(row + 3, column + 1, row + i, column + 1).style()
					.horizontalAlignment("right")
					.verticalAlignment("center")
					.set();

			column += STRUCTURE_COLUMN_MARGIN;
		}

		// Last row for next iteration
		// Keep 2 empty above global data + 2 extra to ensure data sets are separated by 10 empty rows
		return row + globalDataMargin + 4;
	}

}
