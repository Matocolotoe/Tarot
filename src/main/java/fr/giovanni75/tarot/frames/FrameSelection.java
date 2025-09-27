package fr.giovanni75.tarot.frames;

import fr.giovanni75.tarot.DateRecord;
import fr.giovanni75.tarot.Tarot;
import fr.giovanni75.tarot.objects.Game;

import javax.swing.*;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

class FrameSelection extends TarotFrame {

	private static final Color LEFT_CLICKED_COLOR = new Color(255, 200, 200);

	private Game firstGame;
	private JButton firstButton;

	FrameSelection(String header, DateRecord date, int players, BiConsumer<List<Game>, List<Game>> action) {
		List<Game> displayed = new ArrayList<>();
		for (Game game : Tarot.ALL_GAMES.get(date))
			if (game.players.length == players)
				displayed.add(game);

		if (displayed.isEmpty()) {
			Components.popup(Components.NO_GAME_AVAILABLE);
			return;
		}

		create(header + " – Choix de parties", 400, 200, 500, 800);

		JPanel mainPanel = panel(280, true, true);
		mainPanel.add(Components.getSimpleText(date.getName() + " – " + players + " joueurs", 20));
		mainPanel.add(Components.getSimpleText("Sélectionnez des parties", 20));
		mainPanel.add(Components.getEmptySpace(18));

		int day = displayed.getFirst().dayOfMonth;
		for (Game game : displayed) {
			if (day != game.dayOfMonth) {
				mainPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
				mainPanel.add(Components.getEmptySpace(15));
			}
			JButton button = Components.getClickableText(game.getDescription(), 15);
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (SwingUtilities.isLeftMouseButton(e)) {
						if (firstGame == null) {
							firstButton = button;
							firstGame = game;
							button.setBackground(LEFT_CLICKED_COLOR);
						} else if (firstGame == game) {
							firstButton.setBackground(Components.DEFAULT_BUTTON_COLOR);
							firstButton = null;
							firstGame = null;
						} else {
							button.setBackground(LEFT_CLICKED_COLOR);
							execute(displayed, game, button, header, action);
						}
					}
				}
			});
			button.setBackground(Components.DEFAULT_BUTTON_COLOR);
			mainPanel.add(button);
			mainPanel.add(Components.getEmptySpace(15));
			day = game.dayOfMonth;
		}

		setVisible(true);
	}

	private void execute(List<Game> displayed, Game secondGame, JButton secondButton, String header, BiConsumer<List<Game>, List<Game>> action) {
		// Make sure games are ordered (older first, order is reversed in displayed list)
		int firstIndex = displayed.indexOf(firstGame);
		int secondIndex = displayed.indexOf(secondGame);
		int minIndex = Math.min(firstIndex, secondIndex);
		int maxIndex = Math.max(firstIndex, secondIndex);

		List<Game> selected = new ArrayList<>();
		for (int i = minIndex; i <= maxIndex; i++)
			selected.add(displayed.get(i));

		int option = Components.promptConfirmation("Affichage de " + selected.size() + " parties ?", header);
		if (option == JOptionPane.YES_OPTION) {
			dispose();
			action.accept(displayed, selected);
		} else {
			firstButton.setBackground(Components.DEFAULT_BUTTON_COLOR);
			secondButton.setBackground(Components.DEFAULT_BUTTON_COLOR);
			firstButton = null;
			firstGame = null;
		}
	}

}
