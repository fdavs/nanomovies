package no.skavdahl.udacity.popularmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import no.skavdahl.udacity.popularmovies.BuildConfig;
import no.skavdahl.udacity.utils.Arrays;

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

	static final int LIST_INDEX_LIST_NAME = 1;
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
				Log.w(LOG_TAG, "Unsupported operation: getType " + uri.getPath());
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

			case LIST_ITEM:
				cursor = queryListItem(uri, projection);
				break;

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
				Log.w(LOG_TAG, "Unsupported operation: query " + uri.getPath());
				return null;
		}

		// disable warning "getContext() may return null": it is non-null after onCreate()
		//noinspection ConstantConditions
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
				Log.w(LOG_TAG, "Unsupported operation: insert " + uri.getPath());
				return null;
		}

		// disable warning "getContext() may return null": it is non-null after onCreate()
		//noinspection ConstantConditions
		getContext().getContentResolver().notifyChange(uri, null);

		return newUri;
	}

	@Override
	public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
		int insertCount;

		switch (uriMatcher.match(uri)) {
			case LIST_MEMBER_DIRECTORY:
				insertCount = bulkInsertListMembers(uri, values);
				break;

			default:
				Log.w(LOG_TAG, "Unsupported operation: bulkInsert " + uri.getPath());
				return 0;
		}

		if (insertCount > 0) {
			// disable warning "getContext() may return null": it is non-null after onCreate()
			//noinspection ConstantConditions
			getContext().getContentResolver().notifyChange(uri, null);
		}

		return insertCount;
	}

	@Override
	public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
		int deleteCount = 0;

		switch (uriMatcher.match(uri)) {
			case MOVIE_DIRECTORY:
				if (uri.getBooleanQueryParameter("orphan", false)) {
					deleteCount = deleteOrphanedMovies(uri);
					break;
				}
				// else fall through

			default:
				Log.w(LOG_TAG, "Unsupported operation: delete " + uri.getPath());
				break;
		}

		if (deleteCount > 0) {
			// disable warning "getContext() may return null": it is non-null after onCreate()
			//noinspection ConstantConditions
			getContext().getContentResolver().notifyChange(uri, null);
		}

		return deleteCount;
	}

	@Override
	public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int rowCount;

		switch (uriMatcher.match(uri)) {
			case MOVIE_ITEM:
				rowCount = updateMovieItem(uri, values);
				break;

			default:
				Log.w(LOG_TAG, "Unsupported operation: update " + uri.getPath());
				rowCount = 0;
				break;
		}

		if (rowCount > 0) {
			// disable warning "getContext() may return null": it is non-null after onCreate()
			//noinspection ConstantConditions
			getContext().getContentResolver().notifyChange(uri, null);
		}

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

	// --- List directory operations ---


	// --- List item operations ---

	protected Cursor queryListItem(Uri uri, String[] projection) {
		final boolean verbose = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.VERBOSE);
		final boolean debug = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.DEBUG);

		if (verbose) Log.v(LOG_TAG, "Start query: " + uri.getPath());

		String listName =  uri.getPathSegments().get(LIST_INDEX_LIST_NAME);

		Cursor cursor = dbHelper.getReadableDatabase().query(
			PopularMoviesContract.ListContract.TABLE_NAME,
			projection,
			PopularMoviesContract.ListContract.Column.NAME + " = ?",
			new String[]{listName},
			null,
			null,
			null);

		if (debug) Log.d(LOG_TAG, "QUERY " + uri.getPath() + " -> " + cursor.getCount() + " rows returned");

		return cursor;
	}

	// --- List member directory operations ---

	protected Cursor queryListMemberDirectory(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		final boolean verbose = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.VERBOSE);
		final boolean debug = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.DEBUG);

		if (verbose) Log.v(LOG_TAG, "Start query: " + uri.getPath());

		String listName =  uri.getPathSegments().get(LIST_INDEX_LIST_NAME);

		String sql =
			"SELECT M." + TextUtils.join(", M.", projection) + " " +
			"FROM " + MovieContract.TABLE_NAME + " M, " + ListContract.TABLE_NAME + " L, " + ListMembershipContract.TABLE_NAME + " " +
			"WHERE L." + ListContract.Column.NAME + " = ? " +
				"AND L." + ListContract.Column._ID + " = " + ListMembershipContract.Column.LIST_ID + " " +
				"AND " + ListMembershipContract.Column.MOVIE_ID + " = M." + MovieContract.Column._ID +
				(TextUtils.isEmpty(selection) ? "" : " AND " + selection) + " " + // NOTE asserting that selection only includes (at most) pos and page columns
			"ORDER BY " + (TextUtils.isEmpty(sortOrder) ? ListMembershipContract.Column.POSITION : sortOrder);

		String[] effectiveSelArgs = Arrays.prepend(listName, selectionArgs);
		Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sql, effectiveSelArgs);

		if (debug) Log.d(LOG_TAG, "QUERY " + uri.getPath() + " -> " + cursor.getCount() + " rows returned");

		return cursor;
	}

	protected int bulkInsertListMembers(Uri uri, ContentValues[] values) {
		final boolean verbose = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.VERBOSE);
		final boolean debug = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.DEBUG);

		if (verbose) Log.v(LOG_TAG, "Start bulk insert: " + uri.getPath() + " with " + values.length + " values");

		// it is possible to use db.insertWithOnConflict(movie) and db.insert(member) here
		// but it is more clear, more efficient and no more verbose (I tried) to just
		// use SQL directly

		String movieSql =
			"INSERT OR REPLACE INTO " + MovieContract.TABLE_NAME + "(" +
				TextUtils.join(",", new String[] {
					MovieContract.Column._ID,
					MovieContract.Column.MODIFIED,
					MovieContract.Column.JSONDATA
				}) + ") " +
			"VALUES(?, ?, ?)";

		String listSql =
			"INSERT OR REPLACE INTO " + ListMembershipContract.TABLE_NAME + "(" +
				TextUtils.join(",", new String[] {
					ListMembershipContract.Column._ID,
					ListMembershipContract.Column.LIST_ID,
					ListMembershipContract.Column.MOVIE_ID,
					ListMembershipContract.Column.PAGE,
					ListMembershipContract.Column.POSITION,
					ListMembershipContract.Column.ADDED
				}) + ") " +
			"VALUES(?, ?, ?, ?, ?, ?)";

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		SQLiteStatement movieStmt  = db.compileStatement(movieSql);
		SQLiteStatement listStmt  = db.compileStatement(listSql);

		int rowsInserted = 0;

		db.beginTransaction();
		try {
			for (ContentValues cv : values) {
				try {
					long movieId = cv.getAsInteger(ListMembershipContract.Column.MOVIE_ID);
					long modified = cv.getAsLong(MovieContract.Column.MODIFIED);
					String movieJson = cv.getAsString(MovieContract.Column.JSONDATA);
					int listId = cv.getAsInteger(ListMembershipContract.Column.LIST_ID);
					int page = cv.getAsInteger(ListMembershipContract.Column.PAGE);
					int position = cv.getAsInteger(ListMembershipContract.Column.POSITION);

					movieStmt.bindLong(1, movieId);
					movieStmt.bindLong(2, modified);
					movieStmt.bindString(3, movieJson);

					movieStmt.executeInsert();

					listStmt.bindLong(1, getListMemberId(listId, page, position));
					listStmt.bindLong(2, listId);
					listStmt.bindLong(3, movieId);
					listStmt.bindLong(4, page);
					listStmt.bindLong(5, position);
					listStmt.bindLong(6, modified);

					listStmt.executeInsert();

					rowsInserted++;
				}
				catch (Exception e) {
					Log.w(LOG_TAG, "Error during movie insert: " + cv, e);
				}
			}
			db.setTransactionSuccessful();
		}
		finally {
			db.endTransaction();
		}

		if (debug) Log.d(LOG_TAG, "INSERT " + uri.getPath() + " -> " + rowsInserted + " rows inserted");

		return rowsInserted;
	}

	/**
	 * Generates a unique id for a list member row.
	 *
	 * @param listId The id of the list of which the movie is a member
	 * @param page The page the movie belongs to
	 * @param position The position within the page
	 *
	 * @return a unique id number.
	 */
	private long getListMemberId(int listId, int page, int position) {
		// two last digits for position within the page
		// four digits for the page (1 - 1000)
		// additional digits in front for the list
		return listId * 1000000 + page * 100 + position;
	}

	// --- Movie directory operations ---

	protected int deleteOrphanedMovies(Uri uri) {
		final boolean verbose = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.VERBOSE);
		final boolean debug = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.DEBUG);

		if (verbose) Log.v(LOG_TAG, "Start delete: " + uri.getPath() + "?" + uri.getQuery());

		/*dbHelper.getWritableDatabase().execSQL(
			"DELETE FROM " + MovieContract.TABLE_NAME + " " +
			"WHERE " + MovieContract.Column._ID + " NOT IN (" +
				"SELECT " + ListMembershipContract.Column.MOVIE_ID + " " +
				"FROM " + ListMembershipContract.TABLE_NAME +
			")");
		int deletedCount = 1;*/

		/* The code below causes "SQLiteException: no such column: movie._id (code 1)"
		   though I can't find anything wrong with the SQL statement itself:
		   DELETE FROM movie WHERE _id NOT IN (SELECT movieid FROM listmember)*/

		int deletedCount = dbHelper.getWritableDatabase().delete(
			MovieContract.TABLE_NAME,
			MovieContract.Column._ID + " NOT IN (" +
				"SELECT " + ListMembershipContract.Column.MOVIE_ID + " " +
				"FROM " + ListMembershipContract.TABLE_NAME +
			")",
			null);

		if (debug) Log.d(LOG_TAG,
			"DELETE " + uri.getPath() + "?" + uri.getQuery() + " -> " +
			deletedCount + " rows deleted");

		return deletedCount;
	}

	// --- Movie item operations ---

	protected Cursor queryMovieItem(Uri uri, String[] projection) {
		final boolean verbose = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.VERBOSE);
		final boolean debug = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.DEBUG);

		if (verbose) Log.v(LOG_TAG, "Start query: " + uri.getPath());

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

		if (debug) Log.d(LOG_TAG, "QUERY " + uri.getPath() + " -> " + cursor.getCount() + " rows returned");

		return cursor;
	}

	protected Uri insertMovieItem(final Uri uri, final ContentValues values) {
		final boolean verbose = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.VERBOSE);
		final boolean debug = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.DEBUG);

		if (verbose) Log.v(LOG_TAG, "Start insert: " + uri.getPath());

		final int movieId = getPathSegmentAsInt(uri, MOVIE_INDEX_MOVIE_ID, 0);
		if (movieId == 0)
			return null;

		values.put(PopularMoviesContract.MovieContract.Column._ID, movieId);

		try {
			long newMovieId = dbHelper.getWritableDatabase().insertOrThrow(
				PopularMoviesContract.MovieContract.TABLE_NAME,
				null,
				values);

			if (debug) Log.d(LOG_TAG, "INSERT " + uri.getPath() + " -> 1 row inserted, id = " + newMovieId);

			return MovieContract.buildMovieItemUri(newMovieId);
		}
		catch (SQLException e) {
			Log.e(LOG_TAG, "INSERT " + uri.getPath() + " failed: values = " + values, e);
			return null;
		}
	}

	protected int updateMovieItem(final Uri uri, final ContentValues values) {
		final boolean verbose = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.VERBOSE);
		final boolean debug = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.DEBUG);

		if (verbose) Log.v(LOG_TAG, "Start update: " + uri.getPath());

		final int movieId = getPathSegmentAsInt(uri, MOVIE_INDEX_MOVIE_ID, 0);
		if (movieId == 0)
			return 0;

		int rowCount = dbHelper.getWritableDatabase().update(
			MovieContract.TABLE_NAME,
			values,
			MovieContract.Column._ID + "=?",
			new String[]{Integer.toString(movieId)});

		if (debug) Log.d(LOG_TAG, "UPDATE " + uri.getPath() + " -> " + rowCount + " rows updated");

		return rowCount;
	}
}
