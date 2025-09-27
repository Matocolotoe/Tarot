package fr.giovanni75.tarot.objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.giovanni75.tarot.*;
import fr.giovanni75.tarot.enums.*;
import fr.giovanni75.tarot.stats.GlobalStats;
import fr.giovanni75.tarot.stats.LocalStats;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

public class Game implements Serializable {

	private static final int BASE_SCORE = 25;
	private static final int SLAM_SCORE = 200;

	private static final int ATTACKER_INDEX = 0;
	private static final int ALLY_INDEX = 1;

	public static final Function<LocalPlayer, Player> DEFAULT_CONVERTER = local -> local.player;

	public static final int ADD_GAME_DIRECTION = 1;
	public static final int REMOVE_GAME_DIRECTION = -1;

	public static final int ATTACK_SCORE = 1;
	public static final int GLOBAL_STATS = 2;
	public static final int PLAYER_SCORES = 4;
	public static final int PLAYER_STATS = 8;
	public static final int WRITE_INFO = 16;

	private final List<String> description = new ArrayList<>();
	private final List<String> details = new ArrayList<>();

	private int attackBaseScore; // Ignores all modifiers
	private int attackFinalScore; // Ignores player modifiers, but counts handfuls, petits and slam

	public final int dayOfMonth;
	public final DateRecord date;

	public int attackScore;
	public Contract contract;
	public Oudlers oudlers;
	public PetitAuBout petitAuBout;
	public Slam slam;

