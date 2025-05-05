package fr.giovanni75.tarot.enums;

public enum Handful implements Nameable {

	SIMPLE("Simple", "Poignée", 20),
	DOUBLE("Double", "Double poignée", 30),
	TRIPLE("Triple", "Triple poignée", 40);

	private final String name;
	private final String fullName;
	private final int points;

	Handful(String name, String fullName, int points) {
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
	public static final Handful[] ALL_HANDFULS = {null, SIMPLE, DOUBLE, TRIPLE};

}
