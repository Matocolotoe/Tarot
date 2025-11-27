package fr.giovanni75.tarot.frames;

import fr.giovanni75.tarot.DateRecord;
import fr.giovanni75.tarot.Tarot;
import fr.giovanni75.tarot.enums.Month;
import fr.giovanni75.tarot.objects.Game;
import fr.giovanni75.tarot.objects.LocalPlayer;
import fr.giovanni75.tarot.objects.Player;

import javax.swing.*;
import java.awt.Color;
import java.awt.Component;
import java.util.*;
import java.util.function.Consumer;

class FramePlayerProfiles extends TarotFrame {

	private static final Color SELECTED_BUTTON_COLOR = new Color(226, 238, 255);

	private static final int MINIMUM_NICK_LENGTH = 2;
	private static final int MAXIMUM_NICK_LENGTH = 30;

	private static final int GLOBAL_PANEL_HEIGHT = 650;
	private static final int LEFT_PANEL_WIDTH = 200;
	private static final int RIGHT_PANEL_WIDTH = 850;

	private static final int BUTTON_HEIGHT = 20;
	private static final int EDIT_BUTTON_X = 725;
	private static final int HUGE_TEXT_WIDTH = 400;
	private static final int INITIAL_LABELS_Y = 105;
	private static final int INITIAL_PLAYERS_Y = 75;
	private static final int LARGE_TEXT_WIDTH = 200;
	private static final int LEFT_TEXT_X = 25;
	private static final int MID_TEXT_X = 400;
	private static final int RIGHT_TEXT_X = 525;
	private static final int SMALL_TEXT_WIDTH = 100;
	private static final int Y_OFFSET = 30;

	private static int getMaxPlayedYear() {
		int year = DateRecord.START_YEAR;
		for (DateRecord date : Tarot.ALL_GAMES.keySet())
			if (date.year() > year)
				year = date.year();
		return year;
	}

	private static List<Month> getMonthsPlayed(Player player, int year) {
		Set<Month> months = EnumSet.noneOf(Month.class);
		for (var entry : Tarot.ALL_GAMES.entrySet())
			if (entry.getKey().year() == year)
				for (Game game : entry.getValue())
					for (LocalPlayer local : game.players)
						if (local.player == player)
							months.add(entry.getKey().month());
		List<Month> month = new ArrayList<>(months);
		month.sort(Month::compareTo);
		return month;
	}

	private final Map<Player, List<Month>> calculatedMonthsPlayed = new HashMap<>();

	private JButton currentPlayerButton;
	private Player currentPlayer;

	private int currentPlayerCount = 5;
	private Month currentMonth;
	private int currentYear = getMaxPlayedYear();

	private final JPanel leftPanel;
	private final JPanel rightPanel;

	private final JLabel playerCountLabel;
	private final List<Component> editButtons = new ArrayList<>();
	private final List<Component> playerComponents = new ArrayList<>();
	private final List<Component> playerStats = new ArrayList<>();
	private final List<Component> tempComponents = new ArrayList<>();

	private final JComboBox<String> monthBox = new JComboBox<>();
	private final JComboBox<String> playerCountBox = new JComboBox<>();
	private final JComboBox<Integer> yearBox = new JComboBox<>();

	private final Map<Month, JLabel> monthLabels = new EnumMap<>(Month.class);
	private final Map<Month, JLabel> nickLabels = new EnumMap<>(Month.class);

	private JLabel genericNickLabel;
	private JLabel nameLabel;
	private JLabel statsLabel;

	private void addEditButton(JPanel panel, Month month, Player player, int y, Consumer<String> action) {
		JButton button = Components.getClickableText("Modifier", 12, EDIT_BUTTON_X, y);
		button.addMouseListener(Components.getLeftClickAdapter(() -> promptNickname(month, player, action)));
		editButtons.add(button);
		panel.add(button);
	}

	private void addStatsComponent(JPanel panel, Component component) {
		playerStats.add(component);
		panel.add(component);
	}

	private void addTemporaryComponent(JPanel panel, Component component) {
		tempComponents.add(component);
		panel.add(component);
	}

