package fr.giovanni75.tarot.enums;

import java.util.HashMap;
import java.util.Map;

public enum Handful implements Nameable {

	NONE("Aucune", null, 0),
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

	public static final Map<String, Handful> BY_NAME = new HashMap<>();

	static {
		for (Handful handful : values())
			BY_NAME.put(handful.name, handful);
	}

}
