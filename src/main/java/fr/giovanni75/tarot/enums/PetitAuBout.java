package fr.giovanni75.tarot.enums;

import java.util.HashMap;
import java.util.Map;

public enum PetitAuBout implements Nameable {

	NONE("Non", null),
	ATTACK("Attaque", "Petit au bout à l'attaque"),
	DEFENSE("Défense", "Petit au bout à la défense");

	private final String name;
	private final String fullName;

	PetitAuBout(String name, String fullName) {
		this.name = name;
		this.fullName = fullName;
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
