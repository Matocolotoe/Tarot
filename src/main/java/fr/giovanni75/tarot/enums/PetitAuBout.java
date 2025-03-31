package fr.giovanni75.tarot.enums;

import java.util.HashMap;
import java.util.Map;

public enum PetitAuBout implements Nameable {

	ATTACK("Attaque", "Petit au bout à l'attaque", 10),
	DEFENSE("Défense", "Petit au bout à la défense", -10);

	private final String name;
	private final String fullName;
	private final int attackPoints;

	PetitAuBout(String name, String fullName, int attackPoints) {
		this.name = name;
		this.fullName = fullName;
		this.attackPoints = attackPoints;
	}

	public int getAttackPoints() {
		return attackPoints;
	}

	public String getFullName() {
		return fullName;
	}

	@Override
	public String getName() {
		return name;
	}

	public static final Map<String, PetitAuBout> BY_NAME = new HashMap<>();

	static {
		for (PetitAuBout petit : values())
			BY_NAME.put(petit.name, petit);
	}

}
