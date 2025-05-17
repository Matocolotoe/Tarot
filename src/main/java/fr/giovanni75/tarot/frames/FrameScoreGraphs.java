package fr.giovanni75.tarot.frames;

import fr.giovanni75.tarot.DateRecord;
import fr.giovanni75.tarot.Maps;
import fr.giovanni75.tarot.Utils;
import fr.giovanni75.tarot.objects.Game;
import fr.giovanni75.tarot.objects.LocalPlayer;
import fr.giovanni75.tarot.objects.Player;
import fr.giovanni75.tarot.stats.LocalStats;
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
import java.util.function.Function;

class FrameScoreGraphs extends JFrame implements ActionListener {

	private static final int GLOBAL_PANEL_HEIGHT = 800;
	private static final int LEFT_PANEL_WIDTH = 1050;
	private static final int MINIMUM_PLAYED_GAMES = 10;
	private static final int RIGHT_PANEL_WIDTH = 175;

	private final double[] xData;
	private final Map<Player, double[]> yDataMap = new TreeMap<>();

	private final Map<String, JCheckBox> checkBoxes = new HashMap<>();
	private final Map<String, Player> temporaryProfilesByName = new HashMap<>();
	private final Set<String> displayedPlayerNames = new HashSet<>();
	private final Set<String> minimumPlayedNames = new TreeSet<>(); // Requires sorting for showMinimumButton

	private final XChartPanel<XYChart> leftPanel;

	private final XYChart chart = new XYChartBuilder()
			.width(LEFT_PANEL_WIDTH)
			.height(GLOBAL_PANEL_HEIGHT)
			.xAxisTitle("Numéro de partie")
			.yAxisTitle("Score total")
			.theme(Styler.ChartTheme.Matlab)
			.build();

	private final ButtonGroup buttonGroup = new ButtonGroup();
	private final JRadioButton hideAllButton = getButton("Personne");
	private final JRadioButton showAllButton = getButton("Tout le monde");
	private final JRadioButton showMinimumButton = getButton("≥ " + MINIMUM_PLAYED_GAMES + " parties");

