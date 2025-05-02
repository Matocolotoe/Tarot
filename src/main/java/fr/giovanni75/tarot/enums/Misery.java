package fr.giovanni75.tarot.enums;

public enum Misery implements Nameable {

	SIMPLE("Simple", "Misère", 10),
	DOUBLE("Double", "Double misère", 20);

	private final String name;
	private final String fullName;
	private final int points;

	Misery(String name, String fullName, int points) {
		this.name = name;
		this.fullName = fullName;
		this.points = points;
	}

	public int getExtraPoints() {
		return points;
	}

	public String getFullName() {
		return fullName;
	}

	@Override
	public String getName() {
		return name;
	}

	// Insert null first since button index 0 corresponds to nothing
	public static final Misery[] ALL_MISERIES = {null, SIMPLE, DOUBLE};

}
