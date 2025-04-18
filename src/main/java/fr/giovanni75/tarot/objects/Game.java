package fr.giovanni75.tarot.objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.giovanni75.tarot.DateRecord;
import fr.giovanni75.tarot.Maps;
import fr.giovanni75.tarot.Tarot;
import fr.giovanni75.tarot.enums.*;
import fr.giovanni75.tarot.stats.GlobalStats;
import fr.giovanni75.tarot.stats.LocalStats;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

public class Game implements Serializable {

	private static final Function<Integer, Player> DEFAULT_LOCAL_PLAYER_CONVERTER = Tarot::getPlayer;

	public final int dayOfMonth;
	public final DateRecord date;

	public final Contract contract;
	public final Oudlers oudlers;
	public final PetitAuBout petitAuBout;
	public final LocalPlayer[] players;

	private final int attackScore;
	private final Slam slam;

	public int attackFinalScore;

	public Game(Month month, Contract contract, int attackScore, Oudlers oudlers, PetitAuBout petitAuBout, Slam slam, LocalPlayer[] players) {
		LocalDate now = LocalDate.now();
		this.dayOfMonth = now.getDayOfMonth();
		this.date = new DateRecord(month, now.getYear());
		this.contract = contract;
		this.attackScore = attackScore;
		this.oudlers = oudlers;
		this.petitAuBout = petitAuBout;
		this.slam = slam;
		this.players = players;
	}

	public Game(JsonObject json, DateRecord date) {
		JsonElement element = json.get("day");
		this.dayOfMonth = element == null ? 0 : element.getAsInt();
		this.date = date; // Pre-calculated using file name

		this.contract = Contract.valueOf(json.get("contract").getAsString());
		this.attackScore = json.get("attack_score").getAsInt();
		this.oudlers = Oudlers.ALL_OUDLERS[json.get("oudlers").getAsInt()];

		element = json.get("petit_au_bout");
		this.petitAuBout = element == null ? null : PetitAuBout.valueOf(element.getAsString());

		element = json.get("slam");
		this.slam = element == null ? null : Slam.valueOf(element.getAsString());

		JsonArray playersArray = json.get("players").getAsJsonArray();
		int size = playersArray.size();
		this.players = new LocalPlayer[size];

		for (int i = 0; i < size; i++) {
			JsonObject object = playersArray.get(i).getAsJsonObject();
			int id = object.get("id").getAsInt();
			element = object.get("side");
			Side side = element == null ? Side.DEFENSE : Side.valueOf(element.getAsString());
			element = object.get("handful");
			Handful handful = element == null ? null : Handful.valueOf(element.getAsString());
			element = object.get("misery");
			Misery misery = element == null ? null : Misery.valueOf(element.getAsString());
			this.players[i] = new LocalPlayer(id, side, handful, misery);
		}
	}

	public void applyResults() {
		applyResults(DEFAULT_LOCAL_PLAYER_CONVERTER);
	}

	public void applyResults(Function<Integer, Player> localConverter) {
		int diff = attackScore - oudlers.getRequiredScore();
		int attackFinalScore = (25 + Math.abs(diff)) * contract.getMultiplier();
		if (diff < 0)
			attackFinalScore *= -1;

		int numberOfPlayers = players.length;

		GlobalStats globalStats = Tarot.getGlobalStats(date, numberOfPlayers);
		Maps.increment(contract, globalStats.contracts);
		Maps.increment(contract, globalStats.oudlers, oudlers.ordinal());

		/* Handfuls */
		for (LocalPlayer local : players) {
			Handful handful = local.handful();
			if (handful == null)
				continue;
			int points = handful.getExtraPoints();
			if (diff < 0) {
				attackFinalScore -= points;
			} else {
				attackFinalScore += points;
			}
			Player player = localConverter.apply(local.id());
			Maps.increment(contract, player.getStats(date, numberOfPlayers).handfuls);
			Maps.increment(handful, globalStats.handfuls);
		}

		/* Petit au bout */
		if (petitAuBout != null) {
			Maps.increment(petitAuBout, globalStats.petits);
			attackFinalScore += petitAuBout.getAttackPoints() * contract.getMultiplier();
		}

		/* Slam */
		if (slam == Slam.ATTACK) {
			attackFinalScore += attackScore == 91 ? 400 : -200;
		} else if (slam == Slam.DEFENSE) {
			attackFinalScore += attackScore == 0 ? -400 : 200;
		} else if (attackScore == 91) {
			attackFinalScore += 200;
		} else if (attackScore == 0) {
			attackFinalScore -= 200;
		}

		List<Player> defenders = getDefenders(Function.identity(), localConverter);
		Player attacker = getPlayer(Side.ATTACK, localConverter);
		Player ally = getAlly(defenders.size(), localConverter);

		if (attacker == null)
			throw new IllegalStateException("Attacker cannot be null");

		/* Final score */
		Map<Player, Integer> finalScores = new HashMap<>();
		if (numberOfPlayers == 5) {
			if (ally == null)
				throw new IllegalStateException("Ally cannot be null with 5+ players");
			if (ally == attacker) {
				Maps.increment(attacker, finalScores, attackFinalScore * 4);
				for (Player defender : defenders)
					Maps.increment(defender, finalScores, -attackFinalScore);
			} else {
				Maps.increment(attacker, finalScores, attackFinalScore * 2);
				Maps.increment(ally, finalScores, attackFinalScore);
				for (Player defender : defenders)
					Maps.increment(defender, finalScores, -attackFinalScore);
			}
		} else {
			Maps.increment(attacker, finalScores, attackFinalScore * (numberOfPlayers - 1));
			for (Player defender : defenders)
				Maps.increment(defender, finalScores, -attackFinalScore);
		}

		/* Miseries */
		for (LocalPlayer local : players) {
			Misery misery = local.misery();
			if (misery == null)
				continue;
			int points = misery.getExtraPoints();
			Player player = localConverter.apply(local.id());
			Maps.increment(player, finalScores, points * (numberOfPlayers - 1));
			Maps.increment(contract, player.getStats(date, numberOfPlayers).miseries);
			Maps.increment(misery, globalStats.miseries);
			for (LocalPlayer other : players)
				if (!local.equals(other))
					Maps.increment(localConverter.apply(other.id()), finalScores, -points);
		}

		LocalStats stats = attacker.getStats(date, numberOfPlayers);
		Maps.increment(contract, diff >= 0 ? stats.successfulTakes : stats.failedTakes);

		if (ally != null) {
			if (ally == attacker) {
				Maps.increment(contract, stats.selfCalls);
				Maps.increment(contract, globalStats.selfCalls);
			} else {
				stats = ally.getStats(date, numberOfPlayers);
				Maps.increment(contract, stats.calledTimes);
			}
		}

		for (Map.Entry<Player, Integer> entry : finalScores.entrySet()) {
			Player player = entry.getKey();
			int score = entry.getValue();
			stats = player.getStats(date, numberOfPlayers);
			Maps.computeIfHigher(contract, score, stats.bestTurns, 1);
			Maps.computeIfHigher(contract, score, stats.worstTurns, -1);
			Maps.increment(contract, stats.playedGames);
			stats.totalScore += score;
		}

		this.attackFinalScore = attackFinalScore;
	}