	// players[0] = attacker
	// players[1] = ally (or a defender if attacker called themselves)
	// players[2+] = defenders
	public LocalPlayer[] players;

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
		reorderPlayers();
	}

	public Game(JsonObject json, DateRecord date) {
		JsonElement element = json.get("day");
		this.dayOfMonth = element == null ? 0 : element.getAsInt();
		this.date = date; // Pre-calculated using file name

		this.contract = Contract.ALL_CONTRACTS[json.get("contract").getAsInt()];
		this.attackScore = json.get("attack_score").getAsInt();
		this.oudlers = Oudlers.ALL_OUDLERS[json.get("oudlers").getAsInt()];

		element = json.get("petit_au_bout");
		this.petitAuBout = element == null ? null : PetitAuBout.ALL_PETITS[element.getAsInt() + 1];

		element = json.get("slam");
		this.slam = element == null ? null : Slam.ALL_SLAMS[element.getAsInt() + 1];

		JsonArray playersArray = json.get("players").getAsJsonArray();
		int size = playersArray.size();
		this.players = new LocalPlayer[size];
		for (int i = 0; i < size; i++)
			this.players[i] = new LocalPlayer(playersArray.get(i).getAsJsonObject());

		reorderPlayers();
	}

	public void applyResults() {
		applyResults(DEFAULT_CONVERTER, ADD_GAME_DIRECTION);
	}

	public void applyResults(Function<LocalPlayer, Player> converter, int direction) {
		applyResults(converter, direction, ATTACK_SCORE | GLOBAL_STATS | PLAYER_SCORES | PLAYER_STATS | WRITE_INFO);
	}

	public void applyResults(Function<LocalPlayer, Player> converter, int direction, int flags) {
		int diff = attackScore - oudlers.getRequiredScore();
		int absoluteDiff = BASE_SCORE + Math.abs(diff);

		// Assuming players are reordered (otherwise wtf)
		int numberOfPlayers = players.length;
		Handful[] handfuls = new Handful[numberOfPlayers];
		Misery[] miseries = new Misery[numberOfPlayers];
		for (int i = 0; i < numberOfPlayers; i++) {
			handfuls[i] = players[i].handful;
			miseries[i] = players[i].misery;
		}

		// Makes at most 5 stats queries instead of 15
		LocalStats[] stats = new LocalStats[numberOfPlayers];
		if (((flags >> 2) & 1) == 1 || ((flags >> 3) & 1) == 1)
			for (int i = 0; i < numberOfPlayers; i++)
				stats[i] = converter.apply(players[i]).getStats(date, numberOfPlayers);

		if ((flags & 1) == 1)
			calculateAttackScore(diff, absoluteDiff, handfuls, miseries);
		if (((flags >> 1) & 1) == 1)
			calculateGlobalStats(handfuls, miseries, direction);
		if (((flags >> 2) & 1) == 1)
			calculatePlayerScores(stats, direction);
		if (((flags >> 3) & 1) == 1) {
			calculatePlayerStats(diff, handfuls, miseries, stats, direction);
			computeBestTurns(stats);
		}
		if (direction == ADD_GAME_DIRECTION && ((flags >> 4) & 1) == 1)
			writeInformation(diff, absoluteDiff, handfuls, miseries);
	}

	private void calculateAttackScore(int diff, int absoluteDiff, Handful[] handfuls, Misery[] miseries) {
		attackFinalScore = absoluteDiff * contract.getMultiplier();
		if (diff < 0)
			attackFinalScore *= -1;

		// Compute before modifiers are applied
		attackBaseScore = attackFinalScore;

		for (Handful handful : handfuls) {
			if (handful == null)
				continue;
			int points = handful.getExtraPoints();
			if (diff < 0) {
				attackFinalScore -= points;
			} else {
				attackFinalScore += points;
			}
		}

		if (petitAuBout != null)
			attackFinalScore += petitAuBout.getAttackPoints() * contract.getMultiplier();

		if (attackScore == 91) {
			if (slam == Slam.ATTACK) {
				attackFinalScore += 2 * SLAM_SCORE;
			} else {
				attackFinalScore += SLAM_SCORE;
			}
		} else if (attackScore == 0) {
			if (slam == Slam.DEFENSE) {
				attackFinalScore -= 2 * SLAM_SCORE;
			} else {
				attackFinalScore -= SLAM_SCORE;
			}
		}

		int length = players.length;
		LocalPlayer attacker = getAttacker();
		if (selfCalled()) {
			attacker.score += 4 * attackFinalScore;
		} else if (length == 5) {
			LocalPlayer ally = getAlly();
			attacker.score += 2 * attackFinalScore;
			ally.score += attackFinalScore;
		} else {
			attacker.score += (length - 1) * attackFinalScore;
		}

		for (LocalPlayer local : players)
			if (local.side == Side.DEFENSE)
				local.score -= attackFinalScore;

		for (int i = 0; i < miseries.length; i++) {
			Misery misery = miseries[i];
			if (misery == null)
				continue;
			LocalPlayer owner = players[i];
			int points = misery.getExtraPoints();
			owner.score += (length - 1) * points;
			for (LocalPlayer other : players)
				if (!owner.equals(other))
					other.score -= points;
		}
	}

	private void calculateGlobalStats(Handful[] handfuls, Misery[] miseries, int direction) {
		GlobalStats stats = Tarot.getGlobalStats(date, players.length);
		Maps.increment(contract, stats.contracts, 1, direction);
		Maps.increment(contract, stats.oudlers, oudlers.ordinal(), direction);
		if (selfCalled())
			Maps.increment(contract, stats.selfCalls, 1, direction);
		for (Handful handful : handfuls)
			if (handful != null)
				Maps.increment(handful, stats.handfuls, 1, direction);
		for (Misery misery : miseries)
			if (misery != null)
				Maps.increment(misery, stats.miseries, 1, direction);
		if (petitAuBout != null)
			Maps.increment(petitAuBout, stats.petits, 1, direction);
	}

	private void calculatePlayerScores(LocalStats[] stats, int direction) {
		for (int i = 0; i < players.length; i++)
			stats[i].totalScore += direction * players[i].score;
	}

	private void calculatePlayerStats(int diff, Handful[] handfuls, Misery[] miseries, LocalStats[] stats, int direction) {
		LocalStats attackerStats = stats[ATTACKER_INDEX];
		Maps.increment(contract, diff >= 0 ? attackerStats.successfulTakes : attackerStats.failedTakes, 1, direction);
		Maps.increment(contract, attackerStats.playedGames, 1, direction);

		if (selfCalled()) {
			Maps.increment(contract, attackerStats.selfCalls, 1, direction);
		} else if (players.length == 5) {
			LocalStats allyStats = stats[ALLY_INDEX];
			Maps.increment(contract, allyStats.calledTimes, 1, direction);
			Maps.increment(contract, allyStats.playedGames, 1, direction);
		}

		for (int i = getDefenseIndex(); i < players.length; i++) {
			LocalStats defenderStats = stats[i];
			Maps.increment(contract, defenderStats.playedGames, 1, direction);
		}

		for (int i = 0; i < handfuls.length; i++)
			if (handfuls[i] != null)
				Maps.increment(contract, stats[i].handfuls, 1, direction);

		for (int i = 0; i < miseries.length; i++)
			if (miseries[i] != null)
				Maps.increment(contract, stats[i].miseries, 1, direction);
	}

	private void computeBestTurns(LocalStats[] stats) {
		for (int i = 0; i < players.length; i++) {
			Maps.computeIfHigher(contract, players[i].score, stats[i].bestTurns, 1);
			Maps.computeIfHigher(contract, players[i].score, stats[i].worstTurns, -1);
		}
	}

	public void delete() {
		List<Game> games = Tarot.ALL_GAMES.get(date);
		int index = games.indexOf(this);
		games.remove(this);

		// Last game in the month was cleared
		if (games.isEmpty())
			Tarot.ALL_GAMES.remove(date);

		String fileName = date.getFileName();
		JsonArray array = Files.getJsonArrayFromFile(fileName);
		array.remove(index);
		Files.write(fileName, array);
	}

	@Override
	public void edit() {
		List<Game> games = Tarot.ALL_GAMES.get(date);
		int index = games.indexOf(this);
		String fileName = date.getFileName();
		JsonArray array = Files.getJsonArrayFromFile(fileName);
		array.set(index, toJson());
		Files.write(fileName, array);
	}

	private LocalPlayer getAlly() {
		return players[ALLY_INDEX];
	}

	private LocalPlayer getAttacker() {
		return players[ATTACKER_INDEX];
	}

	private int getDefenseIndex() {
		return players.length < 5 || selfCalled() ? ALLY_INDEX : ALLY_INDEX + 1;
	}

	public String getDescription() {
		// Text inside a button ignores "\n", convert it to HTML and skip lines this way
		return "<html>" + String.join("<br>", description) + "</html>";
	}

	public String getDetails() {
		return String.join("\n", details);
	}

	public String getFormattedDayName() {
		return dayOfMonth == 1 ? "1er" : String.valueOf(dayOfMonth);
	}

	private LocalStats getStats(LocalPlayer local, Function<LocalPlayer, Player> converter) {
		return converter.apply(local).getStats(date, players.length);
	}

	public void reorderPlayers() {
		for (int i = ATTACKER_INDEX; i < players.length; i++)
			if (players[i].side == Side.ATTACK)
				Utils.swap(players, i, 0);
		// Index 0 is attacker, we can now start at 1
		for (int i = ALLY_INDEX; i < players.length; i++)
			if (players[i].side == Side.ATTACK_ALLY)
				Utils.swap(players, i, 1);
		// Order defenders by name
		Arrays.sort(players, getDefenseIndex(), players.length, Comparator.comparing(local -> DEFAULT_CONVERTER.apply(local).getName()));
	}

	private boolean selfCalled() {
		return players.length == 5 && players[ALLY_INDEX].side == Side.DEFENSE;
	}

	@Override
	public JsonObject toJson() {
		JsonObject object = new JsonObject();
		object.addProperty("day", dayOfMonth);

		object.addProperty("contract", contract.ordinal());
		object.addProperty("attack_score", attackScore);
		object.addProperty("oudlers", oudlers.ordinal());
		if (petitAuBout != null)
			object.addProperty("petit_au_bout", petitAuBout.ordinal());
		if (slam != null)
			object.addProperty("slam", slam.ordinal());

		JsonArray playersArray = new JsonArray();
		for (LocalPlayer player : players)
			playersArray.add(player.toJson());
		object.add("players", playersArray);

		return object;
	}

	private void writeInformation(int diff, int absoluteDiff, Handful[] handfuls, Misery[] miseries) {
		// Game might have been edited
		description.clear();
		details.clear();

		int month = date.month().ordinal() + 1;
		String day = (dayOfMonth == 0 ? "?" : dayOfMonth) + (month < 10 ? "/0" : "/") + month;
		description.add("(" + day + ") " + contract.getName() + " – " + oudlers.getDisplay()
				+ " – Score : " + attackScore + "/" + oudlers.getRequiredScore()
				+ " (" + attackFinalScore + ")");
		details.add("Partie du " + day + "/" + date.year() + " – " + players.length + " joueurs");

		List<String> defenderNames = new ArrayList<>(players.length - 1);
		for (LocalPlayer local : players)
			if (local.side == Side.DEFENSE)
				defenderNames.add(DEFAULT_CONVERTER.apply(local).getName());

		Player attacker = DEFAULT_CONVERTER.apply(getAttacker());
		String defense = String.join(", ", defenderNames);
		if (players.length < 5 || selfCalled()) {
			description.add(attacker.getName() + " vs. " + defense);
			details.add("Attaque : " + attacker.getName());
			details.add("Défense : " + defense);
		} else {
			Player ally = DEFAULT_CONVERTER.apply(getAlly());
			String attack = attacker.getName() + " & " + ally.getName();
			description.add(attack + " vs. " + defense);
			details.add("Attaque : " + attack);
			details.add("Défense : " + defense);
		}

		details.add("");
		details.add("Score de l'attaque : " + attackScore + "/" + oudlers.getRequiredScore());
		details.add("Points de base : " + (diff == 0 ? BASE_SCORE : BASE_SCORE + " + " + Math.abs(diff) + " = " + absoluteDiff));
		details.add("Multiplicateur : " + contract.getMultiplier() + " (" + contract.getName() + ")");
		details.add("");
		details.add("Total des points : " + attackBaseScore);

		StringJoiner extra = new StringJoiner(" – ");
		for (int i = 0; i < handfuls.length; i++) {
			Handful handful = handfuls[i];
			if (handful == null)
				continue;
			Player player = DEFAULT_CONVERTER.apply(players[i]);
			int points = handful.getExtraPoints();
			if (diff < 0) {
				details.add(handful.getFullName() + " (" + player.getName() + ") : -" + points);
			} else {
				details.add(handful.getFullName() + " (" + player.getName() + ") : +" + points);
			}
			extra.add(handful.getFullName() + " " + Utils.getOfWord(player.getName()));
		}

		for (int i = 0; i < miseries.length; i++) {
			Misery misery = miseries[i];
			if (misery == null)
				continue;
			Player player = DEFAULT_CONVERTER.apply(players[i]);
			details.add(misery.getFullName() + " (" + player.getName() + ") : ±" + misery.getExtraPoints());
			extra.add(misery.getFullName() + " " + Utils.getOfWord(player.getName()));
		}

		if (petitAuBout != null) {
			int points = petitAuBout.getAttackPoints() * contract.getMultiplier();
			details.add(petitAuBout.getFullName() + " : " + (points > 0 ? "+" : "") + points);
			extra.add(petitAuBout.getFullName());
		}

		if (extra.length() > 0)
			description.add(extra.toString());

		if (slam != null)
			description.add("Chelem " + slam.getName().toLowerCase());

		if (attackScore == 91) {
			if (slam == Slam.ATTACK) {
				details.add("Chelem (annoncé) : +" + 2 * SLAM_SCORE);
			} else {
				details.add("Chelem (non annoncé) : +" + SLAM_SCORE);
			}
		} else if (attackScore == 0) {
			if (slam == Slam.DEFENSE) {
				details.add("Chelem (annoncé) : -" + 2 * SLAM_SCORE);
			} else {
				details.add("Chelem (non annoncé) : -" + SLAM_SCORE);
			}
		}

		details.add("");
		details.add("Distribution finale :");
		for (LocalPlayer local : players)
			details.add("‣ " + DEFAULT_CONVERTER.apply(local).getName() + (local.score >= 0 ? " : +" : " : ") + local.score);
	}

}
