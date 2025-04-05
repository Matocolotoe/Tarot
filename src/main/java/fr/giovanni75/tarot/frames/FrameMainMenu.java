package fr.giovanni75.tarot.frames;

import fr.giovanni75.tarot.DateRecord;
import fr.giovanni75.tarot.Files;
import fr.giovanni75.tarot.Tarot;
import fr.giovanni75.tarot.objects.Game;
import fr.giovanni75.tarot.objects.Player;

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
import java.util.List;
import java.util.Map;

public class FrameMainMenu extends JFrame {

	static FrameMainMenu MAIN_MENU;

	private static final Color DEFAULT_BUTTON_COLOR = new Color(247, 247, 247);
	private static final Color SELECTED_BUTTON_COLOR = new Color(235, 245, 255);

	private static final int MAX_GAMES_DISPLAYED = 100;

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

		Player player = Tarot.addPlayer(Tarot.ORDERED_PLAYERS.size() + 1, name);
		player.write("players");
		Components.popup("Joueur ajouté avec succès.\nNom : " + name);
	}

	private final JPanel mainPanel;
	private final List<Component> components = new ArrayList<>();

	private void initializeMenus() {
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu addMenu = new JMenu("Ajouter");
		JMenuItem addGameItem = new JMenuItem("Ajouter une partie...");
		JMenuItem addPlayerItem = new JMenuItem("Ajouter un joueur...");
		addGameItem.addActionListener(event -> new FrameNewGame(null));
		addPlayerItem.addActionListener(event -> inputPlayer());
		addMenu.add(addGameItem);
		addMenu.add(addPlayerItem);
		menuBar.add(addMenu);

		JMenu dataMenu = new JMenu("Données");
		JMenuItem backupItem = new JMenuItem("Créer une sauvegarde...");
		JMenuItem exportItem = new JMenuItem("Exporter les données...");
		dataMenu.add(backupItem);
		dataMenu.add(exportItem);
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
		backupItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, mask));
		exportItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, mask));

		List<DateRecord> dates = new ArrayList<>(Tarot.ALL_GAMES.keySet());
		dates.sort(DateRecord::compareTo);
		for (int i = 5; i > 2; i--) {
			final int players = i; // Must be final for constructors below
			JMenu statsMenu = new JMenu("Tarot à " + players);
			JMenu graphMenu = new JMenu("Graphiques");
			JMenu globalStatsMenu = new JMenu("Stats générales");
			JMenu playerStatsMenu = new JMenu("Stats individuelles");
			for (DateRecord date : dates) {
				String dateName = date.getName();
				JMenuItem graphItem = new JMenuItem(dateName);
				JMenuItem globalStatsItem = new JMenuItem(dateName);
				JMenuItem playerStatsItem = new JMenuItem(dateName);
				graphItem.addActionListener(event -> {
					int minDay = Components.promptDay("De quel jour ?", "Graphiques – " + dateName);
					if (minDay == -1) // Window was just closed
						return;
					int maxDay = Components.promptDay("À quel jour ?", "Graphiques – " + dateName);
					if (maxDay == -1) // Window was just closed
						return;
					new FrameScoreGraphs(minDay, maxDay, date, players);
				});
				globalStatsItem.addActionListener(event -> new FrameGlobalStats(date, players));
				playerStatsItem.addActionListener(event -> new FramePlayerStats(date, players));
				graphMenu.add(graphItem);
				globalStatsMenu.add(globalStatsItem);
				playerStatsMenu.add(playerStatsItem);
			}
			statsMenu.add(graphMenu);
			statsMenu.add(globalStatsMenu);
			statsMenu.add(playerStatsMenu);
			menuBar.add(statsMenu);
		}
	}

	private void onGameRightClick(Game game, JButton button, int x, int y) {
		JPopupMenu menu = new JPopupMenu("Partie");
		JMenuItem editMenuItem = new JMenuItem("Éditer");
		JMenuItem deleteMenuItem = new JMenuItem("Supprimer");
		menu.add(editMenuItem);
		menu.add(deleteMenuItem);
		button.setBackground(SELECTED_BUTTON_COLOR);
		menu.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {}
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				button.setBackground(DEFAULT_BUTTON_COLOR);
			}
		});
		editMenuItem.addActionListener(event -> new FrameNewGame(game));
		deleteMenuItem.addActionListener(event -> promptGameDeletion(game));
		menu.show(button, x, y);
	}

	private void promptGameDeletion(Game game) {
		int option = Components.promptConfirmation("Voulez-vous supprimer la partie ?", "Suppression de partie");
		if (option == JOptionPane.YES_OPTION) {
			game.delete();
			reloadGames();
			Components.popup("Partie supprimée avec succès.");
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
		for (Map.Entry<DateRecord, List<Game>> entry : Tarot.ALL_GAMES.entrySet()) {
			// Avoid executing one iteration if threshold has already been reached
			if (total == MAX_GAMES_DISPLAYED)
				break;
			components.add(Components.getSimpleText(entry.getKey().getName(), 20));
			components.add(Components.getEmptyText(18));
			for (Game game : entry.getValue()) {
				total++;
				// Text inside a button ignores "\n", convert it to HTML and skip lines this way
				String text = "<html>" + String.join("<br>", game.getDescription()) + "</html>";
				JButton button = Components.getClickableText(text, 15);
				button.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if (SwingUtilities.isRightMouseButton(e))
							onGameRightClick(game, button, e.getX(), e.getY());
					}
				});
				button.setBackground(DEFAULT_BUTTON_COLOR);
				components.add(button);
				components.add(Components.getEmptyText(15));
				if (total >= MAX_GAMES_DISPLAYED)
					break;
			}
			components.add(Components.getEmptyText(12));
		}
		for (Component component : components)
			mainPanel.add(component);
	}

	public FrameMainMenu() {
		MAIN_MENU = this;

		setBounds(300, 100, 800, 600);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setTitle("Tarot – Compteur de points");

		mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 0, 280));
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		initializeMenus();
		showAllGames();

		JScrollPane scrollPane = new JScrollPane(mainPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(18);
		add(scrollPane);

		URL url = ClassLoader.getSystemResource("logo.png");
		setIconImage(new ImageIcon(url).getImage());
		setVisible(true);
	}

}
