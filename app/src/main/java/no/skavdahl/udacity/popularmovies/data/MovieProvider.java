package no.skavdahl.udacity.popularmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONObject;

import java.util.List;

import no.skavdahl.udacity.popularmovies.BuildConfig;
import no.skavdahl.udacity.popularmovies.mdb.DiscoverMovies;
import no.skavdahl.udacity.popularmovies.mdb.MdbJSONAdapter;
import no.skavdahl.udacity.popularmovies.model.Movie;

import static no.skavdahl.udacity.utils.Arrays.*;
import static no.skavdahl.udacity.popularmovies.data.PopularMoviesContract.*;

/**
 * Content provider for movie data.
 *
 * @author fdavs
 */
public class MovieProvider extends ContentProvider {

	private static final String LOG_TAG = MovieProvider.class.getSimpleName();

	static final int LIST_DIRECTORY = 1;
	static final int LIST_ITEM = 2;
	static final int LIST_MEMBER_DIRECTORY = 3;
	static final int LIST_MEMBER_ITEM = 4;
	static final int MOVIE_DIRECTORY = 5;
	static final int MOVIE_ITEM = 6;
	static final int IMAGE_DIRECTORY = 7;
	static final int IMAGE_ITEM = 8;

	static final int LIST_INDEX_LISTNAME = 1;
	static final int LIST_INDEX_MOVIE_ID = 3;
	static final int MOVIE_INDEX_MOVIE_ID = 1;
	static final int IMAGE_INDEX_IMAGE_ID = 1;

	private final static UriMatcher uriMatcher = buildUriMatcher();

	private SQLiteOpenHelper dbHelper;

	static UriMatcher buildUriMatcher() {
		UriMatcher m = new UriMatcher(UriMatcher.NO_MATCH);

		// AUTHORITY/list                       -- query all lists
		// AUTHORITY/list/name*                 -- query one specific list (type etc)

		m.addURI(CONTENT_AUTHORITY, ListContract.CONTENT_URI_PATH, LIST_DIRECTORY);
		m.addURI(CONTENT_AUTHORITY, ListContract.CONTENT_URI_PATH + "/*", LIST_ITEM);

		// AUTHORITY/list/name*/movie           -- query list contents
		// AUTHORITY/list/name*/movie/id#       -- query one specific list member

		m.addURI(CONTENT_AUTHORITY, ListContract.CONTENT_URI_PATH + "/*/" + ListContract.CONTENT_URI_PATH_MEMBER, LIST_MEMBER_DIRECTORY);
		m.addURI(CONTENT_AUTHORITY, ListContract.CONTENT_URI_PATH + "/*/" + ListContract.CONTENT_URI_PATH_MEMBER + "/#", LIST_MEMBER_ITEM);

		// AUTHORITY/movie                      -- query movie directory (regardless of list association)
		// AUTHORITY/movie/id#                  -- query movie data

		m.addURI(CONTENT_AUTHORITY, MovieContract.CONTENT_URI_PATH, MOVIE_DIRECTORY);
		m.addURI(CONTENT_AUTHORITY, MovieContract.CONTENT_URI_PATH + "/#", MOVIE_ITEM);

		// AUTHORITY/image                      -- query image directory
		// AUTHORITY/image/id#                  -- query image data

		m.addURI(CONTENT_AUTHORITY, ImageContract.CONTENT_URI_PATH , IMAGE_DIRECTORY);
		m.addURI(CONTENT_AUTHORITY, ImageContract.CONTENT_URI_PATH + "/#", IMAGE_ITEM);

		return m;
	}

	private static int getPathSegmentAsInt(Uri uri, int index, int defaultValue) {
		try {
			return Integer.parseInt(uri.getPathSegments().get(index));
		}
		catch (Exception e) {
			Log.e(LOG_TAG, "Unable to read value as int, index=" + index + ", URI=" + uri);
			return defaultValue;
		}
	}

	@Override
	public boolean onCreate() {
		dbHelper = new MovieDbHelper(getContext());
		return true;
	}

