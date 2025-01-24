package fr.giovanni75.tarot.frames;

import fr.giovanni75.tarot.DateRecord;
import fr.giovanni75.tarot.Tarot;
import fr.giovanni75.tarot.objects.Game;
import fr.giovanni75.tarot.objects.Player;

import javax.swing.*;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FrameMainMenu extends JFrame {

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

		UUID uuid;
		do {
			uuid = UUID.randomUUID();
		} while (Tarot.getPlayer(uuid) != null);

		Player player = Tarot.addPlayer(name, uuid);
		player.write("players");
		Components.popup("Joueur ajouté avec succès.\nNom : " + name);
	}

	public FrameMainMenu() {
		setBounds(300, 100, 800, 600);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setTitle("Tarot – Compteur de points");

		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(Components.getStandardBorder());
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu addMenu = new JMenu("Ajouter");
		JMenuItem addGameItem = new JMenuItem("Ajouter une partie...");
		JMenuItem addPlayerItem = new JMenuItem("Ajouter un joueur...");
		addGameItem.addActionListener(event -> new FrameNewGame());
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
			Tarot.createBackup("games");
			Tarot.createBackup("players");
			Components.popup("Données sauvegardées avec succès.");
		});

		exportItem.addActionListener(event -> {
			Tarot.createLeaderboards();
			Components.popup("Données exportées avec succès.");
		});

		int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
		backupItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, mask));
		exportItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, mask));

		List<DateRecord> dates = new ArrayList<>(Tarot.ALL_GAMES.keySet());
		dates.sort((d1, d2) -> 1 - d1.compareTo(d2));
		for (int i = 5; i > 2; i--) {
			final int players = i; // Must be final for FramePlayerStats constructor
			JMenu statsMenu = new JMenu("Tarot à " + players);
			JMenu globalStatsMenu = new JMenu("Stats générales");
			JMenu playerStatsMenu = new JMenu("Stats individuelles");
			for (DateRecord date : dates) {
				JMenuItem globalStatsItem = new JMenuItem(date.getName());
				JMenuItem playerStatsItem = new JMenuItem(date.getName());
				globalStatsItem.addActionListener(event -> new FrameGlobalStats(date, players));
				playerStatsItem.addActionListener(event -> new FramePlayerStats(date, players));
				globalStatsMenu.add(globalStatsItem);
				playerStatsMenu.add(playerStatsItem);
			}
			statsMenu.add(globalStatsMenu);
			statsMenu.add(playerStatsMenu);
			menuBar.add(statsMenu);
		}

		for (Map.Entry<DateRecord, List<Game>> entry : Tarot.ALL_GAMES.entrySet()) {
			mainPanel.add(Components.getSimpleText(entry.getKey().getName(), 20));
			mainPanel.add(Components.getSimpleText(" ", 18));
			for (Game game : entry.getValue()) {
				mainPanel.add(Components.getSimpleText(game.getDescriptionFirstLine(), 15));
				mainPanel.add(Components.getSimpleText(game.getDescriptionSecondLine(), 15));
				mainPanel.add(Components.getSimpleText(" ", 15));
			}
		}

		JScrollPane scrollPane = new JScrollPane(mainPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(18);
		add(scrollPane);

		URL url = ClassLoader.getSystemResource("logo.png");
		setIconImage(new ImageIcon(url).getImage());
		setVisible(true);
	}

}
