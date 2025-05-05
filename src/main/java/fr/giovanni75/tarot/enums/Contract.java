package fr.giovanni75.tarot.enums;

public enum Contract implements Nameable {

	SMALL("Petite", 1),
	GUARD("Garde", 2),
	GUARD_WITHOUT("Garde sans", 4),
	GUARD_AGAINST("Garde contre", 6);

	private final String name;
	private final int multiplier;

	Contract(String name, int multiplier) {
		this.name = name;
		this.multiplier = multiplier;
	}

	public int getMultiplier() {
		return multiplier;
	}

	@Override
	public String getName() {
		return name;
	}

	public static final Contract[] ALL_CONTRACTS = values();

}
