package fr.giovanni75.tarot.frames;

import fr.giovanni75.tarot.DateRecord;
import fr.giovanni75.tarot.Tarot;
import fr.giovanni75.tarot.objects.Player;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

class FramePlayerStats extends JFrame {

	FramePlayerStats(DateRecord date, int players) {
		Map<Player, List<String>> displays = new TreeMap<>();
		for (Player player : Tarot.ORDERED_PLAYERS) {
			List<String> display = player.getStats(date, players).getDisplay();
			if (!display.isEmpty())
				displays.put(player, display);
		}

		if (displays.isEmpty()) {
			Components.popup("Aucune partie n'est disponible pour cette période.");
			return;
		}

		setBounds(900, 150, 500, 800);
		setResizable(false);
		setTitle("Statistiques – " + players + " joueurs – " + date.getName());

		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(Components.getStandardBorder());
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		add(mainPanel);

		mainPanel.add(Components.getSimpleText("Statistiques individuelles", 20));
		mainPanel.add(Components.getSimpleText(date.getName() + " – " + players + " joueurs", 20));
		mainPanel.add(Components.getEmptyText(25));

		for (Map.Entry<Player, List<String>> entry : displays.entrySet()) {
			mainPanel.add(Components.getSimpleText(entry.getKey().getName(), 18));
			mainPanel.add(Components.getEmptyText(15));
			for (String line : entry.getValue())
				mainPanel.add(Components.getSimpleText(line, 15));
			mainPanel.add(Components.getEmptyText(35));
		}

		JScrollPane scrollPane = new JScrollPane(mainPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(18);
		add(scrollPane);

		setVisible(true);
	}

}
