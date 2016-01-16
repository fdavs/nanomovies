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
// Disable "try can use automatic resource management" tip from Android Studio
// This feature requires API level 19, which is higher than our current minimum API level
@SuppressWarnings("TryFinallyCanBeTryWithResources")
public class MovieProvider extends ContentProvider {

	private static final String LOG_TAG = MovieProvider.class.getSimpleName();

	static final int LIST_DIRECTORY = 1;
	static final int LIST_ITEM = 2;
	static final int LIST_MEMBER_DIRECTORY = 3;
	static final int LIST_MEMBER_ITEM = 4;
	static final int MOVIE_DIRECTORY = 5;
	static final int MOVIE_ITEM = 6;

	static final int LIST_INDEX_LIST_NAME = 1;
	static final int LIST_INDEX_MOVIE_ID = 3;
	static final int MOVIE_INDEX_MOVIE_ID = 1;

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
			case LIST_ITEM:
				cursor = queryListItem(uri, projection);
				break;

			case LIST_MEMBER_DIRECTORY:
				cursor = queryListMemberDirectory(uri, projection, selection, selectionArgs, sortOrder);
				break;

			case MOVIE_ITEM:
				cursor = queryMovieItem(uri, projection);
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

			case LIST_MEMBER_ITEM:
				newUri = addMemberToList(uri);
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
			case LIST_MEMBER_ITEM:
				deleteCount = removeMemberFromList(uri);
				break;

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

		// SELECT <projection>
		// FROM movie M, list L, listmember
		// WHERE L.name = ? AND L._id = listid AND movieid = M._id AND <selection>
		// ORDER BY <sortorder>
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

