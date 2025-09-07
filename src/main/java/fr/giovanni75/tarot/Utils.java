package fr.giovanni75.tarot;

import fr.giovanni75.tarot.enums.Contract;
import fr.giovanni75.tarot.objects.Game;
import fr.giovanni75.tarot.objects.LocalPlayer;
import fr.giovanni75.tarot.objects.Player;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;

public final class Utils {

	private static final DecimalFormat DOUBLE_DECIMAL_FORMAT = new DecimalFormat("#.##");

	public static void calculateScores(List<Game> displayedGames, List<Game> selectedGames, Function<LocalPlayer, Player> converter) {
		int minIndex = displayedGames.indexOf(selectedGames.getFirst());
		for (int i = 0; i < minIndex; i++) {
			Game game = displayedGames.get(i);
			game.applyResults(converter, Game.ADD_GAME_DIRECTION, Game.PLAYER_SCORES);
		}
	}

	public static int count(Collection<Game> games, Contract contract) {
		int count = 0;
		for (Game game : games)
			if (game.contract == contract)
				count++;
		return count;
	}

	public static String format(double value) {
		return DOUBLE_DECIMAL_FORMAT.format(value);
	}

	public static Set<Player> getAllPlayers(Collection<Game> games) {
		Set<Player> players = new HashSet<>();
		for (Game game : games)
			for (LocalPlayer local : game.players)
				players.add(local.player);
		return players;
	}

	public static Function<LocalPlayer, Player> getConverter(Map<Integer, Player> profiles) {
		return local -> profiles.get(local.player.getID());
	}

	public static String getOfWord(String name) {
		return switch (name.charAt(0)) {
			case 'A', 'E', 'H', 'I', 'O', 'U', 'Y' -> "d'" + name;
			default -> "de " + name;
		};
	}

	public static String getTitle(List<Game> selectedGames, DateRecord date) {
		Game first = selectedGames.getFirst();
		Game last = selectedGames.getLast();
		String text;
		if (first.dayOfMonth == last.dayOfMonth) {
			text = "le " + first.getFormattedDayName();
		} else {
			text = "du " + first.getFormattedDayName() + " au " + last.getFormattedDayName();
		}
		// Always plural here since two distinct games have to be selected
		return selectedGames.size() + " parties, " + text + " " + date.getName().toLowerCase();
	}

	public static void swap(Object[] array, int i, int j) {
		Object temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}

}
