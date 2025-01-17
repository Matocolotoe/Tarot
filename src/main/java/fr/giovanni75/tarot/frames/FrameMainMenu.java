package fr.giovanni75.tarot.frames;

import fr.giovanni75.tarot.DateRecord;
import fr.giovanni75.tarot.Tarot;
import fr.giovanni75.tarot.objects.Game;
import fr.giovanni75.tarot.objects.Player;

import javax.swing.*;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.net.URL;
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
		setBounds(300, 90, 800, 600);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setTitle("Tarot");

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu settingsMenu = new JMenu("Données");
		menuBar.add(settingsMenu);

		JMenuItem addGameItem = new JMenuItem("Ajouter une partie...");
		JMenuItem addPlayerItem = new JMenuItem("Ajouter un joueur...");
		JMenuItem backupItem = new JMenuItem("Créer une sauvegarde...");
		JMenuItem exportItem = new JMenuItem("Exporter les données...");
		settingsMenu.add(addGameItem);
		settingsMenu.add(addPlayerItem);
		settingsMenu.add(backupItem);
		settingsMenu.add(exportItem);

		int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
		addGameItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, mask));
		addPlayerItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, mask));
		backupItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, mask));
		exportItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, mask));

		addGameItem.addActionListener(event -> new FrameNewGame());
		addPlayerItem.addActionListener(event -> inputPlayer());
		backupItem.addActionListener(event -> {
			Tarot.createBackup("games");
			Tarot.createBackup("players");
			Components.popup("Données sauvegardées avec succès.");
		});
		exportItem.addActionListener(event -> {
			Tarot.createLeaderboards();
			Components.popup("Données exportées avec succès.");
		});

		mainPanel.add(Components.getSimpleText(" ", 20));
		mainPanel.add(Components.getSimpleText("    Historique des parties", 20));
		mainPanel.add(Components.getSimpleText(" ", 20));

		for (Map.Entry<DateRecord, List<Game>> entry : Tarot.GAMES.entrySet()) {
			DateRecord date = entry.getKey();
			mainPanel.add(Components.getSimpleText("     " + date.month().getName() + " " + date.year(), 18));
			mainPanel.add(Components.getSimpleText(" ", 16));
			for (Game game : entry.getValue()) {
				mainPanel.add(Components.getSimpleText("      " + game.getDescriptionFirstLine(), 15));
				mainPanel.add(Components.getSimpleText("      " + game.getDescriptionSecondLine(), 15));
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
