package fr.giovanni75.tarot.enums;

import java.util.HashMap;
import java.util.Map;

public enum PetitAuBout implements Nameable {

	NONE("Non"),
	ATTACK("Attaque"),
	DEFENSE("Défense");

	private final String name;

	PetitAuBout(String name) {
		this.name = name;
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
