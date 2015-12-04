package no.skavdahl.udacity.popularmovies;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import android.content.Context;
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

	public interface Listener {
		void onDownloadSuccess(List<Movie> movies);
		void onNetworkFailure();
	}

	private final String LOG_TAG = DiscoverMoviesTask.class.getSimpleName();

	private final String apiKey;
	private final Context context;
	private final DiscoveryMode discoveryMode;
	private final Listener listener;

	/** Set to true if the task fails with an error. */
	private boolean failed;

	/**
	 * Prepares a movie discovery task.
	 *
	 * @param discoveryMode how to discover movies
	 * @param apiKey the API key with which to perform the server queries
	 * @param context Access to the application environment (resources)
	 * @param listener An implementation that will be notified when the task has completed
	 *                 (whether with failure or success)
	 */
	public DiscoverMoviesTask(
		final DiscoveryMode discoveryMode,
		final String apiKey,
		final Context context,
		final Listener listener) {

		this.discoveryMode = discoveryMode;
		this.apiKey = apiKey;
		this.context = context;
		this.listener = listener;
	}

	@Override
	protected List<Movie> doInBackground(Void... params) {
		try {
			String jsonResponse = executeQuery();

			DiscoverMoviesJSONAdapter jsonAdapter = new DiscoverMoviesJSONAdapter(context.getResources());
			return jsonAdapter.getMoviesList(jsonResponse);
		}
		catch (Exception e) {
			Log.e(LOG_TAG, "Movie discovery failed with error", e);

			synchronized (this) {
				failed = true;
			}

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
			case NEW_MOVIES:
				return query.getNewMovies(apiKey);
			default:
				throw new AssertionError("Invalid discovery mode: " + discoveryMode);
		}
	}

	@Override
	protected void onPostExecute(List<Movie> movies) {
		boolean failed;
		synchronized (this) {
			failed = this.failed;
		}

		if (failed)
			listener.onNetworkFailure();
		else
			listener.onDownloadSuccess(movies);
	}
}
