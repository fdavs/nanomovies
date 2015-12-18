package no.skavdahl.udacity.popularmovies.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.text.TextUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Tests features of the MovieDbHelper class.
 *
 * @author fdavs
 */
public class MovieDbHelperTest extends AndroidTestCase {

	public final String LOG_TAG = getClass().getSimpleName();

	public void setUp() {
		mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
	}

	public void tearDown() {
	}

	/**
	 * Tests that the database can be successfully created with all the expected
	 * tables (no SQL errors in the CREATE statements).
	 *
	 * This test is based on code from the Sunshine project from the course
	 * "Developing Android Apps" at udacity.com
	 */
	public void testThatDatabaseAndTablesCanBeCreated() throws Throwable {
		// the following tables need to be present in a successfully created database
		final Set<String> remainingTables = new HashSet<>();
		remainingTables.add(MovieContract.MovieTable.TABLE_NAME);
		remainingTables.add(MovieContract.ImageTable.TABLE_NAME);

		SQLiteDatabase db = null;
		Cursor c = null;

		try {
			db = new MovieDbHelper(this.mContext).getWritableDatabase();
			assertEquals(true, db.isOpen());

			c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
			while (c.moveToNext()) {
				String createdTable = c.getString(0);
				remainingTables.remove(createdTable);
			}

			assertTrue(
				"Not all required tables were created: " + TextUtils.join(", ", remainingTables),
				remainingTables.isEmpty());
		}
		finally {
			if (c != null && !c.isClosed())
				c.close();

			if (db != null && db.isOpen())
				db.close();
		}
	}
}