	private Player getAlly(int defenders, Function<Integer, Player> localConverter) {
		if (players.length < 5)
			return null;

		Player ally = getPlayer(Side.ATTACK_ALLY, localConverter);
		if (ally != null) {
			if (defenders != 3)
				throw new IllegalStateException("There has to be 3 defenders with an attacker and an ally");
			return ally;
		}

		if (defenders != 4)
			throw new IllegalStateException("There has to be 4 defenders when the attacker called themselves");

		return getPlayer(Side.ATTACK, localConverter);
	}

	private <T> List<T> getDefenders(Function<Player, T> playerTConverter, Function<Integer, Player> localConverter) {
		List<T> defenders = new ArrayList<>();
		for (LocalPlayer local : players) {
			if (local.side() == Side.DEFENSE) {
				Player player = localConverter.apply(local.id());
				defenders.add(playerTConverter.apply(player));
			}
		}
		return defenders;
	}

	public List<String> getDescription() {
		List<String> lines = new ArrayList<>();
		int m = date.month().ordinal() + 1;
		lines.add("(" + (dayOfMonth == 0 ? "?" : dayOfMonth) + (m < 10 ? "/0" : "/") + m + ") "
				+ contract.getName() + " – " + oudlers.getDisplay()
				+ " – Score : " + attackScore + "/" + oudlers.getRequiredScore()
				+ " (" + attackFinalScore + ")");

		// These are only used in main menu summary, so use default converter
		List<String> defenders = getDefenders(Player::getName, DEFAULT_LOCAL_PLAYER_CONVERTER);
		Player attacker = getPlayer(Side.ATTACK, DEFAULT_LOCAL_PLAYER_CONVERTER);
		Player ally = getAlly(defenders.size(), DEFAULT_LOCAL_PLAYER_CONVERTER);
		defenders.sort(String::compareTo);

		if (attacker == null)
			throw new IllegalStateException("Attacker cannot be null");

		final String defense = String.join(", ", defenders);
		if (players.length < 5 || ally == attacker) {
			lines.add(attacker.getName() + " vs. " + defense);
		} else if (ally != null) {
			lines.add(attacker.getName() + " & " + ally.getName() + " vs. " + defense);
		}

		StringJoiner details = new StringJoiner(" – ");
		for (LocalPlayer local : players) {
			Handful handful = local.handful();
			if (handful != null)
				details.add(handful.getFullName() + " " + getOfWord(DEFAULT_LOCAL_PLAYER_CONVERTER.apply(local.id()).getName()));
			Misery misery = local.misery();
			if (misery != null)
				details.add(misery.getFullName() + " " + getOfWord(DEFAULT_LOCAL_PLAYER_CONVERTER.apply(local.id()).getName()));
		}

		if (petitAuBout != null)
			details.add(petitAuBout.getFullName());

		if (details.length() > 0)
			lines.add(details.toString());

		return lines;
	}

	private String getOfWord(String name) {
		return switch (name.charAt(0)) {
			case 'A', 'E', 'I', 'O', 'U', 'Y' -> "d'" + name;
			default -> "de " + name;
		};
	}

	private Player getPlayer(Side side, Function<Integer, Player> localConverter) {
		for (LocalPlayer local : players)
			if (local.side() == side)
				return localConverter.apply(local.id());
		return null;
	}

	@Override
	public JsonObject toJson() {
		JsonObject object = new JsonObject();
		object.addProperty("day", dayOfMonth);

		object.addProperty("contract", contract.name());
		object.addProperty("attack_score", attackScore);
		object.addProperty("oudlers", oudlers.ordinal());
		if (petitAuBout != null)
			object.addProperty("petit_au_bout", petitAuBout.name());

		JsonArray playersArray = new JsonArray();
		for (LocalPlayer player : players)
			playersArray.add(player.toJson());
		object.add("players", playersArray);

		return object;
	}

}
