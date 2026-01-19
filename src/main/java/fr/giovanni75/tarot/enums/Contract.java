package fr.giovanni75.tarot.enums;

public enum Contract implements Nameable {

	SMALL("Petite", 1, 3),
	GUARD("Garde", 2, 2),
	GUARD_WITHOUT("Garde sans", 4, 1),
	GUARD_AGAINST("Garde contre", 6, 1);

	private final String name;
	private final int multiplier;
	private final int inverseWeight;

	Contract(String name, int multiplier, int inverseWeight) {
		this.name = name;
		this.multiplier = multiplier;
		this.inverseWeight = inverseWeight;
	}

	public int getInverseWeight() {
		return inverseWeight;
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