	private void initializeBoxes(JPanel panel) {
		playerCountBox.addItem("Tarot à 5");
		playerCountBox.addItem("Tarot à 4");
		playerCountBox.addItem("Tarot à 3");
		playerCountBox.addActionListener(event -> {
			Object selectedCount = playerCountBox.getSelectedItem();
			if (selectedCount == null)
				return;
			int targetCount = 5 - playerCountBox.getSelectedIndex();
			if (currentPlayerCount != targetCount) {
				currentPlayerCount = targetCount;
				onPlayerClick(currentPlayer, Source.PLAYER_COUNT_UPDATED, currentPlayerButton);
			}
		});
		playerCountBox.setLocation(EDIT_BUTTON_X - 300, 25);
		playerCountBox.setSize(120, 22);
		panel.add(playerCountBox);

		refreshMonthBoxItems();
		currentMonth = Month.NOVEMBER;
		monthBox.addActionListener(event -> {
			Object selectedMonth = monthBox.getSelectedItem();
			if (selectedMonth == null)
				return;
			Month targetMonth = Month.BY_NAME.get((String) selectedMonth);
			if (currentMonth != targetMonth) {
				currentMonth = targetMonth;
				onPlayerClick(currentPlayer, Source.MONTH_UPDATED, currentPlayerButton);
			}
		});

		monthBox.setLocation(EDIT_BUTTON_X - 160, 25);
		monthBox.setSize(120, 22);
		panel.add(monthBox);

		for (int year = getMaxPlayedYear(); year >= DateRecord.START_YEAR; year--)
			yearBox.addItem(year);

		yearBox.addActionListener(event -> {
			Object selectedYear = yearBox.getSelectedItem();
			if (selectedYear == null)
				return;
			if (currentYear != (int) selectedYear) {
				currentYear = (int) selectedYear;
				onPlayerClick(currentPlayer, Source.YEAR_UPDATED, currentPlayerButton);
			}
		});

		yearBox.setLocation(EDIT_BUTTON_X - 20, 25);
		yearBox.setSize(100, 22);
		panel.add(yearBox);
	}

	private void initializeRightPanel(JPanel panel, Player player, List<Month> monthsPlayed) {
		int y = INITIAL_LABELS_Y;
		panel.add(Components.getSimpleText("Mois", 18, MID_TEXT_X, INITIAL_PLAYERS_Y, SMALL_TEXT_WIDTH, BUTTON_HEIGHT));
		panel.add(Components.getSimpleText("Surnom", 18, RIGHT_TEXT_X, INITIAL_PLAYERS_Y, SMALL_TEXT_WIDTH, BUTTON_HEIGHT));
		panel.add(Components.getSimpleText("Surnom générique", 18, MID_TEXT_X,  500, LARGE_TEXT_WIDTH, BUTTON_HEIGHT));
		for (Month month : Month.ALL_MONTHS) {
			JLabel label = Components.getSimpleText(month.getName(), 16, MID_TEXT_X, y, SMALL_TEXT_WIDTH, BUTTON_HEIGHT);
			if (!monthsPlayed.contains(month))
				label.setForeground(Color.LIGHT_GRAY);
			monthLabels.put(month, label);
			panel.add(label);
			y += Y_OFFSET;
		}
		nameLabel = Components.getSimpleText(player.getName(), 25, LEFT_TEXT_X, 25, LARGE_TEXT_WIDTH, 30);
		panel.add(nameLabel);
		refreshStatsHeader();
	}

