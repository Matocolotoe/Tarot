package fr.giovanni75.tarot.enums;

import java.util.HashMap;
import java.util.Map;

public enum Misery implements Nameable {

	NONE("Aucune", null, 0),
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

	public static final Map<String, Misery> BY_NAME = new HashMap<>();

	static {
		for (Misery misery : values())
			BY_NAME.put(misery.name, misery);
	}

}
