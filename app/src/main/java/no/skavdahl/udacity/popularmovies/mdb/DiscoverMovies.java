package no.skavdahl.udacity.popularmovies.mdb;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Encapsulates /discover/movie network queries performed against themoviedb.org.
 *
 * @author fdavs
 */
public class DiscoverMovies {

	private static final String LOG_TAG = DiscoverMovies.class.getSimpleName();

	/** Standard "small" poster width in pixels */
	// Note: Could have used android.util.Size but this requires API level 21
	// Note: This size was found by measuring a poster from themoviedb.org
	// TODO Refactor: move to another class not tied specifically to themoviedb.org
	public static final Point POSTER_SIZE_PIXELS = new Point(185, 283);

	/**
	 * Queries themoviedb.org for the most popular movies. The response is returned
	 * as a raw JSON string.
	 *
	 * @param apiKey The API key necessary to perform a query at themoviedb.org
	 *
	 * @return the JSON string response
	 *
	 * @throws IOException if the query failed or returned a non-sensible result
	 */
	public String getPopularMovies(String apiKey) throws IOException {
		String endpoint = getDiscoverMovieURL(apiKey, "popular", 1);
		return executeQuery(endpoint);
	}

	/**
	 * Queries themoviedb.org for the highest rated movies. The response is returned
	 * as a raw JSON string.
	 *
	 * @param apiKey The API key necessary to perform a query at themoviedb.org
	 *
	 * @return the JSON string response
	 *
	 * @throws IOException if the query failed or returned a non-sensible result
	 */
	public String getHighestRatedMovies(String apiKey) throws IOException {
		String endpoint = getDiscoverMovieURL(apiKey, "top_rated", 1);
		return executeQuery(endpoint);
	}

	/**
	 * Queries themoviedb.org for new movies. The response is returned as a raw JSON string.
	 *
	 * @param apiKey The API key necessary to perform a query at themoviedb.org
	 *
	 * @return the JSON string response
	 *
	 * @throws IOException if the query failed or returned a non-sensible result
	 */
	public String getNewMovies(String apiKey) throws IOException {
		String endpoint = getDiscoverMovieURL(apiKey, "now_playing", 1);
		return executeQuery(endpoint);
	}

	public static String getPosterThumbnailDownloadURL(String posterPath) {
		return "http://image.tmdb.org/t/p/w" + POSTER_SIZE_PIXELS.x + posterPath;
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

	/**
	 * Returns the query URL to "discover movies" according to the provided parameters.
	 *
	 * @param apiKey The API key necessary to execute a query at themoviedb.org
	 * @param listName The name of the list to return
	 * @param page The page number to return for a long result
	 *
	 * @return a String with the URL that will perform the appropriate query at themoviedb.org.
	 */
	protected String getDiscoverMovieURL(String apiKey, String listName, int page) {
		String MOVIEDB_DISCOVERY_BASEURL = "http://api.themoviedb.org/3/movie"; // TODO extract constant
		return Uri.parse(MOVIEDB_DISCOVERY_BASEURL).buildUpon()
			.appendPath(listName)
			.appendQueryParameter("api_key", apiKey)
			.appendQueryParameter("page", Integer.toString(page))
			.toString();
	}

	/**
	 * Performs a GET request to themoviedb.org. The response is returned as a raw JSON string.
	 *
	 * @param endpoint The URL to access
	 *
	 * @return the JSON string response
	 *
	 * @throws IOException if the query failed or returned a non-sensible result
	 */
	protected String executeQuery(String endpoint) throws IOException {
		// This code is based on the GitHub Gist provided for the Sunshine app
		// https://gist.github.com/udacityandroid/d6a7bb21904046a91695

		// TODO cache query result

		if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
			// don't log the api key
			int keyStart = endpoint.indexOf("key=") + "key=".length();
			int keyEnd = endpoint.indexOf("&", keyStart);
			if (keyEnd < 0)
				keyEnd = endpoint.length();

			String loggedEndpoint = endpoint.substring(0, keyStart) + "..." + endpoint.substring(keyEnd);

			Log.v(LOG_TAG, "Issuing GET request " + loggedEndpoint);
		}

		HttpURLConnection connection = null;

		try {
			URL url = new URL(endpoint);

			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();

			InputStream inputStream = connection.getInputStream();

			String jsonResult = copyToMemory(inputStream);
			if (jsonResult.length() == 0)
				throw new IOException("empty response");

			Log.v(LOG_TAG, "GET request completed successfully");

			return jsonResult;
		}
		finally {
			if (connection != null)
				connection.disconnect();
		}
	}

	/**
	 * Copies the contents of the given stream to an in-memory string.
	 *
	 * @param stream The stream to copy from. This stream is closed upon completion.
	 *
	 * @return a string with the stream contents. This might be an empty string.
	 *
	 * @throws IOException if the copy operation cannot be completed successfully
	 */
	protected String copyToMemory(InputStream stream) throws IOException {
		if (stream == null)
			return ""; // nothing to do

		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new InputStreamReader(stream));
			StringBuilder buffer = new StringBuilder();

			int ch;
			while ((ch = reader.read()) != -1)
				buffer.append((char)ch);

			return buffer.toString();
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				}
				catch (IOException e) {
					Log.e(LOG_TAG, "Unable to close stream", e);
				}
			}
		}
	}
}
