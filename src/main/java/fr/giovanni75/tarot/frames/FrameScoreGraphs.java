package fr.giovanni75.tarot.frames;

import fr.giovanni75.tarot.DateRecord;
import fr.giovanni75.tarot.Maps;
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

	private static final int LEFT_PANEL_WIDTH = 1050;
	private static final int MINIMUM_PLAYED_GAMES = 10;
	private static final int RIGHT_PANEL_WIDTH = 1050;

	private final double[] xData;
	private final Map<Player, double[]> yDataMap = new TreeMap<>();

	private final List<JCheckBox> checkBoxes = new ArrayList<>();
	private final Map<String, Player> temporaryProfilesByName = new HashMap<>();
	private final Set<String> displayedPlayerNames = new HashSet<>();

	private final XChartPanel<XYChart> leftPanel;

	private final XYChart chart = new XYChartBuilder()
			.width(1050)
			.height(600)
			.xAxisTitle("Numéro de partie")
			.yAxisTitle("Score total")
			.theme(Styler.ChartTheme.Matlab)
			.build();

	private final JButton hideAllButton = getButton("Tout masquer");
	private final JButton showAllButton = getButton("Tout afficher");

	private JButton getButton(String text) {
		JButton button = new JButton(text);
		button.addActionListener(this);
		button.setFont(Components.getFont(11));
		button.setSize(50, 15);
		return button;
	}

	private void recalculateColors() {
		float index = 0;
		float total = displayedPlayerNames.size();
		for (XYSeries series : chart.getSeriesMap().values()) {
			// From https://stackoverflow.com/questions/223971/generating-spectrum-color-palettes
			Color color = Color.getHSBColor(index / total, 0.85f, 1.0f);
			series.setFillColor(color);
			series.setLineColor(color);
			series.setMarkerColor(color);
			series.setMarker(SeriesMarkers.CIRCLE);
			index++;
		}
	}

	private void updateAllData(boolean show) {
		if (show) {
			// Remove all data first to preserve name order
			for (String name : displayedPlayerNames)
				chart.removeSeries(name);
			for (Map.Entry<Player, double[]> entry : yDataMap.entrySet()) {
				String name = entry.getKey().getName();
				chart.addSeries(name, xData, entry.getValue());
				displayedPlayerNames.add(name);
			}
		} else {
			for (String name : displayedPlayerNames)
				chart.removeSeries(name);
			displayedPlayerNames.clear();
		}
		recalculateColors();
		for (JCheckBox box : checkBoxes)
			box.setSelected(show);
		leftPanel.revalidate();
		leftPanel.repaint();
	}

	FrameScoreGraphs(int minDay, int maxDay, DateRecord date, int players) {
		setBounds(300, 150, 1200, 800);
		setResizable(false);
		setTitle("Statistiques – " + players + " joueurs – " + minDay + "/" + date.getShortName() + " ➝ " + maxDay + "/" + date.getShortName());

		// Games stored in ALL_GAMES are sorted in descending order
		List<Game> allPossibleGames = Tarot.ALL_GAMES.get(date).reversed();

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
		leftPanel.setSize(LEFT_PANEL_WIDTH, 600);
		leftPanel.setVisible(true);

		JPanel rightPanel = new JPanel();
		rightPanel.setBorder(Components.getStandardBorder());
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.setSize(RIGHT_PANEL_WIDTH, 600);
		rightPanel.setVisible(true);

		rightPanel.add(Components.getSimpleText("Affichage", 20));
		rightPanel.add(Components.getSimpleText(" ", 15));

		// If we check for zero scores before, we might retain no players if the first selected game is the first one of the month
		// Instead, check for scores which remained constant during the selected period
		Iterator<Map.Entry<Player, double[]>> iterator = yDataMap.entrySet().iterator();
		yIter: while (iterator.hasNext()) {
			double[] yData = iterator.next().getValue();
			for (double y : yData)
				if (y != yData[0])
					continue yIter; // There exists at least two distinct elements
			iterator.remove();
		}

		for (Map.Entry<Player, double[]> entry : yDataMap.entrySet()) {
			Player player = entry.getKey();
			String name = player.getName();
			JCheckBox box = new JCheckBox(name);
			box.addActionListener(this);
			checkBoxes.add(box);
			rightPanel.add(box);
			if (Maps.sum(player.getStats(date, players).playedGames) < MINIMUM_PLAYED_GAMES)
				continue;
			box.setSelected(true);
			chart.addSeries(name, entry.getValue());
			displayedPlayerNames.add(name);
		}

		// Need to get the number of displayed players calculated before
		recalculateColors();

		rightPanel.add(Components.getSimpleText(" ", 15));
		rightPanel.add(showAllButton);
		rightPanel.add(Components.getSimpleText(" ", 5));
		rightPanel.add(hideAllButton);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setDividerLocation(LEFT_PANEL_WIDTH);
		splitPane.setDividerSize(0);
		splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setSize(LEFT_PANEL_WIDTH + RIGHT_PANEL_WIDTH, 600);

		splitPane.setLeftComponent(leftPanel);
		splitPane.setRightComponent(rightPanel);

		add(splitPane);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == hideAllButton) {
			updateAllData(false);
			return;
		}

		if (source == showAllButton) {
			updateAllData(true);
			return;
		}

		if (!(source instanceof JCheckBox box))
			return;

		String name = box.getText();
		if (box.isSelected()) {
			Player player = temporaryProfilesByName.get(name);
			displayedPlayerNames.add(name); // Add before to pass condition below
			for (Map.Entry<Player, double[]> entry : yDataMap.entrySet()) {
				Player other = entry.getKey();
				if (other.compareTo(player) < 0)
					continue;
				name = other.getName();
				if (!displayedPlayerNames.contains(name))
					continue;
				// Only rebuild series of shown players with a higher name in alphabetical order
				chart.removeSeries(name);
				chart.addSeries(name, xData, entry.getValue());
			}
		} else {
			chart.removeSeries(name);
			displayedPlayerNames.remove(name);
		}

		recalculateColors();
		leftPanel.revalidate();
		leftPanel.repaint();
	}

}
