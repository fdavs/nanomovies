package no.skavdahl.udacity.utils;

import java.io.IOException;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import no.skavdahl.udacity.popularmovies.BuildConfig;

/**
 * @author fdavs
 */
public class WebApiClient {

	protected final String LOG_TAG = getClass().getSimpleName();

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
		boolean verbose = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.VERBOSE);
		if (verbose) {
			// don't log the api key
			int keyStart = endpoint.indexOf("key=") + "key=".length();
			int keyEnd = endpoint.indexOf("&", keyStart);
			if (keyEnd < 0)
				keyEnd = endpoint.length();

			String loggedEndpoint = endpoint.substring(0, keyStart) + "..." + endpoint.substring(keyEnd);

			Log.v(LOG_TAG, "GET request " + loggedEndpoint);
		}

		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url(endpoint).build();
		String jsonResult = client.newCall(request).execute().body().string();

		if (jsonResult.length() == 0)
			throw new IOException("Empty response from endpoint " + endpoint);

		// TODO cache query result

		return jsonResult;
	}
}
