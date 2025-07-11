package fr.giovanni75.tarot.stats;

import fr.giovanni75.tarot.Maps;
import fr.giovanni75.tarot.Tarot;
import fr.giovanni75.tarot.enums.Contract;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class LocalStats {

	public int totalScore;

	public final Map<Contract, Integer> bestTurns = new EnumMap<>(Contract.class);
	public final Map<Contract, Integer> calledTimes = new EnumMap<>(Contract.class);
	public final Map<Contract, Integer> failedTakes = new EnumMap<>(Contract.class);
	public final Map<Contract, Integer> handfuls = new EnumMap<>(Contract.class);
	public final Map<Contract, Integer> miseries = new EnumMap<>(Contract.class);
	public final Map<Contract, Integer> playedGames = new EnumMap<>(Contract.class);
	public final Map<Contract, Integer> selfCalls = new EnumMap<>(Contract.class);
	public final Map<Contract, Integer> successfulTakes = new EnumMap<>(Contract.class);
	public final Map<Contract, Integer> worstTurns = new EnumMap<>(Contract.class);

	public List<String> getDisplay() {
		boolean hasNeverPlayed = true;
		for (Contract contract : Contract.ALL_CONTRACTS) {
			if (playedGames.getOrDefault(contract, 0) != 0) {
				hasNeverPlayed = false;
				break;
			}
		}

		if (hasNeverPlayed)
			return List.of();

		List<String> result = new ArrayList<>();
		result.add("Score total : " + totalScore);
		result.add("Poignées : " + Maps.sum(handfuls));
		result.add("Misères : " + Maps.sum(miseries));

		result.add(" ");
		result.add("Parties jouées : " + Maps.sum(playedGames));
		for (Contract contract : Contract.ALL_CONTRACTS)
			result.add(" ‣ " + contract.getName() + " : " + playedGames.getOrDefault(contract, 0));

		int successes = Maps.sum(successfulTakes);
		int total = successes + Maps.sum(failedTakes);
		result.add(" ");
		result.add("Prises réussies : " + (total == 0 ? Tarot.NONE_STRING : successes + "/" + total));
		for (Contract contract : Contract.ALL_CONTRACTS) {
			successes = successfulTakes.getOrDefault(contract, 0);
			total = successes + failedTakes.getOrDefault(contract, 0);
			result.add(" ‣ " + contract.getName() + " : " + (total == 0 ? Tarot.NONE_STRING : successes + "/" + total));
		}

		int selfCallAmount = Maps.sum(selfCalls);
		result.add(" ");
		result.add("Appelé·e " + Maps.sum(calledTimes) + " fois" + (selfCallAmount == 0 ? "" : ", dont " + selfCallAmount + " soi-même"));
		for (Contract contract : Contract.ALL_CONTRACTS) {
			selfCallAmount = selfCalls.getOrDefault(contract, 0);
			result.add(" ‣ " + contract.getName() + " : " + calledTimes.getOrDefault(contract, 0)
					+ (selfCallAmount == 0 ? "" : ", dont " + selfCallAmount + " soi-même"));
		}

		result.add(" ");
		result.add("Meilleur tour : " + Maps.max(bestTurns, "%d (%s)", 1));
		result.add("Pire tour : " + Maps.max(worstTurns, "%d (%s)", -1));

		return result;
	}

}
