package fr.giovanni75.tarot.frames;

import fr.giovanni75.tarot.Tarot;
import fr.giovanni75.tarot.enums.*;
import fr.giovanni75.tarot.objects.Game;
import fr.giovanni75.tarot.objects.LocalPlayer;

import javax.swing.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

class FrameNewGame extends TarotFrame implements ActionListener {

	private static final int COMBO_BOX_BASE_X = 150;
	private static final int COMBO_BOX_BASE_Y = 80;
	private static final int LARGE_TEXT_WIDTH = 140;
	private static final int PLAYER_BUTTON_BASE_X = 190;
	private static final int PLAYER_X_SPACING = 110;
	private static final int SECONDARY_BUTTON_BASE_X = 200;
	private static final int SMALL_TEXT_HEIGHT = 20;
	private static final int SMALL_TEXT_WIDTH = 100;
	private static final int TEXT_HEIGHT = 60;

	private static final String[] LAST_SELECTED_NAMES = new String[5];

	static {
		for (int i = 0; i < 5; i++)
			LAST_SELECTED_NAMES[i] = Tarot.NONE_STRING;
	}

	private final Game baseGame;

	@SuppressWarnings("unchecked")
	private final JComboBox<String>[] handfulBoxes = new JComboBox[5];

	@SuppressWarnings("unchecked")
	private final JComboBox<String>[] miseryBoxes = new JComboBox[5];

	@SuppressWarnings("unchecked")
	private final JComboBox<String>[] playerNameBoxes = new JComboBox[5];

	private final JComboBox<String> petitAuBoutBox;
	private final JComboBox<String> slamBox;

	private final JLabel calledLabel;
	private final JLabel[] noneStrings = new JLabel[5];
	private int emptyNames = 5;

	private final JSlider scoreSlider = new JSlider(JSlider.HORIZONTAL, 0, 91, 51);

	private final JRadioButton[] attackerButtons = new JRadioButton[5];
	private final JRadioButton[] calledButtons = new JRadioButton[5];
	private final JRadioButton[] contractButtons = new JRadioButton[Contract.ALL_CONTRACTS.length];
	private final JRadioButton[] oudlersButtons = new JRadioButton[Oudlers.ALL_OUDLERS.length];

	private final JButton submitButton;

	private static boolean exists(Object[] array, Predicate<Object> predicate) {
		for (Object element : array)
			if (predicate.test(element))
				return true;
		return false;
	}

	private static JComboBox<String> getEnumNameList(Nameable[] values, String none, int x, int y, int width) {
		String[] names = new String[values.length + 1];
		names[0] = none;
		for (int i = 1; i <= values.length; i++)
			names[i] = values[i - 1].getName();
		JComboBox<String> box = new JComboBox<>(names);
		box.setFont(Components.getFont(12));
		box.setLocation(x, y);
		box.setSize(width, SMALL_TEXT_HEIGHT);
		return box;
	}

	private static JComboBox<String> getPlayerNameList(String[] names, int index, int x) {
		JComboBox<String> box = new JComboBox<>(names);
		box.setFont(Components.getFont(12));
		box.setLocation(x, COMBO_BOX_BASE_Y);
		box.setSelectedItem(LAST_SELECTED_NAMES[index]);
		box.setSize(SMALL_TEXT_WIDTH, SMALL_TEXT_HEIGHT);
		return box;
	}

	private boolean hasEmptyName(JComboBox<String> box) {
		return box.getSelectedIndex() == 0;
	}

	private void updateCalledLine(boolean showButtons) {
		calledLabel.setForeground(showButtons ? Color.BLACK : Color.LIGHT_GRAY);
		for (JRadioButton button : calledButtons)
			button.setVisible(showButtons);
		for (JLabel label : noneStrings)
			label.setVisible(!showButtons);
		repaint();
		revalidate();
	}

