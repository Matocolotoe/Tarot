package fr.giovanni75.tarot.frames;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import java.awt.Color;
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

	static JLabel getEmptySpace(int size) {
		return getSimpleText(" ", size);
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
		return JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION);
	}

}
