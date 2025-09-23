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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.function.Consumer;

class FramePlayerProfiles extends TarotFrame {

	private static final Color SELECTED_BUTTON_COLOR = new Color(226, 238, 255);

	private static final int MINIMUM_NICK_LENGTH = 2;
	private static final int MAXIMUM_NICK_LENGTH = 30;

	private static final int GLOBAL_PANEL_HEIGHT = 650;
	private static final int LEFT_PANEL_WIDTH = 200;
	private static final int RIGHT_PANEL_WIDTH = 500;

	private static final int EDIT_BUTTON_X = 350;
	private static final int INITIAL_Y = 105;
	private static final int LEFT_TEXT_X = 25;
	private static final int RIGHT_TEXT_X = 150;
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

	private JButton currentPlayerButton;
	private int currentYear = getMaxPlayedYear();

	private final List<Component> tempComponents = new ArrayList<>();
	private final JComboBox<Integer> yearBox = new JComboBox<>();
	private final Map<Month, JLabel> monthLabels = new EnumMap<>(Month.class);
	private final Map<Month, JLabel> nickLabels = new EnumMap<>(Month.class);

	private JLabel genericNickLabel;
	private JLabel nameLabel;

	private void addTempComponent(JPanel panel, Component component) {
		tempComponents.add(component);
		panel.add(component);
	}

	private void createYearBox(JPanel panel, Player player, List<Month> monthsPlayed) {
		for (int year = getMaxPlayedYear(); year >= DateRecord.START_YEAR; year--)
			yearBox.addItem(year);

		yearBox.addActionListener(event -> {
			Object selectedYear = yearBox.getSelectedItem();
			if (selectedYear == null)
				return;
			if (currentYear != (int) selectedYear) {
				currentYear = (int) selectedYear;
				onPlayerClick(player, Source.YEAR_UPDATED, currentPlayerButton, panel);
			}
		});

		yearBox.setLocation(EDIT_BUTTON_X, 25);
		yearBox.setSize(100, 22);
		panel.add(yearBox);

		int y = INITIAL_Y;
		panel.add(Components.getSimpleText("Mois", 18, LEFT_TEXT_X, 75, 100, 20));
		panel.add(Components.getSimpleText("Surnom", 18, RIGHT_TEXT_X, 75, 100, 20));
		panel.add(Components.getSimpleText("Surnom générique", 18, LEFT_TEXT_X,  500, 200, 20));
		for (Month month : Month.ALL_MONTHS) {
			JLabel label = Components.getSimpleText(month.getName(), 16, LEFT_TEXT_X, y, 100, 20);
			if (!monthsPlayed.contains(month))
				label.setForeground(Color.LIGHT_GRAY);
			monthLabels.put(month, label);
			panel.add(label);
			y += Y_OFFSET;
		}

		nameLabel = Components.getSimpleText(player.getName(), 20, LEFT_TEXT_X, 25, 200, 20);
		panel.add(nameLabel);
	}

