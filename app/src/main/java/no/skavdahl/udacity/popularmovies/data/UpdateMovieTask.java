package no.skavdahl.udacity.popularmovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import no.skavdahl.udacity.popularmovies.BuildConfig;
import no.skavdahl.udacity.popularmovies.mdb.DiscoverMovies;
import no.skavdahl.udacity.popularmovies.mdb.MdbJSONAdapter;
import no.skavdahl.udacity.popularmovies.model.Movie;

import static no.skavdahl.udacity.popularmovies.data.PopularMoviesContract.*;

/**
 * Downloads movie data from themoviedb.org and updates the local database.
 *
 * @author fdavs
 */
public class UpdateMovieTask extends AsyncTask<Integer, Void, Void> {

	private final String LOG_TAG = getClass().getSimpleName();

	private final Context context;

	public UpdateMovieTask(final Context context) {
		this.context = context;
	}

	@Override
	protected Void doInBackground(Integer... params) {
		final boolean verbose = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.VERBOSE);

		final int movieId = params[0];

		if (verbose) Log.v(LOG_TAG, "Starting update of data for movie " + movieId + "...");

		try {
			if (verbose) Log.v(LOG_TAG, "Starting download of movie data from server...");

			DiscoverMovies webQuery = new DiscoverMovies();
			String jsonData = webQuery.getMovieDetails(BuildConfig.THEMOVIEDB_API_KEY, movieId);

			if (verbose) Log.v(LOG_TAG, "Download of data for movie " + movieId + " result: " + jsonData.substring(0, Math.min(30, jsonData.length())));

			if (verbose) Log.v(LOG_TAG, "Updating database entry for movie " + movieId + "...");

			// strip out the parts of the JSON data we don't need to keep
			MdbJSONAdapter jsonAdapter = new MdbJSONAdapter(null);
			Movie movie = jsonAdapter.toMovie(new JSONObject(jsonData));
			jsonData = jsonAdapter.toJSONString(movie);

			ContentValues cv = new ContentValues();
			cv.put(PopularMoviesContract.MovieContract.Column.JSONDATA, jsonData);
			cv.put(PopularMoviesContract.MovieContract.Column.MODIFIED, System.currentTimeMillis());

			Uri movieItemUri = MovieContract.buildMovieItemUri(movieId);
			int rowCount = context.getContentResolver().update(movieItemUri, cv, null, null);

			if (verbose) Log.v(LOG_TAG, "Database update for movie " + movieId + " affected " + rowCount + " rows");

			if (rowCount == 0) {
				Uri insertedUri = context.getContentResolver().insert(movieItemUri, cv);

				if (verbose) Log.v(LOG_TAG, "Database insert for movie " + movieId + " returned: " + insertedUri);
			}
		}
		catch (Exception e) {
			Log.e(LOG_TAG, "Error updating extended movie data", e);
		}

		if (verbose) Log.v(LOG_TAG, "Update of data for movie " + movieId + " completed");

		return null;
	}

	@Override
	protected void onPostExecute(Void o) {
		// noop
	}
}
