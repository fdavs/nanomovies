package no.skavdahl.udacity.utils;

import java.lang.reflect.Array;

/**
 * A collection of array utilities.
 *
 * @author fdavs
 */
public class Arrays {

	/**
	 * Returns an array similar to the given source array but with the given item
	 * prepended in front.
	 */
	public static <T> T[] prepend(T item, T[] src) {
		if (item == null)
			return src;

		T[] result;
		if (src == null || src.length == 0)
			result = newArray(item, 1);
		else {
			result = newArray(item, src.length + 1);
			System.arraycopy(src, 0, result, 1, src.length);
		}
		result[0] = item;

		return result;
	}

	/**
	 * Returns an array similar to the given source array but with the given item
	 * appended at the end.
	 */
	public static <T> T[] append(T[] src, T item) {
		T[] result;
		if (src == null || src.length == 0)
			result = newArray(item, 1);
		else {
			result = newArray(item, src.length + 1);
			System.arraycopy(src, 0, result, 0, src.length);
		}
		result[result.length - 1] = item;

		return result;
	}

	@SuppressWarnings("unchecked")
	private static <T> T[] newArray(T prototype, int length) {
		return (T[]) Array.newInstance(prototype.getClass(), length);
	}

	/**
	 * Returns true if the array contains a specific item.
	 */
	public static <T> boolean arrayContains(T[] arr, T item) {
		if (arr == null)
			return false;

		if (item != null) {
			for (T i : arr) {
				if (i == item || item.equals(i))
					return true;
			}
		}
		else {
			for (T i : arr) {
				if (i == null) return true;
			}
		}

		return false;
	}
}