	@Nullable
	@Override
	public String getType(@NonNull Uri uri) {
		switch (uriMatcher.match(uri)) {
			case LIST_DIRECTORY:
				return ListContract.CONTENT_LIST_DIR_TYPE;
			case LIST_ITEM:
				return ListContract.CONTENT_LIST_ITEM_TYPE;
			case LIST_MEMBER_DIRECTORY:
				return ListContract.CONTENT_MEMBER_DIR_TYPE;
			case LIST_MEMBER_ITEM:
				return ListContract.CONTENT_MEMBER_ITEM_TYPE;
			case MOVIE_DIRECTORY:
				return MovieContract.CONTENT_DIR_TYPE;
			case MOVIE_ITEM:
				return MovieContract.CONTENT_ITEM_TYPE;
			case IMAGE_DIRECTORY:
				return ImageContract.CONTENT_DIR_TYPE;
			case IMAGE_ITEM:
				return ImageContract.CONTENT_ITEM_TYPE;
			default:
				Log.w(LOG_TAG, "Unsupported operation: " + uri);
				return null;
		}
	}

	@Nullable
	@Override
	public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		final Cursor cursor;
		switch (uriMatcher.match(uri)) {
			/*case MOVIE_DIRECTORY:
				cursor = dbHelper.getReadableDatabase().query(
					ListMembershipContract.TABLE_NAME,
					projection,
					ListMembershipContract.Column.LIST_ID,
					selectionArgs,
					null, // groupBy
					null, // having
					sortOrder);
				break;*/

			case LIST_MEMBER_DIRECTORY:
				cursor = queryListMemberDirectory(uri, projection, selection, selectionArgs, sortOrder);
				break;

			case MOVIE_ITEM:
				cursor = queryMovieItem(uri, projection);
				break;

			case IMAGE_ITEM:
				cursor = dbHelper.getReadableDatabase().query(
					ImageContract.TABLE_NAME,
					projection,
					selection,
					selectionArgs,
					null, // groupBy
					null, // having
					sortOrder);
				break;

			default:
				Log.w(LOG_TAG, "Unsupported operation: " + uri);
				return null;
		}

		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Nullable
	@Override
	public Uri insert(@NonNull Uri uri, ContentValues values) {
		Uri newUri;

		switch (uriMatcher.match(uri)) {
			case MOVIE_ITEM:
				newUri = insertMovieItem(uri, values);
				break;

			default:
				Log.w(LOG_TAG, "Unsupported operation: " + uri);
				return null;
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return newUri;
	}

	@Override
	public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int rowCount;

		switch (uriMatcher.match(uri)) {
			case MOVIE_ITEM:
				rowCount = updateMovieItem(uri, values);
				break;

			default:
				Log.w(LOG_TAG, "Unsupported operation: " + uri);
				rowCount = 0;
				break;
		}

		if (rowCount > 0)
			getContext().getContentResolver().notifyChange(uri, null);

		return rowCount;
	}

	// For unit testing support. See:
	// http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
	@Override
	@TargetApi(11)
	public void shutdown() {
		dbHelper.close();
		super.shutdown();
	}

	// --- List member directory operations ---

	protected Cursor queryListMemberDirectory(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		final boolean loggable = Log.isLoggable(LOG_TAG, Log.VERBOSE);

		final ListDbQueries listQuery = new ListDbQueries(dbHelper);
		final String listName =  uri.getPathSegments().get(LIST_INDEX_LISTNAME);

		// Query the local database first
		if (loggable) Log.v(LOG_TAG, "Querying list member directory from database: " + uri);

		Cursor memberCursor = listQuery.queryListMemberDirectory(listName, projection, selection, selectionArgs, sortOrder);
		if (memberCursor.moveToFirst()) {
			if (loggable) Log.v(LOG_TAG, "Database query returned results: " + memberCursor.getCount());
			return memberCursor;
		}

		// database did not contain the data we need or it is obsolete
		// we may need to do an online query

		// TODO issue web request asynchronously

		if (loggable) Log.v(LOG_TAG, "Database query returned 0 results");

		int page = 1; // TODO read 'page' value from selection/selectionArgs

		Cursor listCursor = dbHelper.getReadableDatabase().query(
			ListContract.TABLE_NAME,
			new String[] { ListContract.Column._ID, ListContract.Column.TYPE },
			ListContract.Column.NAME + " = ?",
			new String[] { listName },
			null,
			null,
			null);

		if (!listCursor.moveToFirst()) {
			Log.w(LOG_TAG, "List not found in database: " + listName);
			return memberCursor; // nothing more we can do
		}

		int listId = listCursor.getInt(listCursor.getColumnIndex(ListContract.Column._ID));
		int listType = listCursor.getInt(listCursor.getColumnIndex(ListContract.Column.TYPE));

		listCursor.close();

		if (loggable) Log.v(LOG_TAG, "List name=" + listName + ", id=" + listId + ", listType=" + listType);

		switch (listType) {
			case ListContract.LISTTYPE_STANDARD:
				try {
					if (loggable) Log.v(LOG_TAG, "Issuing web query for list " + listName);

					DiscoverMovies webQuery = new DiscoverMovies();
					String jsonResponse = webQuery.discoverStandardMovies(BuildConfig.THEMOVIEDB_API_KEY, listName);

					if (loggable) Log.v(LOG_TAG, "Web query returned a response:" + jsonResponse.substring(0, Math.min(30, jsonResponse.length())));

					MdbJSONAdapter jsonAdapter = new MdbJSONAdapter(getContext().getResources());
					List<Movie> movieList = jsonAdapter.getMoviesList(jsonResponse);

					listQuery.bulkInsert(listId, page, movieList);

					if (loggable) Log.v(LOG_TAG, "Movie data successfully inserted into database");
				}
				catch (Exception e) {
					Log.e(LOG_TAG, "Web query failed for list name=" + listName, e);
				}

			default:
				// no action
				// TODO implement support for public lists on themoviedb.org
		}

		// reissue the database query to get an updated cursor
		if (loggable) Log.v(LOG_TAG, "Re-issuing list member directory query for list " + listName);
		memberCursor.close();
		memberCursor = listQuery.queryListMemberDirectory(listName, projection, selection, selectionArgs, sortOrder);

		return memberCursor;
	}

