package no.skavdahl.udacity.popularmovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import java.util.List;

import no.skavdahl.udacity.popularmovies.BuildConfig;
import no.skavdahl.udacity.popularmovies.mdb.DiscoverMovies;
import no.skavdahl.udacity.popularmovies.mdb.MdbJSONAdapter;
import no.skavdahl.udacity.popularmovies.model.Movie;

import static no.skavdahl.udacity.popularmovies.data.PopularMoviesContract.*;

/**
 * @author fdavs
 */
public class UpdateMovieListTask extends AsyncTask<UpdateMovieListTask.Input, Void, Void> {

	public static class Input {
		public final String listName;
		public final int page;

		public Input(String listName, int page) {
			this.listName = listName;
			this.page = page;
		}
	}

	private final String LOG_TAG = getClass().getSimpleName().substring(0, Math.min(23, getClass().getSimpleName().length()));
	private final boolean verbose = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.VERBOSE);
	private final boolean debug = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.DEBUG);

	private final Context context;

	public UpdateMovieListTask(final Context context) {
		this.context = context;
	}

	@Override
	protected Void doInBackground(Input... input) {

		final String listName = input[0].listName;
		final int page = input[0].page;

		if (verbose) Log.v(LOG_TAG, "Starting update of movie list: name=" + listName + ", page=" + page);

		// find the list's id and type
		Pair<Integer, Integer> listIdAndType = queryListAttributes(listName);
		if (listIdAndType == null)
			return null; // we can't proceed

		int listId = listIdAndType.first;
		int listType = listIdAndType.second;

		if (verbose) Log.v(LOG_TAG, "List name=" + listName + ", id=" + listId + ", listType=" + listType);

		// use the list type to decide how to update the list
		switch (listType) {
			case PopularMoviesContract.ListContract.LISTTYPE_STANDARD:
				updateStandardList(listName, listId, page);
				break;

			default:
				// no action
				// TODO implement support for public lists on themoviedb.org
		}

		return null;
	}

	/**
	 * Queries the database to find the id and type of a named movie list.
	 *
	 * @param listName The name of a list.
	 *
	 * @return A Pair of ints, where the first int is the list id and the second int
	 *         corresponds to the list type.
	 */
	protected Pair<Integer, Integer> queryListAttributes(final String listName) {
		final int listId;
		final int listType;

		Cursor listCursor = null;
		try {
			Uri listItemUri = ListContract.buildListItemUri(listName);
			listCursor = context.getContentResolver().query(
				listItemUri,
				new String[]{PopularMoviesContract.ListContract.Column._ID, PopularMoviesContract.ListContract.Column.TYPE},
				null,
				null,
				null);

			if (listCursor == null || !listCursor.moveToFirst()) {
				Log.w(LOG_TAG, "List not found in database: " + listName);
				return null; // nothing more we can do
			}

			listId = listCursor.getInt(0);
			listType = listCursor.getInt(1);

			return Pair.create(listId, listType);
		}
		finally {
			if (listCursor != null)
				listCursor.close();
		}
	}

	/**
	 * Downloads updated information for the specified standard movie list and inserts or
	 * updates the local database appropriately.
	 *
	 * @param listName The name of hte list to update
	 * @param listId The id of the list
	 * @param page The page of the list we're updating
	 */
	protected void updateStandardList(String listName, int listId, int page) {
		try {
			// download updated movie data from the web service
			if (verbose) Log.v(LOG_TAG, "Starting download of movie list: " + listName + ", page " + page);

			DiscoverMovies webQuery = new DiscoverMovies();
			String jsonResponse = webQuery.discoverStandardMovies(BuildConfig.THEMOVIEDB_API_KEY, listName, page);

			if (verbose) Log.v(LOG_TAG, "Received web query response:" + jsonResponse.substring(0, Math.min(40, jsonResponse.length())) + "...");

			// convert the data to ContentValues for insertion into database
			MdbJSONAdapter jsonAdapter = new MdbJSONAdapter(null);
			List<Movie> movieList = jsonAdapter.getMoviesList(jsonResponse);

			final long now = System.currentTimeMillis();

			ContentValues[] values = new ContentValues[movieList.size()];
			int position = 0;
			for (Movie movie : movieList) {
				ContentValues cv = new ContentValues();
				cv.put(ListMembershipContract.Column.MOVIE_ID, movie.getMovieDbId());
				cv.put(MovieContract.Column.MODIFIED, now);
				cv.put(MovieContract.Column.JSONDATA, jsonAdapter.toJSONString(movie));
				cv.put(ListMembershipContract.Column.LIST_ID, listId);
				cv.put(ListMembershipContract.Column.PAGE, page);
				cv.put(ListMembershipContract.Column.POSITION, position);

				values[position++] = cv;
			}

			// perform insertion into the local database
			Uri listMemberUri = ListContract.buildListMemberDirectoryUri(listName);
			context.getContentResolver().bulkInsert(listMemberUri, values);

			if (debug) Log.d(LOG_TAG, "Movie list " + listName + " (page " + page + ") updated");
		}
		catch (Exception e) {
			Log.e(LOG_TAG, "Web query failed for list name=" + listName, e);
		}
	}
}
