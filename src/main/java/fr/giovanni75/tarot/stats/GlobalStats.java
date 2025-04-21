package fr.giovanni75.tarot.stats;

import fr.giovanni75.tarot.enums.Contract;
import fr.giovanni75.tarot.enums.Handful;
import fr.giovanni75.tarot.enums.Misery;
import fr.giovanni75.tarot.enums.PetitAuBout;

import java.util.EnumMap;
import java.util.Map;

public final class GlobalStats {

	public final Map<Contract, Integer> contracts = new EnumMap<>(Contract.class);
	public final Map<Handful, Integer> handfuls = new EnumMap<>(Handful.class);
	public final Map<Misery, Integer> miseries = new EnumMap<>(Misery.class);
	public final Map<PetitAuBout, Integer> petits = new EnumMap<>(PetitAuBout.class);

	public final Map<Contract, Integer> oudlers = new EnumMap<>(Contract.class);
	public final Map<Contract, Integer> selfCalls = new EnumMap<>(Contract.class);

	public GlobalStats() {
		// Always populate fields with contracts to make global data display look more uniform
		for (Contract contract : Contract.ALL_CONTRACTS) {
			contracts.put(contract, 0);
			oudlers.put(contract, 0);
			selfCalls.put(contract, 0);
		}
	}

}