		// disable warning "getContext() may return null": it is non-null after onCreate()
		//noinspection ConstantConditions
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	protected int bulkInsertListMembers(Uri uri, ContentValues[] values) {
		final boolean verbose = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.VERBOSE);
		final boolean debug = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.DEBUG);

		if (verbose) Log.v(LOG_TAG, "Start bulk insert: " + uri.getPath() + " with " + values.length + " values");

		// it is possible to use db.insertWithOnConflict(movie) and db.insert(member) here
		// but it is more clear, more efficient and no more verbose (I tried) to just
		// use SQL directly

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		SQLiteStatement movieStmt  = db.compileStatement(INSERT_OR_REPLACE_INTO_MOVIE);
		SQLiteStatement listStmt  = db.compileStatement(INSERT_OR_REPLACE_INTO_LISTMEMBER_SQL);

		int rowsInserted = 0;
		try {
			db.beginTransaction();
			try {
				for (ContentValues cv : values) {
					try {
						long movieId = cv.getAsInteger(ListMembershipContract.Column.MOVIE_ID);
						long modified = cv.getAsLong(MovieContract.Column.MODIFIED);
						String title = cv.getAsString(MovieContract.Column.TITLE);
						String posterPath = cv.getAsString(MovieContract.Column.POSTER_PATH);
						String backdropPath = cv.getAsString(MovieContract.Column.BACKDROP_PATH);
						String synopsis = cv.getAsString(MovieContract.Column.SYNOPSIS);
						double popularity = cv.getAsDouble(MovieContract.Column.POPULARITY);
						double voteAvg = cv.getAsDouble(MovieContract.Column.VOTE_AVERAGE);
						long voteCount = cv.getAsLong(MovieContract.Column.VOTE_COUNT);
						long releaseDate = cv.getAsLong(MovieContract.Column.RELEASE_DATE);
						int listId = cv.getAsInteger(ListMembershipContract.Column.LIST_ID);
						int page = cv.getAsInteger(ListMembershipContract.Column.PAGE);
						int position = cv.getAsInteger(ListMembershipContract.Column.POSITION);

						movieStmt.bindLong(1, movieId);
						movieStmt.bindLong(2, modified);
						movieStmt.bindString(3, title);

						if (posterPath != null)
							movieStmt.bindString(4, posterPath);
						else
							movieStmt.bindNull(4);

						if (backdropPath != null)
							movieStmt.bindString(5, backdropPath);
						else
							movieStmt.bindNull(5);

						if (synopsis != null)
							movieStmt.bindString(6, synopsis);
						else
							movieStmt.bindNull(6);

						movieStmt.bindDouble(7, popularity);
						movieStmt.bindDouble(8, voteAvg);
						movieStmt.bindLong(9, voteCount);

						if (releaseDate > 0)
							movieStmt.bindLong(10, releaseDate);
						else
							movieStmt.bindNull(10);

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
		}
		finally {
			movieStmt.close();
			listStmt.close();
		}

		if (debug) Log.d(LOG_TAG, "INSERT " + uri.getPath() + " -> " + rowsInserted + " rows inserted");

		if (rowsInserted > 0) {
			// disable warning "getContext() may return null": it is non-null after onCreate()
			//noinspection ConstantConditions
			getContext().getContentResolver().notifyChange(uri, null);
		}

		return rowsInserted;
	}

	// --- List member item operations ---

	protected Uri addMemberToList(Uri uri) {
		final boolean verbose = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.VERBOSE);
		final boolean debug = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.DEBUG);

		if (verbose) Log.v(LOG_TAG, "Start insert: " + uri.getPath());

		String listName = uri.getPathSegments().get(LIST_INDEX_LIST_NAME);
		int movieId = getPathSegmentAsInt(uri, LIST_INDEX_MOVIE_ID, 0);
		if (movieId == 0) {
			Log.e(LOG_TAG, "INSERT " + uri.getPath() + ": invalid or missing movie id");
			return null;
		}

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.beginTransaction();

		try {
			// find the list id, page and position of the new list member
			final int listId;
			final int page;
			final int position;

			Cursor listCursor = null;
			try {
				listCursor = db.rawQuery(
					SELECT_POSITION_FOR_NEW_LISTMEMBER,
					new String[]{listName});

				if (listCursor.moveToFirst()) {
					listId = listCursor.getInt(0);
					page = listCursor.getInt(1);
					position = listCursor.getInt(2);
				}
				else {
					Log.e(LOG_TAG, "INSERT " + uri.getPath() + ": invalid list name");
					return null;
				}
			}
			finally {
				if (listCursor != null) listCursor.close();
			}

			// insert the new movie in the next available position
			int insertPosition = position + 1;
			long listMemberId = getListMemberId(listId, page, insertPosition);

			if (verbose) Log.v(LOG_TAG,
				"Position for new list member: " +
				"listId=" + listId + ", " +
				"page=" + page + ", " +
				"position=" + insertPosition);

			long newId;
			SQLiteStatement stmt = db.compileStatement(INSERT_OR_REPLACE_INTO_LISTMEMBER_SQL);
			try {
				stmt.bindLong(1, listMemberId);
				stmt.bindLong(2, listId);
				stmt.bindLong(3, movieId);
				stmt.bindLong(4, page);
				stmt.bindLong(5, insertPosition);
				stmt.bindLong(6, System.currentTimeMillis());

				newId = stmt.executeInsert();
			}
			finally {
				stmt.close();
			}

			if (newId == -1) {
				Log.e(LOG_TAG, "INSERT " + uri.getPath() + " failed (reason unknown)");
				return null;
			}
			else {
				db.setTransactionSuccessful();
				if (debug) Log.d(LOG_TAG, "INSERT " + uri.getPath() + " -> 1 row inserted, id = " + newId);
			}
		}
		finally {
			db.endTransaction();
		}

		return uri;
	}

	protected int removeMemberFromList(Uri uri) {
		final boolean verbose = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.VERBOSE);
		final boolean debug = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.DEBUG);

		if (verbose) Log.v(LOG_TAG, "Start delete: " + uri.getPath());

		String listName = uri.getPathSegments().get(LIST_INDEX_LIST_NAME);

		int movieId = getPathSegmentAsInt(uri, LIST_INDEX_MOVIE_ID, 0);
		if (movieId == 0) {
			Log.e(LOG_TAG, "DELETE " + uri.getPath() + ": invalid or missing movie id");
			return 0;
		}

		SQLiteStatement stmt = dbHelper
			.getWritableDatabase()
			.compileStatement(DELETE_FROM_LISTMEMBER);

		int deletedCount;
		try {
			stmt.bindString(1, listName);
			stmt.bindLong(2, movieId);

			deletedCount = stmt.executeUpdateDelete();
		}
		finally {
			stmt.close();
		}

		if (debug) Log.d(LOG_TAG, "DELETE " + uri.getPath() + " -> " + deletedCount + " rows deleted");

		return deletedCount;
	}

	/**
	 * <pre>
	 * INSERT OR REPLACE INTO MOVIE (_id, modified, title, posterPath, backdropPath,
	 *    synopsis, popularity, voteAvg, voteCount, releaseDate)
	 * VALUE (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
	 * </pre>
	 */
	private static final String INSERT_OR_REPLACE_INTO_MOVIE =
		"INSERT OR REPLACE INTO " + MovieContract.TABLE_NAME + "(" +
			TextUtils.join(",", new String[] {
				MovieContract.Column._ID,
				MovieContract.Column.MODIFIED,
				MovieContract.Column.TITLE,
				MovieContract.Column.POSTER_PATH,
				MovieContract.Column.BACKDROP_PATH,
				MovieContract.Column.SYNOPSIS,
				MovieContract.Column.POPULARITY,
				MovieContract.Column.VOTE_AVERAGE,
				MovieContract.Column.VOTE_COUNT,
				MovieContract.Column.RELEASE_DATE,
			}) + ") " +
		"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	/**
	 * <pre>
	 * INSERT OR REPLACE INTO listmember (_id, listid, movieid, page, position, added)
	 * VALUES (?, ?, ?, ?, ?, ?)
	 * </pre>
	 */
	private static final String INSERT_OR_REPLACE_INTO_LISTMEMBER_SQL =
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

	/**
	 * <pre>
	 * SELECT L._id, COALESCE(page, 0), COALESCE(position, -1)
	 * FROM list L LEFT JOIN listmember ON listid = L._id
	 * WHERE L.name = ?
	 * ORDER BY page DESC, position DESC
	 * LIMIT 1
	 * </pre>
	 */
	private static final String SELECT_POSITION_FOR_NEW_LISTMEMBER =
		"SELECT " +
			"L." + ListContract.Column._ID + ", " +
			"COALESCE(" + ListMembershipContract.Column.PAGE + ",1), " +
			"COALESCE(" + ListMembershipContract.Column.POSITION + ",-1) " +
		"FROM " +
			ListContract.TABLE_NAME + " L " +
			"LEFT JOIN " + ListMembershipContract.TABLE_NAME + " " +
				"ON " + ListMembershipContract.Column.LIST_ID + " = L." + ListContract.Column._ID + " " +
		"WHERE " +
			"L." + ListContract.Column.NAME + " = ? " +
		"ORDER BY " +
			ListMembershipContract.Column.PAGE + " DESC, " +
			ListMembershipContract.Column.POSITION + " DESC " +
		"LIMIT 1";

	/**
	 * <pre>
	 * DELETE FROM listmember
	 * WHERE listid = (SELECT _id FROM List where name = ?)
	 *   AND movieid = ?
	 * </pre>
	 */
	private static final String DELETE_FROM_LISTMEMBER =
		"DELETE FROM " + ListMembershipContract.TABLE_NAME + " " +
		"WHERE " +
			ListMembershipContract.Column.LIST_ID + " = (" +
				"SELECT " + ListContract.Column._ID + " " +
				"FROM " + ListContract.TABLE_NAME + " " +
				"WHERE " + ListContract.Column.NAME + " = ?) AND " +
			ListMembershipContract.Column.MOVIE_ID + " = ?";

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
			new String[] { Integer.toString(movieId) });

		if (debug) Log.d(LOG_TAG, "UPDATE " + uri.getPath() + " -> " + rowCount + " rows updated");

		return rowCount;
	}
}
