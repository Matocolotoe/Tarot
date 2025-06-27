package fr.giovanni75.tarot.frames;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Font;

final class Components {

	static final Color DEFAULT_BUTTON_COLOR = new Color(247, 247, 247);
	static final String NO_GAME_AVAILABLE = "Aucune partie n'est disponible pour cette période.";

	private static final int BORDER_TOP_LEFT_MARGIN = 30;
	private static final int SCROLL_VERTICAL_INCREMENT = 18;
	private static final String FONT_NAME = "Helvetica";

	static JButton getClickableText(String text, int size) {
		JButton button = new JButton(text);
		button.setBorderPainted(false);
		button.setRolloverEnabled(false);
		button.setFocusPainted(false);
		button.setFont(getFont(size));
		button.setHorizontalAlignment(SwingConstants.LEFT);
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

	static Border getStandardBorder(int rightMargin) {
		return BorderFactory.createEmptyBorder(BORDER_TOP_LEFT_MARGIN, BORDER_TOP_LEFT_MARGIN, 0, rightMargin);
	}

	static JScrollPane getStandardScrollPane(JPanel panel) {
		JScrollPane pane = new JScrollPane(panel);
		pane.getVerticalScrollBar().setUnitIncrement(SCROLL_VERTICAL_INCREMENT);
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
		return JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION);
	}

}
