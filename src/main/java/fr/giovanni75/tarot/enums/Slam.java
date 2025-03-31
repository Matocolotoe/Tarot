package fr.giovanni75.tarot.enums;

import java.util.HashMap;
import java.util.Map;

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

	public static final Map<String, Slam> BY_NAME = new HashMap<>();

	static {
		for (Slam slam : values())
			BY_NAME.put(slam.name, slam);
	}

}
