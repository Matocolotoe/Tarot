package fr.giovanni75.tarot;

import fr.giovanni75.tarot.objects.Game;
import fr.giovanni75.tarot.objects.LocalPlayer;
import fr.giovanni75.tarot.objects.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class Utils {

	public static void calculateScores(List<Game> displayedGames, List<Game> selectedGames, Function<LocalPlayer, Player> converter) {
		int minIndex = displayedGames.indexOf(selectedGames.getFirst());
		for (int i = 0; i < minIndex; i++) {
			Game game = displayedGames.get(i);
			game.applyResults(converter, Game.ADD_GAME_DIRECTION, Game.PLAYER_SCORES);
		}
	}

	public static Set<Player> getAllPlayers(List<Game> games) {
		Set<Player> players = new HashSet<>();
		for (Game game : games)
			for (LocalPlayer local : game.players)
				players.add(local.player);
		return players;
	}

	public static Function<LocalPlayer, Player> getConverter(Map<Integer, Player> profiles) {
		return local -> profiles.get(local.player.getID());
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
		return selectedGames.size() + " parties, " + text + " " + date.getName().toLowerCase();
	}

	public static void swap(Object[] array, int i, int j) {
		Object temp = array[i];
		array[i] = array[j];
		array[j] = temp;
	}

}
