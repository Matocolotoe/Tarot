package fr.giovanni75.tarot;

import fr.giovanni75.tarot.enums.Month;

public record DateRecord(Month month, int year) implements Comparable<DateRecord> {

	@Override
	public int compareTo(DateRecord other) {
		if (this.year < other.year)
			return 1;
		if (this.year > other.year)
			return -1;
		return other.month.compareTo(month); // Put most recent months first
	}

	public String getFileName() {
		int month = this.month.ordinal() + 1;
		return "games/games_" + (month < 10 ? "0" + month : month) + "_" + year;
	}

	public String getName() {
		return month.getName() + " " + year;
	}

}
