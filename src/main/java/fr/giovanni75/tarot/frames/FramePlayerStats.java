package fr.giovanni75.tarot.frames;

import fr.giovanni75.tarot.DateRecord;
import fr.giovanni75.tarot.Tarot;
import fr.giovanni75.tarot.objects.Player;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.util.List;

class FramePlayerStats extends JFrame {

	FramePlayerStats(DateRecord date, int players) {
		setBounds(900, 120, 500, 800);
		setResizable(false);
		setTitle("Statistiques – " + players + " joueurs – " + date.getName());

		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(Components.getStandardBorder());
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		add(mainPanel);

		mainPanel.add(Components.getSimpleText("Statistiques individuelles", 20));
		mainPanel.add(Components.getSimpleText(date.getName() + " – " + players + " joueurs", 20));
		mainPanel.add(Components.getSimpleText(" ", 25));

		for (Player player : Tarot.ORDERED_PLAYERS) {
			List<String> display = player.getDisplay(date, players);
			if (display.isEmpty())
				continue;
			mainPanel.add(Components.getSimpleText(player.getName(), 18));
			mainPanel.add(Components.getSimpleText(" ", 15));
			for (String line : display)
				mainPanel.add(Components.getSimpleText(line, 15));
			mainPanel.add(Components.getSimpleText(" ", 35));
		}

		JScrollPane scrollPane = new JScrollPane(mainPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(18);
		add(scrollPane);

		setVisible(true);
	}

}
