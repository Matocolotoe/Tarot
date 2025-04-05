package fr.giovanni75.tarot;

import fr.giovanni75.tarot.enums.Month;

public record DateRecord(Month month, int year) implements Comparable<DateRecord> {

	@Override
	public int compareTo(DateRecord other) {
		if (this.year < other.year)
			return 1;
		if (this.year > other.year)
			return -1;
		return -month.compareTo(other.month);
	}

	public String getFileName() {
		return "games/games_" + getShortName("_");
	}

	public String getName() {
		return month.getName() + " " + year;
	}

	public String getShortName(String separator) {
		int m = month.ordinal() + 1;
		return (m < 10 ? "0" + m : m) + separator + year;
	}

}
