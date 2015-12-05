package no.skavdahl.udacity.popularmovies.mdb;

import android.content.Context;
import android.net.Uri;
import android.util.DisplayMetrics;

/**
 * Facade for requests to themoviedb.org.
 *
 * @author fdavs
 */
public class Request {

	/**
	 * Returns the query URL for standard movie lists. See related lists at
	 * http://docs.themoviedb.apiary.io/#reference/movies/movielatest
	 *
	 * @param apiKey The API key necessary to execute a query at themoviedb.org
	 * @param listName The name of the list to return. Valid names are documented
	 *                 in the API docs linked to in the main description.
	 * @param page The page number to return for a long result
	 *
	 * @return a String with the URL that will perform the appropriate query at themoviedb.org.
	 */
	public static String getStandardMovieListURL(String apiKey, String listName, int page) {
		return Uri.parse("http://api.themoviedb.org/3/movie").buildUpon()
			.appendPath(listName)
			.appendQueryParameter("api_key", apiKey)
			.appendQueryParameter("page", Integer.toString(page))
			.toString();
	}

	/**
	 * Returns the query URL for poster thumbnail images.
	 *
 	 * @param posterPath The path to the poster as reported by movie discovery responses.
	 */
	public static String getPosterThumbnailDownloadURL(String posterPath, int posterWidthPixels) {
		return "http://image.tmdb.org/t/p/w" + posterWidthPixels + posterPath;
	}

	/**
	 * Returns a download URL for a high-resolution poster image. The meaning of "high
	 * resolution" is determined by the device capabilities (screen resolution and connection
	 * state).
	 *
	 * @return a download URL for a higher-resolution poster image.
	 */
	public static String getPosterHiresDownloadURL(Context context, String posterPath) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();

		// TODO Consider device connectivity

		// TODO Refactor: Clean up this code (resolution selection)
		int widths[] = new int[] { 92, 154, 185, 342, 500, 780, 1000 };
		String options[]= new String[] { "w92", "w154", "w185", "w342", "w500", "w780", "original"};

		String size = null;
		for (int i = options.length - 1; i >= 0; --i) {
			if (dm.widthPixels > widths[i]) {
				size = options[i];
				break;
			}
		}
		if (size == null)
			size = options[0];

		return "http://image.tmdb.org/t/p/" + size + posterPath;
	}

}
