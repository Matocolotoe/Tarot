package fr.giovanni75.tarot.frames;

import fr.giovanni75.tarot.DateRecord;
import fr.giovanni75.tarot.Files;
import fr.giovanni75.tarot.Tarot;
import fr.giovanni75.tarot.objects.Game;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FrameMainMenu extends TarotFrame {

	static FrameMainMenu MAIN_MENU;

	private static final Color RIGHT_CLICKED_COLOR = new Color(235, 245, 255);

	private static final int MAX_GAMES_DISPLAYED = 100;

	private final JPanel mainPanel;
	private final List<Component> components = new ArrayList<>();

	private void initializeMenus() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu dataMenu = new JMenu("Données");
		JMenuItem addGameItem = new JMenuItem("Ajouter une partie");
		JMenuItem backupItem = new JMenuItem("Créer une sauvegarde");
		JMenuItem exportItem = new JMenuItem("Exporter les données");
		JMenuItem playersItem = new JMenuItem("Menu des joueurs");
		dataMenu.add(addGameItem);
		dataMenu.add(backupItem);
		dataMenu.add(exportItem);
		dataMenu.add(playersItem);

		addGameItem.addActionListener(event -> new FrameNewGame(null));
		playersItem.addActionListener(event -> new FramePlayerProfiles());
		menuBar.add(dataMenu);

		backupItem.addActionListener(event -> {
			Files.createBackup();
			Components.popup("Données sauvegardées avec succès.");
		});

		exportItem.addActionListener(event -> {
			Files.createLeaderboards();
			Components.popup("Données exportées avec succès.");
		});

		int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
		addGameItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, mask));
		backupItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, mask));
		exportItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, mask));
		playersItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, mask));

		List<DateRecord> dates = new ArrayList<>(Tarot.ALL_GAMES.keySet());
		dates.sort(Comparator.reverseOrder());

		for (int i = 5; i > 2; i--) {
			final int players = i; // Must be final for constructors below
			JMenu statsMenu = new JMenu("Tarot à " + players);
			JMenu graphMenu = new JMenu("Graphiques");
			JMenu globalStatsMenu = new JMenu("Stats générales");
			JMenu invidiualStatsMenu = new JMenu("Stats individuelles");
			JMenu periodicStatsMenu = new JMenu("Stats périodiques");
			for (DateRecord date : dates) {
				String dateName = date.month().getShortName() + " " + date.year();
				JMenuItem graphItem = new JMenuItem(dateName);
				JMenuItem globalStatsItem = new JMenuItem(dateName);
				JMenuItem playerStatsItem = new JMenuItem(dateName);
				JMenuItem periodicStatsItem = new JMenuItem(dateName);
				graphItem.addActionListener(event -> new FrameSelection("Évolution des scores", date, players,
						(displayed, selected) -> new FrameScoreGraphs(displayed, selected, date, players)));
				globalStatsItem.addActionListener(event -> new FrameGlobalStats(date, players));
				playerStatsItem.addActionListener(event -> new FramePlayerStats(date, players));
				periodicStatsItem.addActionListener(event -> new FrameSelection("Statistiques périodiques", date, players,
						(displayed, selected) -> new FramePeriodicStats(displayed, selected, date, players)));
				graphMenu.add(graphItem);
				globalStatsMenu.add(globalStatsItem);
				invidiualStatsMenu.add(playerStatsItem);
				periodicStatsMenu.add(periodicStatsItem);
			}
			statsMenu.add(graphMenu);
			statsMenu.add(globalStatsMenu);
			statsMenu.add(invidiualStatsMenu);
			statsMenu.add(periodicStatsMenu);
			menuBar.add(statsMenu);
		}
	}

	private void onGameRightClick(Game game, JButton button, int x, int y) {
		JPopupMenu menu = new JPopupMenu("Partie");
		JMenuItem detailsMenuItem = new JMenuItem("Détails");
		JMenuItem editMenuItem = new JMenuItem("Modifier");
		JMenuItem deleteMenuItem = new JMenuItem("Supprimer");
		menu.add(detailsMenuItem);
		menu.add(editMenuItem);
		menu.add(deleteMenuItem);
		button.setBackground(RIGHT_CLICKED_COLOR);
		menu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {}
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				button.setBackground(Components.DEFAULT_BUTTON_COLOR);
			}
		});
		detailsMenuItem.addActionListener(event -> Components.popup(game.getDetails(), "Détails de la partie"));
		editMenuItem.addActionListener(event -> new FrameNewGame(game));
		deleteMenuItem.addActionListener(event -> promptGameDeletion(game));
		menu.show(button, x, y);
	}

	private void promptGameDeletion(Game game) {
		int option = Components.promptConfirmation("Voulez-vous supprimer cette partie ?", "Suppression de partie");
		if (option == JOptionPane.YES_OPTION) {
			game.applyResults(Game.DEFAULT_CONVERTER, Game.REMOVE_GAME_DIRECTION);
			game.delete();
			reloadGames();
		}
	}

	void reloadGames() {
		for (Component component : components)
			mainPanel.remove(component);
		components.clear();
		showAllGames();
		repaint();
		revalidate();
	}

	private void showAllGames() {
		int total = 0;
		for (var entry : Tarot.ALL_GAMES.entrySet()) {
			// Avoid executing one iteration if threshold has already been reached
			if (total == MAX_GAMES_DISPLAYED)
				break;
			List<Game> games = entry.getValue();
			int size = games.size();
			components.add(Components.getSimpleText(entry.getKey().getName() + " – " + size + (size == 1 ? " partie " : " parties"), 20));
			components.add(Components.getEmptySpace(18));
			for (Game game : games.reversed()) {
				total++;
				JButton button = Components.getClickableText(game.getDescription(), 15);
				button.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if (SwingUtilities.isRightMouseButton(e))
							onGameRightClick(game, button, e.getX(), e.getY());
					}
				});
				button.setBackground(Components.DEFAULT_BUTTON_COLOR);
				components.add(button);
				components.add(Components.getEmptySpace(15));
				if (total >= MAX_GAMES_DISPLAYED)
					break;
			}
			components.add(Components.getEmptySpace(12));
		}
		for (Component component : components)
			mainPanel.add(component);
	}

	public FrameMainMenu() {
		MAIN_MENU = this;

		create("Tarot – Compteur de points", 300, 100, 800, 600);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		mainPanel = panel(280, true, true);
		initializeMenus();
		showAllGames();

		URL url = ClassLoader.getSystemResource("logo.png");
		setIconImage(new ImageIcon(url).getImage());
		setVisible(true);
	}

}
