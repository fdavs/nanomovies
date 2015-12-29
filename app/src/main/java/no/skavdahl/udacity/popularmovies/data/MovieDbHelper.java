package no.skavdahl.udacity.popularmovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import no.skavdahl.udacity.popularmovies.BuildConfig;
import no.skavdahl.udacity.popularmovies.mdb.StandardMovieList;
import no.skavdahl.udacity.popularmovies.model.Movie;

import static no.skavdahl.udacity.popularmovies.data.PopularMoviesContract.*;

/**
 * Manages the local database for movie information.
 *
 * @author fdavs
 */
public class MovieDbHelper extends SQLiteOpenHelper {

	private static final String LOG_TAG = MovieDbHelper.class.getSimpleName();

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "movie.db";

	private static final String ON_DELETE_LIST_TRIGGER = "on_delete_list_trigger";
	private static final String ON_DELETE_MOVIE_TRIGGER = "on_delete_movie_trigger";

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
			db.execSQL("PRAGMA foreign_keys = ON");
		}

		if (BuildConfig.DEBUG) {
			dropExistingTables(db);
			onCreate(db);
		}
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
		createListMembershipTable_v1(db);
		createMovieTable_v1(db);
		createImageTable_v1(db);
		createTriggers_v1(db);

		Log.i(LOG_TAG, "Movie database successfully initialized");
	}

	private void dropExistingTables(SQLiteDatabase db) {
		db.execSQL("DROP TRIGGER IF EXISTS " + ON_DELETE_LIST_TRIGGER);
		db.execSQL("DROP TRIGGER IF EXISTS " + ON_DELETE_MOVIE_TRIGGER);
		db.execSQL("DROP TABLE IF EXISTS " + ImageContract.TABLE_NAME);
		db.execSQL("DROP VIEW IF EXISTS " + MovieContract.TABLE_EX_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ListMembershipContract.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + MovieContract.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ListContract.TABLE_NAME);

		Log.d(LOG_TAG, "Movie database tables dropped");
	}

	private void createListTable_v1(SQLiteDatabase db) {
		db.execSQL(
			"CREATE TABLE " + ListContract.TABLE_NAME + " (" +
				ListContract.Column._ID + " INTEGER PRIMARY KEY," +
				ListContract.Column.NAME + " TEXT NOT NULL," +
				ListContract.Column.TYPE + " INTEGER NOT NULL" +
			")");

		// insert predefined lists
		insertMovie(db, StandardMovieList.POPULAR, ListContract.LISTTYPE_STANDARD);
		insertMovie(db, StandardMovieList.NOW_PLAYING, ListContract.LISTTYPE_STANDARD);
		insertMovie(db, StandardMovieList.TOP_RATED, ListContract.LISTTYPE_STANDARD);
		insertMovie(db, StandardMovieList.UPCOMING, ListContract.LISTTYPE_STANDARD);
		insertMovie(db, StandardMovieList.FAVORITE, ListContract.LISTTYPE_FAVORITE);
	}

	private void insertMovie(SQLiteDatabase db, String listName, int listType) {
		ContentValues values = new ContentValues();
		values.put(ListContract.Column.NAME, listName);
		values.put(ListContract.Column.TYPE, listType);

		db.insert(ListContract.TABLE_NAME, null, values);
	}

	private void createMovieTable_v1(SQLiteDatabase db) {
		db.execSQL(
			"CREATE TABLE " + MovieContract.TABLE_NAME + " (" +
				MovieContract.Column._ID + " INTEGER PRIMARY KEY," +
				MovieContract.Column.MODIFIED + " INTEGER NOT NULL, " +
				MovieContract.Column.JSONDATA + " TEXT NOT NULL" +
			")");

		// CREATE VIEW MovieEx (_id, modified, jsondata, favorite) AS
		// SELECT M._id,
		//        M.modified,
		//        M.jsondata,
		//        (SELECT COUNT(*) FROM listmember LM, list L
		//         WHERE LM.movieid = M._id AND L._id = LM.listid AND L.listtype = 2) AS favorite
		// FROM movie M;
		db.execSQL(
			"CREATE VIEW " + MovieContract.TABLE_EX_NAME + " " +
				"AS SELECT " +
					"M." + MovieContract.Column._ID + "," +
					"M." + MovieContract.Column.MODIFIED + "," +
					"M." + MovieContract.Column.JSONDATA + "," +
					"(SELECT COUNT(*) " +
 					 "FROM " +
						ListMembershipContract.TABLE_NAME + " LM," +
						ListContract.TABLE_NAME + " L " +
					 "WHERE LM." + ListMembershipContract.Column.MOVIE_ID + " = M." + MovieContract.Column._ID + " " +
					   "AND LM." + ListMembershipContract.Column.LIST_ID + " = L." + ListContract.Column._ID + " " +
					   "AND L." + ListContract.Column.TYPE + " = " + ListContract.LISTTYPE_FAVORITE + ") AS " + MovieContract.Column.FAVORITE + " " +
				"FROM " + MovieContract.TABLE_NAME + " M");

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
					"ON DELETE NO ACTION  " +
					"ON UPDATE RESTRICT " +
				    "DEFERRABLE INITIALLY DEFERRED, " +
				"FOREIGN KEY (" + ListMembershipContract.Column.MOVIE_ID + ") " +
					"REFERENCES " + MovieContract.TABLE_NAME + "(" + MovieContract.Column._ID + ") " +
					"ON DELETE NO ACTION " +
					"ON UPDATE RESTRICT " +
				    "DEFERRABLE INITIALLY DEFERRED" +
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
					"ON DELETE NO ACTION " +
					"ON UPDATE RESTRICT " +
					"DEFERRABLE INITIALLY DEFERRED" +
			")");
	}

	private void createTriggers_v1(SQLiteDatabase db) {
		db.execSQL(
			"CREATE TRIGGER " + ON_DELETE_MOVIE_TRIGGER + " " +
			"AFTER DELETE ON " + MovieContract.TABLE_NAME + " " +
			"BEGIN " +
				"DELETE FROM " + ListMembershipContract.TABLE_NAME + " " +
				"WHERE " + ListMembershipContract.TABLE_NAME + "." + ListMembershipContract.Column.MOVIE_ID + " = " +
					MovieContract.TABLE_NAME + "." + MovieContract.Column._ID + ";" +
				"DELETE FROM " + ImageContract.TABLE_NAME + " " +
				"WHERE " + ImageContract.TABLE_NAME + "." + ImageContract.Column.MOVIE_ID + " = " +
					MovieContract.TABLE_NAME + "." + MovieContract.Column._ID + ";" +
			"END");

		db.execSQL(
			"CREATE TRIGGER " + ON_DELETE_LIST_TRIGGER + " " +
				"AFTER DELETE ON " + ListContract.TABLE_NAME + " " +
				"BEGIN " +
					"DELETE FROM " + ListMembershipContract.TABLE_NAME + " " +
					"WHERE " + ListMembershipContract.TABLE_NAME + "." + ListMembershipContract.Column.LIST_ID + " = " +
						ListContract.TABLE_NAME + "." + ListContract.Column._ID + ";" +
				"END");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// currently there is nothing to upgrade
	}
}
