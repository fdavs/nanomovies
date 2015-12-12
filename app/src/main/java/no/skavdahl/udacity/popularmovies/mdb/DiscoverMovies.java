package no.skavdahl.udacity.popularmovies.mdb;

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

	/**
	 * Submits a movie discovery request to themoviedb.org. The query is performed
	 * synchronously. The response is returned as a raw JSON string.
	 *
	 * @param apiKey The API key necessary to perform a query at themoviedb.org
	 * @param list Which movie list to query.
	 *
	 * @return the JSON string response
	 *
	 * @throws IOException if the query failed or returned a non-sensible result
	 */
	public String discoverMovies(String apiKey, StandardMovieList list) throws IOException {
		String endpoint = Request.getStandardMovieListURL(apiKey, list.getListName(), 1);
		return executeQuery(endpoint);
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

			if (Log.isLoggable(LOG_TAG, Log.VERBOSE))
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
