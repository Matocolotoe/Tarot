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

	public LocalStats getStats(DateRecord date, int players) {
		Map<DateRecord, LocalStats> stats = switch (players) {
			case 3 -> statsThreePlayers;
			case 4 -> statsFourPlayers;
			case 5 -> statsFivePlayers;
			default -> throw new IllegalArgumentException("Stats are unavailable for " + players + " players");
		};
		return stats.getOrDefault(date, new LocalStats());
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

	public static class LocalStats {

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
			result.add(" ");
			result.add("Prises totales : " + (successes + Maps.sum(failedTakes)));
			result.add("Prises réussies : " + successes);
			for (Contract contract : Contract.ALL_CONTRACTS) {
				result.add(" ‣ " + contract.getName() + " : " + successfulTakes.getOrDefault(contract, 0)
						+ " \uD83C\uDFC6 / " + failedTakes.getOrDefault(contract, 0) + " ✖");
			}

			int totalSelfCalls = Maps.sum(selfCalls);
			result.add(" ");
			result.add("Appelé(e) " + Maps.sum(calledTimes) + " fois"
					+ (totalSelfCalls == 0 ? "" : ", dont " + totalSelfCalls + " soi-même"));
			for (Contract contract : Contract.ALL_CONTRACTS) {
				totalSelfCalls = selfCalls.getOrDefault(contract, 0);
				result.add(" ‣ " + contract.getName() + " : " + calledTimes.getOrDefault(contract, 0)
						+ (totalSelfCalls == 0 ? "" : ", dont " + totalSelfCalls + " soi-même"));
			}

			result.add(" ");
			result.add("Meilleur tour : " + Maps.max(bestTurns, "%d (%s)", 1));
			result.add("Pire tour : " + Maps.max(worstTurns, "%d (%s)", -1));

			return result;
		}

	}

}
