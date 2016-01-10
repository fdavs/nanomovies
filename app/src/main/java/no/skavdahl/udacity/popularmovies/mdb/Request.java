package no.skavdahl.udacity.popularmovies.mdb;

import android.net.Uri;

/**
 * Facade for requests to themoviedb.org.
 *
 * @author fdavs
 */
public class Request {

	public enum ImageType {
		POSTER,
		BACKDROP
	}

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
	 * Returns the query URL for movie details. See related lists at
	 * http://docs.themoviedb.apiary.io/#reference/movies/movieid/get
	 *
	 * @param apiKey The API key necessary to execute a query at themoviedb.org
	 * @param movieId Which movie to query.
	 *
	 * @return a String with the URL that will perform the appropriate query at themoviedb.org.
	 */
	public static String getMovieURL(String apiKey, int movieId) {
		return Uri.parse("http://api.themoviedb.org/3/movie").buildUpon()
			.appendPath(Integer.toString(movieId))
			.appendQueryParameter("api_key", apiKey)
			.appendQueryParameter("append_to_response", "videos,reviews")
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
	 * Returns a download URL for a high-resolution image. The meaning of "high resolution" is
	 * determined by the device capabilities (screen resolution).
	 *
	 * @return a download URL for a higher-resolution poster image.
	 */
	public static String getImageDownloadURL(ImageType imageType, String imagePath, int availableWidthPixels) {
		// TODO Consider device connectivity quality
		//      if the device is connected over a low bandwidth connection, a lower resolution
		//      picture should be requested

		String optimalWidth;
		switch (imageType) {
			case POSTER:
				optimalWidth = calculateOptimalPosterWidth(availableWidthPixels);
				break;
			case BACKDROP:
				optimalWidth = calculateOptimalBackdropWidth(availableWidthPixels);
				break;
			default:
				throw new AssertionError(imageType);
		}

		return "http://image.tmdb.org/t/p/" + optimalWidth + imagePath;
	}

	/**
	 * Calculates the optimal poster width to request from the server given some available
	 * space (in pixels).
	 *
	 * @param availableWidthPixels Available space in pixels
	 *
	 * @return A width code suitable for inclusion in the request URL
	 *
	 * @see <a href="http://docs.themoviedb.apiary.io/#reference/configuration/configuration/get">themoviedb.org API docs</a>
	 */
	private static String calculateOptimalPosterWidth(int availableWidthPixels) {
		// TODO the available sizes should be retrieved from the configuration
		//      http://api.themoviedb.org/3/configuration
		final int widthOptions[] = { 92, 154, 185, 342, 500, 780 };
		final String optionCodes[]= { "w92", "w154", "w185", "w342", "w500", "w780" };

		return bestMatch(widthOptions, optionCodes, availableWidthPixels);
	}

	/**
	 * Calculates the optimal backdrop width to request from the server given some available
	 * space (in pixels).
	 *
	 * @param availableWidthPixels Available space in pixels
	 *
	 * @return A width code suitable for inclusion in the request URL
	 *
	 * @see <a href="http://docs.themoviedb.apiary.io/#reference/configuration/configuration/get">themoviedb.org API docs</a>
	 */
	private static String calculateOptimalBackdropWidth(int availableWidthPixels) {
		// TODO the available sizes should be retrieved from the configuration
		//      http://api.themoviedb.org/3/configuration
		final int widthOptions[] = { 300, 780, 1280 };
		final String optionCodes[]= { "w300", "w780", "w1280" };

		return bestMatch(widthOptions, optionCodes, availableWidthPixels);
	}

	private static String bestMatch(int[] widthOption, String[] optionCode, int availableWidth) {
		int best = Integer.MAX_VALUE;
		String bestCode = null;
		for (int i = widthOption.length - 1; i >= 0; --i) {
			int distance = Math.abs(availableWidth - widthOption[i]);
			if (distance <= best) {
				best = distance;
				bestCode = optionCode[i];
			}
			else break;
		}

		return bestCode;
	}

}