	private void onPlayerClick(Player player, Source source, JButton button) {
		// We're clicking on the button of the player who's already displayed
		if (source == Source.PLAYER_BUTTON_CLICK && currentPlayerButton == button)
			return;

		List<Month> monthsPlayed = calculatedMonthsPlayed.get(player);
		if (monthsPlayed == null) {
			monthsPlayed = getMonthsPlayed(player, currentYear);
			calculatedMonthsPlayed.put(player, monthsPlayed);
		}

		// No player button has been clicked before
		if (currentPlayerButton == null) {
			initializeBoxes(rightPanel);
			initializeRightPanel(rightPanel, player, monthsPlayed);
		} else if (source == Source.PLAYER_BUTTON_CLICK) {
			currentPlayerButton.setBackground(Color.WHITE);
			if (nameLabel != null)
				nameLabel.setText(player.getName());
		}

		// Temporary components include everything that needs to be changed no matter the source
		for (Component component : tempComponents)
			rightPanel.remove(component);
		tempComponents.clear();

		if (source == Source.PLAYER_BUTTON_CLICK) {
			button.setBackground(SELECTED_BUTTON_COLOR);
			refreshPlayerStats(player);
			currentPlayer = player;
			currentPlayerButton = button;
		}

		if (source == Source.MONTH_UPDATED || source == Source.PLAYER_COUNT_UPDATED || source == Source.YEAR_UPDATED) {
			refreshPlayerStats(player);
			refreshStatsHeader();
			if (source == Source.YEAR_UPDATED) {
				monthBox.removeAllItems();
				refreshMonthBoxItems();
			}
		}

		// Player was changed or year was updated, refresh months and related nicks and create new edit buttons
		if (source == Source.PLAYER_BUTTON_CLICK || source == Source.YEAR_UPDATED) {
			genericNickLabel = Components.getSimpleText(player.getNickname(currentYear, Tarot.NONE_STRING), 16, MID_TEXT_X, 530, HUGE_TEXT_WIDTH, BUTTON_HEIGHT);
			for (Component component : editButtons)
				rightPanel.remove(component);
			editButtons.clear();
			refreshNicknames(rightPanel, player, monthsPlayed);
			addEditButton(rightPanel, null, player, 500, name -> player.setNickname(currentYear, name));
			addTemporaryComponent(rightPanel, genericNickLabel);
		}

		rightPanel.revalidate();
		rightPanel.repaint();
	}

	private void promptNewPlayer() {
		String name = Components.prompt("Nom du joueur ?", "Ajouter un joueur");
		if (name == null) // Window was just closed
			return;

		if (name.isBlank()) { // User input was empty
			Components.popup("Veuillez entrer un nom valide.");
			promptNewPlayer();
			return;
		}

		if (Tarot.getPlayer(name) != null) {
			Components.popup("Il existe déjà un joueur à ce nom.");
			promptNewPlayer();
			return;
		}

		int lastPlayerID = Tarot.ORDERED_PLAYERS.size() + 1;
		Player player = Tarot.addPlayer(lastPlayerID, name, new HashMap<>(), new HashMap<>());
		player.write("players");
		Components.popup("Joueur ajouté avec succès.");

		playerCountLabel.setText(lastPlayerID + " joueurs");
		for (Component component : playerComponents)
			leftPanel.remove(component);

		playerComponents.clear();
		refreshPlayerComponents();
		repaint();
		revalidate();
	}

	private void promptNickname(Month month, Player player, Consumer<String> action) {
		String name = Components.prompt("Surnom du joueur ?", "Modifier un surnom");
		if (name == null) // Window was just closed
			return;

		if (name.isBlank()) // User input was empty
			name = null;

		if (name != null && (name.length() < MINIMUM_NICK_LENGTH || name.length() > MAXIMUM_NICK_LENGTH)) {
			Components.popup("Veuillez entrer un surnom de " + MINIMUM_NICK_LENGTH + " à " + MAXIMUM_NICK_LENGTH + " caractères.");
			return;
		}

		action.accept(name);
		player.edit();
		JLabel previousLabel = month == null ? genericNickLabel : nickLabels.get(month);
		if (previousLabel != null)
			previousLabel.setText(name == null ? Tarot.NONE_STRING : name);
	}

	private void refreshMonthBoxItems() {
		// Insert all months played during the year
		for (DateRecord date : Tarot.ALL_GAMES.keySet())
			if (date.year() == currentYear)
				monthBox.addItem(date.month().getName());
	}

	private void refreshPlayerComponents() {
		for (Player player : Tarot.ORDERED_PLAYERS) {
			JButton button = Components.getClickableText(player.getName(), 14);
			button.addMouseListener(Components.getLeftClickAdapter(() -> onPlayerClick(player, Source.PLAYER_BUTTON_CLICK, button)));
			playerComponents.add(button);
			leftPanel.add(button);
			Component empty = Components.getEmptySpace(15);
			playerComponents.add(empty); // Include them as well to properly reset left panel if a player is added
			leftPanel.add(empty);
		}
	}

