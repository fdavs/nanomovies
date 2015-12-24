package no.skavdahl.udacity.popularmovies.data;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import static no.skavdahl.udacity.popularmovies.data.PopularMoviesContract.*;

/**
 * Tests features of the MovieDbHelper class directly related to movie data (query, insert,
 * update and delete).
 *
 * @author fdavs
 */
public class MovieTableTest extends SQLiteTestCase {

	// TODO Remove these tests
	// Tests like these are included in the Sunshine demo app. However, in my opinion, these
	// are tests of SQLite functionality (does query work? does update work?) and not of
	// functionality of this app. Hence these unit tests are seriously misplaced.

	private void createSeedDatabase() {
		SQLiteDatabase db = new MovieDbHelper(this.mContext).getWritableDatabase();

		db.insert(MovieContract.TABLE_NAME, null, TestData.Movie1.asContentValues());
		db.insert(MovieContract.TABLE_NAME, null, TestData.Movie2.asContentValues());
	}

	/*private String select(String... cols) {
		return "SELECT " + TextUtils.join(", ", cols) + " ";
	}

	private String from(String tableName) {
		return "FROM " + tableName + " ";
	}

	private String where(String conditions) {
		return "WHERE " + conditions + " ";
	}

	private String paramEquals(String paramName) {
		return paramName + " = ?";
	}

	private String[] withValues(String... values) {
		return values;
	}*/

	/**
	 *
	 */
	public void testQueryById() {

		createSeedDatabase();

		db = new MovieDbHelper(this.mContext).getReadableDatabase();

		// TODO perform a rawQuery or use the query() "utility" method?
		/*cursor = db.rawQuery(
			select(MovieContract.Column._ID, MovieContract.Column.DOWNLOAD_TIME, MovieContract.Column.FAVORITE, MovieContract.Column.JSON) +
			from(MovieContract.TABLE_NAME) +
			where(paramEquals(MovieContract.Column._ID)),
			withValues(Integer.toString(MOVIE1_ID))
		);*/

		cursor = db.query(
			MovieContract.TABLE_NAME,
			new String[]{MovieContract.Column._ID, MovieContract.Column.MODIFIED, MovieContract.Column.JSONDATA},
			MovieContract.Column._ID + " = ?",
			new String[]{Integer.toString(TestData.Movie1.ID)},
			null, // groupBy
			null, // having
			null); // orderBy

		assertTrue(cursor.moveToFirst());

		int idIndex = cursor.getColumnIndex(MovieContract.Column._ID);
		int modifiedIndex = cursor.getColumnIndex(MovieContract.Column.MODIFIED);
		int jsonIndex = cursor.getColumnIndex(MovieContract.Column.JSONDATA);

		assertEquals(TestData.Movie1.ID, cursor.getInt(idIndex));
		assertEquals(TestData.Movie1.MODIFIED, cursor.getLong(modifiedIndex));
		assertEquals(TestData.Movie1.JSONDATA, cursor.getString(jsonIndex));
	}

	/**
	 *
	 */
	public void testInsertMovie() {
		fail("Feature not implemented");
	}

	/**
	 *
	 */
	public void testUpdateMovie() {
		fail("Feature not implemented");
	}

	/**
	 *
	 */
	public void testDeleteMovie() {
		fail("Feature not implemented");
	}


}
