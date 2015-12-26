package no.skavdahl.udacity.popularmovies.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

import no.skavdahl.udacity.popularmovies.mdb.DiscoverMoviesJSONAdapter;
import no.skavdahl.udacity.popularmovies.model.Movie;
import no.skavdahl.udacity.utils.Arrays;

import static no.skavdahl.udacity.popularmovies.data.PopularMoviesContract.*;

/**
 * @author fdavs
 */
public class ListDbQueries {

	// TODO Replace SQL generation through string concatenation with a more elegant approach
	// One option: SQLiteQueryBuilder

	private final String LOG_TAG = getClass().getSimpleName();

	private final SQLiteOpenHelper dbHelper;

	public ListDbQueries(SQLiteOpenHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	public Cursor queryListMemberDirectory(String listName, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		String sql =
			"SELECT M." + TextUtils.join(", M.", projection) + " " +
			"FROM " + MovieContract.TABLE_NAME + " M, " + ListContract.TABLE_NAME + " L, " + ListMembershipContract.TABLE_NAME + " " +
			"WHERE L." + ListContract.Column.NAME + " = ? " +
				"AND L." + ListContract.Column._ID + " = " + ListMembershipContract.Column.LIST_ID + " " +
				"AND " + ListMembershipContract.Column.MOVIE_ID + " = M." + MovieContract.Column._ID +
					(TextUtils.isEmpty(selection) ? "" : " AND " + selection) + " " + // NOTE asserting that selection only includes (at most) pos and page columns
			"ORDER BY " + (TextUtils.isEmpty(sortOrder) ? ListMembershipContract.Column.POSITION : sortOrder);

		String[] effectiveSelArgs = Arrays.prepend(listName, selectionArgs);
		return dbHelper.getReadableDatabase().rawQuery(sql, effectiveSelArgs);
	}

	/* *
	 * Returns the type of a list
	 *
	 * @param listName The list whose type to query
	 *
	 * @return a list type or {@link ListContract#LISTTYPE_NONE}.
	 */
	/*public Cursor queryListItem(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		//String effectiveSelection = ListContract.Column.NAME + " = ?" + (TextUtils.isEmpty(selection) ? "" : " AND " + selection);
		//String[] effectiveSelectionArgs = Arrays.prepend(listName, selectionArgs);

		return dbHelper.getReadableDatabase().query(ListContract.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
	}*/


	/*public int queryListType(String listName) {
		Cursor cursor = queryListItem(listName, new String[]{ListContract.Column.TYPE}, null, null, null);
		return cursor.moveToFirst()
			? cursor.getInt(cursor.getColumnIndex(ListContract.Column.TYPE))
			: ListContract.LISTTYPE_NONE;
	}*/

	/**
	 * Inserts movie data and associates the movies with a list.
	 *
	 * @param listId The list to which to associate the movies
	 * @param page The page number (from the request to main movie database)
	 * @param movieList The list of movies to insert.
	 *
	 * @return the number of inserted rows.
	 */
	public int bulkInsert(int listId, int page, List<Movie> movieList) {
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

		final long now = System.currentTimeMillis();
		DiscoverMoviesJSONAdapter jsonAdapter = new DiscoverMoviesJSONAdapter(null);

		int position = 0;

		db.beginTransaction();
		try {
			for (Movie m : movieList) {
				String moviejson = null;
				try {
					moviejson = jsonAdapter.toJSONString(m);

					movieStmt.bindLong(1, m.getMovieDbId());
					movieStmt.bindLong(2, now);
					movieStmt.bindString(3, moviejson);

					movieStmt.executeInsert();

					listStmt.bindLong(1, getListMemberId(listId, page, position));
					listStmt.bindLong(2, listId);
					listStmt.bindLong(3, m.getMovieDbId());
					listStmt.bindLong(4, page);
					listStmt.bindLong(5, position);
					listStmt.bindLong(6, now);

					listStmt.executeInsert();

					position++;
				}
				catch (Exception e) {
					Log.w(LOG_TAG, "Error during movie insert: " +
						"list=" + listId + ", " +
						"page=" + page + ", " +
						"movie=" + m.getMovieDbId() + " " + moviejson, e);
				}
			}
			db.setTransactionSuccessful();
		}
		finally {
			db.endTransaction();
		}

		return position; // the number of inserted rows
	}

	private long getListMemberId(int listId, int page, int position) {
		// two last digits for position within the page
		// four digits for the page (1 - 1000)
		// additional digits in front for the list
		return listId * 1000000 + page * 100 + position;
	}
}
