package no.skavdahl.udacity.popularmovies.data;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

/**
 * Searches the database for unreferenced movie data and deletes it.
 *
 * Movie data can become orphaned when a movie drops out of a list. For example, a movie
 * that is present on the popular movies list, will at some point be pushed out by other
 * movies. When that happens, the movie data will no longer be referenced by the movie
 * list and the row is unreachable.
 *
 * @author fdavs
 */
public class SweepDatabaseTask extends AsyncTask<Void, Void, Void> {

	private final Context context;

	public SweepDatabaseTask(Context context) {
		this.context = context;
	}

	@Override
	protected Void doInBackground(Void... params) {
		Uri uri = PopularMoviesContract.MovieContract.buildMovieDirectoryUri();
		uri = uri.buildUpon()
			.appendQueryParameter("orphan", "true")
			.build();

		context.getContentResolver().delete(uri, null, null);

		return null;
	}
}
