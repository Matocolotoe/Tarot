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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

class FrameGlobalStats extends JFrame {

	private void showMaxPlayerStats(JPanel panel, DateRecord date, int players, String header, String details,
									Function<Player.LocalStats, Map<Contract, Integer>> provider, int multiplier,
									boolean includeTotal) {
		Map<Contract, Integer> maxAmounts = new HashMap<>();
		Map<Contract, Player> maxPlayers = new HashMap<>();
		int maxAmountAll = 0;
		Player maxPlayerAll = null;
		for (Player player : Tarot.ORDERED_PLAYERS) {
			int totalAmount = 0;
			for (Contract contract : Contract.ALL_CONTRACTS) {
				int amount = provider.apply(player.getStats(date, players)).getOrDefault(contract, 0);
				if (multiplier * amount > multiplier * maxAmounts.getOrDefault(contract, 0)) {
					maxAmounts.put(contract, amount);
					maxPlayers.put(contract, player);
				}
				totalAmount += amount;
			}
			if (multiplier * totalAmount > multiplier * maxAmountAll) {
				maxAmountAll = totalAmount;
				maxPlayerAll = player;
			}
		}

		if (maxPlayerAll == null)
			return;

		if (includeTotal) {
			panel.add(Components.getSimpleText(header + " : " + String.format(details, maxPlayerAll.getName(), maxAmountAll), 15));
		} else {
			panel.add(Components.getSimpleText(header + " :", 15));
		}

		for (Contract contract : Contract.ALL_CONTRACTS) {
			int amount = maxAmounts.getOrDefault(contract, 0);
			Player player = maxPlayers.get(contract);
			String name = player == null ? "–" : player.getName();
			panel.add(Components.getSimpleText(" ‣ " + contract.getName() + " : " + String.format(details, name, amount), 15));
		}

		panel.add(Components.getSimpleText(" ", 25));
	}

	FrameGlobalStats(DateRecord date, int players) {
		setBounds(300, 120, 500, 800);
		setResizable(false);
		setTitle("Statistiques – " + players + " joueurs – " + date.getName());

		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(Components.getStandardBorder());
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		add(mainPanel);

		mainPanel.add(Components.getSimpleText("Statistiques générales", 20));
		mainPanel.add(Components.getSimpleText(date.getName() + " – " + players + " joueurs", 20));
		mainPanel.add(Components.getSimpleText(" ", 25));

		Map<Contract, Integer> contracts = new HashMap<>();
		for (Contract contract : Contract.ALL_CONTRACTS)
			contracts.put(contract, 0);

		for (Game game : Tarot.ALL_GAMES.get(date)) {
			if (game.getNumberOfPlayers() == players) {
				Contract contract = game.getContract();
				contracts.put(contract, contracts.get(contract) + 1);
			}
		}

		int total = Maps.sum(contracts);
		mainPanel.add(Components.getSimpleText("Parties jouées : " + total, 15));
		for (Contract contract : Contract.ALL_CONTRACTS) {
			int amount = contracts.get(contract);
			mainPanel.add(Components.getSimpleText(" ‣ " + contract.getName() + " : " + amount + " (" + 100 * amount / total + "%)", 15));
		}

		mainPanel.add(Components.getSimpleText(" ", 25));

		showMaxPlayerStats(mainPanel, date, players, "Le plus de parties jouées", "%s (%d)",
				stats -> stats.playedGames, 1, true);
		showMaxPlayerStats(mainPanel, date, players, "Personne la plus appelée", "%s, %d fois",
				stats -> stats.calledTimes, 1, true);
		showMaxPlayerStats(mainPanel, date, players, "Le plus de prises réussies", "%s (%d)",
				stats -> stats.successfulTakes, 1, true);
		showMaxPlayerStats(mainPanel, date, players, "Le plus de prises ratées", "%s (%d)",
				stats -> stats.failedTakes, 1, true);
		showMaxPlayerStats(mainPanel, date, players, "Le plus d'appels à soi-même", "%s (%d)",
				stats -> stats.selfCalls, 1, true);
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
