package fr.giovanni75.tarot.enums;

import java.util.EnumMap;
import java.util.Map;

public class GlobalStats {

	public final Map<Contract, Integer> contracts = new EnumMap<>(Contract.class);
	public final Map<Handful, Integer> handfuls = new EnumMap<>(Handful.class);
	public final Map<Misery, Integer> miseries = new EnumMap<>(Misery.class);
	public final Map<PetitAuBout, Integer> petits = new EnumMap<>(PetitAuBout.class);

}