	private JButton getEditNicknameButton(Month month, Player player, int y, Consumer<String> action) {
		JButton editButton = Components.getClickableText("Modifier", 12, EDIT_BUTTON_X, y, 0, 0);
		editButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					String name = Components.prompt("Surnom du joueur ?", "Modifier un surnom");
					if (name == null) // Window was just closed
						return;
					if (name.isBlank() || name.length() < MINIMUM_NICK_LENGTH || name.length() > MAXIMUM_NICK_LENGTH) {
						Components.popup("Veuillez entrer un surnom de " + MINIMUM_NICK_LENGTH + " à " + MAXIMUM_NICK_LENGTH + " caractères.");
						return;
					}
					if (name.equals(player.getName()))
						name = null;
					action.accept(name);
					player.edit();
					JLabel previousLabel = month == null ? genericNickLabel : nickLabels.get(month);
					if (previousLabel != null)
						previousLabel.setText(name == null ? Tarot.NONE_STRING : name);
				}
			}
		});
		return editButton;
	}

	private void onPlayerClick(Player player, Source source, JButton button, JPanel panel) {
		// We're clicking on the button of the player who's already displayed
		if (source == Source.PLAYER_BUTTON_CLICK && currentPlayerButton == button)
			return;

		List<Month> monthsPlayed = getMonthsPlayed(player, currentYear);

		// No player button has been clicked before
		if (currentPlayerButton == null) {
			createYearBox(panel, player, monthsPlayed);
		} else if (source == Source.PLAYER_BUTTON_CLICK) {
			currentPlayerButton.setBackground(Components.DEFAULT_BUTTON_COLOR);
			if (nameLabel != null)
				nameLabel.setText(player.getName());
		}

		// Temporary components include everything that needs to be changed no matter the source
		for (Component component : tempComponents)
			panel.remove(component);
		tempComponents.clear();

		if (source == Source.PLAYER_BUTTON_CLICK) {
			button.setBackground(SELECTED_BUTTON_COLOR);
			currentPlayerButton = button;
		}

		// Player was changed or year was updated, refresh months and related nicks and create new edit buttons
		if (source == Source.PLAYER_BUTTON_CLICK || source == Source.YEAR_UPDATED) {
			genericNickLabel = Components.getSimpleText(player.getNickname(currentYear, Tarot.NONE_STRING), 16, LEFT_TEXT_X, 530, 200, 20);
			refreshNicknames(panel, player, monthsPlayed);
			addTempComponent(panel, genericNickLabel);
			addTempComponent(panel, getEditNicknameButton(null, player, 500, name -> player.setNickname(currentYear, name)));
		}

		panel.revalidate();
		panel.repaint();
	}

	private void refreshNicknames(JPanel panel, Player player, Collection<Month> monthsPlayed) {
		int y = INITIAL_Y;
		for (var entry : monthLabels.entrySet()) {
			Month month = entry.getKey();
			JLabel monthLabel = entry.getValue();
			JLabel nickLabel = nickLabels.get(month);
			if (monthsPlayed.contains(month)) {
				DateRecord date = new DateRecord(month, currentYear);
				monthLabel.setForeground(Color.BLACK);
				if (nickLabel == null) {
					nickLabel = Components.getSimpleText(player.getNickname(date, Tarot.NONE_STRING), 16, RIGHT_TEXT_X, y, 400, 20);
					nickLabels.put(month, nickLabel);
					panel.add(nickLabel);
				} else {
					nickLabel.setText(player.getNickname(date, Tarot.NONE_STRING));
				}
				addTempComponent(panel, getEditNicknameButton(month, player, y, name -> player.setNickname(date, name)));
			} else {
				monthLabel.setForeground(Color.GRAY);
				if (nickLabel != null) {
					nickLabels.remove(month);
					panel.remove(nickLabel);
				}
			}
			y += Y_OFFSET;
		}
	}

	FramePlayerProfiles() {
		create("Menu des joueurs", 300, 200, LEFT_PANEL_WIDTH + RIGHT_PANEL_WIDTH, GLOBAL_PANEL_HEIGHT);

		JPanel leftPanel = panel(0, false, true);
		leftPanel.setSize(LEFT_PANEL_WIDTH, GLOBAL_PANEL_HEIGHT);
		leftPanel.setVisible(true);

		leftPanel.add(Components.getSimpleText(Tarot.ORDERED_PLAYERS.size() + " joueurs", 18, 10, 10, 120, 20));
		leftPanel.add(Components.getEmptySpace(10));

		// TODO reload menu when doing this
		JButton newPlayerButton = Components.getClickableText("Ajouter", 15);
		newPlayerButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e))
					inputPlayer();
			}
		});
		newPlayerButton.setBackground(new Color(220, 255, 225));
		leftPanel.add(newPlayerButton);
		leftPanel.add(Components.getEmptySpace(25));

		JPanel rightPanel = panel(-1, false, false);
		rightPanel.setSize(RIGHT_PANEL_WIDTH, GLOBAL_PANEL_HEIGHT);
		rightPanel.setVisible(true);

		for (Player player : Tarot.ORDERED_PLAYERS) {
			JButton button = Components.getClickableText(player.getName(), 14);
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (SwingUtilities.isLeftMouseButton(e))
						onPlayerClick(player, Source.PLAYER_BUTTON_CLICK, button, rightPanel);
				}
			});
			leftPanel.add(button);
			leftPanel.add(Components.getEmptySpace(15));
		}

		JSplitPane splitPane = Components.getSplitPane(leftPanel, rightPanel, LEFT_PANEL_WIDTH, RIGHT_PANEL_WIDTH, GLOBAL_PANEL_HEIGHT);
		splitPane.add(scrollPane(leftPanel));
		add(splitPane);

		setVisible(true);
	}

	private static void inputPlayer() {
		String name = Components.prompt("Nom du joueur ?", "Ajouter un joueur");
		if (name == null) // Window was just closed
			return;

		if (name.isBlank()) {
			Components.popup("Veuillez entrer un nom valide.");
			inputPlayer();
			return;
		}

		if (Tarot.getPlayer(name) != null) {
			Components.popup("Il existe déjà un joueur à ce nom.");
			inputPlayer();
			return;
		}

		Player player = Tarot.addPlayer(Tarot.ORDERED_PLAYERS.size() + 1, name, null, null);
		player.write("players");
		Components.popup("Joueur ajouté avec succès.");
	}

	private enum Source {

		PLAYER_BUTTON_CLICK,
		YEAR_UPDATED

	}

}
