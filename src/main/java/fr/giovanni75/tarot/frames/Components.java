package fr.giovanni75.tarot.frames;

import javax.swing.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

final class Components {

	static final Color DEFAULT_BUTTON_COLOR = new Color(247, 247, 247);
	static final String NO_GAME_AVAILABLE = "Aucune partie n'est disponible pour cette période.";

	private static final String FONT_NAME = "Helvetica";

	static JButton getClickableText(String text, int size) {
		JButton button = new JButton(text);
		button.setBorderPainted(false);
		button.setFocusPainted(false);
		button.setFont(getFont(size));
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setRolloverEnabled(false);
		return button;
	}

	static JButton getClickableText(String text, int size, int width, int height) {
		JButton button = getClickableText(text, size);
		button.setSize(width, height);
		return button;
	}

	static JButton getClickableText(String text, int size, int x, int y, int width, int height) {
		JButton button = getClickableText(text, size);
		button.setLocation(x, y);
		button.setSize(button.getPreferredSize());
		return button;
	}

	static Component getEmptySpace(int height) {
		return Box.createVerticalStrut(height);
	}

	static Font getFont(int size) {
		return new Font(FONT_NAME, Font.PLAIN, size);
	}

	static JLabel getSimpleText(String text, int size) {
		JLabel label = new JLabel(text);
		label.setFont(getFont(size));
		return label;
	}

	static JLabel getSimpleText(String text, int size, int x, int y, int width, int height) {
		JLabel label = getSimpleText(text, size);
		label.setLocation(x, y);
		label.setSize(width, height);
		return label;
	}

	static JSplitPane getSplitPane(JPanel leftPanel, JPanel rightPanel, int leftWidth, int rightWidth, int height) {
		JSplitPane pane = new JSplitPane();
		pane.setDividerLocation(leftWidth);
		pane.setDividerSize(0);
		pane.setLeftComponent(leftPanel);
		pane.setRightComponent(rightPanel);
		pane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		pane.setSize(leftWidth + rightWidth, height);
		return pane;
	}

	static void popup(String message) {
		popup(message, "Information");
	}

	static void popup(String message, String title) {
		JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
	}

	static String prompt(String message, String title) {
		return JOptionPane.showInputDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
	}

	static int promptConfirmation(String message, String title) {
		return JOptionPane.showOptionDialog(null, message, title, JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, new String[] {"Oui", "Non"}, "Oui");
	}

}
