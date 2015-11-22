package no.skavdahl.udacity.popularmovies;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

import no.skavdahl.udacity.popularmovies.mdb.DiscoveryMode;
import no.skavdahl.udacity.popularmovies.model.Movie;
import no.skavdahl.udacity.popularmovies.mdb.DiscoverMovies;
import no.skavdahl.udacity.popularmovies.mdb.DiscoverMoviesJSONAdapter;

/**
 * Task for performing async movie discovery requests to themoviedb.org
 *
 * @author fdavs
 */
public class DiscoverMoviesTask extends AsyncTask<Void, Void, List<Movie>> {

	private final String LOG_TAG = DiscoverMoviesTask.class.getSimpleName();
	private final String apiKey;
	private final MoviePosterAdapter viewAdapter;
	private final DiscoveryMode discoveryMode;

	/**
	 * Prepares a movie discovery task.
	 *
	 * @param discoveryMode how to discover movies
	 * @param apiKey the API key with which to perform the server queries
	 * @param viewAdapter the adapter to which to send the query response
	 */
	public DiscoverMoviesTask(
		final DiscoveryMode discoveryMode,
		final String apiKey,
		final MoviePosterAdapter viewAdapter) {

		this.discoveryMode = discoveryMode;
		this.apiKey = apiKey;
		this.viewAdapter = viewAdapter;
	}

	@Override
	protected List<Movie> doInBackground(Void... params) {
		try {
			String jsonResponse = executeQuery();

			DiscoverMoviesJSONAdapter jsonAdapter = new DiscoverMoviesJSONAdapter();
			return jsonAdapter.getMoviesList(jsonResponse);
		}
		catch (Exception e) {
			Log.e(LOG_TAG, "Movie discovery failed with error", e);
			return Collections.emptyList();
		}
	}

	private String executeQuery() throws IOException {
		DiscoverMovies query = new DiscoverMovies();
		switch (discoveryMode) {
			case POPULAR_MOVIES:
				return query.getPopularMovies(apiKey);
			case HIGH_RATED_MOVIES:
				return query.getHighestRatedMovies(apiKey);
			default:
				throw new AssertionError("Invalid discovery mode: " + discoveryMode);
		}
	}

	@Override
	protected void onPostExecute(List<Movie> movies) {
		viewAdapter.setMovies(movies);
	}
}