	private JRadioButton getButton(String text) {
		JRadioButton button = new JRadioButton(text);
		button.addActionListener(this);
		button.setFont(Components.getFont(13));
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

	private void updateAllData(ViewMode mode) {
		if (mode == ViewMode.SHOW_ALL_PLAYERS || mode == ViewMode.SHOW_MINIMUM_PLAYED) {
			// Remove all data first to preserve name order
			for (String name : displayedPlayerNames)
				chart.removeSeries(name);
			// Recalculate displayed player names for colors
			displayedPlayerNames.clear();
			if (mode == ViewMode.SHOW_ALL_PLAYERS) {
				for (Map.Entry<Player, double[]> entry : yDataMap.entrySet()) {
					String name = entry.getKey().getName();
					chart.addSeries(name, xData, entry.getValue());
					displayedPlayerNames.add(name);
				}
				for (JCheckBox box : checkBoxes.values())
					box.setSelected(true);
			} else {
				// Some boxes might have to disappear when switching from SHOW_ALL to SHOW_MINIMUM_PLAYED
				for (JCheckBox box : checkBoxes.values())
					box.setSelected(false);
				for (String name : minimumPlayedNames) {
					chart.addSeries(name, xData, yDataMap.get(temporaryProfilesByName.get(name)));
					checkBoxes.get(name).setSelected(true);
					displayedPlayerNames.add(name);
				}
			}
		} else {
			for (JCheckBox box : checkBoxes.values())
				box.setSelected(false);
			for (String name : displayedPlayerNames)
				chart.removeSeries(name);
			displayedPlayerNames.clear();
		}
		recalculateColors();
		leftPanel.revalidate();
		leftPanel.repaint();
	}

	FrameScoreGraphs(List<Game> displayedGames, List<Game> selectedGames, DateRecord date, int players) {
		setBounds(300, 150, LEFT_PANEL_WIDTH + RIGHT_PANEL_WIDTH, GLOBAL_PANEL_HEIGHT);
		setResizable(false);
		setTitle("Évolution des scores – " + players + " joueurs – " + Utils.getTitle(selectedGames, date));

		boolean filterGames = players == 5 && selectedGames.size() >= MINIMUM_PLAYED_GAMES;

		// Create a virtual profile for every player involved in those games to avoid conflicts with actual pre-calculated stats
		Map<Integer, Player> temporaryProfiles = new HashMap<>();
		for (Player player : Utils.getAllPlayers(displayedGames)) {
			Player copy = player.copy();
			String name = player.getName();
			temporaryProfiles.put(player.getID(), copy);
			temporaryProfilesByName.put(name, copy);
			if (filterGames && Maps.sum(player.getStats(date, players).playedGames) >= MINIMUM_PLAYED_GAMES)
				minimumPlayedNames.add(name);
		}

		// Grant stats until the first game to the temporary profiles
		Function<LocalPlayer, Player> converter = Utils.getConverter(temporaryProfiles);
		Utils.calculateScores(displayedGames, selectedGames, converter);

		int size = selectedGames.size() + 1;
		xData = new double[size];
		for (int i = 0; i < size; i++)
			xData[i] = i;

		for (Player player : temporaryProfiles.values())
			yDataMap.put(player, new double[size]);

		for (int i = 0; i < size; i++) {
			// First iteration stores the total score earned before the first game
			if (i > 0)
				selectedGames.get(i - 1).applyResults(converter, Game.ADD_GAME_DIRECTION, Game.PLAYER_SCORES);
			for (Player player : temporaryProfiles.values()) {
				LocalStats stats = player.getStats(date, players);
				yDataMap.get(player)[i] = stats.totalScore;
			}
		}

		chart.getStyler().setMarkerSize(Math.max(3, 8 - selectedGames.size() / 20));
		chart.getStyler().setZoomEnabled(true);
		leftPanel = new XChartPanel<>(chart);
		leftPanel.setSize(LEFT_PANEL_WIDTH, GLOBAL_PANEL_HEIGHT);
		leftPanel.setVisible(true);

		JPanel rightPanel = new JPanel();
		rightPanel.setBorder(Components.getStandardBorder());
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.setSize(RIGHT_PANEL_WIDTH, GLOBAL_PANEL_HEIGHT);
		rightPanel.setVisible(true);

		rightPanel.add(Components.getSimpleText("Joueurs", 20));
		rightPanel.add(Components.getEmptySpace(12));

		// If we check for zero scores before, we might retain no players if the first selected game is the first one of the month
		// Instead, check for scores which remained constant during the selected period
		// We also need to check every game since scores might cancel out at the end
		Iterator<Map.Entry<Player, double[]>> iterator = yDataMap.entrySet().iterator();
		yIter: while (iterator.hasNext()) {
			var entry = iterator.next();
			double[] yData = entry.getValue();
			for (double y : yData)
				if (y != yData[0])
					continue yIter; // There exists at least two distinct elements
			// Score has not changed, remove associated profile to keep a correct count
			// Perform before Iterator#remove to ensure it's executed properly (huh)
			temporaryProfilesByName.remove(entry.getKey().getName());
			iterator.remove();
		}

		for (Map.Entry<Player, double[]> entry : yDataMap.entrySet()) {
			Player player = entry.getKey();
			String name = player.getName();
			JCheckBox box = new JCheckBox(name);
			box.addActionListener(this);
			checkBoxes.put(name, box);
			rightPanel.add(box);
			// Previously filled with stats from the original player, which were not copied
			if (!minimumPlayedNames.contains(name))
				continue;
			box.setSelected(true);
			chart.addSeries(name, entry.getValue());
			displayedPlayerNames.add(name);
		}

		// Need to have the number of displayed players calculated before
		recalculateColors();

		buttonGroup.add(showAllButton);
		buttonGroup.add(hideAllButton);

		rightPanel.add(Components.getEmptySpace(30));
		rightPanel.add(Components.getSimpleText("Affichage", 20));
		rightPanel.add(Components.getEmptySpace(10));
		rightPanel.add(showAllButton);
		rightPanel.add(Components.getEmptySpace(5));
		rightPanel.add(hideAllButton);

		// Only show ">= MINIMUM_PLAYED_GAMES" button when necessary
		if (filterGames && displayedPlayerNames.size() < temporaryProfilesByName.size()) {
			buttonGroup.add(showMinimumButton);
			rightPanel.add(Components.getEmptySpace(5));
			rightPanel.add(showMinimumButton);
			showMinimumButton.setSelected(true);
		} else {
			showAllButton.setSelected(true);
		}

		JSplitPane splitPane = new JSplitPane();
		splitPane.setDividerLocation(LEFT_PANEL_WIDTH);
		splitPane.setDividerSize(0);
		splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setSize(LEFT_PANEL_WIDTH + RIGHT_PANEL_WIDTH, GLOBAL_PANEL_HEIGHT);

		splitPane.setLeftComponent(leftPanel);
		splitPane.setRightComponent(rightPanel);

		add(splitPane);
		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (source == hideAllButton) {
			updateAllData(ViewMode.HIDE_ALL_PLAYERS);
			return;
		}

		if (source == showMinimumButton) {
			updateAllData(ViewMode.SHOW_MINIMUM_PLAYED);
			return;
		}

		if (source == showAllButton) {
			updateAllData(ViewMode.SHOW_ALL_PLAYERS);
			return;
		}

		if (!(source instanceof JCheckBox box))
			return;

		String name = box.getText();
		if (box.isSelected()) {
			Player player = temporaryProfilesByName.get(name);
			displayedPlayerNames.add(name); // Add before to pass condition below and ensure all data is properly refreshed
			for (Map.Entry<Player, double[]> entry : yDataMap.entrySet()) {
				Player other = entry.getKey();
				// Only rebuild series of players with a higher name in alphabetical order
				if (other.compareTo(player) < 0)
					continue;
				name = other.getName();
				// Only rebuild series of shown players
				if (!displayedPlayerNames.contains(name))
					continue;
				chart.removeSeries(name);
				chart.addSeries(name, xData, entry.getValue());
			}
		} else {
			chart.removeSeries(name);
			displayedPlayerNames.remove(name);
		}

		// Make button selection consistent with selected boxes at all times
		if (displayedPlayerNames.size() == temporaryProfilesByName.size()) {
			showAllButton.setSelected(true);
		} else if (displayedPlayerNames.isEmpty()) {
			hideAllButton.setSelected(false);
		} else if (displayedPlayerNames.equals(minimumPlayedNames)) {
			showMinimumButton.setSelected(true);
		} else {
			buttonGroup.clearSelection();
		}

		recalculateColors();
		leftPanel.revalidate();
		leftPanel.repaint();
	}

	private enum ViewMode {

		HIDE_ALL_PLAYERS,
		SHOW_ALL_PLAYERS,
		SHOW_MINIMUM_PLAYED

	}

}
