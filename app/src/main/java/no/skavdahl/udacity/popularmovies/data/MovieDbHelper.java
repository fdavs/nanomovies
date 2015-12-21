package no.skavdahl.udacity.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import no.skavdahl.udacity.popularmovies.data.MovieContracts.MovieContract;
import no.skavdahl.udacity.popularmovies.data.MovieContracts.ImageContract;

/**
 * Manages the local database for movie information.
 *
 * @author fdavs
 */
public class MovieDbHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;

	static final String DATABASE_NAME = "movie.db";

	public MovieDbHelper(Context context) {
		super(context, DATABASE_NAME, /* CursorFactory*/ null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createMovieTable_v1(db);
		createImageTable_v1(db);
	}

	private void createMovieTable_v1(SQLiteDatabase db) {
		final String createSQL =
			"CREATE TABLE " + MovieContract.TABLE_NAME + " (" +
				MovieContract.Column._ID + " INTEGER PRIMARY KEY," +
				MovieContract.Column.FAVORITE + " INTEGER NOT NULL, " +
				MovieContract.Column.DOWNLOAD_TIME + " LONG NOT NULL, " +
				MovieContract.Column.JSON + " TEXT NOT NULL" +
			");";
		db.execSQL(createSQL);
	}

	private void createImageTable_v1(SQLiteDatabase db) {
		final String createSQL =
			"CREATE TABLE " + ImageContract.TABLE_NAME + " (" +
				ImageContract.Column._ID + " ID PRIMARY KEY," +
				ImageContract.Column.PATH + " TEXT NOT NULL," +
				ImageContract.Column.WIDTH + " TEXT NOT NULL," +
				ImageContract.Column.MOVIE_ID + " INTEGER NOT NULL, " +
				ImageContract.Column.IMAGEDATA + " BLOB NOT NULL, " +
				"FOREIGN KEY (" + ImageContract.Column.MOVIE_ID + ") " +
					"REFERENCES " + MovieContract.TABLE_NAME + " (" + MovieContract.Column._ID + ") " +
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
