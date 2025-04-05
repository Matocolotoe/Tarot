package fr.giovanni75.tarot.frames;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.Font;

final class Components {

	private static final String FONT_NAME = "Helvetica";
	private static final String INVALID_DAY_MESSAGE = "Veuillez entrer un jour valide.";

	static JButton getClickableText(String text, int size) {
		JButton button = new JButton(text);
		button.setBorderPainted(false);
		button.setRolloverEnabled(false);
		button.setFocusPainted(false);
		button.setFont(getFont(size));
		button.setHorizontalAlignment(SwingConstants.LEFT);
		return button;
	}

	static JLabel getEmptyText(int size) {
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

	static Border getStandardBorder() {
		return BorderFactory.createEmptyBorder(30, 30, 0, 0);
	}

	static void popup(String message) {
		JOptionPane.showMessageDialog(null, message, "Information", JOptionPane.INFORMATION_MESSAGE);
	}

	static String prompt(String message, String title) {
		return JOptionPane.showInputDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
	}

	static int promptConfirmation(String message, String title) {
		return JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION);
	}

	static int promptDay(String message, String title) {
		String prompt = prompt(message, title);
		if (prompt == null) // Window was just closed
			return -1;

		if (prompt.isBlank()) {
			Components.popup(INVALID_DAY_MESSAGE);
			return promptDay(message, title);
		}

		int result;
		try {
			result = Integer.parseInt(prompt);
		} catch (NumberFormatException e) {
			Components.popup(INVALID_DAY_MESSAGE);
			return promptDay(message, title);
		}

		if (result < 1 || result > 31) {
			Components.popup(INVALID_DAY_MESSAGE);
			return promptDay(message, title);
		}

		return result;
	}

}
