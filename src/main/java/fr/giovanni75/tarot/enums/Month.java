package fr.giovanni75.tarot.enums;

public enum Month implements Nameable {

	JANUARY("Janvier", "Janv."),
	FEBRUARY("Février", "Fév."),
	MARCH("Mars", "Mars"),
	APRIL("Avril", "Avril"),
	MAY("Mai", "Mai"),
	JUNE("Juin", "Juin"),
	JULY("Juillet", "Juil."),
	AUGUST("Août", "Août"),
	SEPTEMBER("Septembre", "Sept."),
	OCTOBER("Octobre", "Oct."),
	NOVEMBER("Novembre", "Nov."),
	DECEMBER("Décembre", "Déc.");

	private final String name;
	private final String shortName;

	Month(String name, String shortName) {
		this.name = name;
		this.shortName = shortName;
	}

	@Override
	public String getName() {
		return name;
	}

	public String getShortName() {
		return shortName;
	}

	public static final Month[] ALL_MONTHS = Month.values();

}
