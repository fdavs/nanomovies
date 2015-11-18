package no.skavdahl.udacity.popularmovies;

import java.util.Collections;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;

import no.skavdahl.udacity.popularmovies.model.Movie;
import no.skavdahl.udacity.popularmovies.mdb.DiscoverMovies;
import no.skavdahl.udacity.popularmovies.mdb.DiscoverMoviesJSONAdapter;

/**
 * @author fdavs
 */
public class DiscoverMoviesTask extends AsyncTask<Void, Void, List<Movie>> {

	private final String LOG_TAG = DiscoverMoviesTask.class.getSimpleName();
	private final String apiKey;
	private final MoviePosterAdapter viewAdapter;

	public DiscoverMoviesTask(final String apiKey, final MoviePosterAdapter viewAdapter) {
		this.apiKey = apiKey;
		this.viewAdapter = viewAdapter;
	}

	@Override
	protected List<Movie> doInBackground(Void... params) {
		try {
			DiscoverMovies query = new DiscoverMovies();
			String jsonResponse = query.getPopularMovies(apiKey);

			DiscoverMoviesJSONAdapter jsonAdapter = new DiscoverMoviesJSONAdapter();
			return jsonAdapter.getMoviesList(jsonResponse);
		}
		catch (Exception e) {
			Log.e(LOG_TAG, "Movie discovery failed with error", e);
			return Collections.emptyList();
		}
	}

	@Override
	protected void onPostExecute(List<Movie> movies) {
		viewAdapter.setMovies(movies);
	}
}
