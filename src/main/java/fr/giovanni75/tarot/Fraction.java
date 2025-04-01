package fr.giovanni75.tarot;

public final class Fraction extends Number {

	private final int numerator;
	private final int denominator;

	public Fraction(int numerator, int denominator) {
		if (denominator == 0)
			throw new IllegalArgumentException("Denominator cannot be zero");
		this.numerator = numerator;
		this.denominator = denominator;
	}

	@Override
	public double doubleValue() {
		return (double) numerator / denominator;
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Fraction fraction && numerator == fraction.numerator && denominator == fraction.denominator;
	}

	@Override
	public float floatValue() {
		return (float) numerator / denominator;
	}

	@Override
	public int intValue() {
		return numerator / denominator;
	}

	@Override
	public long longValue() {
		return numerator / denominator;
	}

	@Override
	public String toString() {
		return numerator + "/" + denominator;
	}

}
