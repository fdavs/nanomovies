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
	 * prepended in front
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

	@SuppressWarnings("unchecked")
	private static <T> T[] newArray(T prototype, int length) {
		return (T[]) Array.newInstance(prototype.getClass(), length);
	}
}
