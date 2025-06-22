package fr.giovanni75.tarot.frames;

import fr.giovanni75.tarot.DateRecord;
import fr.giovanni75.tarot.Tarot;
import fr.giovanni75.tarot.Utils;
import fr.giovanni75.tarot.enums.Contract;
import fr.giovanni75.tarot.objects.Game;
import fr.giovanni75.tarot.objects.Player;
import fr.giovanni75.tarot.stats.LocalStats;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.util.*;
import java.util.function.Function;

class FrameGlobalStats extends JFrame {

	static void displayAll(JPanel panel, DateRecord date, int players, Collection<Game> gameCollection, Collection<Player> playerCollection) {
		for (GlobalStatistic statistic : GLOBAL_STATS)
			statistic.display(panel, gameCollection);
		for (MaximumStatistic statistic : MAX_STATS)
			statistic.display(panel, date, players, playerCollection);
	}

	private static String getNameListDisplay(List<Player> players) {
		List<String> names = new ArrayList<>();
		for (Player player : players)
			names.add(player.getName());
		Collections.sort(names);
		return String.join(", ", names);
	}

	private enum GlobalStatistic {

		PLAYED_GAMES("Parties jouées", game -> 1, false),
		OUDLERS("Nombre moyen de bouts", game -> game.oudlers.ordinal(), true),
		PETITS("Petits au bout", game -> game.petitAuBout == null ? 0 : 1, false);

		private final String description;
		private final Function<Game, Integer> adder;
		private final boolean averaged;

		GlobalStatistic(String description, Function<Game, Integer> adder, boolean averaged) {
			this.description = description;
			this.adder = adder;
			this.averaged = averaged;
		}

		private void display(JPanel panel, Collection<Game> gameCollection) {
			Map<Contract, Integer> totalAmounts = new EnumMap<>(Contract.class);
			int totalAmount = 0;
			for (Game game : gameCollection) {
				int value = adder.apply(game);
				if (value != 0) {
					totalAmounts.put(game.contract, totalAmounts.getOrDefault(game.contract, 0) + value);
					totalAmount += value;
				}
			}

			if (averaged) {
				panel.add(Components.getSimpleText(description + " : "
						+ Utils.format((double) totalAmount / gameCollection.size()), 15));
			} else {
				panel.add(Components.getSimpleText(description + " : " + totalAmount, 15));
			}

			for (Contract contract : Contract.ALL_CONTRACTS) {
				Integer amount = totalAmounts.get(contract);
				if (amount == null) {
					panel.add(Components.getSimpleText(" ‣ " + contract.getName() + " : " + Tarot.NONE_STRING, 15));
				} else if (averaged) {
					panel.add(Components.getSimpleText(" ‣ " + contract.getName() + " : " +
							Utils.format((double) amount / Utils.count(gameCollection, contract)), 15));
				} else {
					panel.add(Components.getSimpleText(" ‣ " + contract.getName() + " : "
							+ amount + " (" + 100 * amount / totalAmount + "%)", 15));
				}
			}

			panel.add(Components.getEmptySpace(20));
		}

	}

	private enum MaximumStatistic {

		PLAYED_GAMES("Le plus de parties jouées", stats -> stats.playedGames,
				"%s (%d)", true, false, 1),

		HANDFULS("Le plus de poignées", stats -> stats.handfuls,
			"%s (%d)", true, true, 1),

		MISERIES("Le plus de misères", stats -> stats.miseries,
				"%s (%d)", true, true, 1),

		CALLED_TIMES("Le plus de fois appelé·e", stats -> stats.calledTimes,
				"%s (%d fois)", true, true, 1),

		SUCCESSFUL_TAKES("Le plus de prises réussies", stats -> stats.successfulTakes,
				"%s (%d)", true, true, 1),

		FAILED_TAKES("Le plus de prises ratées", stats -> stats.failedTakes,
				"%s (%d)", true, true, 1),

