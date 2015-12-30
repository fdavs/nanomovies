package no.skavdahl.udacity.popularmovies.data;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import no.skavdahl.udacity.popularmovies.mdb.StandardMovieList;

import static no.skavdahl.udacity.popularmovies.data.PopularMoviesContract.*;

/**
 * Toggles whether a movie is a favorite or not.
 *
 * A movie becomes a favorite by being added to the favorite movies list.
 *
 * @author fdavs
 */
public class ToggleFavoriteTask extends AsyncTask<Void, Void, Void> {

	private final int movieId;
	private final boolean makeFavorite;
	private final Context context;

	/**
	 * Create a new task.
	 *
	 * @param movieId The movie to modify.
	 * @param makeFavorite If true, add the movie to the list of favorite movies.
	 *                     If false, remove it from the list.
	 */
	public ToggleFavoriteTask(final Context context, final int movieId, final boolean makeFavorite) {
		this.context = context;
		this.movieId = movieId;
		this.makeFavorite = makeFavorite;
	}

	@Override
	protected Void doInBackground(Void... params) {
		Uri uri = ListContract.buildListMemberItemUri(StandardMovieList.FAVORITE, movieId);

		if (makeFavorite)
			context.getContentResolver().insert(uri, null);
		else
			context.getContentResolver().delete(uri, null, null);

		return null;
	}
}
