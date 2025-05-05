package fr.giovanni75.tarot;

import fr.giovanni75.tarot.enums.Nameable;

import java.util.Map;

public final class Maps {

	public static <K> void computeIfHigher(K key, int value, Map<K, Integer> map, int direction) {
		Integer current = map.get(key);
		if (current == null || direction * value > direction * current)
			map.put(key, value);
	}

	public static <K> void increment(K key, Map<K, Integer> map, int increment, int direction) {
		map.put(key, map.getOrDefault(key, 0) + direction * increment);
	}

	public static <K extends Nameable> String max(Map<K, Integer> map, String display, int direction) {
		K maxKey = null;
		int maxValue = 0;
		for (Map.Entry<K, Integer> entry : map.entrySet()) {
			int current = entry.getValue();
			if (direction * current > direction * maxValue) {
				maxKey = entry.getKey();
				maxValue = current;
			}
		}
		return maxKey == null ? Tarot.NONE_STRING : String.format(display, maxValue, maxKey.getName());
	}

	public static <K> int sum(Map<K, Integer> map) {
		int sum = 0;
		for (int value : map.values())
			sum += value;
		return sum;
	}

}
