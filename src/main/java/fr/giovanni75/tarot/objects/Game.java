package fr.giovanni75.tarot.objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.giovanni75.tarot.DateRecord;
import fr.giovanni75.tarot.Maps;
import fr.giovanni75.tarot.Tarot;
import fr.giovanni75.tarot.enums.*;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

public class Game implements Serializable {

	private final int dayOfMonth;
	private final DateRecord date;

	private final Contract contract;
	private final int attackScore;
	private final Oudlers oudlers;
	private final PetitAuBout petitAuBout;
	private final Slam slam;

	private final LocalPlayer[] players;

	private int attackFinalScore;

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

	public Game(JsonObject json) {
		JsonElement element = json.get("day");
		this.dayOfMonth = element == null ? 0 : element.getAsInt();

		Month month = Month.valueOf(json.get("month").getAsString());
		this.date = new DateRecord(month, json.get("year").getAsInt());

		this.contract = Contract.valueOf(json.get("contract").getAsString());
		this.attackScore = json.get("attack_score").getAsInt();
		this.oudlers = Oudlers.valueOf(json.get("oudlers").getAsString());

		element = json.get("petit_au_bout");
		this.petitAuBout = element == null ? PetitAuBout.NONE : PetitAuBout.valueOf(element.getAsString());

		element = json.get("slam");
		this.slam = element == null ? Slam.UNANNOUNCED : Slam.valueOf(element.getAsString());

		JsonArray playersArray = json.get("players").getAsJsonArray();
		int size = playersArray.size();
		this.players = new LocalPlayer[size];

		for (int i = 0; i < size; i++) {
			JsonObject object = playersArray.get(i).getAsJsonObject();
			UUID uuid = UUID.fromString(object.get("uuid").getAsString());
			element = object.get("side");
			Side side = element == null ? Side.DEFENSE : Side.valueOf(element.getAsString());
			element = object.get("handful");
			Handful handful = element == null ? Handful.NONE : Handful.valueOf(element.getAsString());
			element = object.get("misery");
			Misery misery = element == null ? Misery.NONE : Misery.valueOf(element.getAsString());
			this.players[i] = new LocalPlayer(uuid, side, handful, misery);
		}
	}

	public void applyResults() {
		int diff = attackScore - oudlers.getRequiredScore();
		int attackFinalScore = (25 + Math.abs(diff)) * contract.getMultiplier();
		if (diff < 0)
			attackFinalScore *= -1;

		int numberOfPlayers = players.length;

		/* Handfuls */
		for (LocalPlayer local : players) {
			int points = local.handful().getExtraPoints();
			if (points == 0)
				continue;
			if (diff < 0) {
				attackFinalScore -= points;
			} else {
				attackFinalScore += points;
			}
			Player player = Tarot.getPlayer(local.uuid());
			Maps.increment(contract, player.getStats(date, numberOfPlayers).handfuls);
		}

		/* Petit au bout */
		switch (petitAuBout) {
			case ATTACK -> attackFinalScore += 10 * contract.getMultiplier();
			case DEFENSE -> attackFinalScore -= 10 * contract.getMultiplier();
		}

		/* Slam */
		switch (slam) {
			case ATTACK -> attackFinalScore += attackScore == 91 ? 400 : -200;
			case DEFENSE -> attackFinalScore += attackScore == 0 ? -400 : 200;
			case UNANNOUNCED -> {
				if (attackScore == 91) {
					attackFinalScore += 200;
				} else if (attackScore == 0) {
					attackFinalScore -= 200;
				}
			}
		}

		List<Player> defenders = getDefenders(Function.identity());
		Player attacker = getPlayer(Side.ATTACK);
		Player ally = getAlly(defenders.size());

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
			int points = local.misery().getExtraPoints();
			if (points == 0)
				continue;
			Player player = Tarot.getPlayer(local.uuid());
			Maps.increment(player, finalScores, points * (numberOfPlayers - 1));
			Maps.increment(contract, player.getStats(date, numberOfPlayers).miseries);
			for (LocalPlayer other : players)
				if (!local.equals(other))
					Maps.increment(Tarot.getPlayer(other.uuid()), finalScores, -points);
		}

		Player.LocalStats stats = attacker.getStats(date, numberOfPlayers);
		Maps.increment(contract, diff >= 0 ? stats.successfulTakes : stats.failedTakes);

		if (ally != null) {
			if (ally == attacker) {
				Maps.increment(contract, stats.selfCalls);
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

	private Player getAlly(int defenders) {
		if (players.length < 5)
			return null;

		Player ally = getPlayer(Side.ATTACK_ALLY);
		if (ally != null) {
			if (defenders != 3)
				throw new IllegalStateException("There has to be 3 defenders with an attacker and an ally");
			return ally;
		}

		if (defenders != 4)
			throw new IllegalStateException("There has to be 4 defenders when the attacker called themselves");

		return getPlayer(Side.ATTACK);
	}

	public int getAttackFinalScore() {
		return attackFinalScore;
	}

	public Contract getContract() {
		return contract;
	}

	public DateRecord getDate() {
		return date;
	}

	private <T> List<T> getDefenders(Function<Player, T> function) {
		List<T> defenders = new ArrayList<>();
		for (LocalPlayer localPlayer : players) {
			if (localPlayer.side() == Side.DEFENSE) {
				Player player = Tarot.getPlayer(localPlayer.uuid());
				defenders.add(function.apply(player));
			}
		}
		return defenders;
	}

	public List<String> getDescription(int index) {
		List<String> lines = new ArrayList<>();
		lines.add(index + ". " + contract.getName() + " – " + oudlers.getDisplay()
				+ " – Score : " + attackScore + "/" + oudlers.getRequiredScore()
				+ " (" + attackFinalScore + ")");

		List<String> defenders = getDefenders(Player::getName);
		Player attacker = getPlayer(Side.ATTACK);
		Player ally = getAlly(defenders.size());
		defenders.sort(String::compareTo);

		if (attacker == null)
			throw new IllegalStateException("Attacker cannot be null");

		final String defense = String.join(", ", defenders);
		if (players.length < 5 || ally == attacker) {
			lines.add(attacker.getName() + " vs. " + defense);
		} else if (ally != null) {
			lines.add(attacker.getName() + " & " + ally.getName() + " vs. " + defense);
		}

		return lines;
	}

	public int getNumberOfOudlers() {
		return oudlers.ordinal();
	}

	public int getNumberOfPlayers() {
		return players.length;
	}

	private Player getPlayer(Side side) {
		for (LocalPlayer localPlayer : players)
			if (localPlayer.side() == side)
				return Tarot.getPlayer(localPlayer.uuid());
		return null;
	}

	public boolean hasPetitAuBout() {
		return petitAuBout != PetitAuBout.NONE;
	}

	@Override
	public JsonObject toJson() {
		JsonObject object = new JsonObject();
		object.addProperty("day", dayOfMonth);
		object.addProperty("month", date.month().name());
		object.addProperty("year", date.year());

		object.addProperty("contract", contract.name());
		object.addProperty("attack_score", attackScore);
		object.addProperty("oudlers", oudlers.name());
		if (petitAuBout != PetitAuBout.NONE)
			object.addProperty("petit_au_bout", petitAuBout.name());

		JsonArray playersArray = new JsonArray();
		for (LocalPlayer player : players)
			playersArray.add(player.toJson());
		object.add("players", playersArray);

		return object;
	}

}
