package fr.giovanni75.tarot.frames;

import fr.giovanni75.tarot.DateRecord;
import fr.giovanni75.tarot.Tarot;
import fr.giovanni75.tarot.objects.Game;
import fr.giovanni75.tarot.objects.LocalPlayer;
import fr.giovanni75.tarot.objects.Player;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.Styler;
import org.knowm.xchart.style.markers.SeriesMarkers;

import javax.swing.*;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

class FrameScoreGraphs extends JFrame implements ActionListener {

	private final double[] xData;
	private final Map<Player, double[]> yDataMap = new TreeMap<>();
	private final Map<String, Player> temporaryProfilesByName = new HashMap<>();

	private final XChartPanel<XYChart> leftPanel;

	private final XYChart chart = new XYChartBuilder()
			.width(1050)
			.height(600)
			.xAxisTitle("Numéro de partie")
			.yAxisTitle("Score total")
			.theme(Styler.ChartTheme.Matlab)
			.build();

	FrameScoreGraphs(int minDay, int maxDay, DateRecord date, int players) {
		setBounds(300, 150, 1200, 800);
		setResizable(false);
		setTitle("Statistiques – " + players + " joueurs – " + minDay + "/" + date.getShortName() + " ➝ " + maxDay + "/" + date.getShortName());

		List<Game> allPossibleGames = Tarot.ALL_GAMES.get(date);

		// Store all games that have been played between minDay and maxDay
		// Need to add all players to recalculate stats for now
		List<Game> games = new ArrayList<>();
		Set<UUID> uuids = new HashSet<>();
		for (Game game : allPossibleGames) {
			if (game.players.length == players) {
				for (LocalPlayer local : game.players)
					uuids.add(local.uuid());
				if (game.dayOfMonth == 0 || (game.dayOfMonth >= minDay && game.dayOfMonth <= maxDay))
					games.add(game);
			}
		}

		// Create a virtual profile for every player involved in those games to avoid conflicts with actual pre-calculated stats
		Map<UUID, Player> temporaryProfiles = new HashMap<>();
		for (UUID uuid : uuids) {
			String name = Tarot.getPlayer(uuid).getName();
			Player player = new Player(name, UUID.randomUUID());
			temporaryProfiles.put(uuid, player);
			temporaryProfilesByName.put(name, player);
		}

		// Grant stats until the first game to the temporary profiles
		for (Game game : allPossibleGames)
			if (game.players.length == players && game.dayOfMonth < minDay)
				game.applyResults(temporaryProfiles::get);

		chart.getStyler().setZoomEnabled(true);

		int size = games.size() + 1;
		xData = new double[size];
		for (int i = 0; i < size; i++)
			xData[i] = i;

		for (Player player : temporaryProfiles.values())
			yDataMap.put(player, new double[size]);

		for (int i = 0; i < size; i++) {
			for (Player player : temporaryProfiles.values()) {
				Player.LocalStats stats = player.getStats(date, players);
				yDataMap.get(player)[i] = stats.totalScore;
			}
			// First iteration stores the total score earned before the first game
			if (i > 0)
				games.get(i - 1).applyResults(temporaryProfiles::get);
		}

		leftPanel = new XChartPanel<>(chart);
		leftPanel.setSize(1050, 600);
		leftPanel.setVisible(true);

		JPanel rightPanel = new JPanel();
		rightPanel.setBorder(Components.getStandardBorder());
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.setSize(150, 600);
		rightPanel.setVisible(true);

		rightPanel.add(Components.getSimpleText("Affichage", 20));
		rightPanel.add(Components.getSimpleText(" ", 15));

		for (Map.Entry<Player, double[]> entry : yDataMap.entrySet()) {
			// If we check for zero scores before, we might retain no players if the first selected game is the first one of the month
			// Instead, check for scores which remained constant during the selected period
			boolean allEqual = true;
			double[] yData = entry.getValue();
			for (double y : yData) {
				if (y != yData[0]) {
					allEqual = false;
					break;
				}
			}
			if (allEqual)
				continue;

			String name = entry.getKey().getName();
			XYSeries series = chart.addSeries(name, xData, yData);
			series.setMarker(SeriesMarkers.CIRCLE);

			// From https://stackoverflow.com/questions/223971/generating-spectrum-color-palettes
			Color color = Color.getHSBColor((float) total / (float) yDataMap.size(), 0.85f, 1.0f);
			series.setFillColor(color);
			series.setLineColor(color);
			series.setMarkerColor(color);

			JCheckBox box = new JCheckBox(name);
			box.addActionListener(this);
			box.setSelected(true);
			rightPanel.add(box);
		}

		JSplitPane splitPane = new JSplitPane();
		splitPane.setDividerLocation(1050);
		splitPane.setDividerSize(0);
		splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setSize(1200, 600);

		splitPane.setLeftComponent(leftPanel);
		splitPane.setRightComponent(rightPanel);

		add(splitPane);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (!(source instanceof JCheckBox box))
			return;

		// Always required, at least for error bars
		double[] empty = new double[xData.length];

		String name = box.getText();
		if (box.isSelected()) {
			Player player = temporaryProfilesByName.get(name);
			chart.updateXYSeries(name, xData, yDataMap.get(player), empty);
		} else {
			// Removing the whole series and adding it back again would put the name at the end of the list
			// Only hide actual data to keep name list sorted
			chart.updateXYSeries(name, empty, empty, empty);
		}

		leftPanel.revalidate();
		leftPanel.repaint();
	}

}
