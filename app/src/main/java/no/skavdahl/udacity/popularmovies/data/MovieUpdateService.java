package no.skavdahl.udacity.popularmovies.data;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
public class MovieUpdateService extends IntentService {

	private final String LOG_TAG = getClass().getSimpleName();

	private static final String SERVICE_NAME = "MDBMovieDownload";

	private static final String EXTRA_MOVIEID = "movieId";

	/**
	 * Create an intent to download extended movie data for a movie.
	 *
	 * @param packageContext A context of the application package implementing this intent
	 * @param movieId Identifies the movie to download details for
	 */
	public static Intent createExplicitIntent(Context packageContext, int movieId) {
		Intent intent = new Intent(packageContext, MovieUpdateService.class);
		intent.putExtra(EXTRA_MOVIEID, movieId);
		return intent;
	}

	public MovieUpdateService() {
		super(SERVICE_NAME);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		final boolean verbose = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.VERBOSE);
		final boolean debug = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.DEBUG);

		final int movieId = intent.getIntExtra(EXTRA_MOVIEID, -1);
		if (movieId == -1) {
			Log.e(LOG_TAG, "Starting intent missing required movieId");
			return;
		}

		if (verbose) Log.v(LOG_TAG, "Starting update of data for movie " + movieId + "...");

		try {
			if (verbose) Log.v(LOG_TAG, "Starting download of movie data from server...");

			DiscoverMovies webQuery = new DiscoverMovies();
			String jsonData = webQuery.getMovieDetails(BuildConfig.THEMOVIEDB_API_KEY, movieId);

			if (verbose) Log.v(LOG_TAG, "Download of data for movie " + movieId + " completed, result: " + jsonData.substring(0, Math.min(40, jsonData.length())));

			if (verbose) Log.v(LOG_TAG, "Updating database entry for movie " + movieId + "...");

			Movie movie = MdbJSONAdapter.toMovie(new JSONObject(jsonData));

			ContentValues cv = new ContentValues();
			cv.put(MovieContract.Column.MODIFIED, System.currentTimeMillis());
			cv.put(MovieContract.Column.VOTE_COUNT, movie.getVoteCount());
			cv.put(MovieContract.Column.VOTE_AVERAGE, movie.getVoteAverage());
			cv.put(MovieContract.Column.POPULARITY, movie.getPopularity());
			cv.put(MovieContract.Column.EXTENDED_DATA, 1);
			cv.put(MovieContract.Column.REVIEWS_JSON, MdbJSONAdapter.toReviewJSONString(movie.getReviews()));
			cv.put(MovieContract.Column.VIDEOS_JSON, MdbJSONAdapter.toVideoJSONString(movie.getVideos()));

			Uri movieItemUri = MovieContract.buildMovieItemUri(movieId);
			int rowCount = getContentResolver().update(movieItemUri, cv, null, null);

			if (verbose) Log.v(LOG_TAG, "Database update for movie " + movieId + " complete, " + rowCount + " rows updated");

			if (rowCount == 0) {
				Uri insertedUri = getContentResolver().insert(movieItemUri, cv);

				if (verbose) Log.v(LOG_TAG, "Database insert for movie " + movieId + " complete, result: " + insertedUri);
			}

			if (debug) Log.v(LOG_TAG, "Download of movie data for movie " + movieId + " completed");
		}
		catch (Exception e) {
			Log.e(LOG_TAG, "Error downloading extended movie data for movie " + movieId, e);
		}
	}
}
