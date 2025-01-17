package fr.giovanni75.tarot.enums;

import java.util.HashMap;
import java.util.Map;

public enum Misery implements Nameable {

	NONE("Aucune", 0),
	SIMPLE("Simple", 10),
	DOUBLE("Double", 20);

	private final String name;
	private final int points;

	Misery(String name, int points) {
		this.name = name;
		this.points = points;
	}

	public int getExtraPoints() {
		return points;
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
