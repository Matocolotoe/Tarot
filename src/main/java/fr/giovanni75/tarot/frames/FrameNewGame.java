package fr.giovanni75.tarot.frames;

import fr.giovanni75.tarot.Tarot;
import fr.giovanni75.tarot.enums.*;
import fr.giovanni75.tarot.objects.Game;
import fr.giovanni75.tarot.objects.LocalPlayer;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class FrameNewGame extends JFrame implements ActionListener {

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

	@SuppressWarnings("unchecked")
	private final JComboBox<String>[] handfulBoxes = new JComboBox[5];

	@SuppressWarnings("unchecked")
	private final JComboBox<String>[] miseryBoxes = new JComboBox[5];

	@SuppressWarnings("unchecked")
	private final JComboBox<String>[] playerNameBoxes = new JComboBox[5];

	private final JComboBox<String> petitAuBoutBox;
	private final JComboBox<String> slamBox;

	private final JSlider scoreSlider = new JSlider(JSlider.HORIZONTAL, 0, 91, 51);

	private final JRadioButton[] attackerButtons = new JRadioButton[5];
	private final JRadioButton[] calledButtons = new JRadioButton[5];
	private final JRadioButton[] contractButtons = new JRadioButton[Contract.ALL_CONTRACTS.length];
	private final JRadioButton[] oudlersButtons = new JRadioButton[Oudlers.values().length];

	private final JButton submitButton;

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

	FrameNewGame() {
		setBounds(300, 200, 800, 700);
		setResizable(false);
		setTitle("Ajouter une partie");

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(null);
		add(mainPanel);

		final List<String> nameList = new ArrayList<>(Tarot.PLAYER_NAMES);
		nameList.sort(String::compareTo);
		nameList.addFirst(Tarot.NONE_STRING);

		mainPanel.add(Components.getSimpleText("Joueurs", 18, COMBO_BOX_BASE_X - 80, COMBO_BOX_BASE_Y - 20, LARGE_TEXT_WIDTH, TEXT_HEIGHT));
		mainPanel.add(Components.getSimpleText("Misères", 18, COMBO_BOX_BASE_X - 80, COMBO_BOX_BASE_Y + 20, LARGE_TEXT_WIDTH, TEXT_HEIGHT));
		mainPanel.add(Components.getSimpleText("Poignées", 18, COMBO_BOX_BASE_X - 92, COMBO_BOX_BASE_Y + 60, LARGE_TEXT_WIDTH, TEXT_HEIGHT));

		String[] names = nameList.toArray(new String[0]);
		for (int i = 0; i < 5; i++) {
			int x = COMBO_BOX_BASE_X + PLAYER_X_SPACING * i;
			playerNameBoxes[i] = getPlayerNameList(names, i, x);
			miseryBoxes[i] = getEnumNameList(Misery.values(), "Aucune", x, 120, SMALL_TEXT_WIDTH);
			handfulBoxes[i] = getEnumNameList(Handful.values(), "Aucune", x, 160, SMALL_TEXT_WIDTH);
			mainPanel.add(playerNameBoxes[i]);
			mainPanel.add(miseryBoxes[i]);
			mainPanel.add(handfulBoxes[i]);
		}

		final ButtonGroup attackerButtonGroup = new ButtonGroup();
		final ButtonGroup calledPlayerButtonGroup = new ButtonGroup();
		final ButtonGroup contractButtonGroup = new ButtonGroup();
		final ButtonGroup oudlersButtonGroup = new ButtonGroup();

		mainPanel.add(Components.getSimpleText("Preneur", 18, COMBO_BOX_BASE_X - 80, COMBO_BOX_BASE_Y + 110, SMALL_TEXT_WIDTH, TEXT_HEIGHT));
		for (int i = 0; i < 5; i++) {
			JRadioButton button = new JRadioButton();
			attackerButtons[i] = button;
			attackerButtonGroup.add(button);
			button.setLocation(PLAYER_BUTTON_BASE_X + PLAYER_X_SPACING * i, COMBO_BOX_BASE_Y + 130);
			button.setSize(20, SMALL_TEXT_HEIGHT);
			mainPanel.add(button);
		}

		mainPanel.add(Components.getSimpleText("Appelé", 18, COMBO_BOX_BASE_X - 80, COMBO_BOX_BASE_Y + 140, SMALL_TEXT_WIDTH, TEXT_HEIGHT));
		for (int i = 0; i < 5; i++) {
			JRadioButton button = new JRadioButton();
			calledButtons[i] = button;
			calledPlayerButtonGroup.add(button);
			button.setLocation(PLAYER_BUTTON_BASE_X + PLAYER_X_SPACING * i, COMBO_BOX_BASE_Y + 160);
			button.setSize(20, SMALL_TEXT_HEIGHT);
			mainPanel.add(button);
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
		for (Oudlers oudler : Oudlers.values()) {
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

		submitButton = new JButton("Ajouter");
		submitButton.addActionListener(this);
		submitButton.setFont(Components.getFont(18));
		submitButton.setLocation(325, 600);
		submitButton.setSize(100, 25);
		mainPanel.add(submitButton);

		setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() != submitButton)
			return;

		int numberOfPlayers = 0;
		for (int i = 0; i < 5; i++)
			if (!Tarot.NONE_STRING.equals(playerNameBoxes[i].getSelectedItem()))
				numberOfPlayers++;

		if (numberOfPlayers < 3) {
			Components.popup("Veuillez sélectionner au moins 3 joueurs.");
			return;
		}

		Month month = Month.values()[LocalDate.now().getMonthValue() - 1];
		Contract contract = null;
		int attackScore = scoreSlider.getValue();
		Oudlers oudlers = null;
		PetitAuBout petitAuBout;
		Slam slam;
		LocalPlayer[] players;

		for (JRadioButton contractButton : contractButtons) {
			if (contractButton.isSelected()) {
				contract = Contract.BY_NAME.get(contractButton.getText());
				break;
			}
		}

		if (contract == null) {
			Components.popup("Veuillez sélectionner un contrat.");
			return;
		}

		for (int i = 0; i < oudlersButtons.length; i++) {
			if (oudlersButtons[i].isSelected()) {
				oudlers = Oudlers.values()[i];
				break;
			}
		}

		if (oudlers == null) {
			Components.popup( "Veuillez sélectionner un nombre de bouts.");
			return;
		}

		Object selectedItem = petitAuBoutBox.getSelectedItem();
		if (selectedItem == null)
			throw new IllegalStateException("Petit au bout box cannot have null selection");
		petitAuBout = PetitAuBout.BY_NAME.get(selectedItem.toString());

		selectedItem = slamBox.getSelectedItem();
		if (selectedItem == null)
			throw new IllegalStateException("Slam box cannot have null selection");
		slam = Slam.BY_NAME.get(selectedItem.toString());

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
			selectedItem = playerNameBoxes[i].getSelectedItem();
			if (selectedItem == null || Tarot.NONE_STRING.equals(selectedItem))
				continue;

			String name = selectedItem.toString();
			int id = Tarot.getPlayer(name).getID();
			LAST_SELECTED_NAMES[i] = name;

			selectedItem = handfulBoxes[i].getSelectedItem();
			if (selectedItem == null)
				throw new IllegalStateException("Handful box cannot have null selection");
			Handful handful = Handful.BY_NAME.get(selectedItem.toString());

			selectedItem = miseryBoxes[i].getSelectedItem();
			if (selectedItem == null)
				throw new IllegalStateException("Misery box cannot have null selection");
			Misery misery = Misery.BY_NAME.get(selectedItem.toString());

			players[nonEmptyIndex] = new LocalPlayer(id, sides[i], handful, misery);
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

		Game game = new Game(month, contract, attackScore, oudlers, petitAuBout, slam, players);
		Tarot.ALL_GAMES.computeIfAbsent(game.date, key -> new ArrayList<>()).addFirst(game); // Add first so that game is shown on top
		game.write("games/games_" + game.date.getShortName("_"));

		for (JComboBox<String> handfulBox : handfulBoxes)
			handfulBox.setSelectedIndex(0);
		for (JComboBox<String> miseryBox : miseryBoxes)
			miseryBox.setSelectedIndex(0);

		petitAuBoutBox.setSelectedIndex(0);
		slamBox.setSelectedIndex(0);
		scoreSlider.setValue(51);

		game.applyResults();
		Components.popup("Partie ajoutée avec succès !\nPoints à l'attaque : " + game.attackFinalScore);
		FrameMainMenu.MAIN_MENU.reloadGames();
	}

}
