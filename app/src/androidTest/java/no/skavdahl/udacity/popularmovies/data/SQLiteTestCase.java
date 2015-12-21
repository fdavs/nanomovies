package no.skavdahl.udacity.popularmovies.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

/**
 * @author fdavs
 */
public class SQLiteTestCase extends AndroidTestCase {

	protected final String LOG_TAG = getClass().getSimpleName();

	protected SQLiteDatabase db;
	protected Cursor cursor;

	public void setUp() {
		mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);

		db = null;
		cursor = null;
	}

	public void tearDown() {
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
			cursor = null;
		}

		if (db != null && db.isOpen()) {
			db.close();
			db = null;
		}
	}
}
