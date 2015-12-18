package no.skavdahl.udacity.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import no.skavdahl.udacity.popularmovies.data.MovieContract.MovieTable;
import no.skavdahl.udacity.popularmovies.data.MovieContract.ImageTable;

/**
 * Manages the local database for movie information.
 *
 * @author fdavs
 */
public class MovieDbHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;

	static final String DATABASE_NAME = "movie.db";

	public MovieDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createMovieTable_v1(db);
		createImageTable_v1(db);
	}

	private void createMovieTable_v1(SQLiteDatabase db) {
		final String createSQL =
			"CREATE TABLE " + MovieTable.TABLE_NAME + " (" +
				MovieTable._ID + " INTEGER PRIMARY KEY," +
				MovieTable.COLUMN_FAVORITE + " INTEGER NOT NULL, " +
				MovieTable.COLUMN_DOWNLOAD_TIME + " INTEGER NOT NULL, " +
				MovieTable.COLUMN_JSON + " TEXT NOT NULL" +
			");";
		db.execSQL(createSQL);
	}

	private void createImageTable_v1(SQLiteDatabase db) {
		final String createSQL =
			"CREATE TABLE " + ImageTable.TABLE_NAME + " (" +
				ImageTable._ID + " TEXT PRIMARY KEY," +
				ImageTable.COLUMN_WIDTH + " TEXT NOT NULL," +
				ImageTable.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
				ImageTable.COLUMN_IMAGEDATA + " BLOB NOT NULL, " +
				"FOREIGN KEY (" + ImageTable.COLUMN_MOVIE_ID + ") " +
					"REFERENCES " + MovieTable.TABLE_NAME + " (" + MovieTable._ID + ") " +
					"ON DELETE CASCADE " +
					"ON UPDATE RESTRICT" +
			");";
		db.execSQL(createSQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// currently there is nothing to upgrade
	}
}
