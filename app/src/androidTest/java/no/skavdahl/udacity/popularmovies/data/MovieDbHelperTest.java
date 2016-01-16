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

	/** Returns the set of tables that need to be present in a successfully created database. */
	private Set<String> getRequiredTables() {
		final Set<String> requiredTables = new HashSet<>();
		requiredTables.add(PopularMoviesContract.ListContract.TABLE_NAME);
		requiredTables.add(PopularMoviesContract.ListMembershipContract.TABLE_NAME);
		requiredTables.add(PopularMoviesContract.MovieContract.TABLE_NAME);
		return requiredTables;
	}

	/**
	 * Tests that the database can be successfully created with all the expected
	 * tables (no SQL errors in the CREATE statements).
	 *
	 * This test is based on code from the Sunshine project from the course
	 * "Developing Android Apps" at udacity.com
	 */
	public void testThatDatabaseAndTablesCanBeCreated() {
		final Set<String> remainingTables = getRequiredTables();

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
}
