package no.skavdahl.udacity.popularmovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import no.skavdahl.udacity.popularmovies.BuildConfig;
import no.skavdahl.udacity.popularmovies.mdb.StandardMovieList;

import static no.skavdahl.udacity.popularmovies.data.PopularMoviesContract.*;

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
	public void onConfigure(SQLiteDatabase db) {
		// onConfigure is available since API level 16 (4.1.x Jelly Bean). However, Android
		// Studio doesn't know this and complains unless we include the follow if test
		if (Build.VERSION.SDK_INT >= 16) {
			super.onConfigure(db);
			db.setForeignKeyConstraintsEnabled(true);
		}
		else {
			db.execSQL("PRAGMA foreign_keys=ON");
		}

		//if (BuildConfig.DEBUG)
		//	dropExistingTables(db);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);

		if (db.isReadOnly())
			return;

		if (Build.VERSION.SDK_INT < 16)
			onConfigure(db);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createListTable_v1(db);
		createMovieTable_v1(db);
		createListMembershipTable_v1(db);
		createImageTable_v1(db);
	}

	private void dropExistingTables(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + ImageContract.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ListMembershipContract.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ListContract.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + MovieContract.TABLE_NAME);
	}

	private void createListTable_v1(SQLiteDatabase db) {
		db.execSQL(
			"CREATE TABLE " + ListContract.TABLE_NAME + " (" +
				ListContract.Column._ID + " INTEGER PRIMARY KEY," +
				ListContract.Column.NAME + " TEXT NOT NULL," +
				ListContract.Column.TYPE + " INTEGER NOT NULL" +
			")");

		// insert standard lists
		for (StandardMovieList standardList : StandardMovieList.values()) {
			db.insert(ListContract.TABLE_NAME, null,
				makeContentValuesForList(
					standardList.getListName(),
					ListContract.LISTTYPE_STANDARD));
		}

		// insert the favorite list
		db.insert(ListContract.TABLE_NAME, null,
			makeContentValuesForList(
				"favorites", // TODO Replace the magic value with a reference to PredefinedMovieList.Favorite constant in the mdb package
				ListContract.LISTTYPE_FAVORITE));
		// TODO Let R.string define labels for the predefined movie list internal constants
	}

	private ContentValues makeContentValuesForList(String listName, int listType) {
		ContentValues cv = new ContentValues();
		cv.put(ListContract.Column.NAME, listName);
		cv.put(ListContract.Column.TYPE, listType);
		return cv;
	}

	private void createMovieTable_v1(SQLiteDatabase db) {
		db.execSQL(
			"CREATE TABLE " + MovieContract.TABLE_NAME + " (" +
				MovieContract.Column._ID + " INTEGER PRIMARY KEY," +
				MovieContract.Column.MODIFIED + " INTEGER NOT NULL, " +
				MovieContract.Column.JSONDATA + " TEXT NOT NULL" +
			")");
	}

	private void createListMembershipTable_v1(SQLiteDatabase db) {
		db.execSQL(
			"CREATE TABLE " + ListMembershipContract.TABLE_NAME + " (" +
				ListMembershipContract.Column._ID + " INTEGER PRIMARY KEY," +
				ListMembershipContract.Column.LIST_ID + " INTEGER NOT NULL," +
				ListMembershipContract.Column.MOVIE_ID + " INTEGER NOT NULL," +
				ListMembershipContract.Column.ADDED + " INTEGER NOT NULL," +
				ListMembershipContract.Column.PAGE + " INTEGER NOT NULL," +
				ListMembershipContract.Column.POSITION + " INTEGER NOT NULL," +
				"FOREIGN KEY (" + ListMembershipContract.Column.LIST_ID + ") " +
					"REFERENCES " + ListContract.TABLE_NAME + "(" + ListContract.Column._ID + ") " +
					"ON DELETE CASCADE " +
					"ON UPDATE RESTRICT," +
				"FOREIGN KEY (" + ListMembershipContract.Column.MOVIE_ID + ") " +
					"REFERENCES " + MovieContract.TABLE_NAME + "(" + MovieContract.Column._ID + ") " +
					"ON DELETE CASCADE " +
					"ON UPDATE RESTRICT" +
			")");
	}

	private void createImageTable_v1(SQLiteDatabase db) {
		db.execSQL(
			"CREATE TABLE " + ImageContract.TABLE_NAME + " (" +
				ImageContract.Column._ID + " INTEGER PRIMARY KEY," +
				ImageContract.Column.PATH + " TEXT NOT NULL," +
				ImageContract.Column.WIDTH + " INTEGER NOT NULL," +
				ImageContract.Column.MOVIE_ID + " INTEGER NOT NULL, " +
				ImageContract.Column.IMAGEDATA + " BLOB NOT NULL, " +
				"FOREIGN KEY (" + ImageContract.Column.MOVIE_ID + ") " +
					"REFERENCES " + MovieContract.TABLE_NAME + "(" + MovieContract.Column._ID + ") " +
					"ON DELETE CASCADE " +
					"ON UPDATE RESTRICT" +
			")");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// currently there is nothing to upgrade
	}
}
