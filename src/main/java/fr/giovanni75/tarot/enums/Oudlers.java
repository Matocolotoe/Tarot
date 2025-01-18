package fr.giovanni75.tarot.enums;

public enum Oudlers {

	ZERO("Pas de bouts", 56),
	ONE("1 bout", 51),
	TWO("2 bouts", 41),
	THREE("3 bouts", 36);

	private final String display;
	private final int score;

	Oudlers(String display, int score) {
		this.display = display;
		this.score = score;
	}

	public String getDisplay() {
		return display;
	}

	public int getRequiredScore() {
		return score;
	}

}
