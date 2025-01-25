package fr.giovanni75.tarot.objects;

import com.google.gson.JsonObject;
import fr.giovanni75.tarot.DateRecord;
import fr.giovanni75.tarot.Maps;
import fr.giovanni75.tarot.Tarot;
import fr.giovanni75.tarot.enums.Contract;

import java.util.*;

public class Player implements Serializable {

	private final String name;
	private final UUID uuid;

	private final Map<DateRecord, LocalStats> statsFivePlayers = new HashMap<>();
	private final Map<DateRecord, LocalStats> statsFourPlayers = new HashMap<>();
	private final Map<DateRecord, LocalStats> statsThreePlayers = new HashMap<>();

	public Player(String name, UUID uuid) {
		this.name = name;
		this.uuid = uuid;
		for (DateRecord record : Tarot.ALL_GAMES.keySet()) {
			statsFivePlayers.put(record, new LocalStats());
			statsFourPlayers.put(record, new LocalStats());
			statsThreePlayers.put(record, new LocalStats());
		}
	}

	public String getName() {
		return name;
	}

	public UUID getUniqueID() {
		return uuid;
	}

	@Override
	public JsonObject toJson() {
		JsonObject object = new JsonObject();
		object.addProperty("name", name);
		object.addProperty("uuid", uuid.toString());
		return object;
	}

	// ** Stats ** //

	public static class LocalStats {

		public int totalScore;

		public final Map<Contract, Integer> bestTurns = new HashMap<>();
		public final Map<Contract, Integer> calledTimes = new HashMap<>();
		public final Map<Contract, Integer> failedTakes = new HashMap<>();
		public final Map<Contract, Integer> playedGames = new HashMap<>();
		public final Map<Contract, Integer> selfCalls = new HashMap<>();
		public final Map<Contract, Integer> successfulTakes = new HashMap<>();
		public final Map<Contract, Integer> worstTurns = new HashMap<>();

	}

	public List<String> getDisplay(DateRecord date, int players) {
		LocalStats stats = getStats(date, players);

		boolean hasNeverPlayed = true;
		for (Contract contract : Contract.ALL_CONTRACTS) {
			if (stats.playedGames.getOrDefault(contract, 0) != 0) {
				hasNeverPlayed = false;
				break;
			}
		}

		if (hasNeverPlayed)
			return List.of();

		List<String> result = new ArrayList<>();
		result.add("Score total : " + stats.totalScore);
		result.add("Parties jouées : " + Maps.sum(stats.playedGames));
		for (Contract contract : Contract.ALL_CONTRACTS)
			result.add(" ‣ " + contract.getName() + " : " + stats.playedGames.getOrDefault(contract, 0));

		int successes = Maps.sum(stats.successfulTakes);
		result.add(" ");
		result.add("Prises totales : " + (successes + Maps.sum(stats.failedTakes)));
		result.add("Prises réussies : " + successes);
		for (Contract contract : Contract.ALL_CONTRACTS) {
			result.add(" ‣ " + contract.getName() + " : " + stats.successfulTakes.getOrDefault(contract, 0)
					+ " \uD83C\uDFC6 / " + stats.failedTakes.getOrDefault(contract, 0) + " ✖");
		}

		if (players == 5) {
			int selfCalls = Maps.sum(stats.selfCalls);
			result.add(" ");
			result.add("Appelé(e) " + Maps.sum(stats.calledTimes) + " fois"
					+ (selfCalls == 0 ? "" : ", dont " + selfCalls + " soi-même"));
			for (Contract contract : Contract.ALL_CONTRACTS) {
				selfCalls = stats.selfCalls.getOrDefault(contract, 0);
				result.add(" ‣ " + contract.getName() + " : " + stats.calledTimes.getOrDefault(contract, 0)
						+ (selfCalls == 0 ? "" : ", dont " + selfCalls + " soi-même"));
			}
		}

		result.add(" ");
		result.add("Meilleur tour : " + Maps.max(stats.bestTurns, "%d (%s)", 1));
		result.add("Pire tour : " + Maps.max(stats.worstTurns, "%d (%s)", -1));

		return result;
	}

	public LocalStats getStats(DateRecord date, int players) {
		return switch (players) {
			case 3 -> statsThreePlayers.getOrDefault(date, new LocalStats());
			case 4 -> statsFourPlayers.getOrDefault(date, new LocalStats());
			case 5 -> statsFivePlayers.getOrDefault(date, new LocalStats());
			default -> throw new IllegalArgumentException("Stats are unavailable for " + players + " players");
		};
	}

}
