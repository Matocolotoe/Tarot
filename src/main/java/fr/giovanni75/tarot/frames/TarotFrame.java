package fr.giovanni75.tarot.frames;

import javax.swing.*;

class TarotFrame extends JFrame {

	private static final int BORDER_TOP_LEFT_MARGIN = 30;
	private static final int SCROLL_VERTICAL_INCREMENT = 18;

	void create(String title, int x, int y, int width, int height) {
		setBounds(x, y, width, height);
		setResizable(false);
		setTitle(title);
	}

	JPanel panel(int rightMargin, boolean add) {
		JPanel panel = new JPanel();

		if (rightMargin == -1) {
			panel.setLayout(null);
		} else {
			panel.setBorder(BorderFactory.createEmptyBorder(BORDER_TOP_LEFT_MARGIN, BORDER_TOP_LEFT_MARGIN, 0, rightMargin));
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		}

		if (add) {
			add(panel);
			if (rightMargin != -1) {
				JScrollPane pane = new JScrollPane(panel);
				pane.getVerticalScrollBar().setUnitIncrement(SCROLL_VERTICAL_INCREMENT);
				add(pane);
			}
		}

		return panel;
	}

}
