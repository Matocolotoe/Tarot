package fr.giovanni75.tarot.frames;

import fr.giovanni75.tarot.DateRecord;
import fr.giovanni75.tarot.Tarot;
import fr.giovanni75.tarot.objects.Player;

import javax.swing.JPanel;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

class FramePlayerStats extends TarotFrame {

	FramePlayerStats(DateRecord date, int players) {
		Map<Player, List<String>> displays = new TreeMap<>();
		for (Player player : Tarot.ORDERED_PLAYERS) {
			List<String> display = player.getStats(date, players).getDisplay();
			if (!display.isEmpty())
				displays.put(player, display);
		}

		if (displays.isEmpty()) {
			Components.popup(Components.NO_GAME_AVAILABLE);
			return;
		}

		create("Statistiques individiuelles – " + players + " joueurs – " + date.getName(), 300, 150, 500, 800);

		JPanel mainPanel = panel(0, true);
		mainPanel.add(Components.getSimpleText("Statistiques individuelles", 20));
		mainPanel.add(Components.getSimpleText(date.getName() + " – " + players + " joueurs", 20));
		mainPanel.add(Components.getEmptySpace(25));

		for (var entry : displays.entrySet()) {
			mainPanel.add(Components.getSimpleText(entry.getKey().getName(), 18));
			mainPanel.add(Components.getEmptySpace(15));
			for (String line : entry.getValue())
				mainPanel.add(Components.getSimpleText(line, 15));
			mainPanel.add(Components.getEmptySpace(35));
		}
	}

}