	FrameNewGame(Game baseGame) {
		this.baseGame = baseGame;

		create(baseGame == null ? "Ajouter une partie" : "Modifier une partie", 300, 200, 800, 700);

		JPanel mainPanel = panel(-1, true, false);

		final List<String> nameList = new ArrayList<>(Tarot.PLAYER_NAMES);
		nameList.sort(String::compareTo);
		nameList.addFirst(Tarot.NONE_STRING);

		mainPanel.add(Components.getSimpleText("Joueurs", 18, SwingConstants.RIGHT, COMBO_BOX_BASE_X - 120, COMBO_BOX_BASE_Y - 20, SMALL_TEXT_WIDTH, TEXT_HEIGHT));
		mainPanel.add(Components.getSimpleText("Misères", 18, SwingConstants.RIGHT, COMBO_BOX_BASE_X - 120, COMBO_BOX_BASE_Y + 20, SMALL_TEXT_WIDTH, TEXT_HEIGHT));
		mainPanel.add(Components.getSimpleText("Poignées", 18, SwingConstants.RIGHT, COMBO_BOX_BASE_X - 120, COMBO_BOX_BASE_Y + 60, SMALL_TEXT_WIDTH, TEXT_HEIGHT));

		String[] names = nameList.toArray(new String[0]);
		for (int i = 0; i < 5; i++) {
			int x = COMBO_BOX_BASE_X + PLAYER_X_SPACING * i;
			playerNameBoxes[i] = getPlayerNameList(names, i, x);
			miseryBoxes[i] = getEnumNameList(Misery.values(), "Aucune", x, COMBO_BOX_BASE_Y + 40, SMALL_TEXT_WIDTH);
			handfulBoxes[i] = getEnumNameList(Handful.values(), "Aucune", x, COMBO_BOX_BASE_Y + 80, SMALL_TEXT_WIDTH);
			mainPanel.add(playerNameBoxes[i]);
			mainPanel.add(miseryBoxes[i]);
			mainPanel.add(handfulBoxes[i]);
			playerNameBoxes[i].addActionListener(this);
		}

		final ButtonGroup attackerButtonGroup = new ButtonGroup();
		final ButtonGroup calledPlayerButtonGroup = new ButtonGroup();
		final ButtonGroup contractButtonGroup = new ButtonGroup();
		final ButtonGroup oudlersButtonGroup = new ButtonGroup();

		mainPanel.add(Components.getSimpleText("Preneur", 18, SwingConstants.RIGHT, COMBO_BOX_BASE_X - 120, COMBO_BOX_BASE_Y + 110, SMALL_TEXT_WIDTH, TEXT_HEIGHT));
		for (int i = 0; i < 5; i++) {
			JRadioButton button = new JRadioButton();
			attackerButtons[i] = button;
			attackerButtonGroup.add(button);
			button.setLocation(PLAYER_BUTTON_BASE_X + PLAYER_X_SPACING * i, COMBO_BOX_BASE_Y + 130);
			button.setSize(20, SMALL_TEXT_HEIGHT);
			mainPanel.add(button);
		}

		calledLabel = Components.getSimpleText("Appelé", 18, SwingConstants.RIGHT, COMBO_BOX_BASE_X - 120, COMBO_BOX_BASE_Y + 140, SMALL_TEXT_WIDTH, TEXT_HEIGHT);
		mainPanel.add(calledLabel);

		boolean hideCalledButtons = (baseGame == null || baseGame.players.length < 5) && exists(LAST_SELECTED_NAMES, Tarot.NONE_STRING::equals);
		if (hideCalledButtons)
			calledLabel.setForeground(Color.LIGHT_GRAY);

		for (int i = 0; i < 5; i++) {
			int x = PLAYER_BUTTON_BASE_X + PLAYER_X_SPACING * i;
			int y = COMBO_BOX_BASE_Y + 160;

			JRadioButton button = new JRadioButton();
			calledButtons[i] = button;
			calledPlayerButtonGroup.add(button);
			button.setLocation(x, y);
			button.setSize(20, SMALL_TEXT_HEIGHT);
			mainPanel.add(button);

			JLabel label = new JLabel(Tarot.NONE_STRING);
			noneStrings[i] = label;
			label.setFont(Components.getFont(12));
			label.setLocation(x + 3, y);
			label.setSize(20, SMALL_TEXT_HEIGHT);
			mainPanel.add(label);

			if (hideCalledButtons) {
				button.setVisible(false);
				label.setForeground(Color.LIGHT_GRAY);
			} else {
				label.setVisible(false);
			}
		}

		scoreSlider.setMajorTickSpacing(10);
		scoreSlider.setMinorTickSpacing(1);
		scoreSlider.setPaintLabels(true);
		scoreSlider.setPaintTicks(true);
		scoreSlider.setPreferredSize(new Dimension(600, 65));

		JPanel sliderPanel = new JPanel();
		sliderPanel.add(scoreSlider);
		sliderPanel.setLocation(90, 290);
		sliderPanel.setSize(600, 65);
		mainPanel.add(sliderPanel);

		JLabel attackLabel = Components.getSimpleText("Score de l'attaque : 51", 14, 160, 340, 200, 50);
		JLabel defenseLabel = Components.getSimpleText("Score de la défense : 40", 14, 445, 340, 200, 50);
		mainPanel.add(attackLabel);
		mainPanel.add(defenseLabel);

		scoreSlider.addChangeListener(event -> {
			if (event.getSource() == scoreSlider) {
				attackLabel.setText("Score de l'attaque : " + scoreSlider.getValue());
				defenseLabel.setText("Score de la défense : " + (91 - scoreSlider.getValue()));
			}
		});

		mainPanel.add(Components.getSimpleText("Contrat", 18, 100, 400, SMALL_TEXT_WIDTH, TEXT_HEIGHT));
		for (Contract contract : Contract.ALL_CONTRACTS) {
			JRadioButton button = new JRadioButton(contract.getName());
			contractButtons[contract.ordinal()] = button;
			button.setLocation(SECONDARY_BUTTON_BASE_X + contract.ordinal() * 120, 420);
			button.setSize(120, SMALL_TEXT_HEIGHT);
			contractButtonGroup.add(button);
			mainPanel.add(button);
		}

		mainPanel.add(Components.getSimpleText("Bouts", 18, 100, 450, SMALL_TEXT_WIDTH, TEXT_HEIGHT));
		for (Oudlers oudler : Oudlers.ALL_OUDLERS) {
			JRadioButton button = new JRadioButton(String.valueOf(oudler.ordinal()));
			oudlersButtons[oudler.ordinal()] = button;
			button.setLocation(SECONDARY_BUTTON_BASE_X + oudler.ordinal() * 50, 470);
			button.setSize(40, SMALL_TEXT_HEIGHT);
			mainPanel.add(button);
			oudlersButtonGroup.add(button);
		}

		mainPanel.add(Components.getSimpleText("Petit au bout", 18, 100, 500, 120, TEXT_HEIGHT));
		mainPanel.add(petitAuBoutBox = getEnumNameList(PetitAuBout.values(), "Non", 240, 520, SMALL_TEXT_WIDTH));

		mainPanel.add(Components.getSimpleText("Chelem", 18, 410, 500, SMALL_TEXT_WIDTH, TEXT_HEIGHT));
		mainPanel.add(slamBox = getEnumNameList(Slam.values(), "Non déclaré", 500, 520, 170));

		submitButton = new JButton(baseGame == null ? "Ajouter" : "Modifier");
		submitButton.addActionListener(this);
		submitButton.setFont(Components.getFont(18));
		submitButton.setLocation(310, 600);
		submitButton.setSize(115, 25);
		mainPanel.add(submitButton);

		if (baseGame != null) {
			int numberOfPlayers = baseGame.players.length;
			boolean hasAlly = false;
			int attackerIndex = -1;
			for (int i = 0; i < numberOfPlayers; i++) {
				LocalPlayer local = baseGame.players[i];
				Misery misery = local.misery;
				Handful handful = local.handful;
				Side side = local.side;
				playerNameBoxes[i].setSelectedIndex(Tarot.ORDERED_PLAYERS.indexOf(Tarot.getPlayer(local.getID())) + 1);
				miseryBoxes[i].setSelectedIndex(misery == null ? 0 : misery.ordinal() + 1);
				handfulBoxes[i].setSelectedIndex(handful == null ? 0 : handful.ordinal() + 1);
				if (side == Side.ATTACK) {
					attackerButtons[i].setSelected(true);
					attackerIndex = i;
				} else if (side == Side.ATTACK_ALLY) {
					calledButtons[i].setSelected(true);
					hasAlly = true;
				}
			}
			if (attackerIndex == -1)
				throw new IllegalStateException("what");
			// Attacker has called themselves
			if (numberOfPlayers == 5 && !hasAlly)
				calledButtons[attackerIndex].setSelected(true);
			contractButtons[baseGame.contract.ordinal()].setSelected(true);
			oudlersButtons[baseGame.oudlers.ordinal()].setSelected(true);
			scoreSlider.setValue(baseGame.attackScore);
			petitAuBoutBox.setSelectedIndex(baseGame.petitAuBout == null ? 0 : baseGame.petitAuBout.ordinal() + 1);
			slamBox.setSelectedIndex(baseGame.slam == null ? 0 : baseGame.slam.ordinal() + 1);
		}

		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if (exists(playerNameBoxes, element -> element == source)) {
			int empty = 0;
			for (var box : playerNameBoxes)
				if (hasEmptyName(box))
					empty++;
			if (empty != emptyNames) {
				emptyNames = empty;
				updateCalledLine(empty == 0);
				return;
			}
		}

		if (source != submitButton)
			return;

		int numberOfPlayers = 0;
		for (var box : playerNameBoxes)
			if (!hasEmptyName(box))
				numberOfPlayers++;

		if (numberOfPlayers < 3) {
			Components.popup("Veuillez sélectionner au moins 3 joueurs.");
			return;
		}

		Month month = Month.ALL_MONTHS[LocalDate.now().getMonthValue() - 1];
		Contract contract = null;
		int attackScore = scoreSlider.getValue();
		Oudlers oudlers = null;
		PetitAuBout petitAuBout;
		Slam slam;
		LocalPlayer[] players;

		for (int i = 0; i < contractButtons.length; i++) {
			if (contractButtons[i].isSelected()) {
				contract = Contract.ALL_CONTRACTS[i];
				break;
			}
		}

		if (contract == null) {
			Components.popup("Veuillez sélectionner un contrat.");
			return;
		}

		for (int i = 0; i < oudlersButtons.length; i++) {
			if (oudlersButtons[i].isSelected()) {
				oudlers = Oudlers.ALL_OUDLERS[i];
				break;
			}
		}

		if (oudlers == null) {
			Components.popup( "Veuillez sélectionner un nombre de bouts.");
			return;
		}

		petitAuBout = PetitAuBout.ALL_PETITS[petitAuBoutBox.getSelectedIndex()];
		slam = Slam.ALL_SLAMS[slamBox.getSelectedIndex()];

		int attackerIndex = -1;
		int calledPlayerIndex = -1;
		for (int i = 0; i < 5; i++) {
			if (attackerButtons[i].isSelected())
				attackerIndex = i;
			if (calledButtons[i].isSelected())
				calledPlayerIndex = i;
		}

		if (attackerIndex == -1) {
			Components.popup( "Veuillez sélectionner un attaquant.");
			return;
		}

		if (calledPlayerIndex == -1 && numberOfPlayers == 5) {
			Components.popup( "Veuillez sélectionner un appelé.");
			return;
		}

		Side[] sides = new Side[5];
		for (int i = 0; i < 5; i++)
			sides[i] = Side.DEFENSE;
		sides[attackerIndex] = Side.ATTACK;
		if (numberOfPlayers == 5 && attackerIndex != calledPlayerIndex)
			sides[calledPlayerIndex] = Side.ATTACK_ALLY;

		players = new LocalPlayer[numberOfPlayers];

		// Count up to 5 since names need not be empty after last index
		int nonEmptyIndex = 0;
		for (int i = 0; i < 5; i++) {
			Object selectedItem = playerNameBoxes[i].getSelectedItem();
			if (selectedItem == null || Tarot.NONE_STRING.equals(selectedItem))
				continue;

			String name = selectedItem.toString();
			if (baseGame != null)
				LAST_SELECTED_NAMES[i] = name;

			Handful handful = Handful.ALL_HANDFULS[handfulBoxes[i].getSelectedIndex()];
			Misery misery = Misery.ALL_MISERIES[miseryBoxes[i].getSelectedIndex()];
			players[nonEmptyIndex] = new LocalPlayer(Tarot.getPlayer(name), sides[i], handful, misery);
			nonEmptyIndex++;
		}

		for (int i = 0; i < numberOfPlayers; i++) {
			LocalPlayer player = players[i];
			for (int j = 0; j < numberOfPlayers; j++) {
				if (i != j && player.equals(players[j])) {
					Components.popup("Veuillez sélectionner des joueurs distincts.");
					return;
				}
			}
		}

		// An already existing game was edited
		if (baseGame != null) {
			// Figuring out what to change precisely would be way too complicated
			// Instead, undo previous calculations then redo required changes
			baseGame.applyResults(Game.DEFAULT_CONVERTER, Game.REMOVE_GAME_DIRECTION);

			baseGame.attackScore = attackScore;
			baseGame.contract = contract;
			baseGame.oudlers = oudlers;
			baseGame.petitAuBout = petitAuBout;
			baseGame.slam = slam;

			// Number of players might have changed
			int playerDiff = numberOfPlayers - baseGame.players.length;
			if (playerDiff != 0) {
				baseGame.players = new LocalPlayer[numberOfPlayers];
				// If the number of players is smaller, LAST_SELECTED_NAMES will have uncleared extra data
				if (playerDiff < 0)
					for (int i = 4 + playerDiff; i < 5; i++)
						LAST_SELECTED_NAMES[i] = Tarot.NONE_STRING;
			}

			System.arraycopy(players, 0, baseGame.players, 0, numberOfPlayers);
			baseGame.reorderPlayers();

			baseGame.edit();
			baseGame.applyResults(Game.DEFAULT_CONVERTER, Game.ADD_GAME_DIRECTION);
			Components.popup("Partie modifiée avec succès.");
			dispose();
			FrameMainMenu.MAIN_MENU.reloadGames();
			return;
		}

		Game game = new Game(month, contract, attackScore, oudlers, petitAuBout, slam, players);
		game.write(game.date.getFileName());

		// Use computeIfAbsent since this might be the first game having the corresponding DateRecord
		Tarot.ALL_GAMES.computeIfAbsent(game.date, key -> new ArrayList<>()).add(game);

		for (JComboBox<String> handfulBox : handfulBoxes)
			handfulBox.setSelectedIndex(0);
		for (JComboBox<String> miseryBox : miseryBoxes)
			miseryBox.setSelectedIndex(0);
		petitAuBoutBox.setSelectedIndex(0);
		slamBox.setSelectedIndex(0);
		scoreSlider.setValue(51);

		game.applyResults();
		Components.popup("Partie ajoutée avec succès.");
		FrameMainMenu.MAIN_MENU.reloadGames();
	}

}
