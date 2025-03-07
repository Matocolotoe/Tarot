package fr.giovanni75.tarot.frames;

import fr.giovanni75.tarot.DateRecord;
import fr.giovanni75.tarot.Tarot;
import fr.giovanni75.tarot.enums.Contract;
import fr.giovanni75.tarot.enums.PetitAuBout;
import fr.giovanni75.tarot.objects.Game;
import fr.giovanni75.tarot.objects.Player;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

class FrameGlobalStats extends JFrame {

	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

	private static String getNameListDisplay(List<Player> players) {
		List<String> names = new ArrayList<>();
		for (Player player : players)
			names.add(player.getName());
		Collections.sort(names);
		return String.join(", ", names);
	}

	private static void showAverageStats(JPanel panel, DateRecord date, int players, String header, Function<Game, Integer> adder) {
		int matchingAmount = 0;
		int totalAmount = 0;
		Map<Contract, Integer> matchingAmounts = new EnumMap<>(Contract.class);
		Map<Contract, Integer> totalAmounts = new EnumMap<>(Contract.class);
		for (Game game : Tarot.ALL_GAMES.get(date)) {
			if (game.players.length == players) {
				Contract contract = game.contract;
				int amount = adder.apply(game);
				matchingAmounts.put(contract, matchingAmounts.getOrDefault(contract, 0) + 1);
				totalAmounts.put(contract, totalAmounts.getOrDefault(contract, 0) + amount);
				matchingAmount++;
				totalAmount += amount;
			}
		}

		panel.add(Components.getSimpleText(header + " : " + DECIMAL_FORMAT.format((double) totalAmount / matchingAmount), 15));
		for (Contract contract : Contract.ALL_CONTRACTS) {
			Integer amount = matchingAmounts.get(contract);
			if (amount == null) {
				panel.add(Components.getSimpleText(" ‣ " + contract.getName() + " : " + Tarot.NONE_STRING, 15));
			} else {
				panel.add(Components.getSimpleText(" ‣ " + contract.getName() + " : "
						+ DECIMAL_FORMAT.format((double) totalAmounts.get(contract) / amount), 15));
			}
		}

		panel.add(Components.getSimpleText(" ", 20));
	}

	private static void showGlobalStats(JPanel panel, DateRecord date, int players, String header, Predicate<Game> matcher) {
		int totalAmount = 0;
		Map<Contract, Integer> amounts = new EnumMap<>(Contract.class);
		for (Game game : Tarot.ALL_GAMES.get(date)) {
			if (game.players.length == players && matcher.test(game)) {
				Contract contract = game.contract;
				amounts.put(contract, amounts.getOrDefault(contract, 0) + 1);
				totalAmount++;
			}
		}

		panel.add(Components.getSimpleText(header + " : " + totalAmount, 15));
		for (Contract contract : Contract.ALL_CONTRACTS) {
			Integer amount = amounts.get(contract);
			if (amount == null) {
				panel.add(Components.getSimpleText(" ‣ " + contract.getName() + " : " + Tarot.NONE_STRING, 15));
			} else {
				panel.add(Components.getSimpleText(" ‣ " + contract.getName() + " : " + amount + " (" + 100 * amount / totalAmount + "%)", 15));
			}
		}

		panel.add(Components.getSimpleText(" ", 20));
	}

