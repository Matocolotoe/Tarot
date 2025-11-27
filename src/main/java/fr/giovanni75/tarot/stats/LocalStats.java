package fr.giovanni75.tarot.stats;

import fr.giovanni75.tarot.Maps;
import fr.giovanni75.tarot.Tarot;
import fr.giovanni75.tarot.Utils;
import fr.giovanni75.tarot.enums.Contract;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class LocalStats {

	public int callScore;
	public int defScore;
	public int takeScore;
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

	public List<String> getDisplay(int players) {
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
		result.add(" ‣ Prises : " + Utils.formatSign(takeScore));
		if (players == 5)
			result.add(" ‣ Appels : " + Utils.formatSign(callScore));
		result.add(" ‣ Défense : " + Utils.formatSign(defScore));

		result.add(" ");
		result.add("Parties jouées : " + Maps.sum(playedGames));
		result.add("Poignées : " + Maps.sum(handfuls));
		result.add("Misères : " + Maps.sum(miseries));

		int successes = Maps.sum(successfulTakes);
		int totalTakes = successes + Maps.sum(failedTakes);
		result.add(" ");
		result.add("Prises réussies : " + (totalTakes == 0 ? Tarot.NONE_STRING : successes + "/" + totalTakes));
		for (Contract contract : Contract.ALL_CONTRACTS) {
			successes = successfulTakes.getOrDefault(contract, 0);
			totalTakes = successes + failedTakes.getOrDefault(contract, 0);
			result.add(" ‣ " + contract.getName() + " : " + (totalTakes == 0 ? Tarot.NONE_STRING : successes + "/" + totalTakes));
		}

		int selfCallAmount = Maps.sum(selfCalls);
		result.add(" ");
		result.add("Appelé·e " + Maps.sum(calledTimes) + " fois" + (selfCallAmount == 0 ? "" : ", dont " + selfCallAmount + " soi-même"));
		for (Contract contract : Contract.ALL_CONTRACTS) {
			selfCallAmount = selfCalls.getOrDefault(contract, 0);
			result.add(" ‣ " + contract.getName() + " : " + calledTimes.getOrDefault(contract, 0)
					+ "/" + (playedGames.getOrDefault(contract, 0) - (successfulTakes.getOrDefault(contract, 0)
			+ failedTakes.getOrDefault(contract, 0)))
					+ (selfCallAmount == 0 ? "" : " + " + selfCallAmount + " solo"));
		}

		result.add(" ");
		result.add("Meilleur tour : " + Maps.max(bestTurns, "%d (%s)", 1));
		result.add("Pire tour : " + Maps.max(worstTurns, "%d (%s)", -1));

		return result;
	}

}