	private void refreshPlayerStats(Player player) {
		for (Component component : playerStats)
			rightPanel.remove(component);
		playerStats.clear();

		DateRecord date = new DateRecord(currentMonth, currentYear);
		List<String> display = player.getStats(date, currentPlayerCount).getDisplay(currentPlayerCount);
		int y = INITIAL_PLAYERS_Y + 40;
		if (display.isEmpty()) {
			addStatsComponent(rightPanel, Components.getSimpleText("Pas de stats ce mois-ci.", 16, LEFT_TEXT_X, y, LARGE_TEXT_WIDTH, BUTTON_HEIGHT));
		} else {
			for (String line : display) {
				addStatsComponent(rightPanel, Components.getSimpleText(line, 16, LEFT_TEXT_X, y, HUGE_TEXT_WIDTH, BUTTON_HEIGHT));
				y += 20;
			}
		}
	}

	private void refreshNicknames(JPanel panel, Player player, Collection<Month> monthsPlayed) {
		int y = INITIAL_LABELS_Y;
		for (var entry : monthLabels.entrySet()) {
			Month month = entry.getKey();
			JLabel monthLabel = entry.getValue();
			JLabel nickLabel = nickLabels.get(month);
			if (monthsPlayed.contains(month)) {
				DateRecord date = new DateRecord(month, currentYear);
				monthLabel.setForeground(Color.BLACK);
				if (nickLabel == null) {
					nickLabel = Components.getSimpleText(player.getNickname(date, Tarot.NONE_STRING), 16, RIGHT_TEXT_X, y, 180, BUTTON_HEIGHT);
					nickLabels.put(month, nickLabel);
					panel.add(nickLabel);
				} else {
					nickLabel.setText(player.getNickname(date, Tarot.NONE_STRING));
				}
				addEditButton(panel, month, player, y, name -> player.setNickname(date, name));
			} else {
				monthLabel.setForeground(Color.LIGHT_GRAY);
				if (nickLabel != null) {
					nickLabels.remove(month);
					panel.remove(nickLabel);
				}
			}
			y += Y_OFFSET;
		}
	}

	private void refreshStatsHeader() {
		String header = "Stats – " + currentMonth.getShortName() + " " + currentYear + " – " + currentPlayerCount + " joueurs";
		if (statsLabel == null) {
			statsLabel = Components.getSimpleText(header, 18, LEFT_TEXT_X, INITIAL_PLAYERS_Y, HUGE_TEXT_WIDTH, BUTTON_HEIGHT);
			rightPanel.add(statsLabel);
		} else {
			statsLabel.setText(header);
		}
	}

	FramePlayerProfiles() {
		create("Menu des joueurs", 300, 200, LEFT_PANEL_WIDTH + RIGHT_PANEL_WIDTH, GLOBAL_PANEL_HEIGHT);

		leftPanel = panel(0, false, true);
		leftPanel.setSize(LEFT_PANEL_WIDTH, GLOBAL_PANEL_HEIGHT);
		leftPanel.setVisible(true);

		playerCountLabel = Components.getSimpleText(Tarot.ORDERED_PLAYERS.size() + " joueurs", 18, 10, 10, SMALL_TEXT_WIDTH, BUTTON_HEIGHT);
		leftPanel.add(playerCountLabel);
		leftPanel.add(Components.getEmptySpace(10));

		JButton newPlayerButton = Components.getClickableText("Ajouter", 15);
		newPlayerButton.addMouseListener(Components.getLeftClickAdapter(this::promptNewPlayer));
		newPlayerButton.setBackground(new Color(220, 255, 225));
		leftPanel.add(newPlayerButton);
		leftPanel.add(Components.getEmptySpace(25));

		rightPanel = panel(-1, false, false);
		rightPanel.setSize(RIGHT_PANEL_WIDTH, GLOBAL_PANEL_HEIGHT);
		rightPanel.setVisible(true);

		refreshPlayerComponents();

		JSplitPane splitPane = Components.getSplitPane(leftPanel, rightPanel, LEFT_PANEL_WIDTH, RIGHT_PANEL_WIDTH, GLOBAL_PANEL_HEIGHT);
		splitPane.add(scrollPane(leftPanel));
		add(splitPane);

		setVisible(true);
	}

	private enum Source {

		MONTH_UPDATED,
		PLAYER_BUTTON_CLICK,
		PLAYER_COUNT_UPDATED,
		YEAR_UPDATED

	}

}