		SELF_CALLS("Le plus d'appels à soi-même", stats -> stats.selfCalls,
				"%s (%d fois)", true, true, 1),

		BEST_TURNS("Meilleurs tours", stats -> stats.bestTurns,
				"%s (+%d)", false, true, 1),

		WORST_TURNS("Pires tours", stats -> stats.worstTurns,
				"%s (%d)", false, true, -1);

		private final String description;
		private final Function<LocalStats, Map<Contract, Integer>> provider;
		private final String details;
		private final boolean includeTotal;
		private final boolean includeDetailsByContract;
		private final int direction;

		MaximumStatistic(String description, Function<LocalStats, Map<Contract, Integer>> provider,
						 String details, boolean includeTotal, boolean includeDetailsByContract, int direction) {
			this.description = description;
			this.provider = provider;
			this.details = details;
			this.includeTotal = includeTotal;
			this.includeDetailsByContract = includeDetailsByContract;
			this.direction = direction;
		}

		private void display(JPanel panel, DateRecord date, int players, Collection<Player> playerCollection) {
			Map<Contract, Integer> maxAmounts = new EnumMap<>(Contract.class);
			Map<Contract, List<Player>> maxPlayers = new EnumMap<>(Contract.class);
			int maxAmountAll = 0;
			List<Player> maxPlayersAll = new ArrayList<>();
			for (Contract contract : Contract.ALL_CONTRACTS)
				maxPlayers.put(contract, new ArrayList<>());

			for (Player player : playerCollection) {
				int totalAmount = 0;
				for (Contract contract : Contract.ALL_CONTRACTS) {
					int amount = provider.apply(player.getStats(date, players)).getOrDefault(contract, 0);
					totalAmount += amount;
					// Skip calculations if details are not required
					if (!includeDetailsByContract)
						continue;
					int diff = direction * (amount - maxAmounts.getOrDefault(contract, 0));
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
				int diff = direction * (totalAmount - maxAmountAll);
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
				panel.add(Components.getSimpleText(description + " : " +
						String.format(details, getNameListDisplay(maxPlayersAll), maxAmountAll), 15));
			} else {
				panel.add(Components.getSimpleText(description + " :", 15));
			}

			if (includeDetailsByContract) {
				for (Contract contract : Contract.ALL_CONTRACTS) {
					List<Player> maxPlayerList = maxPlayers.get(contract);
					if (maxPlayerList == null || maxPlayerList.isEmpty()) {
						panel.add(Components.getSimpleText(" ‣ " + contract.getName() + " : " + Tarot.NONE_STRING, 15));
					} else {
						panel.add(Components.getSimpleText(" ‣ " + contract.getName() + " : "
								+ String.format(details, getNameListDisplay(maxPlayerList), maxAmounts.getOrDefault(contract, 0)), 15));
					}
				}
			}

			panel.add(Components.getEmptySpace(20));
		}

	}

	private static final GlobalStatistic[] GLOBAL_STATS = GlobalStatistic.values();
	private static final MaximumStatistic[] MAX_STATS = MaximumStatistic.values();

	FrameGlobalStats(DateRecord date, int players) {
		List<Game> games = new ArrayList<>();
		for (Game game : Tarot.ALL_GAMES.get(date))
			if (game.players.length == players)
				games.add(game);

		if (games.isEmpty()) {
			Components.popup(Components.NO_GAME_AVAILABLE);
			return;
		}

		setBounds(300, 150, 500, 800);
		setResizable(false);
		setTitle("Statistiques – " + players + " joueurs – " + date.getName());

		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(Components.getStandardBorder(0));
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		add(mainPanel);

		mainPanel.add(Components.getSimpleText("Statistiques générales", 20));
		mainPanel.add(Components.getSimpleText(date.getName() + " – " + players + " joueurs", 20));
		mainPanel.add(Components.getEmptySpace(25));

		displayAll(mainPanel, date, players, games, Tarot.ORDERED_PLAYERS);
		add(Components.getStandardScrollPane(mainPanel));
		setVisible(true);
	}

}
