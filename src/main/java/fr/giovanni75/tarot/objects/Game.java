package fr.giovanni75.tarot.objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.giovanni75.tarot.DateRecord;
import fr.giovanni75.tarot.Tarot;
import fr.giovanni75.tarot.enums.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class Game implements Serializable {

	private final DateRecord date;

	private final Contract contract;
	private final int attackScore;
	private final Oudlers oudlers;
	private final PetitAuBout petitAuBout;

	private final LocalPlayer[] players;

	public Game(Month month, Contract contract, int attackScore, Oudlers oudlers, PetitAuBout petitAuBout, LocalPlayer[] players) {
		this.date = new DateRecord(month, LocalDate.now().getYear());
		this.contract = contract;
		this.attackScore = attackScore;
		this.oudlers = oudlers;
		this.petitAuBout = petitAuBout;
		this.players = players;
	}

	public Game(JsonObject json) {
		Month month = Month.valueOf(json.get("month").getAsString());
		int year = json.get("year").getAsInt();
		this.date = new DateRecord(month, year);

		this.contract = Contract.valueOf(json.get("contract").getAsString());
		this.attackScore = json.get("attack_score").getAsInt();
		this.oudlers = Oudlers.valueOf(json.get("oudlers").getAsString());

		JsonElement element = json.get("petit");
		this.petitAuBout = element == null ? PetitAuBout.NONE : PetitAuBout.valueOf(element.getAsString());

		JsonArray playersArray = json.get("players").getAsJsonArray();
		int size = playersArray.size();
		this.players = new LocalPlayer[size];

		for (int i = 0; i < size; i++) {
			JsonObject object = playersArray.get(i).getAsJsonObject();
			UUID uuid = UUID.fromString(object.get("uuid").getAsString());
			Side side = Side.valueOf(object.get("side").getAsString());
			element = object.get("handful");
			Handful handful = element == null ? Handful.NONE : Handful.valueOf(element.getAsString());
			element = object.get("misery");
			Misery misery = element == null ? Misery.NONE : Misery.valueOf(element.getAsString());
			this.players[i] = new LocalPlayer(uuid, side, handful, misery);
		}
	}

	public int applyResults() {
		int diff = attackScore - oudlers.getRequiredScore();
		int attackFinalScore = (25 + Math.abs(diff)) * contract.getMultiplier();
		if (diff < 0)
			attackFinalScore *= -1;

		/* Handfuls */
		for (LocalPlayer player : players) {
			int points = player.handful().getExtraPoints();
			if (points == 0)
				continue;
			if (diff < 0) {
				attackFinalScore -= points;
			} else {
				attackFinalScore += points;
			}
		}

		/* Petit au bout */
		if (petitAuBout == PetitAuBout.ATTACK) {
			attackFinalScore += 10 * contract.getMultiplier();
		} else if (petitAuBout == PetitAuBout.DEFENSE) {
			attackFinalScore -= 10 * contract.getMultiplier();
		}

		List<Player> defenders = getDefenders(Function.identity());
		Player attacker = getPlayer(Side.ATTACK);
		Player ally = getAlly(defenders.size());

		if (attacker == null)
			throw new IllegalStateException("Attacker cannot be null");

		/* Final score */
		int numberOfPlayers = players.length;
		if (numberOfPlayers == 5) {
			if (ally == null)
				throw new IllegalStateException("Ally cannot be null with 5+ players");
			if (ally == attacker) {
				attacker.addScore(date, attackFinalScore * 4, numberOfPlayers);
				for (Player defender : defenders)
					defender.addScore(date, -attackFinalScore, numberOfPlayers);
			} else {
				attacker.addScore(date, attackFinalScore * 2, numberOfPlayers);
				ally.addScore(date, attackFinalScore, numberOfPlayers);
				for (Player defender : defenders)
					defender.addScore(date, -attackFinalScore, numberOfPlayers);
			}
		} else {
			attacker.addScore(date, attackFinalScore * (numberOfPlayers - 1), numberOfPlayers);
			for (Player defender : defenders)
				defender.addScore(date, -attackFinalScore, numberOfPlayers);
		}

		/* Miseries */
		for (LocalPlayer player : players) {
			int points = player.misery().getExtraPoints();
			if (points == 0)
				continue;
			player.addScore(date, points * (numberOfPlayers - 1), numberOfPlayers);
			for (LocalPlayer other : players)
				if (!player.equals(other))
					other.addScore(date, -points, numberOfPlayers);
		}

		return attackFinalScore;
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

	public String getDescriptionFirstLine() {
		return contract.getName() + " – " + oudlers.getDisplay() + " – Score : " + attackScore + "/" + oudlers.getRequiredScore();
	}

	public String getDescriptionSecondLine() {
		List<String> defenders = getDefenders(Player::getName);
		Player attacker = getPlayer(Side.ATTACK);
		Player ally = getAlly(defenders.size());
		defenders.sort(String::compareTo);

		if (attacker == null)
			throw new IllegalStateException("Attacker cannot be null");

		final String defense = String.join(", ", defenders);
		final String simple = attacker.getName() + " vs. " + defense;
		if (players.length < 5)
			return simple;

		if (ally == null)
			throw new IllegalStateException("Ally cannot be null with 5+ players");

		return ally == attacker ? simple : attacker.getName() + " & " + ally.getName() + " vs. " + defense;
	}

	private Player getPlayer(Side side) {
		for (LocalPlayer localPlayer : players)
			if (localPlayer.side() == side)
				return Tarot.getPlayer(localPlayer.uuid());
		return null;
	}

	@Override
	public JsonObject toJson() {
		JsonObject object = new JsonObject();
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
