package fr.giovanni75.tarot.frames;

import fr.giovanni75.tarot.DateRecord;
import fr.giovanni75.tarot.Maps;
import fr.giovanni75.tarot.Tarot;
import fr.giovanni75.tarot.enums.Contract;
import fr.giovanni75.tarot.objects.Game;
import fr.giovanni75.tarot.objects.Player;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.util.*;
import java.util.function.Function;

class FrameGlobalStats extends JFrame {

	private static String getNameListDisplay(List<Player> players) {
		List<String> names = new ArrayList<>();
		for (Player player : players)
			names.add(player.getName());
		Collections.sort(names);
		return String.join(", ", names);
	}

	private static void showMaxPlayerStats(JPanel panel, DateRecord date, int players, String header, String details,
									Function<Player.LocalStats, Map<Contract, Integer>> provider, int multiplier,
									boolean includeTotal) {
		Map<Contract, Integer> maxAmounts = new HashMap<>();
		Map<Contract, List<Player>> maxPlayers = new HashMap<>();
		int maxAmountAll = 0;
		List<Player> maxPlayersAll = new ArrayList<>();
		for (Contract contract : Contract.ALL_CONTRACTS)
			maxPlayers.put(contract, new ArrayList<>());

		for (Player player : Tarot.ORDERED_PLAYERS) {
			int totalAmount = 0;
			for (Contract contract : Contract.ALL_CONTRACTS) {
				int amount = provider.apply(player.getStats(date, players)).getOrDefault(contract, 0);
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
				totalAmount += amount;
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

		for (Contract contract : Contract.ALL_CONTRACTS) {
			List<Player> playerList = maxPlayers.get(contract);
			if (playerList == null || playerList.isEmpty()) {
				panel.add(Components.getSimpleText(" ‣ " + contract.getName() + " : " + Tarot.NONE_STRING, 15));
			} else {
				panel.add(Components.getSimpleText(" ‣ " + contract.getName() + " : "
						+ String.format(details, getNameListDisplay(playerList), maxAmounts.getOrDefault(contract, 0)), 15));
			}
		}

		panel.add(Components.getSimpleText(" ", 25));
	}

	FrameGlobalStats(DateRecord date, int players) {
		Map<Contract, Integer> contracts = new HashMap<>();
		for (Game game : Tarot.ALL_GAMES.get(date)) {
			if (game.getNumberOfPlayers() == players) {
				Contract contract = game.getContract();
				contracts.put(contract, contracts.getOrDefault(contract, 0) + 1);
			}
		}

		if (contracts.isEmpty()) {
			Components.popup("Aucune partie n'est disponible pour cette période.");
			return;
		}

		setBounds(300, 150, 550, 800);
		setResizable(false);
		setTitle("Statistiques – " + players + " joueurs – " + date.getName());

		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(Components.getStandardBorder());
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		add(mainPanel);

		mainPanel.add(Components.getSimpleText("Statistiques générales", 20));
		mainPanel.add(Components.getSimpleText(date.getName() + " – " + players + " joueurs", 20));
		mainPanel.add(Components.getSimpleText(" ", 25));

		int total = Maps.sum(contracts);
		mainPanel.add(Components.getSimpleText("Parties jouées : " + total, 15));
		for (Contract contract : Contract.ALL_CONTRACTS) {
			int amount = contracts.getOrDefault(contract, 0);
			mainPanel.add(Components.getSimpleText(" ‣ " + contract.getName() + " : " + amount + " (" + 100 * amount / total + "%)", 15));
		}

		mainPanel.add(Components.getSimpleText(" ", 25));

		showMaxPlayerStats(mainPanel, date, players, "Le plus de parties jouées", "%s (%d)",
				stats -> stats.playedGames, 1, true);
		showMaxPlayerStats(mainPanel, date, players, "Le plus de fois appelé·e", "%s, %d fois",
				stats -> stats.calledTimes, 1, true);
		showMaxPlayerStats(mainPanel, date, players, "Le plus de prises réussies", "%s (%d)",
				stats -> stats.successfulTakes, 1, true);
		showMaxPlayerStats(mainPanel, date, players, "Le plus de prises ratées", "%s (%d)",
				stats -> stats.failedTakes, 1, true);
		showMaxPlayerStats(mainPanel, date, players, "Le plus d'appels à soi-même", "%s (%d)",
				stats -> stats.selfCalls, 1, true);

		showMaxPlayerStats(mainPanel, date, players, "Le plus de poignées", "%s (%d)",
				stats -> stats.handfuls, 1, true);
		showMaxPlayerStats(mainPanel, date, players, "Le plus de misères", "%s (%d)",
				stats -> stats.miseries, 1, true);

		showMaxPlayerStats(mainPanel, date, players, "Meilleurs tours", "%s (%d pts)",
				stats -> stats.bestTurns, 1, false);
		showMaxPlayerStats(mainPanel, date, players, "Pires tours", "%s (%d pts)",
				stats -> stats.worstTurns, -1, false);

		JScrollPane scrollPane = new JScrollPane(mainPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(18);
		add(scrollPane);

		setVisible(true);
	}

}
