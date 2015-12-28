package no.skavdahl.udacity.popularmovies.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

import no.skavdahl.udacity.popularmovies.mdb.MdbJSONAdapter;
import no.skavdahl.udacity.popularmovies.model.Movie;
import no.skavdahl.udacity.utils.Arrays;

import static no.skavdahl.udacity.popularmovies.data.PopularMoviesContract.*;

/**
 * @author fdavs
 */
public class ListDbQueries {

	// TODO Replace SQL generation through string concatenation with a more elegant approach
	// One option: SQLiteQueryBuilder

	private final String LOG_TAG = getClass().getSimpleName();

	private final SQLiteOpenHelper dbHelper;

	public ListDbQueries(SQLiteOpenHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	/*
	This query includes a calculated "favorite" column which holds the value 1 if a movie
	is marked as a favorite movie:

	 		String sql =
			"SELECT " + TextUtils.join(",", projection) + " " +
			"FROM " + MovieContract.TABLE_NAME + " " +
			"LEFT OUTER JOIN " +
				"(SELECT " + ListMembershipContract.Column.MOVIE_ID + ", 1 AS favorite " +
				"FROM " +
					ListMembershipContract.TABLE_NAME + " LM, " +
					ListContract.TABLE_NAME + " L " +
				"WHERE L." + ListContract.Column.TYPE + " = 2 " +
					"AND LM." + ListMembershipContract.Column.LIST_ID + " = L." + ListContract.Column._ID + ") " +
				"ON (" + MovieContract.Column._ID + " = " + ListMembershipContract.Column.MOVIE_ID + ")" +
			(TextUtils.isEmpty(selection) ? "" : "WHERE " + selection + " " +
			(TextUtils.isEmpty(sortOrder) ? "" : "ORDER BY "+ sortOrder;

	 */
}
