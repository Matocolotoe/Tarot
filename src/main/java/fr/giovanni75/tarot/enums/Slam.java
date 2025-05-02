package fr.giovanni75.tarot.enums;

public enum Slam implements Nameable {

	ATTACK("Annoncé à l'attaque"),
	DEFENSE("Annoncé à la défense");

	private final String name;

	Slam(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	// Insert null first since button index 0 corresponds to nothing
	public static final Slam[] ALL_SLAMS = {null, ATTACK, DEFENSE};

}
