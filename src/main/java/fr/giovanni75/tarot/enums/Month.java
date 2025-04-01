package fr.giovanni75.tarot.enums;

public enum Month implements Nameable {

	JANUARY("Janvier"),
	FEBRUARY("Février"),
	MARCH("Mars"),
	APRIL("Avril"),
	MAY("Mai"),
	JUNE("Juin"),
	JULY("Juillet"),
	AUGUST("Août"),
	SEPTEMBER("Septembre"),
	OCTOBER("Octobre"),
	NOVEMBER("Novembre"),
	DECEMBER("Décembre");

	private final String name;

	Month(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public static final Month[] ALL_MONTHS = Month.values();

}