	private static void showMaxPlayerStats(JPanel panel, DateRecord date, int players, String header, String details,
									Function<Player.LocalStats, Map<Contract, Integer>> provider, int multiplier,
									boolean includeTotal, boolean includeDetailsByContract) {
		Map<Contract, Integer> maxAmounts = new EnumMap<>(Contract.class);
		Map<Contract, List<Player>> maxPlayers = new EnumMap<>(Contract.class);
		int maxAmountAll = 0;
		List<Player> maxPlayersAll = new ArrayList<>();
		for (Contract contract : Contract.ALL_CONTRACTS)
			maxPlayers.put(contract, new ArrayList<>());

		for (Player player : Tarot.ORDERED_PLAYERS) {
			int totalAmount = 0;
			for (Contract contract : Contract.ALL_CONTRACTS) {
				int amount = provider.apply(player.getStats(date, players)).getOrDefault(contract, 0);
				totalAmount += amount;
				// Skip calculations if details are not required
				if (!includeDetailsByContract)
					continue;
				int diff = multiplier * (amount - maxAmounts.getOrDefault(contract, 0));
				if (diff >= 0) {
					List<Player> playerList = maxPlayers.get(contract);
					if (diff != 0) {
						// Amount is strictly greater than any value seen before, reset data
						maxAmounts.put(contract, amount);
						playerList.clear();
						playerList.add(player);
					} else if (!playerList.isEmpty()) {
						// Amount equals one that has already been encountered (hence the non-emptiness condition)
						playerList.add(player);
					}
				}
			}
			int diff = multiplier * (totalAmount - maxAmountAll);
			if (diff > 0) {
				// Amount is strictly greater than any value seen before, reset data
				maxAmountAll = totalAmount;
				maxPlayersAll.clear();
				maxPlayersAll.add(player);
			} else if (diff == 0 && maxAmountAll != 0) {
				// Amount equals one that has already been encountered (hence the non-zero condition)
				maxPlayersAll.add(player);
			}
		}

		if (maxPlayersAll.isEmpty())
			return;

		if (includeTotal) {
			panel.add(Components.getSimpleText(header + " : " + String.format(details, getNameListDisplay(maxPlayersAll), maxAmountAll), 15));
		} else {
			panel.add(Components.getSimpleText(header + " :", 15));
		}

		if (!includeDetailsByContract)
			return;

		for (Contract contract : Contract.ALL_CONTRACTS) {
			List<Player> playerList = maxPlayers.get(contract);
			if (playerList == null || playerList.isEmpty()) {
				panel.add(Components.getSimpleText(" ‣ " + contract.getName() + " : " + Tarot.NONE_STRING, 15));
			} else {
				panel.add(Components.getSimpleText(" ‣ " + contract.getName() + " : "
						+ String.format(details, getNameListDisplay(playerList), maxAmounts.getOrDefault(contract, 0)), 15));
			}
		}

		panel.add(Components.getSimpleText(" ", 20));
	}

	FrameGlobalStats(DateRecord date, int players) {
		boolean hasStatsRecorded = false;
		for (Game game : Tarot.ALL_GAMES.get(date)) {
			if (game.players.length == players) {
				hasStatsRecorded = true;
				break;
			}
		}

		if (!hasStatsRecorded) {
			Components.popup("Aucune partie n'est disponible pour cette période.");
			return;
		}

		setBounds(300, 150, 500, 800);
		setResizable(false);
		setTitle("Statistiques – " + players + " joueurs – " + date.getName());

		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(Components.getStandardBorder());
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		add(mainPanel);

		mainPanel.add(Components.getSimpleText("Statistiques générales", 20));
		mainPanel.add(Components.getSimpleText(date.getName() + " – " + players + " joueurs", 20));
		mainPanel.add(Components.getSimpleText(" ", 25));

		showGlobalStats(mainPanel, date, players, "Parties jouées", ignored -> true);
		showAverageStats(mainPanel, date, players, "Nombre moyen de bouts", game -> game.oudlers.ordinal());
		showGlobalStats(mainPanel, date, players, "Petits au bout", game -> game.petitAuBout != PetitAuBout.NONE);

		showMaxPlayerStats(mainPanel, date, players, "Le plus de parties jouées", "%s (%d)",
				stats -> stats.playedGames, 1, true, false);
		showMaxPlayerStats(mainPanel, date, players, "Le plus de poignées", "%s (%d)",
				stats -> stats.handfuls, 1, true, false);
		showMaxPlayerStats(mainPanel, date, players, "Le plus de misères", "%s (%d)",
				stats -> stats.miseries, 1, true, false);

		mainPanel.add(Components.getSimpleText(" ", 20));

		showMaxPlayerStats(mainPanel, date, players, "Le plus de fois appelé·e", "%s, %d fois",
				stats -> stats.calledTimes, 1, true, true);
		showMaxPlayerStats(mainPanel, date, players, "Le plus de prises réussies", "%s (%d)",
				stats -> stats.successfulTakes, 1, true, true);
		showMaxPlayerStats(mainPanel, date, players, "Le plus de prises ratées", "%s (%d)",
				stats -> stats.failedTakes, 1, true, true);
		showMaxPlayerStats(mainPanel, date, players, "Le plus d'appels à soi-même", "%s (%d)",
				stats -> stats.selfCalls, 1, true, true);

		showMaxPlayerStats(mainPanel, date, players, "Meilleurs tours", "%s (%d pts)",
				stats -> stats.bestTurns, 1, false, true);
		showMaxPlayerStats(mainPanel, date, players, "Pires tours", "%s (%d pts)",
				stats -> stats.worstTurns, -1, false, true);

		JScrollPane scrollPane = new JScrollPane(mainPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(18);
		add(scrollPane);

		setVisible(true);
	}

}
