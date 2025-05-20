package fr.giovanni75.tarot.frames;

import fr.giovanni75.tarot.DateRecord;
import fr.giovanni75.tarot.Utils;
import fr.giovanni75.tarot.objects.Game;
import fr.giovanni75.tarot.objects.LocalPlayer;
import fr.giovanni75.tarot.objects.Player;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

class FramePeriodicStats extends JFrame {

	FramePeriodicStats(List<Game> displayedGames, List<Game> selectedGames, DateRecord date, int players) {
		setBounds(300, 150, 500, 800);
		setResizable(false);
		setTitle("Statistiques périodiques – " + players + " joueurs – " + selectedGames.size() + " parties");

		// Create a temporary profile for each player to track score evolution
		Map<Integer, Player> temporaryProfiles = new HashMap<>();
		for (Player player : Utils.getAllPlayers(displayedGames))
			temporaryProfiles.put(player.getID(), player.copy());

		// Grant scores until the first game to the temporary profiles
		Function<LocalPlayer, Player> converter = Utils.getConverter(temporaryProfiles);
		Utils.calculateScores(displayedGames, selectedGames, converter);

		Map<Player, Integer> scoresBefore = new HashMap<>();
		for (Player player : temporaryProfiles.values())
			scoresBefore.put(player, player.getStats(date, players).totalScore);

		for (Game game : selectedGames)
			game.applyResults(converter, Game.ADD_GAME_DIRECTION, Game.GLOBAL_STATS | Game.PLAYER_SCORES | Game.PLAYER_STATS);

		Map<Player, Integer> orderedEvolutions = new TreeMap<>((k1, k2) -> {
			int diff1 = k1.getStats(date, players).totalScore - scoresBefore.get(k1);
			int diff2 = k2.getStats(date, players).totalScore - scoresBefore.get(k2);
			return diff1 == diff2 ? k1.getName().compareTo(k2.getName()) : Integer.compare(diff2, diff1);
		});

		for (var entry : scoresBefore.entrySet()) {
			int diff = entry.getKey().getStats(date, players).totalScore - entry.getValue();
			if (diff != 0)
				orderedEvolutions.put(entry.getKey(), diff);
		}

		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(Components.getStandardBorder());
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		add(mainPanel);

		mainPanel.add(Components.getSimpleText("Statistiques périodiques – " + players + " joueurs", 20));
		mainPanel.add(Components.getSimpleText(Utils.getTitle(selectedGames, date), 20));
		mainPanel.add(Components.getEmptySpace(18));

		mainPanel.add(Components.getSimpleText("Évolution des scores :", 15));
		for (var entry : orderedEvolutions.entrySet()) {
			int diff = entry.getValue();
			mainPanel.add(Components.getSimpleText(" ‣ " + entry.getKey().getName() + " : " + (diff >= 0 ? "+" : "") + diff, 15));
		}

		mainPanel.add(Components.getSimpleText(" ", 20));

		FrameGlobalStats.showAllStats(mainPanel, date, players, selectedGames, temporaryProfiles.values());

		add(Components.getStandardScrollPane(mainPanel));
		setVisible(true);
	}

}
