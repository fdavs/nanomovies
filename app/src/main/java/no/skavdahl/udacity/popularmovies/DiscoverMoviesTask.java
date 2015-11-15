package no.skavdahl.udacity.popularmovies;

import android.Manifest;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import no.skavdahl.udacity.popularmovies.mdb.MoviesQuery;

/**
 * @author fdavs
 */
public class DiscoverMoviesTask extends AsyncTask<Void, Void, List> {

	private final String LOG_TAG = DiscoverMoviesTask.class.getSimpleName();
	private final String apiKey;

	public DiscoverMoviesTask(final String apiKey) {
		this.apiKey = apiKey;
	}

	@Override
	protected List doInBackground(Void... params) {
		try {
			MoviesQuery query = new MoviesQuery();
			String json = query.getPopularMovies(apiKey);
			return Collections.emptyList();
		}
		catch (IOException e) {
			Log.e(LOG_TAG, "Movie discovery failed with error", e);
			return Collections.emptyList();
		}
	}
}
