package no.skavdahl.udacity.popularmovies.data;

import android.text.TextUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Tests features of the MovieDbHelper class.
 *
 * @author fdavs
 */
public class MovieDbHelperTest extends SQLiteTestCase {

	/**
	 * Tests that the database can be successfully created with all the expected
	 * tables (no SQL errors in the CREATE statements).
	 *
	 * This test is based on code from the Sunshine project from the course
	 * "Developing Android Apps" at udacity.com
	 */
	public void testThatDatabaseAndTablesCanBeCreated() {
		// the following tables need to be present in a successfully created database
		final Set<String> remainingTables = new HashSet<>();
		remainingTables.add(PopularMoviesContract.ListContract.TABLE_NAME);
		remainingTables.add(PopularMoviesContract.ListMembershipContract.TABLE_NAME);
		remainingTables.add(PopularMoviesContract.MovieContract.TABLE_NAME);
		remainingTables.add(PopularMoviesContract.ImageContract.TABLE_NAME);

		db = new MovieDbHelper(this.mContext).getWritableDatabase();
		assertEquals(true, db.isOpen());

		cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
		while (cursor.moveToNext()) {
			String createdTable = cursor.getString(0);
			remainingTables.remove(createdTable);
		}

		assertTrue(
			"Not all required tables were created: " + TextUtils.join(", ", remainingTables),
			remainingTables.isEmpty());
	}


	/**
	 *
	 */
	public void testSelectImageById() {
		fail("Feature not implemented");
	}

	/**
	 *
	 */
	public void testInsertImage() {
		fail("Feature not implemented");
	}

	/**
	 *
	 */
	public void testDeleteImage() {
		fail("Feature not implemented");
	}
}