	// --- Movie item operations ---

	protected Cursor queryMovieItem(Uri uri, String[] projection) {
		final boolean verbose = Log.isLoggable(LOG_TAG, Log.VERBOSE);

		if (verbose) Log.v(LOG_TAG, "Querying movie item: " + uri);

		// it is not strictly necessary to parse the path segment into an integer
		// but doing so is more secure since we'll know it is actually an integer
		final int movieId = getPathSegmentAsInt(uri, MOVIE_INDEX_MOVIE_ID, 0);
		if (movieId == 0)
			return null;

		// ensure that the projection includes the modified field
		// we need it to verify the age of the data
		if (!arrayContains(projection, MovieContract.Column.MODIFIED))
			projection = append(projection, MovieContract.Column.MODIFIED);

		// Query the local database first
		Cursor cursor = dbHelper.getReadableDatabase().query(
			MovieContract.TABLE_EX_NAME,
			projection,
			MovieContract.Column._ID + "=?",
			new String[]{Integer.toString(movieId)},
			null,
			null,
			null);

		if (verbose) Log.v(LOG_TAG, "Database query for movie " + movieId + " returned " + cursor.getCount() + " row(s)");

		return cursor;
	}

	protected Uri insertMovieItem(final Uri uri, final ContentValues values) {
		final boolean verbose = Log.isLoggable(LOG_TAG, Log.VERBOSE);

		if (verbose) Log.v(LOG_TAG, "Inserting movie item: " + uri);

		final int movieId = getPathSegmentAsInt(uri, MOVIE_INDEX_MOVIE_ID, 0);
		if (movieId == 0)
			return null;

		values.put(PopularMoviesContract.MovieContract.Column._ID, movieId);

		long newMovieId = dbHelper.getWritableDatabase().insert(
			PopularMoviesContract.MovieContract.TABLE_NAME,
			null,
			values);

		if (newMovieId == -1)
			Log.e(LOG_TAG, "Insert of movie data for movie " + movieId + " failed");
		else if (verbose)
			Log.v(LOG_TAG, "Insert statement for movie " + movieId + " completed successfully (id = " + newMovieId + ")");

		return MovieContract.buildMovieItemUri(newMovieId);
	}

	protected int updateMovieItem(final Uri uri, final ContentValues values) {
		final boolean verbose = Log.isLoggable(LOG_TAG, Log.VERBOSE);

		if (verbose) Log.v(LOG_TAG, "Updating movie item: " + uri);

		final int movieId = getPathSegmentAsInt(uri, MOVIE_INDEX_MOVIE_ID, 0);
		if (movieId == 0)
			return 0;

		int rowCount = dbHelper.getWritableDatabase().update(
			MovieContract.TABLE_NAME,
			values,
			MovieContract.Column._ID + "=?",
			new String[]{Integer.toString(movieId)});

		if (verbose) Log.v(LOG_TAG, "Update of movie " + movieId + " affected " + rowCount + " rows");

		return rowCount;
	}
}
