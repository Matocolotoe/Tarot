package fr.giovanni75.tarot.enums;

import java.util.HashMap;
import java.util.Map;

public enum Handful implements Nameable {

	NONE("Aucune", 0),
	SIMPLE("Simple", 20),
	DOUBLE("Double", 30),
	TRIPLE("Triple", 40);

	private final String name;
	private final int points;

	Handful(String name, int points) {
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

	public static final Map<String, Handful> BY_NAME = new HashMap<>();

	static {
		for (Handful handful : values())
			BY_NAME.put(handful.name, handful);
	}

}
