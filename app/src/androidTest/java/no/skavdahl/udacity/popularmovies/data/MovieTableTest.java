package no.skavdahl.udacity.popularmovies.data;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import static no.skavdahl.udacity.popularmovies.data.MovieContracts.*;

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

	private int MOVIE1_ID;
	private ContentValues MOVIE1;

	private int MOVIE2_ID;
	private ContentValues MOVIE2;

	public void setUp() {
		super.setUp();

		MOVIE1_ID = 206647;

		MOVIE1 = new ContentValues();
		MOVIE1.put(MovieContract.Column._ID, MOVIE1_ID);
		MOVIE1.put(MovieContract.Column.DOWNLOAD_TIME, System.currentTimeMillis());
		MOVIE1.put(MovieContract.Column.FAVORITE, 0);
		MOVIE1.put(MovieContract.Column.JSON,
			"{" +
				"`adult`:false," +
				"`backdrop_path`:`/wVTYlkKPKrljJfugXN7UlLNjtuJ.jpg`," +
				"`genre_ids`:[28,12,80]," +
				"`id`:" + MOVIE1_ID + "," +
				"`original_language`:`en`," +
				"`original_title`:`Spectre`," + "" +
				"`overview`:`A cryptic message from Bond’s past ...`," +
				"`release_date`:`2015-11-06`," +
				"`poster_path`:`/1n9D32o30XOHMdMWuIT4AaA5ruI.jpg`," +
				"`popularity`:54.146108," +
				"`title`:`Spectre`," +
				"`video`:false," +
				"`vote_average`:6.5," +
				"`vote_count`:490" +
				"}".replace("`", "\""));

		MOVIE2_ID = 135397;

		MOVIE2 = new ContentValues();
		MOVIE2.put(MovieContract.Column._ID, MOVIE2_ID);
		MOVIE2.put(MovieContract.Column.DOWNLOAD_TIME, System.currentTimeMillis());
		MOVIE2.put(MovieContract.Column.FAVORITE, 1);
		MOVIE2.put(MovieContract.Column.JSON,
			"{" +
				"`adult`:false," +
				"`backdrop_path`:`/dkMD5qlogeRMiEixC4YNPUvax2T.jpg`," +
				"`genre_ids`:[28,12,878,53]," +
				"`id`:" + MOVIE2_ID + "," +
				"`original_language`:`en`," +
				"`original_title`:`Jurassic World`," +
				"`overview`:`Twenty-two years after the events of Jurassic Park...`," +
				"`release_date`:`2015-06-12`," +
				"`poster_path`:`/jjBgi2r5cRt36xF6iNUEhzscEcb.jpg`," +
				"`popularity`:35.837265," +
				"`title`:`Jurassic World`," +
				"`video`:false," +
				"`vote_average`:6.8," +
				"`vote_count`:2941" +
				"}".replace("`", "\""));
	}

	private void createSeedDatabase() {
		SQLiteDatabase db = new MovieDbHelper(this.mContext).getWritableDatabase();

		db.insert(MovieContract.TABLE_NAME, null, MOVIE1);
		db.insert(MovieContract.TABLE_NAME, null, MOVIE2);
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
			new String[]{MovieContract.Column._ID, MovieContract.Column.DOWNLOAD_TIME, MovieContract.Column.FAVORITE, MovieContract.Column.JSON},
			MovieContract.Column._ID + " = ?",
			new String[]{Integer.toString(MOVIE1_ID)},
			null, // groupBy
			null, // having
			null); // orderBy

		assertTrue(cursor.moveToFirst());

		int idIndex = cursor.getColumnIndex(MovieContract.Column._ID);
		int dlTimeIndex = cursor.getColumnIndex(MovieContract.Column.DOWNLOAD_TIME);
		int favIndex = cursor.getColumnIndex(MovieContract.Column.FAVORITE);
		int jsonIndex = cursor.getColumnIndex(MovieContract.Column.JSON);

		assertEquals(MOVIE1_ID, cursor.getInt(idIndex));
		assertEquals(MOVIE1.getAsLong(MovieContract.Column.DOWNLOAD_TIME).longValue(), cursor.getLong(dlTimeIndex));
		assertEquals(MOVIE1.getAsInteger(MovieContract.Column.FAVORITE).intValue(), cursor.getInt(favIndex));
		assertEquals(MOVIE1.getAsString(MovieContract.Column.JSON), cursor.getString(jsonIndex));
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
